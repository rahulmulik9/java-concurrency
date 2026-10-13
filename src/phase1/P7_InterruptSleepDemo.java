package phase1;

public class P7_InterruptSleepDemo {
    public static void main(String[] args) throws InterruptedException {

        Thread worker = new Thread(() -> {
            try {
                System.out.println("worker: going to sleep for 10 seconds");
                Thread.sleep(10_000);
                System.out.println("worker: woke up normally");
            } catch (InterruptedException e) {
                // sleep() was cut short, and the interrupt flag was cleared
                System.out.println("worker: interrupted while sleeping");
                System.out.println("worker: flag after exception = "
                        + Thread.currentThread().isInterrupted());   // false

                /*why it looks like it should be true,
                but it really prints false. When sleep() throws InterruptedException,
                the JVM clears the interrupt flag just before throwing.*/
            }
            System.out.println("worker: finished");
        }, "worker");

        worker.start();

        System.out.println("Man : going to sleep for 2 seconds");
        Thread.sleep(2000);   // main sleeps 2 second, worker is sleeping too
        System.out.println("main is going to interrupt worker, worker current state = " + worker.getState());
        worker.interrupt();   // wakes the worker immediately

        worker.join();
        System.out.println("main: done");
    }
}