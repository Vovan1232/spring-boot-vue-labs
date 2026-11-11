#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [ -e .env ]; then
    echo ".env already exists; keeping its values."
    exit 0
fi
umask 077
admin_password="$(openssl rand -hex 32)"
app_password="$(openssl rand -hex 32)"
printf 'POSTGRES_ADMIN_PASSWORD=%s\nAPP_DB_PASSWORD=%s\n' "$admin_password" "$app_password" > .env
unset admin_password app_password
echo "Created .env with permissions 600."

