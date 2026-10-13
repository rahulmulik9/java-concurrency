package phase2;

/*
Each thread may keep a cached copy of a variable (in a CPU cache or register).
Without volatile, one thread can change a value and another thread may never see it. This is a visibility problem.
volatile forces every read to get the latest value and every write to be seen by other threads.
volatile does not make counter++ atomic. It fixes visibility only.*/

public class P4_VolatileDemo {

    // volatile makes the change visible to the worker thread
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