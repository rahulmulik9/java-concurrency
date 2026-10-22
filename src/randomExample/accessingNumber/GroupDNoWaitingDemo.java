package randomExample.accessingNumber;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   Group D: a correct count WITHOUT threads waiting for each other.
 *   AtomicInteger (CAS), LongAdder, ConcurrentHashMap.merge
 *
 * PROBLEM:
 *   Locks make threads wait. Sometimes we only need the final number to be right.
 *
 * SOLUTION:
 *   Make only the increment itself atomic. The 1-second sleep happens OUTSIDE the atomic
 *   step, so all 3 threads sleep at the same time: total about 1 sec, counter = 3.
 *   This does NOT meet "each thread waits for the others". It is safe, not sequential.
 */
public class GroupDNoWaitingDemo {

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

    private static void runThreads(Runnable task) throws InterruptedException {
        Thread[] threads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            threads[i] = new Thread(task, "T" + (i + 1));
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    private static void run(String name, Way way, java.util.function.Supplier<Number> result) throws Exception {
        System.out.println("=== " + name + " ===");
        long start = System.currentTimeMillis();
        way.run();
        long ms = System.currentTimeMillis() - start;
        System.out.println("   final counter = " + result.get() + ", time = " + ms + " ms\n");
    }

    // ---------- the ways ----------

    /** Way 1: AtomicInteger. incrementAndGet uses CAS (compare-and-set), no lock. */
    private static final AtomicInteger atomicCounter = new AtomicInteger();

    static void atomicIntegerWay() throws InterruptedException {
        atomicCounter.set(0);
        runThreads(() -> {
            sleep1s(); // all three sleep in parallel
            int now = atomicCounter.incrementAndGet();
            System.out.println("   " + Thread.currentThread().getName() + " -> counter = " + now);
        });
    }

    /** Way 2: LongAdder. Splits the count into cells to reduce contention. Read with sum(). */
    private static final LongAdder adder = new LongAdder();

    static void longAdderWay() throws InterruptedException {
        adder.reset();
        runThreads(() -> {
            sleep1s();
            adder.increment();
            System.out.println("   " + Thread.currentThread().getName() + " incremented");
        });
    }

    /** Way 3: ConcurrentHashMap.merge. The update of one key is atomic. */
    private static final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    static void concurrentHashMapWay() throws InterruptedException {
        map.clear();
        runThreads(() -> {
            sleep1s();
            map.merge("counter", 1, Integer::sum); // insert 1, or add 1 to the existing value
            System.out.println("   " + Thread.currentThread().getName() + " merged");
        });
    }

    // ---------- main ----------

    public static void main(String[] args) throws Exception {
        run("1. AtomicInteger", GroupDNoWaitingDemo::atomicIntegerWay, atomicCounter::get);
        run("2. LongAdder", GroupDNoWaitingDemo::longAdderWay, adder::sum);
        run("3. ConcurrentHashMap.merge", GroupDNoWaitingDemo::concurrentHashMapWay, () -> map.get("counter"));

        /*
         * OUTPUT EXPLANATION:
         *   All three end with counter = 3 in about 1 sec (not 3).
         *   The count is correct, but the threads never waited for each other.
         */
    }
}