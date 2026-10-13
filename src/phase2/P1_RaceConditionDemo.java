package phase2;
/*
 * CONCEPT
 * - Race condition: the result depends on how threads happen to interleave.
 *
 * MAIN RULES
 * - counter++ is 3 steps: read, add 1, write
 * - Two threads can read the same value and both write back the same result
 * - One of the two updates is lost
 *
 * KEY POINT
 * - counter++ looks like one operation but is NOT atomic.
 *
 * GOAL
 * - Two threads each add 1 to a shared counter 100,000 times, with no protection.
 * - Expected: should be 200000, but RESULT is usually less (LOST > 0)
 * - Result changes on every run
 * - Fixed version: P2_SynchronizedFixDemo
 */
public class P1_RaceConditionDemo {

    // shared by both threads, no protection
    static int counter = 0;

    public static void main(String[] args) throws InterruptedException {

        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                // read + add + write: 3 steps, threads can interleave
                counter++;
            }
        };

        Thread t1 = new Thread(task, "worker-1");
        Thread t2 = new Thread(task, "worker-2");

        System.out.println("main: starting workers");
        t1.start();
        t2.start();

        // wait so we read the counter only after both finish
        t1.join();
        t2.join();

        System.out.println("EXPECTED: 200000");
        System.out.println("RESULT: " + counter);
        System.out.println("LOST: " + (200_000 - counter) + " -> updates overwritten");
    }
}