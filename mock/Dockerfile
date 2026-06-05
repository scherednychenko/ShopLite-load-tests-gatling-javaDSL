# Minimal mock backend for the ShopLite load-test demo.
# Dependency-free: just Python's stdlib HTTP server.
FROM python:3.13-slim

WORKDIR /app
COPY mock_server.py .

EXPOSE 8080
CMD ["python", "mock_server.py", "8080"]
