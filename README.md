# ELIA — Native Android Personal AI Assistant

This is a genuine native Kotlin/Jetpack Compose Android project with a launcher identity, app-drawer entry, animated assistant UI, microphone speech recognition, Android Text-to-Speech, persistent local conversation history, explicit local memories, settings, and a secure AI-backend architecture.

## Build and install
1. Install Android Studio.
2. Open this folder as a project.
3. Let Gradle sync and install requested Android SDK components.
4. Connect your Android phone with USB debugging enabled, or use an emulator.
5. Press Run to install ELIA.
6. For an APK: Build > Build App Bundle(s) / APK(s) > Build APK(s).
7. Copy the generated APK to the phone and install it. If Android asks, allow installation from that source.
8. ELIA will appear in the app drawer; long-press the app icon to place it on the home screen.

## AI connection
The app intentionally does not contain a private AI key. Put a secure backend URL in Settings. `server/` contains an example backend contract. Advanced features such as wake-word activation, widgets, system-assistant role, alarms, calendar and device actions are not falsely claimed as implemented.

## Environment limitation
This chat environment does not have an Android SDK/Gradle build toolchain available, so I cannot honestly provide a compiled APK from here. The source project is provided so Android Studio can compile the real APK.
