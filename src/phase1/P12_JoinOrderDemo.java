package phase1;
/*
 * CONCEPT
 * - Where you put start() and join() decides if work runs in parallel or one by one.
 *
 * MAIN RULES
 * - start all threads first, then join all -> parallel
 * - start + join inside the same loop -> sequential
 * - join() always makes only the CALLER wait (here: main), never another worker
 *
 * KEY POINT
 * - join() inside the start loop silently turns a parallel program into a serial one.
 *
 * GOAL
 * - 4 workers each sleep 1 second, run in two ways and time them.
 * - Expected: Example 1 about 1000 ms (parallel), Example 2 about 4000 ms (one after another)
 */
import java.util.ArrayList;
import java.util.List;

public class P12_JoinOrderDemo {

    // Creates 4 workers, each one sleeps 1 second to pretend it is working
    static List<Thread> createWorkers() {
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            threads.add(new Thread(() -> {
                String me = Thread.currentThread().getName();
                System.out.println(me + ": started");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                System.out.println(me + ": finished");
            }, "worker-" + i));
        }
        return threads;
    }

    // ===== Example 1: start and join in DIFFERENT loops =====
    static void differentLoops() throws InterruptedException {
        System.out.println("EXAMPLE 1: start all threads first, then join all");
        List<Thread> threads = createWorkers();
        long begin = System.currentTimeMillis();

        // Loop 1: all 4 threads start now and run at the same time
        for (Thread t : threads) {
            t.start();
        }

        // Loop 2: main waits for each thread, one by one
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("RESULT: " + (System.currentTimeMillis() - begin) + " ms -> parallel");
        System.out.println();
    }

    // ===== Example 2: start and join in the SAME loop =====
    static void sameLoop() throws InterruptedException {
        System.out.println("EXAMPLE 2: start and join each thread together");
        List<Thread> threads = createWorkers();
        long begin = System.currentTimeMillis();

        for (Thread t : threads) {
            t.start();
            t.join(); //tell main thread wait till first thread complete exclusion
        }

        System.out.println("RESULT: " + (System.currentTimeMillis() - begin) + " ms -> one after another");
    }

    public static void main(String[] args) throws InterruptedException {
        differentLoops();
        sameLoop();
    }

    /*
     * NOTE: who is really waiting during join()?
     *
     * Each time main calls t.join(), ONLY main waits. No other thread waits.
     *
     * In the second loop of Example 1 it looks like "thread 2 is waiting for thread 1", but that is wrong.
     * Threads 2, 3 and 4 are already running on their own.It is the main thread that is waiting.
     *
     * What really happens:
     *   1. main calls worker-1.join() and waits until worker-1 is done.
     *   2. Once worker-1 is done, the join returns and main goes to the next item in the loop.
     *   3. main calls worker-2.join(). If worker-2 is already finished, it returns immediately.
     *   4. Same for worker-3 and worker-4.
     */
}