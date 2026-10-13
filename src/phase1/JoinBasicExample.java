package phase1;

import java.time.LocalDateTime;

public class JoinBasicExample {
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                // worker thread sleeps for 5000 ms (TIMED_WAITING)
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupt flag
            }
        }, "worker");

        // t.start() launches the worker. It begins running in parallel.
        // Main does not wait for it, unless we explicitly call t.join().
        t.start();
        System.out.println("Worker started, main continues");
        System.out.println("Main about to join : " + LocalDateTime.now());
        System.out.println("Worked thread status :" + t.getState());
        // Only here does main block (WAITING) until the worker terminates
        t.join();
        System.out.println("Main joined : " + LocalDateTime.now());
    }
}