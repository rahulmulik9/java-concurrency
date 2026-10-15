package phase1;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - User threads vs daemon threads
 *   - setDaemon(true) must be called BEFORE start()
 *   - The JVM exits when the LAST USER thread ends, and kills daemon threads
 *
 * PROBLEM:
 *   A background helper (heartbeat, logger) runs an endless loop. If it is a
 *   normal user thread, the JVM waits for it forever and never exits.
 *   Also, setDaemon(true) after start() does not work.
 *
 * SOLUTION:
 *   Mark background helpers as daemon threads (setDaemon(true) BEFORE start()).
 *   The JVM does not wait for them. Once the last user thread ends, the JVM
 *   exits and the daemon threads are killed.
 */

/*
 * THREE CONDITIONS TO REMEMBER:
 *
 * 1) setDaemon(true) after start()
 *    - setDaemon() must be called BEFORE start().
 *    - Calling it on a thread that is already started throws
 *      IllegalThreadStateException.
 *
 * 2) Daemon thread dies when the last USER thread ends
 *    - The JVM exits when no user threads are left, and then kills all daemon threads, even if their while(true) loop is not finished.
 *    - main is only one user thread. If main is the ONLY user thread, the daemon  dies as soon as main ends.
 *    - If another user thread is still running, the daemon keeps running even after main ends. It dies only when that last user thread finishes.
 *
 * 3) User (non-daemon) thread outlives main
 *    - The JVM waits for every user thread to finish.
 *    - So even after main ends, a non-daemon thread keeps running until it
 *      completes its work, and only then does the JVM exit.
 */
public class P5_DaemonThreadDemo {

    // true  -> a user worker outlives main (shows conditions 2 and 3 together)
    // false -> main is the only user thread (shows the daemon dying with main)
    static final boolean START_USER_WORKER = true;

    static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // CONDITION 1: setDaemon() after start() fails
        System.out.println("1. setDaemon(true) after start()");
        Thread late = new Thread(() -> pause(300), "late");
        late.start();
        try {
            late.setDaemon(true);                 // too late, thread is already alive
        } catch (IllegalThreadStateException e) {
            System.out.println("   IllegalThreadStateException: call setDaemon BEFORE start()");
        }
        late.join();


        // CONDITIONS 2 and 3: daemon vs user thread when main ends
        System.out.println("\n2/3. Daemon heartbeat vs user worker");

        Thread heartbeat = new Thread(() -> {
            int tick = 1;
            while (true) {                        // endless loop, never finishes by itself
                System.out.println("   heartbeat tick " + tick++);
                pause(300);
            }
        }, "heartbeat");
        heartbeat.setDaemon(true);                // BEFORE start()
        heartbeat.start();


        Thread worker = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println("   worker step " + i);
                pause(500);
            }
            System.out.println("   worker finished -> last user thread ends, JVM exits");
        }, "worker");                         // user thread (default)
        worker.start();


        System.out.println(" Main finished but worker thread is running in background \n As soon as worker finish, demon thread also dies.\n Reason : It dies only when that last user thread finishes\n\n");

    }
}