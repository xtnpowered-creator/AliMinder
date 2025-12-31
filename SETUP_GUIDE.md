# Android Studio Setup & Galaxy S22 Configuration Guide

## 📥 Part 1: Installing Android Studio

### Step 1: Download Android Studio

1. Open your browser and go to: **https://developer.android.com/studio**
2. Click the green **"Download Android Studio"** button
3. Accept the terms and conditions
4. The download should start automatically (file size: ~1GB)

### Step 2: Install Android Studio

**For Windows:**
1. Once downloaded, run the `.exe` file
2. Follow the setup wizard:
   - ✅ Check "Android Studio"
   - ✅ Check "Android Virtual Device" (optional, for emulator)
3. Choose installation location (default is fine: `C:\Program Files\Android\Android Studio`)
4. Click **Install**
5. Wait for installation to complete (5-10 minutes)
6. Click **Finish**

### Step 3: First Launch Setup

1. Android Studio will launch and show **"Import Android Studio Settings"**
   - Select **"Do not import settings"** (first time)
2. Click **OK**
3. **Setup Wizard** will start:
   - Click **Next**
   - Choose **"Standard"** installation type
   - Select your preferred UI theme (Darcula or Light)
   - Click **Next**
4. **Verify Settings** screen will show what will be downloaded:
   - Android SDK
   - Android SDK Platform
   - Android Virtual Device
   - Click **Next**
5. Accept all license agreements (click each item and accept)
6. Click **Finish**
7. **Components download** begins (this takes 10-20 minutes depending on internet speed)

### Step 4: SDK Configuration

Once downloads complete:

1. Click **Finish**
2. You'll see the **"Welcome to Android Studio"** screen
3. Click **More Actions** (three dots) → **SDK Manager**
4. In **SDK Platforms** tab:
   - ✅ Check **Android 14.0 ("UpsideDownCake")** - API Level 34
   - ✅ Check **Android 8.0 (Oreo)** - API Level 26 (for testing minimum SDK)
   - ✅ Check **"Show Package Details"** at bottom right
5. In **SDK Tools** tab, ensure these are checked:
   - ✅ Android SDK Build-Tools (latest version)
   - ✅ Android Emulator
   - ✅ Android SDK Platform-Tools
   - ✅ Google Play services
6. Click **Apply** → **OK**
7. Wait for downloads to complete
8. Click **Finish**

---

## 📱 Part 2: Configuring Your Galaxy S22 for Development

### Step 1: Enable Developer Options

1. On your Galaxy S22, go to **Settings**
2. Scroll down and tap **About phone**
3. Find **Software information**
4. Tap **Build number** **7 times quickly**
5. You'll see a message: "You are now a developer!"

### Step 2: Enable USB Debugging

1. Go back to main **Settings**
2. Scroll down and tap **Developer options** (now visible)
3. Toggle **Developer options** to **ON** at the top
4. Scroll down and find **USB debugging**
5. Toggle **USB debugging** to **ON**
6. A warning will appear → Tap **OK**

### Step 3: Connect Phone to Computer

1. Take your USB cable and connect Galaxy S22 to your Windows PC
2. On your phone, a popup will appear: **"Allow USB debugging?"**
   - ✅ Check **"Always allow from this computer"**
   - Tap **OK**
3. Your phone should now be recognized

### Step 4: Verify Connection in Android Studio

1. In Android Studio, look for the **device dropdown** in the toolbar (top right area)
2. Your device should appear as: **"Samsung Galaxy S22"** or similar
3. If you don't see it:
   - Click **File** → **Invalidate Caches** → **Invalidate and Restart**
   - Or click the device dropdown → **Troubleshoot Device Connections**

**Troubleshooting:**
- If phone doesn't appear, try:
  - Different USB cable (must be data cable, not charge-only)
  - Different USB port (USB 3.0 ports work best)
  - Re-toggling USB debugging OFF then ON
  - Installing Samsung USB drivers: https://developer.samsung.com/android-usb-driver

---

## 🚀 Part 3: Creating the AliMinder Project

### Project Creation Steps

1. From Android Studio welcome screen, click **New Project**
2. Select **Empty Activity** (with Jetpack Compose)
3. Click **Next**
4. Configure your project:
   - **Name**: `AliMinder`
   - **Package name**: `com.aliminder.app`
   - **Save location**: `D:\MyApps\ForAlisara\AliMinder`
   - **Language**: `Kotlin`
   - **Minimum SDK**: `API 26 ("Oreo"; Android 8.0)`
   - **Build configuration language**: `Kotlin DSL (build.gradle.kts)`
5. Click **Finish**
6. Wait for Gradle sync to complete (first time takes 5-10 minutes)

---

## ✅ Verification

Once the project is created and Gradle sync completes:

1. You should see the project structure in the **Project** panel on the left
2. The **MainActivity.kt** file should be open showing "Hello Android" code
3. Click the **green play button** (▶) in the toolbar
4. Select your **Galaxy S22** from the device list
5. Click **OK**
6. The app should install and launch on your phone showing "Hello Android!"

**If the app launches successfully on your Galaxy S22, you're ready to build AliMinder! 🎉**

---

## 📋 Next Steps

After verification:
1. I'll set up the proper project architecture (MVVM + Clean Architecture)
2. Configure dependencies (Hilt, Room, Compose, etc.)
3. Begin implementing Phase 1: Foundation

---

## 🆘 Common Issues & Solutions

**Issue**: "ADB not found"
- **Solution**: Go to SDK Manager → SDK Tools → Install "Android SDK Platform-Tools"

**Issue**: "Gradle sync failed"
- **Solution**: Check internet connection, try **File** → **Sync Project with Gradle Files**

**Issue**: Phone not recognized
- **Solution**: Install Samsung USB drivers or try **File** → **Settings** → **Android** → **Enable ADB integration**

**Issue**: "Minimum SDK version" error
- **Solution**: Ensure phone is running Android 8.0 or higher (S22 should be fine)

---

**Once you've completed Parts 1-3 and verified the app runs, let me know and I'll proceed with the architecture setup!**
