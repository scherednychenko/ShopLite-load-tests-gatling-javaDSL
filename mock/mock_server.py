#!/usr/bin/env python3
"""
Minimal mock backend for the ShopLite JMeter skeleton.

It implements just enough of the placeholder API contract so the test plan
produces a realistic (green) run for demonstrating the reporting artifacts:

  GET  /api/catalog     -> 200  {"items":[...]}
  POST /api/cart/items  -> 201  {"cartId": "<uuid>"}
  POST /api/orders      -> 201  {"orderId": "<uuid>"}

This is a DEMO aid only - it is not a real backend and the latencies it
produces are not representative of any real system.

Usage:
    python3 mock/mock_server.py [port]   # default port 8080
"""
import json
import sys
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def _send(self, status, payload):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _drain_body(self):
        length = int(self.headers.get("Content-Length", 0) or 0)
        if length:
            self.rfile.read(length)

    def do_GET(self):
        if self.path.split("?")[0] == "/api/catalog":
            self._send(200, {"items": [{"productId": p} for p in (1001, 1002, 1003)]})
        else:
            self._send(404, {"error": "not found"})

    def do_POST(self):
        self._drain_body()
        path = self.path.split("?")[0]
        if path == "/api/cart/items":
            self._send(201, {"cartId": str(uuid.uuid4())})
        elif path == "/api/orders":
            self._send(201, {"orderId": str(uuid.uuid4())})
        else:
            self._send(404, {"error": "not found"})

    def log_message(self, *args):
        pass  # keep the console quiet


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
    # Bind all interfaces so the server is reachable both locally (127.0.0.1)
    # and from other containers on a Docker network.
    server = ThreadingHTTPServer(("0.0.0.0", port), Handler)
    print(f"ShopLite mock backend listening on http://0.0.0.0:{port}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        server.shutdown()
