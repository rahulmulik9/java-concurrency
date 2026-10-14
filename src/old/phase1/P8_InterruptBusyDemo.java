package old.phase1;
/*
 * CONCEPT
 * - interrupt() does not stop a thread by force. It only sets a flag.
 *
 * MAIN RULES
 * - A busy thread must check isInterrupted() itself
 * - The worker decides when and how to stop
 * - State is RUNNABLE while the loop runs, TERMINATED only after run() ends
 *
 * KEY POINT
 * - A busy loop that never checks the flag can never be interrupted.
 *
 * GOAL
 * - Worker counts in a loop until main interrupts it after 1 second.
 * - Expected: "worker is interrupted : true", worker state RUNNABLE
 *   (it is still running those print lines), then "main: done"
 */
public class P8_InterruptBusyDemo {
    public static void main(String[] args) throws InterruptedException {

        Thread worker = new Thread(() -> {
            long count = 0;

            // The worker checks the flag on every pass and stops when it is set
            while (!Thread.currentThread().isInterrupted()) {
                count++;
            }

            System.out.println("worker is interrupt by main");
            System.out.println("worker is interrupted : " + Thread.currentThread().isInterrupted());
            System.out.println("Worker current state :"+ Thread.currentThread().getState()); //it always RUNNABLE, because It only becomes TERMINATED, when it stopped executing
        }, "worker");

        worker.start();

        System.out.println("Main thread is going to sleep for sec");
        Thread.sleep(1000);   // main sleeps 1 second, worker is busy counting
        System.out.println("main is interrupting worker, state = " + worker.getState());   // RUNNABLE
        worker.interrupt();   // only sets the flag, the worker decides what to do

        worker.join();
        System.out.println("main: done");
        //worker.join() => if enabled: main waits here until the worker finishes,
        //The worker is a non-daemon thread, so the JVM keeps running until it finishes
    }
}