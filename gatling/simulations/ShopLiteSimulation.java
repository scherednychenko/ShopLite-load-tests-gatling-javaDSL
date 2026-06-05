import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * ShopLite load test — Gatling (Java DSL).
 *
 * Mirrors the JMeter/k6/Locust scenario: Browse catalog -> Add to cart (N items)
 * -> Checkout, against placeholder endpoints served by the local mock backend.
 */
public class ShopLiteSimulation extends Simulation {

  private final String baseUrl = System.getenv().getOrDefault("BASE_URL", "http://localhost:8080");
  private final int cartSize = Integer.parseInt(System.getenv().getOrDefault("CART_SIZE", "10"));
  private final int vus = Integer.parseInt(System.getenv().getOrDefault("VUS", "10"));

  private final HttpProtocolBuilder httpProtocol = http
      .baseUrl(baseUrl)
      .contentTypeHeader("application/json")
      .acceptHeader("application/json");

  private final String[] products = {"1001", "1002", "1003"};

  // Per-pass data: unique guest email + a random product.
  private final Iterator<Map<String, Object>> guestFeeder =
      Stream.<Map<String, Object>>generate(() -> {
        Map<String, Object> data = new HashMap<>();
        data.put("email", "qa.perf+" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        data.put("productId", products[ThreadLocalRandom.current().nextInt(products.length)]);
        return data;
      }).iterator();

  private final ScenarioBuilder scn = scenario("ShopLite")
      .feed(guestFeeder)
      .exec(http("TX_Browse_Catalog")
          .get("/api/catalog?page=1&size=20")
          .check(status().is(200)))
      .pause(Duration.ofMillis(300), Duration.ofMillis(1200))
      .repeat(cartSize).on(
          feed(guestFeeder)
              .exec(http("TX_Add_To_Cart")
                  .post("/api/cart/items")
                  .body(StringBody("{\"productId\":\"#{productId}\",\"qty\":1}")).asJson()
                  .check(status().in(200, 201))
                  .check(jsonPath("$.cartId").saveAs("cartId")))
              .pause(Duration.ofMillis(300), Duration.ofMillis(1200)))
      .exec(http("TX_Checkout_PlaceOrder")
          .post("/api/orders")
          .body(StringBody("{\"cartId\":\"#{cartId}\",\"guest\":{\"email\":\"#{email}\",\"firstName\":\"Perf\",\"lastName\":\"Guest\",\"phone\":\"+10000000000\"},\"shippingAddress\":{\"country\":\"HR\",\"city\":\"Zagreb\",\"addressLine1\":\"Perf Street 1\",\"zip\":\"10000\"}}")).asJson()
          .check(status().in(200, 201)))
      .pause(Duration.ofMillis(300), Duration.ofMillis(1200));

  {
    setUp(scn.injectOpen(rampUsers(vus).during(Duration.ofSeconds(10))))
        .protocols(httpProtocol)
        .assertions(
            global().failedRequests().percent().lt(1.0),
            global().responseTime().percentile(95.0).lt(500));
  }
}
