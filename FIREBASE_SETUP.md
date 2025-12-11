# Firebase Crashlytics Setup Guide

## Prerequisites

1. A Google account
2. Access to Firebase Console: https://console.firebase.google.com

## Setup Steps

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project"
3. Enter project name: `ArguMentor` (or your preferred name)
4. Follow the wizard (you can disable Google Analytics if you prefer)

### 2. Add Android App

1. In your Firebase project, click "Add app" → Android
2. Enter package name: `com.argumentor.app`
3. Enter app nickname: `ArguMentor`
4. (Optional) Enter SHA-1 signing certificate for additional security

### 3. Download Configuration File

1. Download `google-services.json`
2. Place it in `app/google-services.json` (replace the template)

### 4. Enable Crashlytics

1. In Firebase Console, go to **Crashlytics** in the left menu
2. Click "Enable Crashlytics"
3. Wait for the first crash report (or force one for testing)

## Testing Crashlytics

### Force a Test Crash (Debug Only)

Add this temporarily to test:

```kotlin
// In any Activity or ViewModel
FirebaseCrashlytics.getInstance().apply {
    log("Test log message")
    recordException(RuntimeException("Test non-fatal exception"))
}

// Or force a fatal crash (use with caution!)
throw RuntimeException("Test crash")
```

### Verify Integration

1. Build and run the app
2. Check Firebase Console → Crashlytics
3. You should see "Crashlytics is enabled" message

## File Structure

```
app/
├── google-services.json          # Your Firebase config (add to .gitignore!)
├── google-services.json.template # Template for reference
└── build.gradle.kts              # Already configured with Firebase plugins
```

## Security Notes

- **Never commit `google-services.json` to public repositories**
- Add to `.gitignore`: `app/google-services.json`
- Use the template for reference when setting up on other machines

## Troubleshooting

### Build Errors

1. Ensure `google-services.json` is in the `app/` directory
2. Sync Gradle files after adding the file
3. Check that package name matches exactly: `com.argumentor.app`

### Crashes Not Appearing

1. Wait 5-10 minutes for crashes to appear in console
2. Ensure device has internet connection
3. Check that Crashlytics is enabled in Firebase Console

## Features Enabled

- **Crash Reports**: Automatic fatal crash collection
- **Non-Fatal Errors**: Logged via `Timber.e()` in release builds
- **Crash Context**: Last 50 log entries before crash
- **Custom Keys**: Error tags for categorization
