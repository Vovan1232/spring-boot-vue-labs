#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
username="${1:-}"
if [[ ! "$username" =~ ^[a-z0-9_.-]{3,32}$ ]]; then
    echo "Pass an existing registered username."
    exit 1
fi
docker compose exec -T db psql -U postgres -d science_db \
    --set=ON_ERROR_STOP=1 --set=username="$username" <<'SQL'
SELECT id AS app_user_id FROM users WHERE username = :'username' \gset
INSERT INTO user_roles (user_id, role_id)
SELECT :app_user_id, id FROM roles WHERE name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;
SQL
echo "Role granted. Sign out and sign in again."

