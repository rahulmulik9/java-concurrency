package phase1;

/*
 * SUMMARY: Uncaught exceptions in threads
 *
 * 1. An exception thrown inside a thread can only be caught inside that same thread.
 * 2. If nobody catches it there, it is "uncaught" and it kills ONLY that thread. main and the other threads keep running.
 * 3. try/catch around start() does NOT catch it, because the exception happens later, on a different thread.
 * 4. An UncaughtExceptionHandler is a callback that runs on the dying thread just before it ends.
 *     Use it to log or report the failure.
 * 5. A handler does NOT rescue the thread. The thread still ends as TERMINATED.
 * 6. If a thread has both handlers, only its own handler runs.
 * 7. Best handling: try/catch inside run(), so the thread can recover and continue. The handler is only a safety net for unexpected exceptions.
 *
 * Order of defence:
 *    try/catch inside run()  ->  per-thread handler  ->  default handler  ->  JVM prints stack trace
 */
public class P11_UncaughtExceptionDemo {

    public static void main(String[] args) throws InterruptedException {

        // ===== Part 1: no handler, the thread just dies =====
        System.out.println("PART 1 GOING TO: start a thread that throws, with no handler");
        Thread crasher = new Thread(() -> {
            System.out.println("crasher-1 GOING TO: throw a RuntimeException");
            throw new RuntimeException("boom from crasher-1");
        }, "crasher-1");

        crasher.start();
        crasher.join();
        System.out.println("PART 1 RESULT: crasher-1 state = " + crasher.getState() + " -> thread died, but main is still running");
        System.out.println();


        // ===== Part 2: try/catch around start() does NOT catch it =====
        System.out.println("PART 2 GOING TO: wrap start() in try/catch and see if it catches the exception");
        Thread crasher2 = new Thread(() -> {
            throw new RuntimeException("boom from crasher-2");
        }, "crasher-2");

        boolean caughtByMain = false;
        try {
            crasher2.start();
            crasher2.join();
        } catch (RuntimeException e) {
            caughtByMain = true;
        }
        System.out.println("PART 2 RESULT: main caught the exception = " + caughtByMain + " because the exception happened on crasher-2, not on main");
        System.out.println();


        // ===== Part 3: handler for ONE thread =====
        System.out.println("PART 3 GOING TO: set a handler on one thread, then let it throw");
        Thread crasher3 = new Thread(() -> {
            throw new RuntimeException("boom from crasher-3");
        }, "crasher-3");

        crasher3.setUncaughtExceptionHandler((thread, ex) ->
                System.out.println("PART 3 RESULT: handler ran on thread '" + Thread.currentThread().getName()
                        + "', failed thread = '" + thread.getName()
                        + "', error = " + ex.getMessage()));

        crasher3.start();
        crasher3.join();
        System.out.println();


        // ===== Part 4: DEFAULT handler for all threads without their own =====
        System.out.println("PART 4 GOING TO: set a default handler, then start a thread with no handler of its own");
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) ->
                System.out.println("PART 4 RESULT: default handler caught '" + ex.getMessage()
                        + "' from thread '" + thread.getName() + "'"));

        Thread crasher4 = new Thread(() -> {
            throw new RuntimeException("boom from crasher-4");
        }, "crasher-4");

        crasher4.start();
        crasher4.join();
        System.out.println();

        System.out.println("CONCLUSION: an uncaught exception kills only its own thread,");
        System.out.println("try/catch around start() cannot catch it, a handler can");
    }
}