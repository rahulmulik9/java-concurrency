package phase1;

import java.util.ArrayList;
import java.util.List;

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
