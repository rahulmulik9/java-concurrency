package phase1;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Three ways to create a thread: extend Thread, implement Runnable, lambda
 *   - The difference between start() and run()
 *   - How to name a thread and see which thread is executing your code
 *
 * PROBLEM:
 *   A beginner calls thread.run() thinking it starts a new thread.
 *   It does not. run() is just a normal method call, so the code
 *   executes on the CALLING thread (main). No concurrency happens.
 *
 * SOLUTION:
 *   Always call thread.start(). The JVM then creates a new OS-level thread
 *   and that new thread calls run() for you.
 */
public class P1_ThreadCreationDemo {

    // Way 1: extend Thread (simple, but uses up your single inheritance slot)
    static class MyThread extends Thread {
        MyThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            printWho("extends Thread");
        }
    }

    // Way 2: implement Runnable (preferred: task is separate from the thread)
    static class MyTask implements Runnable {
        @Override
        public void run() {
            printWho("implements Runnable");
        }
    }

    static void printWho(String how) {
        System.out.println("  [" + how + "] running on thread: " + Thread.currentThread().getName());
    }

    public static void main(String[] args) throws InterruptedException {

        // 1. Fixed version: start() creates a real new thread
        Thread t1 = new MyThread("worker-1");                         // Way 1
        Thread t2 = new Thread(new MyTask(), "worker-2");             // Way 2
        Thread t3 = new Thread(() -> printWho("lambda"), "worker-3"); // Way 3

        t1.start();
        t2.start();
        t3.start();

        // Wait for all three so the output finishes before main ends
        t1.join();
        t2.join();
        t3.join();

        // 2. Broken version: run() does NOT create a thread
        Thread t4 = new Thread(() -> printWho("run() called directly"), "worker-4");
        t4.run(); // runs on "main", NOT on "worker-4"
    }
}