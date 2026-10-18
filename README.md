# Ultra2 Remapper

Remap the buttons on the Samsung Galaxy Watch Ultra 2 (Wear OS; tested on SM-L715F only) to media controls or any app:

- **Action button:** single press, double press and long press, each mapped separately
- **Bottom button:** long press (a short press still goes Back)
- **Original (Samsung)** is available for every gesture, keeping Samsung's own behaviour for that gesture
- **Pass-through apps:** the bottom button behaves as stock in the apps you pick (Samsung Health by default, so workout controls keep working)

The emergency siren (hold the Action button 5–6 s) keeps working.

## How it works

The Action button never delivers key events to apps or accessibility services. Instead, the system starts the service named in the global setting `setting_action_button_press` (by default Samsung Health's workout launcher). It passes `ACTION_KEY_EVENT` extras: `2` = single click, `5` + `ACTION_KEY_COUNT=2` = double press, `3` = long press.

Ultra2 Remapper provides that service and performs whatever you've assigned to each gesture in its settings screen.

The bottom button has no such setting: its long press is hard-wired to Samsung Wallet in the framework. It does deliver key events (`KEYCODE_STEM_PRIMARY`) to accessibility services, so Ultra2 Remapper's accessibility service intercepts that key. A short press is passed on as Back; a 500 ms hold runs your mapping instead of opening Samsung Wallet. The service subscribes to no accessibility events (key filtering only), so it doesn't wake for screen changes and costs essentially no battery.

When you press the bottom button, the service checks which app is on screen (a one-off lookup, no event subscription). If that app is on your pass-through list, or the bottom long press is set to Original, the press goes through untouched.

For an Action button gesture set to Original, the press is forwarded to Samsung Health's handler with the same extras, as if the system had sent it.

This is optional: if you only want the Action button, skip step 5.

## Setup (one-time, via adb)

1. Enable **Developer options → ADB debugging** and **Wireless debugging** on the watch, then pair and connect with `adb pair` / `adb connect`.
2. Install the APK:
   ```
   adb install ultra-remapper.apk
   ```
3. Route the Action button to Ultra2 Remapper:
   ```
   adb shell settings put global setting_action_button_press com.bennybar.ultraremapper/com.bennybar.ultraremapper.ActionButtonService
   ```
4. Allow it to open apps from the background (needed for "open app" actions):
   ```
   adb shell appops set com.bennybar.ultraremapper SYSTEM_ALERT_WINDOW allow
   ```
5. *(Optional, for the bottom button)* Enable the accessibility service. Use **Settings → Accessibility → Installed apps → Ultra2 Remapper** on the watch, or run:
   ```
   adb shell settings put secure enabled_accessibility_services com.bennybar.ultraremapper/com.bennybar.ultraremapper.BottomButtonService
   adb shell settings put secure accessibility_enabled 1
   ```
   The adb command replaces any other enabled accessibility services. Read the current list first with `adb shell settings get secure enabled_accessibility_services` and join entries with `:` if you need to keep them. After updating the app, check the service is still enabled.

Open **Ultra2 Remapper** on the watch to choose an action for each gesture: open an app, play/pause, next or previous track, volume up/down, Original (Samsung), or nothing. Under **Bottom Button → Pass-through apps**, pick the apps where the bottom button should behave as stock.

Changing the Action button in Samsung's settings overwrites step 3. To restore Samsung's default:
```
adb shell settings put global setting_action_button_press com.samsung.android.wear.shealth/com.samsung.android.wear.shealth.service.ActionButtonService
```

## License

GPL-3.0. See [LICENSE](LICENSE).
