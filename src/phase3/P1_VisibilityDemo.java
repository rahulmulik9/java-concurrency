package phase3;
/*
 * CONCEPT
 * - Visibility problem: a thread may not see a change made by another thread.
 *   It may use a cached copy, or the JIT may read the field only once.
 *
 * MAIN RULES
 * - Plain field: no promise that other threads see the update
 * - volatile field: every read sees the latest write
 * - Empty busy loop is the most likely place to see the problem
 *
 * KEY POINT
 * - volatile fixes visibility but does not make count++ atomic.
 *
 * GOAL
 * - Worker loops while running is true, main sets running = false after 1 second.
 * - Expected with volatile: "worker: stopped", "worker alive = false -> change seen"
 * - Broken version: remove volatile (the commented line), worker may never stop,
 *   "worker alive = true -> change NOT seen"
 * - Worker is a daemon, so the JVM can still exit if it is stuck
 */
// a shared field may be cached by a thread (or the JIT reads it only once)
// so a change made by another thread may not be seen
// volatile makes the change visible, but it does not make count++ atomic

public class P1_VisibilityDemo {

    // no volatile: worker may keep using its own copy
   //static boolean running = true;
    static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            System.out.println("worker: started");
            while (running) {
                // empty loop: JIT can read 'running' only once
            }
            System.out.println("worker: stopped");
        });

        // daemon, so the JVM can exit even if worker is stuck
        worker.setDaemon(true);
        worker.start();

        Thread.sleep(1000);
        System.out.println("main: setting running = false");
        running = false;

        // wait max 2 seconds, then check
        worker.join(2000);
        System.out.println("RESULT: worker alive = " + worker.isAlive()
                + " -> " + (worker.isAlive() ? "change NOT seen" : "change seen"));
    }
}