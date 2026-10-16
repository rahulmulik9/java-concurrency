package phase5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Callable: a task that returns a value and can throw an exception
 *   - Future: a ticket for a result that is not ready yet. get(), isDone(), cancel()
 *   - get() blocks until the result is ready. get(timeout) waits only a limited time
 *   - Exceptions: submit() hides them inside the Future. get() throws ExecutionException
 *   - invokeAll: run many tasks, wait for ALL results
 *   - invokeAny: run many tasks, return the FIRST successful result
 *
 * PROBLEM:
 *   Runnable returns nothing. To get a result out of a thread, we needed
 *   a shared variable (like long[] in step 21). That is ugly and not safe.
 *   Also, if a Runnable fails inside a pool, we may never see the error.
 *
 * SOLUTION:
 *   Callable returns a value. submit() gives us a Future.
 *   future.get() gives the value, or throws the task's exception.
 *   No shared variable is needed.
 */
public class P23_CallableFutureDemo {

    public static void main(String[] args) throws Exception {

        ExecutorService pool = Executors.newFixedThreadPool(3);

        try {
            // ---------------------------------------------------------
            // Section 1: BROKEN - Runnable cannot return a value
            // We need a shared array to get the result out.
            // ---------------------------------------------------------
            int[] box = {0};
            Future<?> f1 = pool.submit(() -> {
                box[0] = 10 + 20;   // result goes into a shared variable
            });
            f1.get();               // wait, so the value is visible (get() gives happens-before)
            System.out.println("Section 1 Runnable result (via array) = " + box[0]);

            // ---------------------------------------------------------
            // Section 2: FIXED - Callable returns the value
            // ---------------------------------------------------------
            Callable<Integer> sumTask = () -> 10 + 20;   // returns a value
            Future<Integer> f2 = pool.submit(sumTask);
            Integer result = f2.get();                   // blocks until the result is ready
            System.out.println("Section 2 Callable result = " + result);

            // ---------------------------------------------------------
            // Section 3: get() blocks. isDone() does not.
            // The task takes 500 ms.
            // ---------------------------------------------------------
            Future<Integer> f3 = pool.submit(() -> {
                Thread.sleep(500);          // pretend this is real work
                return 10+40;
            });
            System.out.println("Section 3 isDone right after submit = " + f3.isDone());
            long start = System.currentTimeMillis();
            int  slow = f3.get();         // main waits here
            System.out.println("Section 3 got '" + slow + "' after " + (System.currentTimeMillis() - start) + " ms, isDone = " + f3.isDone());

            // ---------------------------------------------------------
            // Section 4: get(timeout) - do not wait forever
            // ---------------------------------------------------------
            Future<Integer> f4 = pool.submit(() -> {
                Thread.sleep(2_000);
                return 10+40;
            });
            try {
                f4.get(200, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("Section 4 timeout after 200 ms. Cancel the task.");
                f4.cancel(true);            // true = interrupt the task if it is running
            }
            System.out.println("Section 4 isCancelled = " + f4.isCancelled());

            // ---------------------------------------------------------
            // Section 5: Exception in a task
            // execute() prints the error. submit() HIDES it until get() is called.
            // ---------------------------------------------------------
            Future<Integer> f5 = pool.submit(() -> {
                int x = 10 / 0;             // throws ArithmeticException
                return x;
            });
            System.out.println("Section 5 submit done. No error is visible yet.");
            try {
                f5.get();                   // the exception comes out here
            } catch (ExecutionException e) {
                System.out.println("Section 5 get() threw ExecutionException");
                System.out.println("Section 5 real cause = " + e.getCause());
            }

            // ---------------------------------------------------------
            // Section 6: invokeAll - run 3 tasks, wait for ALL of them
            // ---------------------------------------------------------
            List<Callable<String>> tasks = new ArrayList<>();
            tasks.add(() -> { Thread.sleep(300); return "A"; });
            tasks.add(() -> { Thread.sleep(100); return "B"; });
            tasks.add(() -> { Thread.sleep(200); return "C"; });

            start = System.currentTimeMillis();
            List<Future<String>> all = pool.invokeAll(tasks);   // blocks until ALL are done
            System.out.println("Section 6 invokeAll finished after "
                    + (System.currentTimeMillis() - start) + " ms");
            for (Future<String> f : all) {
                System.out.println("Section 6 result = " + f.get());   // same order as the list
            }

            // ---------------------------------------------------------
            // Section 7: invokeAny - return the FIRST successful result
            // The other tasks are cancelled.
            // ---------------------------------------------------------
            List<Callable<String>> race = new ArrayList<>();
            race.add(() -> { Thread.sleep(500); return "slow server"; });
            race.add(() -> { Thread.sleep(100); return "fast server"; });
            race.add(() -> { Thread.sleep(300); return "medium server"; });

            start = System.currentTimeMillis();
            String winner = pool.invokeAny(race);               // blocks until ONE succeeds
            System.out.println("Section 7 invokeAny winner = " + winner + " after "
                    + (System.currentTimeMillis() - start) + " ms");

        } finally {
            pool.shutdown();   // always close the pool
        }
    }
}