package phase1;
/*
 * CONCEPT
 * - start() launches a thread, and the caller keeps running without waiting.
 * - join() makes the caller wait until that thread has terminated.
 *
 * MAIN RULES
 * - Only the thread that calls join() waits (here: main)
 * - While worker sleeps its state is TIMED_WAITING
 * - join() returns only after the worker is TERMINATED
 *
 * KEY POINT
 * - Without join(), main does not wait for the worker at all.
 *
 * GOAL
 * - Start a worker that sleeps 5 seconds, print timestamps before and after join().
 * - Expected: "Main about to join" and "Main joined" are about 5 seconds apart
 * - Worker state printed right after start() is usually RUNNABLE
 *   (can be TIMED_WAITING if the worker already reached sleep)
 */
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