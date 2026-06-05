# Mock backend (demo aid)

`mock_server.py` is a minimal, dependency-free HTTP server that implements just
enough of the placeholder API contract for the JMeter skeleton to produce a
realistic run:

| Method & path        | Response                          |
|----------------------|-----------------------------------|
| `GET  /api/catalog`     | `200` `{"items":[...]}`        |
| `POST /api/cart/items`  | `201` `{"cartId":"<uuid>"}`    |
| `POST /api/orders`      | `201` `{"orderId":"<uuid>"}`   |

```bash
python3 mock/mock_server.py 8080   # default port 8080
```

It exists purely so the reporting artifacts can be demonstrated end-to-end
without a real backend. Its response times are **not** representative of any
real system. See the "Demo run against a local mock backend" section in the
root `README.md`.
