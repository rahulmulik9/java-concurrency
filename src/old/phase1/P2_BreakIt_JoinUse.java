package old.phase1;

import java.util.ArrayList;
import java.util.List;
/*
 * CONCEPT
 * - Break it on purpose: see what goes wrong with start() twice and with no join().
 *
 * MAIN RULES
 * - A Thread object can be started only once (second start() throws IllegalThreadStateException)
 * - Without join(), main can finish before the workers
 * - With join(), main waits for every worker
 *
 * KEY POINT
 * - Worker output lines are interleaved and the order changes on every run,
 *   but "main done (after join)" is always last.
 *
 * GOAL
 * - Part 1: start() twice (commented line, uncomment to see the exception)
 * - Part 2: 3 workers without join (commented block)
 * - Part 3: 3 workers with join
 * - Expected: each worker prints 1 to 5 (mixed order), then "main done (after join)" last
 */
public class P2_BreakIt_JoinUse {
    public static void main(String[] args) throws InterruptedException {

        // Part 1: calling start() twice throws IllegalThreadStateException
        Thread t = new Thread(() -> System.out.println("hello"));
        t.start();
        // t.start();   // uncomment to see the exception, then comment it out again

        // Part 2: three threads without join()
//        for (int i = 1; i <= 3; i++) {
//            Thread w = new Thread(() -> {
//                for (int n = 1; n <= 5; n++) {
//                    System.out.println(Thread.currentThread().getName() + " -> " + n);
//                }
//            }, "worker-" + i);
//            w.start();
//        }
//        System.out.println("main done (without join)");

        // Part 3: with join(), "main done" always prints last.
        // Comment out the Part 2 loop and uncomment the block below.
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Thread w = new Thread(() -> {
                for (int n = 1; n <= 5; n++) {
                    System.out.println(Thread.currentThread().getName() + " -> " + n);
                }
            }, "Join worker-" + i);
            threads.add(w);
            w.start();
        }
        for (Thread w : threads) {
            w.join();
        }
        System.out.println("main done (after join)");
    }
}
