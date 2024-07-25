# Flutter-App-Mod-Loader

## Overview

**Flutter-App-Mod-Loader** is a proof-of-concept Android application designed to dynamically load Flutter libraries (both engine and data) from another application without requiring recompilation. By utilizing the `System.load()` function, this loader can access the shared objects used by Flutter within the target app. It modifies the `io.flutter` Android package to leverage `libapp.so` (the snapshot with data) from the target app, providing access to packages, RAM memory, and app data—all without root access.

### Key Features
- **Dynamic Library Loading**: Load Flutter engine and data libraries from a separate app at runtime.
- **Native Library Modification**: Edit and debug native parts of Flutter packages.
- **Runtime Memory Manipulation**: Access and modify memory, enabling powerful testing and analysis capabilities.
- **Pentesting Capabilities**: Simulate attacks and perform security testing on Flutter applications.
- **No Root Required**: Operate without root access, facilitating testing on various devices.
- **Enhanced Debugging**: Gain deeper insights into Flutter app behavior through dynamic library interaction.

## How It Works

1. **Installation**: Install both the loader app and the target app.
2. **Library Loading**: The loader app initiates the system to load the private libraries of the target app.
3. **Flutter View Initialization**: Start a Flutter view within the loader that utilizes the target app's snapshot and code.
4. **Internal Response Handling**: The loader app handles internal requests, including those involving native bridge libraries.
5. **Memory Editing**: Access and modify memory at runtime, leveraging the loader’s privileges.

> Note: The native libraries used by the target app must be included in the loader for proper functionality and modification.

## Video Demonstration

Watch the video below to see how the mod loader loads the Flutter engine and code from a second app, and how it uses secure storage to manage sensitive data:

https://youtube.com/shorts/OGcorzsI27g?feature=share

## Requirements
- Android Studio
- Kotlin
- Android device or emulator
- Flutter SDK
- Jadx (for decompiling and modifying APKs)

## Installation

1. **Clone the Repository**:
    ```sh
    git clone https://github.com/yourusername/Flutter-App-Mod-Loader.git
    cd Flutter-App-Mod-Loader
    ```

2. **Open the Project in Android Studio**:
    - Launch Android Studio and open the cloned project directory.

3. **Set Up Your Environment**:
    - Ensure Kotlin and the Android SDK are installed and configured.
    - Set up an Android device or emulator.

4. **Build and Run the App**:
    - Build the project using Android Studio.
    - Deploy the app on your device or emulator.

5. **Build Target Flutter Example**:
    - Open the `target_app_lod` directory.
    - Run `flutter run --release`.

### Example Usage

1. Install the "target_app_lod" app.
2. Open the ModLoader.
3. Click the first button to launch the target app inside the loader.
4. Add elements as needed.
5. Use the back gesture to return to the loader.
6. Click "Load data" and verify the results.

**Explanation**: While the data remains secure, the ModLoader has access to the target app's memory and native libraries, allowing runtime modifications without root access, emulators, or APK editing.

### Setup from Scratch

**Create the Loader App**:
- Develop a basic loader app using Jetpack Compose.
- Add the following dependencies to `build.gradle`:
    ```groovy
    implementation "androidx.window:window:1.3.0"
    implementation "androidx.window:window-java:1.3.0"
    implementation 'net.fornwall:jelf:0.9.0'
    implementation libs.androidx.fragment
    implementation libs.androidx.lifecycle.process
    implementation libs.core
    implementation libs.androidx.tracing
    ```

**Modify `FlutterJNI.java`**:
```java
public void loadLibrary() {
    if (FlutterJNI.loadLibraryCalled) {
        Log.w(TAG, "FlutterJNI.loadLibrary called more than once");
    }
    // Comment out this call
    // System.loadLibrary("flutter");
    FlutterJNI.loadLibraryCalled = true;
}
```

**Modify `ApplicationInfoLoader.java`**:
```java
@NonNull
public static FlutterApplicationInfo load(@NonNull Context applicationContext) {
    ApplicationInfo appInfo = getApplicationInfo(applicationContext);
    // Get the lib path of the target app
    String newLibPath = applicationContext.getSharedPreferences("FL", MODE_PRIVATE).getString("lib", appInfo.nativeLibraryDir);
    return new FlutterApplicationInfo(
        getString(appInfo.metaData, PUBLIC_AOT_SHARED_LIBRARY_NAME),
        getString(appInfo.metaData, PUBLIC_VM_SNAPSHOT_DATA_KEY),
        getString(appInfo.metaData, PUBLIC_ISOLATE_SNAPSHOT_DATA_KEY),
        getString(appInfo.metaData, PUBLIC_FLUTTER_ASSETS_DIR_KEY),
        getNetworkPolicy(appInfo, applicationContext),
        // Use new path
        newLibPath,
        getBoolean(appInfo.metaData, PUBLIC_AUTOMATICALLY_REGISTER_PLUGINS_METADATA_KEY, true)
    );
}
```

## Next Steps

1. **Prepare the Target Flutter App**:
    - Identify the target app and extract its package name and libraries.

2. **Modify the App**:
    - Decompile the APK using Jadx.
    - Adjust the loader's package to use native packages from the target app.
    - Copy the flutter assets folder

3. **Load Libraries**:
    - Use the `LibLoader` class to load shared objects (e.g., `libflutter.so`) from the target app.
    - Ensure all native dependencies are included.

4. **Example Code**:
    ```kotlin
    val pm = packageManager
    val info: PackageInfo = pm.getPackageInfo(
        <TargetAppPackage>,
        PackageManager.GET_SHARED_LIBRARY_FILES
    )
    val nativeLibraryDir = info.applicationInfo.nativeLibraryDir
    getSharedPreferences("FL", MODE_PRIVATE).edit()
        .putString("lib", nativeLibraryDir).apply()
    val loader = LibLoader("$nativeLibraryDir:/system/lib64")
    loader.loadLib("libflutter.so")
    startActivity(
        FlutterActivity.createDefaultIntent(context)
    )
    ```

## Contributing

Contributions are welcome! To contribute:
1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Commit your changes.
4. Push the branch to your forked repository.
5. Create a pull request with a description of your changes.

## Disclaimer

This project is a proof of concept intended for educational purposes. Unauthorized use of this technology to load libraries from other apps may violate terms of service or end-user license agreements.