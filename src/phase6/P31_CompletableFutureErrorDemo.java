package phase6;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - What happens when a step in a CompletableFuture chain throws an exception
 *   - exceptionally(): if something failed, give a fallback result
 *   - handle(): always runs (success or failure). We get the result AND the exception.
 *   - join() throws CompletionException. The real error is inside getCause().
 *
 * REAL-LIFE EXAMPLE: PIZZA, THE OVEN BREAKS
 *   - Kitchen makes the pizza, the oven breaks.   = exception in supplyAsync()
 *   - "Then add cheese" is skipped.               = thenApply() is skipped
 *   - "If it fails, give a frozen pizza."         = exceptionally()
 *   - "Whatever happens, tell me the outcome."    = handle()
 *
 * PROBLEM:
 *   When a step fails, all the next steps are skipped.
 *   The error stays hidden inside the future until we call join().
 *   Then join() crashes our program.
 *
 * SOLUTION:
 *   Put exceptionally() or handle() in the chain.
 *   They catch the error and give a normal result, so the chain goes on.
 */
public class P31_CompletableFutureErrorDemo {

    // (sleep only pretends that the work is slow.)
    static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {

        // ---------------------------------------------------------
        // Section 1: BROKEN - no error handling
        // The oven breaks. "cheese added" is skipped. Nothing is printed
        // until we call join(). Then join() throws.
        // ---------------------------------------------------------
        CompletableFuture<String> order1 = CompletableFuture
                .supplyAsync(() -> {
                    pause(300);
                    throw new IllegalStateException("oven broken");
                })
                .thenApply(p -> {
                    System.out.println("Section 1 cheese added");   // never prints
                    return p + " + cheese";
                });

        System.out.println("Section 1 main is free. No error is visible yet.");
        try {
            order1.join();                                // the error comes out here
        } catch (CompletionException e) {
            System.out.println("Section 1 join() threw CompletionException");
            System.out.println("Section 1 real cause = " + e.getCause());
        }

        // ---------------------------------------------------------
        // Section 2: FIXED - exceptionally() gives a fallback result
        // thenApply is still skipped, but exceptionally catches the error.
        // ---------------------------------------------------------
        CompletableFuture<String> order2 = CompletableFuture
                .supplyAsync(() -> {
                    pause(300);
                    throw new IllegalStateException("oven broken");
                })
                .thenApply(p -> {
                    System.out.println("Section 2 cheese added");   // never prints
                    return p + " + cheese";
                })
                .exceptionally(ex -> {
                    System.out.println("Section 2 error caught: " + ex.getCause().getMessage());
                    return "frozen pizza";                // fallback result
                });

        System.out.println("Section 2 result = " + order2.join());

        // ---------------------------------------------------------
        // Section 3: handle() - runs on success AND on failure
        // We get two values: the result and the exception.
        // One of them is always null.
        // ---------------------------------------------------------
        CompletableFuture<String> good = CompletableFuture
                .supplyAsync(() -> {
                    pause(300);
                    return "pizza";
                })
                .handle((result, ex) -> ex != null ? "frozen pizza" : result);

        CompletableFuture<String> bad = CompletableFuture
                .supplyAsync(() -> {
                    pause(300);
                    throw new IllegalStateException("oven broken");
                })
                .handle((result, ex) -> ex != null ? "frozen pizza" : (String) result);

        System.out.println("Section 3 good order result = " + good.join());
        System.out.println("Section 3 bad order result  = " + bad.join());
    }
}