package phase1;
/*
 * CONCEPT
 * - start() creates a new thread and runs the task on it.
 * - run() is only a normal method call on the current thread.
 *
 * MAIN RULES
 * - start() -> new thread, real concurrency
 * - run()   -> same thread (main), no concurrency
 * - Same idea works with a Runnable class, anonymous class and lambda
 *
 * KEY POINT
 * - Calling run() instead of start() compiles and looks fine,
 *   but everything runs on main.
 *
 * GOAL
 * - For each of 3 styles (class, anonymous, lambda), call run() then start()
 *   and print Thread.currentThread().getName().
 * - Expected: run() prints "main", start() prints "worker-1", "worker-2", "worker-3"
 *   (order of the worker lines vs main lines can vary)
 */

class PrintTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Running on: " + Thread.currentThread().getName());
    }
}

public class P1_StartVsRunDemo {
    public static void main(String[] args) {
        Thread t1 = new Thread(new PrintTask(), "worker-1");
        t1.run();
        t1.start();

        //with lambda function
        //first example
        Runnable printTask2 = new Runnable() {
            @Override
            public void run() {
                System.out.println("Running on: " + Thread.currentThread().getName());
            }
        };

        Thread t2 = new Thread(printTask2, "worker-2");
        t2.run();
        t2.start();

        //second example
        Runnable printTask3 = () -> System.out.println("Running on: " + Thread.currentThread().getName());
        Thread t3 = new Thread(printTask3, "worker-3");
        t3.run();
        t3.start();
    }
}