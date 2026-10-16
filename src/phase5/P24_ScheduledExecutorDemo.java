package phase5;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - ScheduledExecutorService and Executors.newScheduledThreadPool
 *   - schedule: run a task ONCE after a delay
 *   - scheduleAtFixedRate: repeat the task on a timetable (see below)
 *   - scheduleWithFixedDelay: repeat the task with a rest after each run (see below)
 *   - Stopping a repeating task: cancel() and shutdown()
 *   - A repeating task that throws an exception stops silently
 *
 * scheduleAtFixedRate(task, 0, 300, ms):
 *   - Starts the task on a timetable: 0, 300, 600, 900 ms ...
 *   - Time is measured from START to START.
 *   - If run 1 finishes at 100 ms, run 2 does NOT start early. It waits and starts exactly at 300 ms.
 *   - If run 1 is slow and finishes at 500 ms (after the planned 300 ms),
 *     run 2 starts at 500 ms, right after run 1 ends. It is late.
 *     Two runs never overlap, so run 2 must wait for run 1.
 *
 * scheduleWithFixedDelay(task, 0, 300, ms):
 *   - Gives a rest (delay) between two runs.
 *   - Time is measured from the END of one run to the START of the next.
 *   - If run 1 ends at 100 ms, run 2 starts at 400 ms (100 + 300).
 *
 * PROBLEM:
 *   With a normal pool or a plain Thread, "run this after 1 second" or
 *   "run this every second" needs a loop with Thread.sleep() inside.
 *   That wastes a thread, drifts in time, and is hard to stop.
 *
 * SOLUTION:
 *   ScheduledExecutorService keeps the timing for us.
 *   We say what to run and when. The pool does the waiting.
 */
public class P24_ScheduledExecutorDemo {

    public static void main(String[] args) throws InterruptedException {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        long begin = System.currentTimeMillis();

        try {
            // ---------------------------------------------------------
            // Section 1: BROKEN - delay with sleep inside a thread
            // The thread is blocked and does nothing while it sleeps.
            // There is no easy way to cancel it.
            // ---------------------------------------------------------
            Thread sleeper = new Thread(() -> {
                try {
                    Thread.sleep(500);
                    System.out.println("Section 1 sleep-thread ran after 500 ms");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            sleeper.start();
            sleeper.join();

            // ---------------------------------------------------------
            // Section 2: schedule() - run ONCE after a delay
            // No thread is blocked while we wait. The scheduler holds the task.
            // ---------------------------------------------------------
            long t2 = System.currentTimeMillis();
            scheduler.schedule(() -> {
                System.out.println("Section 2 delayed task ran after "
                        + (System.currentTimeMillis() - t2) + " ms");
            }, 500, TimeUnit.MILLISECONDS);

            Thread.sleep(700); // only so Section 2 output does not mix with Section 3

            // ---------------------------------------------------------
            // Section 3: scheduleAtFixedRate repeats the task after the given interval
            // scheduleAtFixedRate - every 300 ms, measured start to start
            // The task takes 100 ms. Runs start at 0, 300, 600, ...
            // ---------------------------------------------------------
            long t3 = System.currentTimeMillis();
            ScheduledFuture<?> rate = scheduler.scheduleAtFixedRate(() -> {
                System.out.println("Section 3 fixed rate   start at "
                        + (System.currentTimeMillis() - t3) + " ms");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, 0, 300, TimeUnit.MILLISECONDS);   // initial delay 0, period 300
            Thread.sleep(1_000);
            rate.cancel(false);                  // stop repeating. false = let a running run finish

            // ---------------------------------------------------------
            // Section 4: scheduleWithFixedDelay - wait 300 ms AFTER each run ends
            // The task takes 100 ms. Runs start at 0, 400, 800, ...
            // ---------------------------------------------------------
            long t4 = System.currentTimeMillis();
            ScheduledFuture<?> delay = scheduler.scheduleWithFixedDelay(() -> {
                System.out.println("Section 4 fixed delay  start at "
                        + (System.currentTimeMillis() - t4) + " ms");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, 0, 300, TimeUnit.MILLISECONDS);
            Thread.sleep(1_000);
            delay.cancel(false);

            // ---------------------------------------------------------
            // Section 5: BROKEN - exception in a repeating task
            // After the exception, the task is NOT run again. And no error is printed.
            // ---------------------------------------------------------
            int[] runs = {0};
            scheduler.scheduleAtFixedRate(() -> {
                runs[0]++;
              //  System.out.println("Section 5 run " + runs[0]);
                if (runs[0] == 2) {
                    throw new RuntimeException("boom");   // hidden inside the ScheduledFuture
                }
            }, 0, 200, TimeUnit.MILLISECONDS);
            Thread.sleep(1_000);
            System.out.println("Section 5 total runs = " + runs[0] + " (expected more than 2)");

            // ---------------------------------------------------------
            // Section 6: FIXED - catch the exception INSIDE the task
            // ---------------------------------------------------------
            int[] runs6 = {0};
            ScheduledFuture<?> safe = scheduler.scheduleAtFixedRate(() -> {
                try {
                    runs6[0]++;
                  //  System.out.println("Section 6 run " + runs6[0]);
                    if (runs6[0] == 2) {
                        throw new RuntimeException("boom");
                    }
                } catch (Exception e) {
                    System.out.println("Section 6 caught: " + e.getMessage() + ". Task continues.");
                }
            }, 0, 200, TimeUnit.MILLISECONDS);
            Thread.sleep(1_000);
            safe.cancel(false);
            System.out.println("Section 6 total runs = " + runs6[0]);

        } finally {
            scheduler.shutdown();   // always close the scheduler
        }
        System.out.println("Done after " + (System.currentTimeMillis() - begin) + " ms");
    }
}