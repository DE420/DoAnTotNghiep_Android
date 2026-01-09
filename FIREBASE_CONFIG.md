# Firebase Android Configuration - Backend Compatibility Guide

> **Quick guide to configure Android app's Firebase to work with your existing backend Firebase setup**

## ⚡ Overview

Your backend is already configured with Firebase. This guide helps you configure the Android app to use the **same Firebase project**.

## 🎯 Critical Rule

**Android app and backend MUST use the same Firebase project!**

```
┌─────────────────────────────────────────────────────┐
│         SAME FIREBASE PROJECT REQUIRED              │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Backend                          Android App      │
│    ↓                                   ↓           │
│  Uses:                              Uses:          │
│  firebase-service-account.json   google-services   │
│                                     .json          │
│         ↓                              ↓           │
│         └──────→ SAME PROJECT ←────────┘           │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## 📝 Step-by-Step Configuration

### Step 1: Get Firebase Project Info from Backend

Ask your backend developer for:

```
Firebase Project ID: ___________________
   (Found in firebase-service-account.json → "project_id")
```

**OR** Check the backend file directly:

```bash
# View backend's Firebase config
cat ../DoAnTotNghiep/BE/DoanBE/src/main/resources/firebase-service-account.json | grep project_id

# Output example: "project_id": "fitness-app-xxxxx"
```

### Step 2: Access Firebase Console

1. Go to: https://console.firebase.google.com
2. Select the **same project** the backend is using
   - ⚠️ **CRITICAL**: Do NOT create a new project!
   - ⚠️ Use the exact project ID from Step 1

### Step 3: Add Android App (If Not Already Added)

**In Firebase Console:**

1. Click **"Add app"** → Select **Android** icon
2. Fill in **EXACTLY**:
   ```
   Android package name: com.example.fitnessapp
   ```
   - ⚠️ **CRITICAL**: Must be exactly `com.example.fitnessapp`
   - This matches the `applicationId` in Android app
3. App nickname: `Fitness App` (optional)
4. Click **"Register app"**

**If Android app already exists:**
- Skip to Step 4
- Verify package name is `com.example.fitnessapp`

### Step 4: Download google-services.json

1. In Firebase Console → Project Settings → Your apps
2. Find the Android app (package: `com.example.fitnessapp`)
3. Click **"Download google-services.json"**
4. **Save the file**

### Step 5: Place File in Android Project

```bash
# Copy the downloaded file to Android app directory
cp ~/Downloads/google-services.json app/google-services.json

# Verify placement
ls -la app/google-services.json
```

**Correct location:**
```
DoAnTotNghiep_Android/
└── app/
    ├── build.gradle.kts
    ├── google-services.json  ← HERE (same level as build.gradle.kts)
    └── src/
```

**❌ Wrong locations:**
- `DoAnTotNghiep_Android/google-services.json` (too high)
- `DoAnTotNghiep_Android/app/src/google-services.json` (too deep)

### Step 6: Verify Configuration Match

**Open google-services.json and check:**

```json
{
  "project_info": {
    "project_id": "fitness-app-xxxxx"  ← Must match backend
  },
  "client": [{
    "client_info": {
      "android_client_info": {
        "package_name": "com.example.fitnessapp"  ← Must be this
      }
    }
  }]
}
```

**Verification checklist:**

```bash
# 1. Check project_id matches backend
grep project_id app/google-services.json
grep project_id ../DoAnTotNghiep/BE/DoanBE/src/main/resources/firebase-service-account.json
# ↑ These MUST show the same project_id

# 2. Check package name is correct
grep package_name app/google-services.json
# Output must be: "package_name": "com.example.fitnessapp"

# 3. Check Android app config
grep applicationId app/build.gradle.kts
# Output must be: applicationId = "com.example.fitnessapp"
```

### Step 7: Build and Test

```bash
# Clean and build
./gradlew clean assembleDebug

