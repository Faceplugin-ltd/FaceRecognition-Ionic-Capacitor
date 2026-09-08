#!/bin/bash
# Re-sign Face Recognition frameworks copied into the app (device installs).
# Drive binaries keep a vendor signature that iOS rejects as 0xe8008014
# unless we sign them with the same identity as the host app.
set -euo pipefail

if [ "${PLATFORM_NAME:-}" = "iphonesimulator" ]; then
  exit 0
fi

if [ -z "${EXPANDED_CODE_SIGN_IDENTITY:-}" ] || [ "${EXPANDED_CODE_SIGN_IDENTITY}" = "-" ]; then
  echo "error: EXPANDED_CODE_SIGN_IDENTITY unset; cannot sign Face Recognition frameworks" >&2
  echo "error: In Xcode, select the App target → Signing & Capabilities → your Team." >&2
  exit 1
fi

DEST="${TARGET_BUILD_DIR}/${FRAMEWORKS_FOLDER_PATH}"
if [ ! -d "${DEST}" ]; then
  echo "error: missing Frameworks folder: ${DEST}" >&2
  exit 1
fi

sign_one() {
  local fw="$1"
  if [ ! -d "${fw}" ]; then
    return 0
  fi
  if [ -d "${fw}/Frameworks" ]; then
    for nested in "${fw}/Frameworks"/*.framework; do
      [ -d "${nested}" ] || continue
      sign_one "${nested}"
    done
  fi
  echo "Signing $(basename "${fw}") (${TARGET_NAME:-App})"
  /usr/bin/codesign --remove-signature "${fw}" 2>/dev/null || true
  /usr/bin/codesign --force --sign "${EXPANDED_CODE_SIGN_IDENTITY}" \
    --timestamp=none \
    "${fw}"
}

signed=0
for name in facerecognitionsdk FaceRecognitionEngine onnxruntime; do
  fw="${DEST}/${name}.framework"
  if [ -d "${fw}" ]; then
    sign_one "${fw}"
    signed=1
  fi
done

if [ "${signed}" -eq 0 ]; then
  echo "error: no Face Recognition frameworks found in ${DEST}" >&2
  exit 1
fi
