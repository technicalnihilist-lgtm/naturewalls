#!/usr/bin/env bash
# Captures real device screenshots of NatureWalls on a running emulator.
# Used by .github/workflows/android.yml (Android 13 / Pixel 5, 1080x2340).
set -uo pipefail

PKG="${NW_PACKAGE:-com.naturewalls.app}"
ACTIVITY="$PKG/com.naturewalls.app.MainActivity"
APK="${NW_APK:-NatureWalls-1.0.0.apk}"
OUT="docs/screenshots"
ADB="adb -e"

mkdir -p "$OUT"

echo "== waiting for device"
$ADB wait-for-device
$ADB shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 2; done'
sleep 5

echo "== installing $APK"
$ADB install -r -g "$APK" || $ADB install -r "$APK"

$ADB shell am force-stop "$PKG"
$ADB shell am start -n "$ACTIVITY" > /dev/null
echo "== waiting for live content from Wikimedia Commons"
sleep 35

shot() {
  $ADB exec-out screencap -p > "$OUT/$1.png"
  echo "captured $1 ($(stat -c%s "$OUT/$1.png") bytes)"
}

# Taps a UI element by its visible text, using the accessibility tree (works with Compose).
tap_text() {
  local label="$1"
  $ADB shell uiautomator dump /sdcard/ui.xml > /dev/null 2>&1
  $ADB pull /sdcard/ui.xml /tmp/ui.xml > /dev/null 2>&1
  local xy
  xy=$(python3 - "$label" <<'PY'
import re, sys
try:
    xml = open('/tmp/ui.xml', encoding='utf-8', errors='ignore').read()
except Exception:
    sys.exit(0)
label = sys.argv[1]
for m in re.finditer(r'text="([^"]*)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml):
    if m.group(1).strip() == label:
        x1, y1, x2, y2 = map(int, m.groups()[1:])
        print((x1 + x2) // 2, (y1 + y2) // 2)
        break
PY
)
  if [ -n "${xy:-}" ]; then
    set -- $xy
    echo "   tap '$label' at $1,$2"
    $ADB shell input tap "$1" "$2"
    return 0
  fi
  echo "   (label '$label' not found)"
  return 1
}

# 1 — Home, live feed loaded
shot 01-home

# 2 — Collections further down the home feed
$ADB shell input swipe 540 1750 540 550 400
sleep 6
shot 02-home-collections

# 3 — Wallpaper detail (open the daily hero)
if tap_text "View wallpaper"; then
  sleep 14
  shot 03-wallpaper-detail
  # 4 — Credits block + licence terms on the detail screen
  $ADB shell input swipe 540 1700 540 450 400
  sleep 4
  shot 04-wallpaper-credits
  $ADB shell input keyevent 4
  sleep 4
else
  $ADB shell input tap 540 1050
  sleep 14
  shot 03-wallpaper-detail
  $ADB shell input keyevent 4
  sleep 4
fi

# 5 — Search
if tap_text "Search"; then
  sleep 10
  tap_text "waterfall" || true
  sleep 12
  shot 05-search
fi

# 6 — Favourites
if tap_text "Saved"; then
  sleep 8
  shot 06-favourites
fi

# 7 — Downloads
if tap_text "Files"; then
  sleep 8
  shot 07-downloads
fi

# 8 — Category browsing with sub-category chips
if tap_text "Home"; then
  sleep 8
  if tap_text "See all"; then
    sleep 14
    shot 08-category-browse
    $ADB shell input keyevent 4
    sleep 4
  fi
fi

# 9 — Credits ledger
if tap_text "Credits"; then
  sleep 10
  shot 09-credits-ledger
fi

echo "== screenshots:"
ls -lh "$OUT"
