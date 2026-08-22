#!/bin/zsh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IOS_DIR="$ROOT/iOS"
APP_NAME="DesktopPetOrHumanMobile"
VERSION="${VERSION:-1.0.0}"
DIST_DIR="$ROOT/releases/ios/v$VERSION"
BUILD_DIR="$IOS_DIR/build"
ARCHIVE_PATH="$BUILD_DIR/$APP_NAME.xcarchive"
EXPORT_DIR="$BUILD_DIR/export"
SIGNED_IPA="$DIST_DIR/$APP_NAME-v$VERSION-ios.ipa"
BUNDLE_ID="${BUNDLE_ID:-com.huang1988pioneer.desktoppetorhumanmobile}"
TEAM_ID="${DEVELOPMENT_TEAM:-CR63JJ5KWY}"

mkdir -p "$DIST_DIR" "$EXPORT_DIR"

"$IOS_DIR/package-unsigned-ipa.sh"

rm -rf "$ARCHIVE_PATH" "$EXPORT_DIR"
mkdir -p "$EXPORT_DIR"

xcodebuild archive \
  -project "$IOS_DIR/$APP_NAME.xcodeproj" \
  -scheme "$APP_NAME" \
  -configuration Release \
  -destination 'generic/platform=iOS' \
  -archivePath "$ARCHIVE_PATH" \
  -derivedDataPath "$BUILD_DIR/DerivedData-signed" \
  -allowProvisioningUpdates \
  DEVELOPMENT_TEAM="$TEAM_ID" \
  CODE_SIGN_STYLE=Automatic \
  CODE_SIGN_IDENTITY="Apple Development" \
  CODE_SIGNING_ALLOWED=YES \
  CODE_SIGNING_REQUIRED=YES \
  PRODUCT_BUNDLE_IDENTIFIER="$BUNDLE_ID"

xcodebuild -exportArchive \
  -archivePath "$ARCHIVE_PATH" \
  -exportPath "$EXPORT_DIR" \
  -exportOptionsPlist "$IOS_DIR/ExportOptions.plist" \
  -allowProvisioningUpdates

EXPORTED="$(find "$EXPORT_DIR" -maxdepth 1 -name '*.ipa' | head -n 1)"
if [[ -z "$EXPORTED" ]]; then
  echo "export did not produce an IPA" >&2
  exit 1
fi

cp "$EXPORTED" "$SIGNED_IPA"
shasum -a 256 "$SIGNED_IPA" | awk '{print $1}' > "$SIGNED_IPA.sha256"
ls -lh "$DIST_DIR"
echo "$SIGNED_IPA"
