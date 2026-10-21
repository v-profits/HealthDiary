# Health Diary / Дневник здоровья

Android app (Kotlin, Jetpack Compose, Room) for patients: log blood pressure, pulse, sugar,
weight, temperature and oxygen by keyboard or voice, keep your own medicine list, and send a
text report to the doctor through any messenger (Telegram, WhatsApp, MAX, email and so on).

- Data is stored only on the phone (Room database). The app has no internet permission.
- Languages: English and Russian (follows the phone). Light and dark theme follow the phone.
- Voice: the Voice button opens the system speech recognizer. If the phone has none, use the
  microphone on the keyboard to dictate into the quick-entry field, then press "Fill in".
- The app only stores and sends records; it gives no medical advice.

## Build

1. Android Studio: File > Open > select this folder, wait for Gradle sync.
2. Run on a device or emulator (minSdk 24).
3. Change `applicationId` and `namespace` in `app/build.gradle.kts` before publishing
   (the package folders under `app/src/main/java/com/example/healthdiary` follow the namespace).
4. Launcher icon: right-click `res` > New > Image Asset.

## Structure

- `data/` Room entities, DAO, database
- `voice/VoiceParser.kt` phrase to reading ("давление 135 на 80 пульс 72")
- `report/ReportBuilder.kt` plain-text report for the doctor
- `ui/` Compose screens: readings, medicines, send
- `util/Format.kt` number formatting, units, input checks
