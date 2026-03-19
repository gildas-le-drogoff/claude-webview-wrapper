#!/bin/bash
set -euo pipefail

APK_BASENAME="ClaudeWebView"
APK_EXT="apk"
CURRENT_DATE="$(date "+%F_%H")"
FINAL_APK_NAME="${APK_BASENAME}.${APK_EXT}"
TIMESTAMPED_APK_NAME="${APK_BASENAME}_${CURRENT_DATE}.${APK_EXT}"

RED=$(printf '\033[0;31m')
GREEN=$(printf '\033[0;32m')
PURPLE=$(printf '\033[0;35m')
NC=$(printf '\033[0m')

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

MODE_BUILD="${1:-release}"

if [[ $MODE_BUILD != "release" && $MODE_BUILD != "debug" ]]; then
	echo "Usage : $0 [release|debug]"
	exit 1
fi

echo "Vérification du keystore..."

cat > keystore.properties << EOF
storeFile=debug.keystore
storePassword=android
keyAlias=androiddebugkey
keyPassword=android
EOF

if [ ! -f debug.keystore ]; then
	echo "Génération de debug.keystore..."
	keytool -genkey -v -keystore debug.keystore -alias androiddebugkey \
		-keyalg RSA -keysize 2048 -validity 10000 \
		-storepass android -keypass android \
		-dname "CN=Android Debug,O=Android,C=US"
else
	echo "debug.keystore déjà présent."
fi

cp -u debug.keystore ./app
chmod +x ./gradlew

if [[ $MODE_BUILD == "release" ]]; then
	echo "Construction en mode RELEASE..."
	./gradlew assembleRelease --parallel --build-cache --configure-on-demand
	APK_PATH=$(find "$PROJECT_DIR/app/build/outputs/apk/release" -name "*.apk" | head -n 1)
else
	echo "Construction en mode DEBUG..."
	./gradlew assembleDebug --parallel --build-cache --configure-on-demand
	APK_PATH=$(find "$PROJECT_DIR/app/build/outputs/apk/debug" -name "*.apk" | head -n 1)
fi

if [ -f "$APK_PATH" ]; then
	cp "$APK_PATH" "$PROJECT_DIR/$FINAL_APK_NAME"
	printf "%b\n" "${GREEN}APK généré : ${PURPLE}${PROJECT_DIR}/${FINAL_APK_NAME}${NC}"

	# Optionnel : copie vers un répertoire de destination
	# Adapter DEST_DIR selon votre configuration
	# DEST_DIR="/chemin/vers/destination"
	# rsync -ah --info=progress2 "$PROJECT_DIR/$FINAL_APK_NAME" "${DEST_DIR}/${TIMESTAMPED_APK_NAME}"

	exit 0
else
	echo "Échec du build : APK introuvable."
	exit 1
fi
