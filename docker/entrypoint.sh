#!/bin/sh
set -eu
http_port="${PORT:-10000}"
case "$http_port" in
    ''|*[!0-9]*) echo "PORT must be a number between 1 and 65535" >&2; exit 1 ;;
esac
if [ "$http_port" -lt 1 ] || [ "$http_port" -gt 65535 ]; then
    echo "PORT must be between 1 and 65535" >&2
    exit 1
fi
export CATALINA_OPTS="${CATALINA_OPTS:-} -Dhttp.port=$http_port"
exec catalina.sh run
