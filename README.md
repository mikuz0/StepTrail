# StepTrail — Трекер прогулок

Android-приложение для отслеживания прогулок с записью GPS-данных в реальном времени.

![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)
![Compose](https://img.shields.io/badge/Compose-1.7.2-green.svg)
![Min SDK](https://img.shields.io/badge/min%20SDK-26-lightgrey.svg)

## 📱 Возможности

### Главный экран
- Большая кнопка **«Старт / Стоп»** для управления трекингом
- Отображение в реальном времени:
  - Текущая скорость (км/ч)
  - Средняя скорость (км/ч)
  - Пройденная дистанция (км)
  - Длительность прогулки (чч:мм:сс)
- Индикатор статуса GPS (нет разрешения / поиск сигнала / активен)
- Foreground-уведомление во время записи

### Журнал прогулок
- Список последних 10 прогулок, отсортированный по дате (новые — первыми)
- Карточки с краткой информацией: дата, дистанция, длительность, скорости
- Экран деталей: полная информация о завершённой прогулке
- Пустое состояние с подсказкой для нового пользователя

### Расчёты
- Дистанция по формуле Haversine между GPS-точками
- Фильтрация точек: accuracy > 30 м отбрасываются
- Игнорирование точек с скоростью < 0.3 м/с
- Средняя скорость = дистанция / время движения

## 🏗 Архитектура

Проект построен по принципам **Clean Architecture** + **MVVM**:

```
com.mikuz.steptrail
├── ui/                    # Jetpack Compose экраны
│   ├── main/              # Главный экран (трекинг)
│   ├── journal/           # Журнал прогулок
│   └── detail/            # Детали прогулки
├── domain/                # Бизнес-логика
│   ├── model/             # Walk, TrackPoint
│   ├── repository/        # WalkRepository (интерфейс)
│   └── usecase/           # StartWalk, StopWalk, GetWalks...
├── data/                  # Реализации
│   ├── local/             # Room: Database, DAO, Entities
│   ├── repository/        # WalkRepositoryImpl
│   └── location/          # LocationTracker (FusedLocation)
├── service/               # WalkTrackingService (foreground)
├── di/                    # Hilt-модули
└── util/                  # Haversine, форматтеры
```

### Стек технологий

| Компонент | Технология |
|-----------|-----------|
| UI | Jetpack Compose, Material 3 |
| Навигация | Navigation Compose |
| DI | Hilt (KSP) |
| Хранение | Room Database |
| Геолокация | FusedLocationProviderClient |
| Асинхронность | Kotlin Coroutines + Flow |
| Фоновый трекинг | Foreground Service (location) |

## 📦 Зависимости

- **AGP** 8.6.0
- **Kotlin** 2.0.21
- **Compose BOM** 2024.09.00
- **Hilt** 2.51.1
- **Room** 2.6.1
- **Navigation Compose** 2.8.4
- **Play Services Location** 21.1.0

## 🛠 Сборка и запуск

### Требования
- Android Studio (актуальная версия)
- Android SDK (путь указан в `local.properties`)
- `compileSdk = 34`, `minSdk = 26`

### Команды

```bash
# Сборка debug-версии
./gradlew :app:assembleDebug

# Установка на устройство
./gradlew :app:installDebug

# Запуск тестов
./gradlew :app:testDebugUnitTest
```

## 📝 Структура пакетов

| Пакет | Назначение |
|-------|-----------|
| `ui/` | Compose-композиции, ViewModels, UI-состояния |
| `domain/` | Модели, интерфейсы репозиториев, use cases |
| `data/` | Room, реализация репозитория, LocationTracker |
| `service/` | Foreground Service для GPS-трекинга |
| `di/` | Hilt-модули для DI |
| `util/` | Утилиты: Haversine, конвертеры |

## 🔐 Разрешения

Приложение запрашивает следующие разрешения:

| Разрешение | Назначение |
|------------|-----------|
| `ACCESS_FINE_LOCATION` | Точная геолокация (GPS) |
| `ACCESS_COARSE_LOCATION` | Приблизительная геолокация |
| `FOREGROUND_SERVICE` | Фоновый сервис |
| `FOREGROUND_SERVICE_LOCATION` | Фоновый сервис геолокации |
| `POST_NOTIFICATIONS` | Уведомления (Android 13+) |

## 📐 Дизайн

- **Material 3** с кастомной цветовой схемой
- Основной цвет: зелёный `#2E7D32`
- Вторичный цвет: графит `#263238`
- Поддержка тёмной темы и динамических цветов (Android 12+)

## ⚠️ Известные ограничения

1. **Максимум 10 прогулок** — при превышении самая старая удаляется автоматически
2. **Расчёт макс. скорости** — в текущей реализации не обновляется во время трекинга
3. **Нет офлайн-режима** — требуется подключение к Google Play Services
4. **Отсутствие тестов** — unit и instrumented тесты ещё не написаны

## 📄 Лицензия

This project is licensed under the MIT License.

## 👤 Автор

StepTrail — трекер прогулок для Android.
