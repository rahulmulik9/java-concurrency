package phase6;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Thread dump: a snapshot of all threads (state, stack, locks)
 *   - jstack <pid>: take a thread dump of a running program
 *   - How to read a dump: BLOCKED, "waiting to lock", "locked", "Found one Java-level deadlock"
 *   - Name your threads. A dump with "payment-thread" is easy to read. "Thread-0" is not.
 *   - ThreadMXBean.findDeadlockedThreads(): find a deadlock from inside the program
 *
 * REAL-LIFE EXAMPLE: TWO PEOPLE, TWO KEYS
 *   - Person A holds key 1, waits for key 2.   = payment-thread holds lockA, wants lockB
 *   - Person B holds key 2, waits for key 1.   = refund-thread holds lockB, wants lockA
 *   - The thread dump is a photo that shows who waits for whom.
 *
 * PROBLEM:
 *   The program is frozen. No error, no exception, no log. CPU is at 0%.
 *   We do not know which thread is stuck or why.
 *
 * SOLUTION:
 *   Take a thread dump with jstack. The JVM even prints "Found one Java-level deadlock"
 *   and shows the threads and the locks that make the circle.
 */
public class P34_ThreadDumpDemo {

    public static void main(String[] args) throws InterruptedException {

        Object lockA = new Object();
        Object lockB = new Object();

        // ---------------------------------------------------------
        // Section 1: BROKEN - two threads take the locks in opposite order (step 12)
        // Daemon threads, so the program can still exit at the end of the demo.
        // (sleep only gives the other thread time to take its first lock.)
        // ---------------------------------------------------------
        Thread payment = new Thread(() -> {
            synchronized (lockA) {                                // takes A first
                System.out.println("payment-thread has lockA, wants lockB");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized (lockB) {                            // waits for B forever
                    System.out.println("payment-thread has both locks");
                }
            }
        }, "payment-thread");                                     // our own thread name

        Thread refund = new Thread(() -> {
            synchronized (lockB) {                                // takes B first
                System.out.println("refund-thread has lockB, wants lockA");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                synchronized (lockA) {                            // waits for A forever
                    System.out.println("refund-thread has both locks");
                }
            }
        }, "refund-thread");

        payment.setDaemon(true);
        refund.setDaemon(true);
        payment.start();
        refund.start();

        Thread.sleep(1000);                                       // let the deadlock form

        // ---------------------------------------------------------
        // Section 2: Find the deadlock from inside the program
        // ---------------------------------------------------------
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] ids = bean.findDeadlockedThreads();                // null if there is no deadlock

        if (ids == null) {
            System.out.println("Section 2 no deadlock found");
        } else {
            System.out.println("Section 2 deadlock found. Threads in the circle: " + ids.length);
            for (ThreadInfo info : bean.getThreadInfo(ids)) {
                System.out.println("Section 2   " + info.getThreadName()
                        + " is " + info.getThreadState()
                        + ", waits for a lock held by " + info.getLockOwnerName());
            }
        }

        // ---------------------------------------------------------
        // Section 3: Take a thread dump yourself
        // The program waits 60 seconds. Open a second terminal and run jstack.
        // ---------------------------------------------------------
        long pid = ProcessHandle.current().pid();
        System.out.println("Section 3 process id = " + pid);
        System.out.println("Section 3 open another terminal and run:  jstack " + pid);
        System.out.println("Section 3 waiting 60 seconds...");
        Thread.sleep(60_000);                                     // demo only: gives you time to run jstack
    }
}