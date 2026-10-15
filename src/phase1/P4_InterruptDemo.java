package phase1;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - interrupt(): politely ASK a thread to stop (it does not force it)
 *   - How a thread in sleep()/wait()/join() reacts: InterruptedException
 *   - How a busy thread checks the flag: isInterrupted()
 *   - Why you must never swallow InterruptedException
 *
 * PROBLEM:
 *   You can't safely kill a thread (Thread.stop() is deprecated and unsafe,
 *   because it can leave shared data half-updated). The usual workaround is
 *   a boolean flag, but a flag can't wake a thread that is stuck in sleep().
 *   Worse, code that catches InterruptedException and ignores it makes the
 *   thread impossible to stop.
 *
 * SOLUTION:
 *   Use interrupt(). The target thread decides how to react:
 *     - blocked in sleep/wait/join -> catch InterruptedException, clean up, exit
 *     - busy in a loop             -> check Thread.currentThread().isInterrupted()
 *   Never swallow the exception silently. Exit, or restore the flag with
 *   Thread.currentThread().interrupt().
 */
public class P4_InterruptDemo {

    public static void main(String[] args) throws InterruptedException {

        // 1. Broken version: swallowing InterruptedException
        System.out.println("1. BROKEN: interrupt is swallowed");
        Thread broken = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // BAD: the interrupt request is lost, the thread carries on
                    System.out.println("  [broken] interrupted, but ignoring it");
                }
            }
            System.out.println("  [broken] finished ALL steps despite the interrupt");
        }, "broken");
        broken.start();
        Thread.sleep(200);
        broken.interrupt();      // asks the thread to stop, but it ignores us
        broken.join();


        // 2. Fixed version A: thread blocked in sleep() reacts to the interrupt
        System.out.println("\n2. FIXED A: blocked thread (sleep) stops on interrupt");
        Thread sleeper = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("  [sleeper] interrupted, cleaning up and stopping");
                    // When a thread is interrupted while in sleep()/wait()/join(), Java throws
                    // InterruptedException AND resets the interrupt flag to false.
                    // So the "I was interrupted" information is lost once we catch the exception.
                    // Calling interrupt() again sets the flag back to true, so any code that
                    // runs after this (callers, thread pools) can still see the interrupt.
                    Thread.currentThread().interrupt();
                    return; // exit run() so the thread ends
                }
            }
        }, "sleeper");
        sleeper.start();
        Thread.sleep(1200);
        sleeper.interrupt();     // wakes it from sleep() immediately
        sleeper.join();


        // 3. Fixed version B: busy thread polls the interrupt flag
        System.out.println("\n3. FIXED B: busy thread checks isInterrupted()");
        Thread busy = new Thread(() -> {
            long count = 0;
            while (!Thread.currentThread().isInterrupted()) {
                count++;         // no sleep/wait here, so no exception will be thrown
            }
            System.out.println("  [busy] saw the interrupt flag, stopping");
        }, "busy");
        busy.start();
        Thread.sleep(200);
        busy.interrupt();        // only sets the flag, the loop must check it
        busy.join();

    }
}