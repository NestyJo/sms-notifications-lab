#!/usr/bin/env bash
set -euo pipefail

APP_NAME="lab-notification"
IMAGE_TAG="${APP_NAME}:latest"
SERVER_HOST="192.168.30.246"
SERVER_USER="babou_mamc"
REMOTE_TMP="/tmp/${APP_NAME}.tar.gz"
REMOTE_CONTAINER="${APP_NAME}"

if [[ $# -gt 0 ]]; then
  IMAGE_TAG="$1"
fi

if [[ $# -gt 1 ]]; then
  REMOTE_CONTAINER="$2"
fi

if [[ -z "${SERVER_PASSWORD:-}" ]]; then
  read -rsp "Enter password for ${SERVER_USER}@${SERVER_HOST}: " SERVER_PASSWORD
  echo
fi

cleanup() {
  rm -f "${APP_NAME}.tar.gz"
}
trap cleanup EXIT

./mvnw clean package -DskipTests

docker build -t "${IMAGE_TAG}" .

docker save "${IMAGE_TAG}" | gzip > "${APP_NAME}.tar.gz"

sshpass -p "${SERVER_PASSWORD}" scp "${APP_NAME}.tar.gz" "${SERVER_USER}@${SERVER_HOST}:${REMOTE_TMP}"

sshpass -p "${SERVER_PASSWORD}" ssh -o StrictHostKeyChecking=no "${SERVER_USER}@${SERVER_HOST}" <<EOF
set -euo pipefail
if docker ps -a --format '{{.Names}}' | grep -Eq '^${REMOTE_CONTAINER}
'; then
  docker stop "${REMOTE_CONTAINER}" || true
  docker rm "${REMOTE_CONTAINER}" || true
fi

docker load < "${REMOTE_TMP}"

rm -f "${REMOTE_TMP}"

docker run -d --restart unless-stopped --name "${REMOTE_CONTAINER}" -p 8080:8080 "${IMAGE_TAG}"
EOF
