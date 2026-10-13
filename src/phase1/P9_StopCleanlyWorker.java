package phase1;
/*
 * CONCEPT
 * - The correct way to stop a worker: handle BOTH cases of interrupt.
 *   Busy case: flag is set. Sleeping case: InterruptedException is thrown.
 *
 * MAIN RULES
 * - Check isInterrupted() in the loop condition
 * - Catch InterruptedException, then restore the flag with interrupt()
 * - Put cleanup in finally so it runs on every exit path
 *
 * KEY POINT
 * - Swallowing InterruptedException (empty catch) hides the stop request
 *   from the rest of the program.
 *
 * GOAL
 * - Worker processes items (50 ms work + 300 ms sleep), main interrupts after 1 second.
 * - Expected: items 0, 1, 2 processed, then
 *   "interrupted while sleeping, flag restored", "cleanup done (flag = true)",
 *   main prints worker state = TERMINATED
 */
public class P9_StopCleanlyWorker {

    static class Worker implements Runnable {
        @Override
        public void run() {
            int item = 0;
            try {
                // Busy case: interrupt() only sets the flag, so we check it on every iteration
                while (!Thread.currentThread().isInterrupted()) {
                    processItem(item++);

                    // Sleeping case: interrupt() wakes the thread here with InterruptedException
                    Thread.sleep(300);
                }
                System.out.println("worker: interrupt flag seen in loop condition, stopping");
            } catch (InterruptedException e) {
                // The exception cleared the interrupt flag (flag = false).
                // Restore it manually so code further up the call stack can still see the request.
                Thread.currentThread().interrupt();
                System.out.println("worker: interrupted while sleeping, flag restored, stopping");
            } finally {
                // Runs on every exit path: loop condition, exception, or normal end
                System.out.println("worker: cleanup done (flag = "
                        + Thread.currentThread().isInterrupted() + ")");
            }
        }

        // Simulates about 50 ms of CPU work
        private void processItem(int item) {
            long end = System.currentTimeMillis() + 50;
            while (System.currentTimeMillis() < end) {
                // busy work
            }
            System.out.println("worker: processed item " + item);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(new Worker(), "worker");
        worker.start();

        Thread.sleep(1000);   // main sleeps 1 second while the worker processes a few items
        System.out.println("main: requesting stop");
        worker.interrupt();   // sets the flag, or wakes the worker if it is sleeping

        worker.join();        // main waits until the worker finishes its cleanup
        System.out.println("main: worker state = " + worker.getState());   // TERMINATED
        System.out.println("main: done");
    }
}