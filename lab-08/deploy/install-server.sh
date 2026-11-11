#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
domain="${1:-}"
email="${2:-}"
if [ "$(id -u)" -ne 0 ]; then
    echo "Run with sudo on the target Ubuntu server."
    exit 1
fi
if [[ ! "$domain" =~ ^[a-z0-9]([a-z0-9.-]*[a-z0-9])?\.[a-z]{2,}$ ]] || [[ "$email" != *@*.* ]]; then
    echo "Usage: sudo bash deploy/install-server.sh example.org admin@example.org"
    exit 1
fi
if [ -n "${SSH_CONNECTION:-}" ] && [ "${SSH_CONNECTION##* }" != 22 ]; then
    echo "This lab firewall requires SSH on port 22. Keep your current SSH access before changing firewall rules."
    exit 1
fi

apt-get update
apt-get install -y ca-certificates curl openssl nginx certbot ufw
if ! command -v docker >/dev/null 2>&1; then
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    chmod a+r /etc/apt/keyrings/docker.asc
    . /etc/os-release
    printf 'deb [arch=%s signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu %s stable\n' \
        "$(dpkg --print-architecture)" "$VERSION_CODENAME" > /etc/apt/sources.list.d/docker.list
    apt-get update
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
fi
docker compose version >/dev/null
systemctl enable --now docker nginx

bash deploy/prepare-env.sh
docker compose config --quiet
docker compose up -d --build --wait --wait-timeout 240

ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

install -d -m 0755 /var/www/letsencrypt
site_file=/etc/nginx/sites-available/project-science
if [ ! -f "/etc/letsencrypt/live/$domain/fullchain.pem" ]; then
    sed "s/__DOMAIN__/$domain/g" deploy/nginx-http.conf > "$site_file"
    ln -sfn "$site_file" /etc/nginx/sites-enabled/project-science
    nginx -t
    systemctl reload nginx
    certbot certonly --webroot -w /var/www/letsencrypt \
        --non-interactive --agree-tos --email "$email" -d "$domain"
fi
sed "s/__DOMAIN__/$domain/g" deploy/nginx-https.conf > "$site_file"
ln -sfn "$site_file" /etc/nginx/sites-enabled/project-science
nginx -t
systemctl reload nginx
install -d -m 0755 /etc/letsencrypt/renewal-hooks/deploy
install -m 0755 deploy/reload-nginx.sh /etc/letsencrypt/renewal-hooks/deploy/project-science
systemctl enable --now certbot.timer

curl --fail --silent --show-error "https://$domain/" >/dev/null
echo "Deployed at https://$domain/"

