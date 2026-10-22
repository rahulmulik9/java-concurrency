package randomExample.accessingNumber;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   Group B: strict order. T1 runs, then T2, then T3, always.
 *   join chain, wait/notifyAll, Lock+Condition, Semaphore chain,
 *   CountDownLatch chain, CyclicBarrier, Phaser
 *
 * PROBLEM:
 *   A plain lock only gives mutual exclusion. The scheduler picks who goes first,
 *   so the order can be T3, T1, T2.
 *
 * SOLUTION:
 *   Give each thread an id (1..3) and make it wait for its turn.
 *   To prove the ordering works, threads are STARTED in reverse order (T3, T2, T1),
 *   yet they still print and increment as T1, T2, T3.
 */
public class GroupBStrictOrderDemo {

    private static int counter = 0;

    @FunctionalInterface
    interface Way { void run() throws Exception; }

    // ---------- helpers ----------

    private static void sleep1s() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void incrementSlowly() {
        int read = counter;
        sleep1s();
        counter = read + 1;
        System.out.println("   " + Thread.currentThread().getName() + " -> counter = " + counter);
    }

    /** Starts threads T3, T2, T1 (reverse on purpose). Each gets its id. */
    private static void runOrdered(IntConsumer task) throws InterruptedException {
        Thread[] threads = new Thread[4];
        for (int id = 3; id >= 1; id--) {
            final int myId = id;
            threads[id] = new Thread(() -> task.accept(myId), "T" + id);
            threads[id].start();
        }
        for (int id = 1; id <= 3; id++) {
            threads[id].join();
        }
    }

    private static void run(String name, Way way) throws Exception {
        counter = 0;
        System.out.println("=== " + name + " ===");
        long start = System.currentTimeMillis();
        way.run();
        long ms = System.currentTimeMillis() - start;
        System.out.println("   final counter = " + counter + ", time = " + ms + " ms\n");
    }

    // ---------- the ways ----------

    /** Way 1: start a thread, join it, then start the next. Simplest, but no real concurrency. */
    static void joinChainWay() throws InterruptedException {
        for (int id = 1; id <= 3; id++) {
            Thread t = new Thread(GroupBStrictOrderDemo::incrementSlowly, "T" + id);
            t.start();
            t.join(); // the next thread does not even start until this one is done
        }
    }

    /** Way 2: wait/notifyAll with a "turn" variable. Always wait in a while loop. */
    private static final Object monitor = new Object();
    private static int turn;

    static void waitNotifyWay() throws InterruptedException {
        turn = 1;
        runOrdered(id -> {
            synchronized (monitor) {
                try {
                    while (turn != id) {
                        monitor.wait();
                    }
                    incrementSlowly();
                    turn++;
                    monitor.notifyAll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /** Way 3: ReentrantLock + one Condition per thread, so we wake only the next one. */
    static void conditionWay() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition[] turnCondition = new Condition[5];
        for (int i = 1; i <= 4; i++) {
            turnCondition[i] = lock.newCondition();
        }
        int[] currentTurn = {1};

        runOrdered(id -> {
            lock.lock();
            try {
                while (currentTurn[0] != id) {
                    turnCondition[id].await();
                }
                incrementSlowly();
                currentTurn[0]++;
                turnCondition[currentTurn[0]].signal(); // wake only the next thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });
    }

    /** Way 4: semaphore chain. T1 starts with a permit, each thread releases the next one's. */
    static void semaphoreChainWay() throws InterruptedException {
        Semaphore[] sems = new Semaphore[4];
        sems[1] = new Semaphore(1); // T1 may go immediately
        sems[2] = new Semaphore(0);
        sems[3] = new Semaphore(0);

        runOrdered(id -> {
            try {
                sems[id].acquire();
                incrementSlowly();
                if (id < 3) {
                    sems[id + 1].release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /** Way 5: latch chain. latch[i] opens when thread i is finished. One-shot only. */
    static void latchChainWay() throws InterruptedException {
        CountDownLatch[] done = new CountDownLatch[4];
        for (int i = 1; i <= 3; i++) {
            done[i] = new CountDownLatch(1);
        }

        runOrdered(id -> {
            try {
                if (id > 1) {
                    done[id - 1].await(); // wait for the previous thread
                }
                incrementSlowly();
                done[id].countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Way 6: CyclicBarrier. Not a natural fit (it is made for "everyone meets" rounds).
     * Trick: 3 rounds. In round r only thread r works, then everyone meets at the barrier.
     */
    static void cyclicBarrierWay() throws InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(3);

        runOrdered(id -> {
            try {
                for (int round = 1; round <= 3; round++) {
                    if (id == round) {
                        incrementSlowly();
                    }
                    barrier.await(); // everyone waits until the working thread arrives too
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Way 7: Phaser. Same round idea as CyclicBarrier, with a reusable phase counter. */
    static void phaserWay() throws InterruptedException {
        Phaser phaser = new Phaser(3); // 3 registered parties

        runOrdered(id -> {
            for (int phase = 1; phase <= 3; phase++) {
                if (id == phase) {
                    incrementSlowly();
                }
                phaser.arriveAndAwaitAdvance();
            }
        });
    }

    // ---------- main ----------

    public static void main(String[] args) throws Exception {
        run("1. join() chain", GroupBStrictOrderDemo::joinChainWay);
        run("2. wait / notifyAll", GroupBStrictOrderDemo::waitNotifyWay);
        run("3. Lock + Condition", GroupBStrictOrderDemo::conditionWay);
        run("4. Semaphore chain", GroupBStrictOrderDemo::semaphoreChainWay);
        run("5. CountDownLatch chain", GroupBStrictOrderDemo::latchChainWay);
        run("6. CyclicBarrier (rounds)", GroupBStrictOrderDemo::cyclicBarrierWay);
        run("7. Phaser (phases)", GroupBStrictOrderDemo::phaserWay);

        /*
         * OUTPUT EXPLANATION:
         *   Every way prints T1, T2, T3 in that order, final counter = 3, about 3 sec,
         *   even though threads were started as T3, T2, T1 (except way 1, which starts in order).
         */
    }
}