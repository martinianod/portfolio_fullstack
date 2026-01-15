#!/bin/bash

# Portfolio CRM Bootstrap Script
# This script helps you get the application running quickly

set -e

echo "🚀 Portfolio CRM Bootstrap Script"
echo "=================================="
echo ""

# Check for required tools
echo "📋 Checking prerequisites..."

command -v docker >/dev/null 2>&1 || { echo "❌ Docker is required but not installed. Aborting." >&2; exit 1; }
command -v docker-compose >/dev/null 2>&1 || command -v docker compose >/dev/null 2>&1 || { echo "❌ Docker Compose is required but not installed. Aborting." >&2; exit 1; }

echo "✅ Docker and Docker Compose found"
echo ""

# Create .env files if they don't exist
echo "📝 Setting up environment files..."

if [ ! -f backend/.env ]; then
    echo "Creating backend/.env from template..."
    cp backend/.env.example backend/.env
    echo "⚠️  Please review backend/.env and update with your credentials"
fi

if [ ! -f frontend/.env ]; then
    echo "Creating frontend/.env from template..."
    cp frontend/.env.example frontend/.env
    echo "⚠️  Please review frontend/.env if needed"
fi

echo "✅ Environment files ready"
echo ""

# Start services
echo "🐳 Starting services with Docker Compose..."
echo ""

if command -v docker-compose >/dev/null 2>&1; then
    docker-compose up -d
else
    docker compose up -d
fi

echo ""
echo "✅ Services are starting!"
echo ""
echo "⏳ Waiting for services to be healthy (this may take 1-2 minutes)..."
sleep 10

# Wait for backend to be healthy
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Backend is healthy!"
        break
    fi
    attempt=$((attempt + 1))
    echo "   Waiting for backend... ($attempt/$max_attempts)"
    sleep 5
done

if [ $attempt -eq $max_attempts ]; then
    echo "⚠️  Backend health check timeout. Please check logs with: docker-compose logs backend"
fi

echo ""
echo "🎉 Portfolio CRM is ready!"
echo ""
echo "📍 Access URLs:"
echo "   - Public Portfolio: http://localhost:5173"
echo "   - Admin Login:      http://localhost:5173/admin/login"
echo "   - Backend API:      http://localhost:8080"
echo "   - Health Check:     http://localhost:8080/actuator/health"
echo ""
echo "🔐 Default Admin Credentials:"
echo "   Username: admin"
echo "   Password: admin123"
echo "   ⚠️  CHANGE THESE IN PRODUCTION!"
echo ""
echo "📚 Documentation:"
echo "   - README.md - Setup and API documentation"
echo "   - NEXT_STEPS.md - Future enhancements guide"
echo ""
echo "🛠️  Useful commands:"
echo "   - View logs:    docker-compose logs -f"
echo "   - Stop:         docker-compose down"
echo "   - Rebuild:      docker-compose up -d --build"
echo ""
