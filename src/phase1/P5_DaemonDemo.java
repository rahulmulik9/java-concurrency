package phase1;
/*
 * CONCEPT
 * - A daemon thread is a background helper. The JVM does not wait for it.
 *
 * MAIN RULES
 * - JVM exits when all NON-daemon threads are finished
 * - setDaemon(true) must be called BEFORE start()
 * - Daemon threads are killed abruptly when the JVM exits
 *
 * KEY POINT
 * - A non-daemon thread with an endless loop keeps the JVM alive forever,
 *   even after main ends.
 *
 * GOAL
 * - Ticker prints every 300 ms while main sleeps 3 seconds.
 * - Expected with setDaemon(true): about 10 ticks, "main exiting", program ends
 * - Expected with setDaemon(false): ticks continue forever after main exits
 */
public class P5_DaemonDemo {
    public static void main(String[] args) throws InterruptedException {

        // ticker: an endless loop that prints every 300 ms
        Thread t = new Thread(() -> {
            int i = 0;
            while (true) {
                System.out.println("tick " + i++);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "ticker");

        // Try true first, then false
        // true  -> JVM exits when main ends, ticker is killed
        // false -> ticker keeps the JVM alive forever
        t.setDaemon(true);
        t.start();

        // Step 1: main sleeps for 3000 ms.
        // The ticker (daemon) keeps printing in the meantime.
        //so 3000/300 => about 10 . so demon thread tick about 10 times
        Thread.sleep(3000);

        // Step 2: main wakes up and prints "main exiting".
        //         Waking up is nothing special here. It just lets main continue to its next line.
        System.out.println("main exiting");

        // Step 3: main has no more code, so it ends.
        // Step 4: now the only threads left are daemon threads, so the JVM shuts down
        // and the ticker is killed on the spot.
    }
}