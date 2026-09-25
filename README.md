# SugarBreak

SugarBreak is a production-quality, native Android application designed to help users track and reduce their added/free sugar consumption through simple daily awareness. It emphasizes a supportive, non-judgmental approach to habit tracking.

## Architecture

The application is built using modern Android development practices and Clean Architecture principles. 

### Technology Stack
*   **Language**: Kotlin
*   **UI Toolkit**: Jetpack Compose (Material 3)
*   **Architecture**: MVVM + Clean Architecture (Domain, Data, UI)
*   **Concurrency**: Coroutines & Flow (StateFlow for UI state)
*   **Dependency Injection**: Hilt
*   **Local Persistence**: Room (for relational data like Challenges and Check-ins) & DataStore (for simple preferences like reminder settings)
*   **Navigation**: Jetpack Navigation for Compose
*   **Background Work & Notifications**: AlarmManager and BroadcastReceivers for reliable, exact daily reminders. 

### Module Structure
The project is organized by feature within the core layers:
*   **`domain`**: Contains the core business logic, entities (`Challenge`, `DailyCheckIn`), repositories interfaces, and Use Cases (`RecordCheckInUseCase`, `CalculateStreakUseCase`, etc.). This layer has no Android dependencies.
*   **`data`**: Implements the repository interfaces. Contains Room DAOs, Entities, the Database setup, and DataStore preferences managers. 
*   **`ui`**: Contains the ViewModels and Compose screens organized by feature (`onboarding`, `home`, `checkin`, `history`, `statistics`, `settings`). 
*   **`notification`**: Contains the `AlarmManager` scheduling logic, `BroadcastReceiver` implementations, and Notification channel setup to handle daily reminders safely across reboots and permission changes.
*   **`util`**: Contains core utility helpers, including date manipulations and the `StreakCalculator`.

## Setup and Installation

1.  **Clone / Open the Project**: Open the project folder `NoSugar` in Android Studio (Ladybug or newer recommended).
2.  **Gradle Sync**: Allow Gradle to sync dependencies. The project uses a `libs.versions.toml` catalog for dependency management.
3.  **Run the App**: Select a device or emulator running API 24 (Android 7.0) or higher. 
4.  **Permissions**: On Android 13+, the app will request Notification permissions to send daily reminders. Exact alarm permissions are requested seamlessly. 

## Testing

The project includes unit tests for core domain logic, specifically the `StreakCalculator` which is responsible for deriving current streaks, best streaks, and success rates dynamically from the history of check-ins. Timezone boundary cases and skipped days are handled smoothly. 

## Core Flows
- **Onboarding**: A clean 5-step flow to configure the user's tracking rules, target successful days, and preferred reminder time.
- **Home**: A dashboard displaying the current streak prominently, progress towards the goal, and a primary CTA to check in.
- **Check-in**: A supportive daily check-in screen. A "slip" records the reason without penalizing the user's overarching journey, optionally adding a recovery day based on their chosen settings.
- **History & Stats**: A calendar view and statistics overview to track long-term progress.
