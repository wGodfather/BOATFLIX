#!/usr/bin/env bash
# Run inside Xvfb; keep the display alive until the application has exited.
set -euo pipefail
destination="$1"
shift
mkdir -p "$destination"
export NO_AT_BRIDGE=1
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Dskiko.renderApi=SOFTWARE"
setsid "$@" > "$destination/launch.log" 2>&1 &
app_pid=$!
cleanup() {
  kill -TERM -- "-$app_pid" 2>/dev/null || true
  wait "$app_pid" 2>/dev/null || true
}
trap cleanup EXIT
window_id=''
for attempt in {1..30}; do
  kill -0 "$app_pid" || { cat "$destination/launch.log"; exit 1; }
  window_id=$(xdotool search --onlyvisible --name '^BOATFLIX$' 2>/dev/null | head -1 || true)
  test -n "$window_id" && break
  sleep 1
done
test -n "$window_id"
sleep 12
kill -0 "$app_pid"
xdotool getwindowname "$window_id" > "$destination/window-title.txt"
import -window "$window_id" "$destination/screenshot.png"
if grep -E 'Exception in thread|UnsatisfiedLinkError|Could not initialize class|A fatal error has been detected|SIGSEGV' "$destination/launch.log"; then exit 1; fi
cleanup
trap - EXIT
cat "$destination/launch.log"
