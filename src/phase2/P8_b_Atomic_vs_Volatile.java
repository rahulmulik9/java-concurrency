package phase2;

import java.util.concurrent.atomic.AtomicInteger;
/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP (STEP 8, PART B):
 *   - volatile guarantees visibility, NOT atomicity
 *   - count++ is three steps: read, add 1, write back
 *   - Regular int vs volatile int vs AtomicInteger, with the same two threads

 * SOLUTION:
 *   Make the whole read-add-write one indivisible step:
 *   - AtomicInteger.incrementAndGet()  (used here)
 *   - or synchronized (step 7)
 *
 * EXPECTED RESULT:
 *   Regular counter  -> less than 200000 (lost updates)
 *   volatile counter -> less than 200000 (volatile does not fix it)
 *   Atomic counter   -> exactly 200000
 *
 * RULE:
 *   - Thread must see another thread's change (a flag)  -> volatile
 *   - New value depends on the old value (count++)      -> synchronized or AtomicInteger
 */
public class P8_b_Atomic_vs_Volatile {
    static int regularCount = 0;
    static int volatileCount = 0;
    static AtomicInteger atomicCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        // Section 3: volatile does NOT make count++ atomic (still BROKEN)
        //volatile guarantees visibility, not atomicity.
        //Read the current value from memory
        Thread c1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                regularCount++;
                volatileCount++;                   // still read, add, write
                atomicCount.incrementAndGet();
            }
        });
        Thread c2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                regularCount++;
                volatileCount++;
                atomicCount.incrementAndGet();
            }
        });
        c1.start();
        c2.start();
        c1.join();
        c2.join();
        System.out.println("\n1. Regular counter: " + regularCount + "  (expected 200000)");
        System.out.println("\n2. volatile counter: " + volatileCount + "  (expected 200000)");
        System.out.println("\n3. Automic counter: " + atomicCount + "  (expected 200000)");



    }
}
