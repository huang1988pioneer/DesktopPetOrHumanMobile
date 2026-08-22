#!/bin/zsh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IOS_DIR="$ROOT/iOS"
APP_NAME="DesktopPetOrHumanMobile"
VERSION="${VERSION:-1.0.0}"
BUILD_NUMBER="${BUILD_NUMBER:-1}"
BUNDLE_ID="${BUNDLE_ID:-com.huang1988pioneer.desktoppetorhumanmobile}"
BUILD_DIR="$IOS_DIR/build"
APP_BUNDLE="$BUILD_DIR/Payload/$APP_NAME.app"
DIST_DIR="$ROOT/releases/ios/v$VERSION"
SDK_PATH="$(xcrun --sdk iphoneos --show-sdk-path | tail -n 1)"
SWIFTC="$(xcrun --sdk iphoneos --find swiftc | tail -n 1)"
OUTPUT_IPA="$DIST_DIR/$APP_NAME-v$VERSION-ios-unsigned.ipa"
ASSET_SRC="$ROOT/app/src/main/res/drawable-nodpi"

rm -rf "$BUILD_DIR/Payload"
mkdir -p "$APP_BUNDLE/Characters" "$DIST_DIR" "$BUILD_DIR/ModuleCache"

/usr/bin/plutil -convert xml1 "$IOS_DIR/$APP_NAME/Info.plist" -o "$APP_BUNDLE/Info.plist"
/usr/bin/plutil -replace CFBundleShortVersionString -string "$VERSION" "$APP_BUNDLE/Info.plist"
/usr/bin/plutil -replace CFBundleVersion -string "$BUILD_NUMBER" "$APP_BUNDLE/Info.plist"
/usr/bin/plutil -replace CFBundleIdentifier -string "$BUNDLE_ID" "$APP_BUNDLE/Info.plist"
printf 'APPL????' > "$APP_BUNDLE/PkgInfo"

cp "$ASSET_SRC/"*.png "$APP_BUNDLE/Characters/"
sips -z 180 180 "$ASSET_SRC/fengge_idle.png" --out "$APP_BUNDLE/AppIcon.png" >/dev/null

"$SWIFTC" \
  -sdk "$SDK_PATH" \
  -target arm64-apple-ios16.0 \
  -module-cache-path "$BUILD_DIR/ModuleCache" \
  -parse-as-library \
  -O \
  -whole-module-optimization \
  -framework SwiftUI \
  -framework UIKit \
  "$IOS_DIR/$APP_NAME/App.swift" \
  "$IOS_DIR/$APP_NAME/CharacterTalk.swift" \
  -o "$APP_BUNDLE/$APP_NAME"

if [[ -n "${IOS_PROVISIONING_PROFILE_PATH:-}" ]]; then
  cp "$IOS_PROVISIONING_PROFILE_PATH" "$APP_BUNDLE/embedded.mobileprovision"
fi

if [[ -n "${IOS_CODE_SIGN_IDENTITY:-}" ]]; then
  codesign_args=(--force --sign "$IOS_CODE_SIGN_IDENTITY" --timestamp=none)
  if [[ -n "${IOS_ENTITLEMENTS_PATH:-}" ]]; then
    codesign_args+=(--entitlements "$IOS_ENTITLEMENTS_PATH")
  fi
  codesign "${codesign_args[@]}" "$APP_BUNDLE"
else
  echo "No IOS_CODE_SIGN_IDENTITY set; producing ad-hoc signed IPA without provisioning profile."
  codesign --force --sign - --timestamp=none "$APP_BUNDLE"
fi

rm -f "$OUTPUT_IPA"
(
  cd "$BUILD_DIR"
  COPYFILE_DISABLE=1 /usr/bin/zip -qry "$OUTPUT_IPA" Payload
)

shasum -a 256 "$OUTPUT_IPA" | awk '{print $1}' > "$OUTPUT_IPA.sha256"
file "$APP_BUNDLE/$APP_NAME"
/usr/bin/plutil -lint "$APP_BUNDLE/Info.plist"
/usr/bin/unzip -l "$OUTPUT_IPA" | sed -n '1,28p'
echo "$OUTPUT_IPA"
