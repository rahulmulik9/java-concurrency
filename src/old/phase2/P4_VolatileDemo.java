package old.phase2;
/*
 * CONCEPT
 * - A thread may keep a cached copy of a variable (CPU cache or register).
 *   Without volatile, one thread's change may never be seen by another.
 *
 * MAIN RULES
 * - volatile: every read sees the latest write
 * - volatile fixes VISIBILITY only
 * - volatile does NOT make counter++ atomic
 *
 * KEY POINT
 * - Good for a simple stop flag (one writer, simple true/false).
 *   Not good for read-modify-write operations.
 *
 * GOAL
 * - Worker spins on "running", main sets it to false after 1 second.
 * - Expected with volatile: "worker saw running false, stopping", program ends
 * - Broken version: remove volatile (the commented line), worker may spin forever
 */
/*
Each thread may keep a cached copy of a variable (in a CPU cache or register).
Without volatile, one thread can change a value and another thread may never see it. This is a visibility problem.
volatile forces every read to get the latest value and every write to be seen by other threads.
volatile does not make counter++ atomic. It fixes visibility only.*/

public class P4_VolatileDemo {

    // volatile makes the change visible to the worker thread
    //static boolean running = true;
    static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {

        Thread worker = new Thread(() -> {
            System.out.println("worker started");
            // empty busy loop, nothing else forces the thread to refresh running
            while (running) {
            }
            System.out.println("worker saw running false, stopping");
        }, "worker");

        worker.start();

        // let the worker spin for a while
        Thread.sleep(1000);

        System.out.println("main setting running to false");
        running = false;

        worker.join();
        System.out.println("main thread done");
    }
}