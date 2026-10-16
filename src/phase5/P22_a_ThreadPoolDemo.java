package phase5;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Thread pool: a few worker threads that run many tasks
 *   - ExecutorService, Executors.newFixedThreadPool, submit
 *   - shutdown(): stop accepting NEW tasks. Old tasks still run. It does NOT wait.
 *   - awaitTermination(): make the calling thread WAIT until all tasks are finished
 *   - What happens if we forget shutdown() or awaitTermination()
 *
 * PROBLEM:
 *   Worker threads of a pool stay alive and wait for new tasks.
 *   - If we never call shutdown(), the program never exits.
 *   - If we call shutdown() but do not wait, main continues
 *     while the tasks are still running.
 *
 * SOLUTION:
 *   Call shutdown() when there are no more tasks.
 *   Then call awaitTermination() to wait until all tasks are finished.
 */
public class P22_a_ThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {

        // ---------------------------------------------------------
        // Section 1: Thread pool basics
        // 2 workers, 4 tasks. Only 2 thread names appear = threads are reused.
        // ---------------------------------------------------------
        ExecutorService pool1 = Executors.newFixedThreadPool(2);
        for (int i = 1; i <= 4; i++) {
            final int id = i;
            pool1.submit(() -> {
                System.out.println("Section 1 task " + id + " runs on " + Thread.currentThread().getName());
            });
        }
        pool1.shutdown();                            // no new tasks
        pool1.awaitTermination(1, TimeUnit.MINUTES); // wait until all tasks are done

        // ---------------------------------------------------------
        // Section 2: shutdown() WITHOUT awaitTermination()
        // shutdown() returns at once. main does not wait.
        // ---------------------------------------------------------
        ExecutorService pool2 = Executors.newFixedThreadPool(2);
        for (int i = 1; i <= 2; i++) {
            final int id = i;
            pool2.submit(() -> {
                try {
                    Thread.sleep(300); // pretend this is real work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Section 2 task " + id + " finished");
            });
        }
        pool2.shutdown();
        System.out.println("Section 2 main continues. isTerminated = " + pool2.isTerminated());
        // The tasks are still running here. isTerminated is false.
        pool2.awaitTermination(1,TimeUnit.MINUTES);
        System.out.println("Section 2 main continues. isTerminated = " + pool2.isTerminated());

    }
}