# If build succeeds, install and test
./gradlew installDebug
```

**Expected result:**
```
BUILD SUCCESSFUL
```

**If build fails, see:** [Troubleshooting](#troubleshooting) below

---

## ✅ Verification Test

After installation, check if Firebase is working:

```bash
# Watch for FCM token
adb logcat | grep "FCMService"

# Expected output:
# D/FCMService: New FCM token received
# D/FCMService: Token registered with backend
```

**Both messages should appear** within 10 seconds of app launch.

---

## 🐛 Troubleshooting

### Error: "google-services.json not found"

**Solution:**
```bash
# Verify file exists
ls app/google-services.json

# If not found, place it there (see Step 5)
# Then sync Gradle:
# Android Studio: File → Sync Project with Gradle Files
```

### Error: "Package name mismatch"

**Problem:** google-services.json shows different package name

**Solution:**
1. Delete the existing Android app from Firebase Console
2. Add new Android app with package: `com.example.fitnessapp`
3. Download new google-services.json
4. Replace the old file
5. Rebuild

### Error: "No FCM token generated"

**Check:**
```bash
# 1. Ensure google-services.json has correct project_id
grep project_id app/google-services.json

# 2. Check logcat for Firebase errors
adb logcat | grep -i firebase

# 3. Verify Google Play Services installed
# (Use real device or emulator with Play Store)
```

### Error: Backend and Android can't communicate

**Check project match:**
```bash
# Backend project:
cat ../DoAnTotNghiep/BE/DoanBE/src/main/resources/firebase-service-account.json \
  | grep project_id

# Android project:
cat app/google-services.json \
  | grep project_id

# These MUST be the same!
```

---

## 🔐 Security Reminder

**After configuration, verify .gitignore:**

```bash
# Check if already in .gitignore
grep google-services.json .gitignore

# If not found, add it (already should be in root .gitignore)
# google-services.json should be excluded from Git
```

**⚠️ NEVER commit google-services.json to Git!**

---

## 📋 Quick Checklist

Use this to verify your configuration:

```
Configuration Checklist:

□ Backend's firebase-service-account.json exists
□ Found Firebase project_id from backend
□ Opened SAME project in Firebase Console
□ Added Android app with package: com.example.fitnessapp
□ Downloaded google-services.json
□ Placed file in: app/google-services.json
□ Verified project_id matches backend
□ Verified package_name is com.example.fitnessapp
□ Android app builds successfully
□ FCM token appears in logcat
□ google-services.json in .gitignore

If all checked ✓, configuration is complete!
```

---

## 🆘 Still Not Working?

**For detailed troubleshooting, see root project files:**
- `FIREBASE_SETUP_GUIDE.md` - Complete setup guide
- `NOTIFICATION_TROUBLESHOOTING.md` - Diagnostic flowcharts

**Quick diagnostic:**
```bash
# Collect this info and share with team:

echo "=== Android Config ==="
grep project_id app/google-services.json
grep package_name app/google-services.json

echo "=== Backend Config ==="
grep project_id ../DoAnTotNghiep/BE/DoanBE/src/main/resources/firebase-service-account.json

echo "=== Build Config ==="
grep applicationId app/build.gradle.kts
```

---

## 📚 Summary

**What you did:**
1. ✅ Used the **same Firebase project** as backend
2. ✅ Added Android app with **correct package name**
3. ✅ Downloaded and placed **google-services.json**
4. ✅ Verified **project_id matches** between Android and backend
5. ✅ Built and tested the Android app

**What happens now:**
- Android app can receive push notifications from backend
- Both use the same Firebase Cloud Messaging project
- Notifications sent by backend will reach Android app

**Key files:**
```
Backend:  ../DoAnTotNghiep/BE/DoanBE/src/main/resources/firebase-service-account.json
Android:  app/google-services.json
          ↓
      SAME Firebase Project
```

---

**Last Updated**: 2026-01-03
**Time to Complete**: ~10 minutes
**Difficulty**: Easy (if you follow steps exactly)
