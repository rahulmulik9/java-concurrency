package old.phase1;
/*
 * CONCEPT
 * - Thread priority (1 to 10) is a hint to the scheduler, not an order.
 *
 * MAIN RULES
 * - MIN_PRIORITY = 1, NORM_PRIORITY = 5, MAX_PRIORITY = 10
 * - The OS scheduler decides, and may ignore priorities
 * - Both threads stay RUNNABLE while counting
 *
 * KEY POINT
 * - Never depend on priority for correctness. Results differ by OS, JVM and CPU cores.
 *
 * GOAL
 * - Two busy counters (LOW and HIGH) run for 5 seconds, then compare counts.
 * - Expected: HIGH count is often larger, but this is NOT guaranteed
 *   (with enough free cores the counts can be almost equal)
 */
public class P6_PriorityDemo {

    static class CountTask implements Runnable {

        private volatile boolean running = true;
        long count = 0;

        @Override
        public void run() {
            while (running) {
                count++;
            }
        }

        public void stop() {
            running = false;
        }
    }

    public static void main(String[] args)
            throws InterruptedException {

        CountTask lowTask = new CountTask();
        CountTask highTask = new CountTask();

        Thread low = new Thread(lowTask, "LOW");
        Thread high = new Thread(highTask, "HIGH");

        // Assign priorities
        low.setPriority(Thread.MIN_PRIORITY);   // Priority 1
        high.setPriority(Thread.MAX_PRIORITY);  // Priority 10

        System.out.println("LOW priority: " + low.getPriority());
        System.out.println("HIGH priority: " + high.getPriority());

        // Start both threads
        low.start();
        high.start();

        //check thread before main thread sleep
        System.out.println("LOW priority state before main thread sleep: " + low.getState());
        System.out.println("HIGH priority state before main thread sleep: " + high.getState());

        // Sleep main thread for 5 second
        //in background both thread are runnin only main thread is sleep for 5 sec
        // Let both threads count for 5 seconds
        Thread.sleep(5000);

        //check thread after main thread wake up
        System.out.println("LOW priority state after main thread wakeup: " + low.getState());
        System.out.println("HIGH priority state after main thread wakeup: " + high.getState());

        // Ask both threads to stop
        lowTask.stop();
        highTask.stop();

        // Main thread waits for both thread  to finish
        low.join();
        high.join();

        System.out.println("LOW count:  " + lowTask.count);
        System.out.println("HIGH count: " + highTask.count);
    }
}