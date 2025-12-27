#!/bin/bash
cd "$(dirname "$0")/../.."

echo "=========================================="
echo "Building applications..."
echo "=========================================="
./gradlew clean build -x test

echo ""
echo "=========================================="
echo "Starting all services..."
echo "=========================================="
cd docker
docker compose up -d --build

echo ""
echo "=========================================="
echo "Waiting for services to be ready..."
echo "=========================================="
sleep 10

echo ""
echo "=========================================="
echo "Service Status:"
echo "=========================================="
docker compose ps

echo ""
echo "=========================================="
echo "Access URLs:"
echo "  - MVC API:     http://localhost/mvc/api/info"
echo "  - WebFlux API: http://localhost/webflux/api/info"
echo "  - Prometheus:  http://localhost:9090"
echo "  - Grafana:     http://localhost:3000 (admin/admin)"
echo "=========================================="