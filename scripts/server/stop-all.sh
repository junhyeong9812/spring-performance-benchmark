#!/bin/bash
cd "$(dirname "$0")/../../docker"

echo "Stopping all services..."

docker compose down 2>/dev/null
docker compose -f docker-compose.mvc.yml down 2>/dev/null
docker compose -f docker-compose.webflux.yml down 2>/dev/null

echo ""
echo "All services stopped."