#!/usr/bin/env bash
set -euo pipefail
mkdir -p build/upgrade-qa
package=com.wgodfather.boatflix
old=build/upgrade-133/BOATFLIX-Android-universal-1.33.apk
new="dist/BOATFLIX-Android-x86_64-$RELEASE_VERSION.apk"
build_tools=$(find "$ANDROID_HOME/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -1)
for apk in "$old" "$new"; do
  "$build_tools/apksigner" verify --print-certs "$apk" | grep 'certificate SHA-256 digest' | head -1 | sed 's/.*digest: //' >> build/upgrade-qa/certificates.txt
done
test "$(sort -u build/upgrade-qa/certificates.txt | wc -l)" = 1
adb install "$old"
launch_app() {
  local component
  component=$(adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p "$package" | tr -d '\r' | tail -1)
  case "$component" in "$package/"*) ;; *) echo 'BOATFLIX launcher could not be resolved' >&2; return 1 ;; esac
  adb shell am start -W -n "$component" | tee "$1"
}
launch_app build/upgrade-qa/old-launch.txt
sleep 5
adb shell am force-stop "$package"
# This script runs only on a disposable emulator. Root permits a small owned-data
# sentinel without making the release application debuggable.
adb root
adb wait-for-device
uid_before=$(adb shell cmd package list packages -U "$package" | tr -d '\r')
sentinel="/data/user/0/$package/files/episode-upgrade-sentinel.txt"
adb shell "mkdir -p /data/user/0/$package/files; printf 'BOATFLIX episode upgrade QA' > $sentinel"
uid=$(printf '%s' "$uid_before" | sed -n 's/.*uid:\([0-9]*\).*/\1/p')
test -n "$uid"
adb shell "chown $uid:$uid $sentinel"
before=$(adb shell sha256sum "$sentinel" | tr -d '\r')
adb install -r "$new"
uid_after=$(adb shell cmd package list packages -U "$package" | tr -d '\r')
test "$uid_before" = "$uid_after"
test "$before" = "$(adb shell sha256sum "$sentinel" | tr -d '\r')"
adb shell dumpsys package "$package" > build/upgrade-qa/current-package.txt
version_code=$(sed -n 's/^VERSION_CODE=//p' composeApp/Configuration/DesktopVersion.properties | tr -d '\r')
grep -q "versionCode=$version_code" build/upgrade-qa/current-package.txt
grep -q "versionName=$RELEASE_VERSION" build/upgrade-qa/current-package.txt
launch_app build/upgrade-qa/current-launch.txt
adb shell am force-stop "$package"
printf 'PASS: same signing certificate, package UID and owned data preserved from 1.33 to %s\n' "$RELEASE_VERSION" > build/upgrade-qa/result.txt
