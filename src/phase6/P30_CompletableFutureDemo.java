package phase6;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - CompletableFuture: a Future where we say "WHEN ready, do this next"
 *   - supplyAsync(): run a task in the background and give a result
 *   - thenApply(): when ready, change the result (like map)
 *   - thenCompose(): when ready, start ANOTHER background task that needs this result
 *   - thenCombine(): when TWO independent tasks are ready, join their results
 *   - allOf(): wait until ALL tasks are ready
 *   - join(): get the result (like get(), but without checked exceptions)
 *
 * REAL-LIFE EXAMPLE: ORDERING FOOD
 *   - Kitchen makes pizza and drink at the same time.  = supplyAsync()
 *   - "When pizza is ready, add cheese."               = thenApply()
 *   - "When order is taken, send the delivery boy."    = thenCompose()
 *   - "When pizza AND drink are ready, make a tray."   = thenCombine()
 *   - "Wait until every dish is ready."                = allOf()
 *
 * PROBLEM:
 *   Future.get() blocks. To use the result of a task, main must stand and wait.
 *   Future cannot say "when ready, do this next".
 *
 * SOLUTION:
 *   CompletableFuture. We describe the steps. They run when the result is ready.
 *   main is free until it really needs the final result.
 */
public class P30_CompletableFutureDemo {

    // Supplier cannot throw InterruptedException, so we need this small helper.
    // (sleep only pretends that the work is slow.)
    static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws Exception {

        // ---------------------------------------------------------
        // Section 1: BROKEN - Future. main must wait at get().
        // There is no way to say "when pizza is ready, add cheese".
        // ---------------------------------------------------------
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<String> pizzaFuture = pool.submit(() -> {
            pause(500);
            return "pizza";
        });
        System.out.println("Section 1 main waits at get() and can do nothing else...");
        String pizza1 = pizzaFuture.get();               // main is BLOCKED here
        System.out.println("Section 1 " + pizza1 + " + cheese");   // main adds the cheese itself
        pool.shutdown();

        // ---------------------------------------------------------
        // Section 2: FIXED - supplyAsync + thenApply
        // We describe "make pizza, then add cheese". main is free.
        // ---------------------------------------------------------
        CompletableFuture<String> order2 = CompletableFuture
                .supplyAsync(() -> {
                    pause(500);
                    return "pizza";
                })
                .thenApply(p -> p + " + cheese");         // runs when pizza is ready

        System.out.println("Section 2 main is free. It is NOT blocked.");
        System.out.println("Section 2 result = " + order2.join());   // wait only here

        // ---------------------------------------------------------
        // Section 3: thenCompose - the next task needs the result of the first
        // Step 1: take the order (get an id). Step 2: send delivery for that id.
        // ---------------------------------------------------------
        CompletableFuture<String> delivery = CompletableFuture
                .supplyAsync(() -> {
                    pause(300);
                    return "order-7";                     // order is taken
                })
                .thenCompose(id -> CompletableFuture.supplyAsync(() -> {
                    pause(300);
                    return id + " delivered";             // second background task
                }));
        System.out.println("Section 3 result = " + delivery.join());

        // ---------------------------------------------------------
        // Section 4: thenCombine - two independent tasks run AT THE SAME TIME
        // Pizza 500 ms, drink 500 ms. Total about 500 ms, not 1000 ms.
        // ---------------------------------------------------------
        long start = System.currentTimeMillis();
        CompletableFuture<String> pizza4 = CompletableFuture.supplyAsync(() -> {
            pause(500);
            return "pizza";
        });
        CompletableFuture<String> drink4 = CompletableFuture.supplyAsync(() -> {
            pause(500);
            return "drink";
        });
        CompletableFuture<String> tray = pizza4.thenCombine(drink4, (p, d) -> "tray(" + p + " + " + d + ")");
        System.out.println("Section 4 result = " + tray.join()
                + " after " + (System.currentTimeMillis() - start) + " ms");

        // ---------------------------------------------------------
        // Section 5: allOf - wait until ALL dishes are ready
        // ---------------------------------------------------------
        CompletableFuture<String> soup = CompletableFuture.supplyAsync(() -> {
            pause(300);
            return "soup";
        });
        CompletableFuture<String> pasta = CompletableFuture.supplyAsync(() -> {
            pause(500);
            return "pasta";
        });
        CompletableFuture<String> salad = CompletableFuture.supplyAsync(() -> {
            pause(200);
            return "salad";
        });

        CompletableFuture.allOf(soup, pasta, salad).join();   // waits for the slowest (500 ms)
        System.out.println("Section 5 all ready: " + soup.join() + ", " + pasta.join() + ", " + salad.join());
    }
}