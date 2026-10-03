#!/usr/bin/env bash
set -euo pipefail
mkdir -p tv-qa
adb wait-for-device
for attempt in {1..60}; do
  test "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 && break
  sleep 2
done
adb shell input keyevent KEYCODE_WAKEUP
./gradlew :androidApp:connectedFullDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.nuvio.android.BoatflixTvRemoteTest \
  -Pandroid.testInstrumentationRunnerArguments.leaveInstalled=true \
  -Pkotlin.compiler.execution.strategy=in-process --max-workers=1 --no-configuration-cache --no-daemon
adb pull /sdcard/Android/data/com.wgodfather.boatflix.debug/files/fork-ui-qa tv-qa/screenshots
adb install -r "dist/BOATFLIX-Android-${TV_ABI:-x86_64}-$RELEASE_VERSION.apk"
adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER -p com.wgodfather.boatflix > tv-qa/release-launch.txt
sleep 12
adb shell dumpsys activity activities > tv-qa/release-activity.txt
grep -q 'com.wgodfather.boatflix/com.nuvio.app.BoatflixTvActivity' tv-qa/release-activity.txt
adb shell input keyevent KEYCODE_DPAD_DOWN
adb shell input keyevent KEYCODE_DPAD_UP
adb shell screencap -p /sdcard/boatflix-tv-release.png
adb pull /sdcard/boatflix-tv-release.png tv-qa/boatflix-tv-release.png
adb logcat -d > tv-qa/logcat.txt
if grep -A 6 'FATAL EXCEPTION' tv-qa/logcat.txt | grep -q 'com.wgodfather.boatflix'; then
  echo 'BOATFLIX crashed on Android TV' >&2
  exit 1
fi
