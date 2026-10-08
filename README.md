# Installment Tracker

An Android app to track installment payments with Persian (Shamsi) date support and notifications.

## Features
- Add, edit, delete installments
- Enter due date in Persian calendar format (yyyy/MM/dd)
- Set notification days before due date (0 for same day, positive for days before)
- Receive notifications at the specified time
- Material Design UI

## Requirements
- Android Studio Arctic Fox or later
- Android SDK minSdkVersion 24 (for PersianCalendar support)
- Java 8 or Kotlin (this project uses Java)

## How to Run
1. Clone or copy this project to Android Studio
2. Make sure you have Android SDK 24 or higher installed
3. Build and run on an emulator or device

## Files Structure
- `app/src/main/java/com/example/installmentapp/` - Source code
- `app/src/main/res/` - Resources (layouts, strings, colors)
- `app/src/main/AndroidManifest.xml` - Manifest file

## Notes
- The app uses SQLite for local storage
- Notifications are scheduled using AlarmManager
- Date conversion between Persian and Gregorian is done using Android's PersianCalendar (API 24+)