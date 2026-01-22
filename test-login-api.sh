#!/bin/bash
# Test script to demonstrate the login functionality
# This script shows how to use the login API with both email and username

echo "=========================================="
echo "Login API Test Script"
echo "=========================================="
echo ""

API_URL="http://localhost:8080/api/v1/auth/login"

echo "1. Login with EMAIL (standard way):"
echo "   Request: { \"email\": \"admin@martiniano.dev\", \"password\": \"admin123\" }"
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@martiniano.dev","password":"admin123"}' \
  -w "\n\n" 2>/dev/null || echo "Backend not running. Start with: docker compose up"

echo "2. Login with USERNAME (backward compatibility):"
echo "   Request: { \"username\": \"admin\", \"password\": \"admin123\" }"
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  -w "\n\n" 2>/dev/null || echo "Backend not running. Start with: docker compose up"

echo "3. Validation Error - Missing both email and username:"
echo "   Request: { \"password\": \"admin123\" }"
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{"password":"admin123"}' \
  -w "\n\n" 2>/dev/null || echo "Backend not running. Start with: docker compose up"

echo "4. Validation Error - Missing password:"
echo "   Request: { \"email\": \"admin@martiniano.dev\" }"
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@martiniano.dev"}' \
  -w "\n\n" 2>/dev/null || echo "Backend not running. Start with: docker compose up"

echo "5. Authentication Error - Wrong password:"
echo "   Request: { \"email\": \"admin@martiniano.dev\", \"password\": \"wrong\" }"
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@martiniano.dev","password":"wrong"}' \
  -w "\n\n" 2>/dev/null || echo "Backend not running. Start with: docker compose up"

echo "=========================================="
echo "Test completed!"
echo "=========================================="
echo ""
echo "To run the backend:"
echo "  cd /path/to/portfolio_fullstack"
echo "  docker compose up"
echo ""
echo "Expected results:"
echo "  1 & 2: 200 OK with JWT token"
echo "  3 & 4: 400 Bad Request - Validation failed"
echo "  5: 401 Unauthorized - Authentication failed"
