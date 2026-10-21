package phase6;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

//eat sleep train repeat
//eat sleep train repeat
/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - Immutability: an object that never changes is always thread-safe (record, final fields)
 *   - Replace, do not change: publish a NEW object through a volatile reference
 *   - Confinement: do not share. Give each thread its own object.
 *   - A checklist of common concurrency mistakes
 *
 * REAL-LIFE EXAMPLE 1: MENU CARD (immutability)
 *   - A printed menu card cannot be changed.      = immutable object
 *   - To change a price, print a NEW card.        = new Point(i, i)
 *   - Put the new card on the table.              = volatile reference
 *
 * REAL-LIFE EXAMPLE 2: COOK'S NOTEBOOK (confinement)
 *   - Each cook has his own notebook.             = one object per thread
 *   - Nobody else writes in it, so no lock.
 *
 * PROBLEM:
 *   1. A shared object that changes in 2 steps can be read half-changed (x != y).
 *   2. Some classes (SimpleDateFormat) are not thread-safe. Shared by many threads,
 *      they give wrong results or throw exceptions.
 *
 * SOLUTION:
 *   1. Make the object immutable. Replace it as a whole.
 *   2. Do not share it. Each thread creates its own copy.
 */
public class P35_BestPracticesDemo {

    // Mutable: x and y change one by one. The rule is: x must always equal y.
    static class MutablePoint {
        int x;
        int y;
    }

    // Immutable: a record has final fields and no setters.
    record Point(int x, int y) { }

    static MutablePoint mutable = new MutablePoint();
    static volatile Point immutable = new Point(0, 0);   // volatile: readers see the new card at once

    static final SimpleDateFormat SHARED_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) throws Exception {

        // ---------------------------------------------------------
        // Section 1: BROKEN - shared mutable object
        // The writer changes x, then y. The reader can come in between.
        // ---------------------------------------------------------
        int[] bad1 = {0};

        Thread writer1 = new Thread(() -> {
            for (int i = 1; i <= 1_000_000; i++) {
                mutable.x = i;
                mutable.y = i;                       // reader can look between these two lines
            }
        });
        Thread reader1 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                int a = mutable.x;
                int b = mutable.y;
                if (a != b) {
                    bad1[0]++;
                }
            }
        });
        writer1.start();
        reader1.start();
        writer1.join();
        reader1.join();
        System.out.println("Section 1 mutable object   : wrong reads = " + bad1[0]);

        // ---------------------------------------------------------
        // Section 2: FIXED - immutable object, replaced as a whole
        // The writer never changes a Point. It puts a NEW Point on the table.
        // ---------------------------------------------------------
        int[] bad2 = {0};

        Thread writer2 = new Thread(() -> {
            for (int i = 1; i <= 1_000_000; i++) {
                immutable = new Point(i, i);         // new card, x and y are set together
            }
        });
        Thread reader2 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                Point card = immutable;              // read the reference ONCE
                if (card.x() != card.y()) {
                    bad2[0]++;
                }
            }
        });
        writer2.start();
        reader2.start();
        writer2.join();
        reader2.join();
        System.out.println("Section 2 immutable object : wrong reads = " + bad2[0]);

        // ---------------------------------------------------------
        // Section 3: BROKEN - one SimpleDateFormat shared by 4 threads
        // SimpleDateFormat keeps temporary data inside. Threads overwrite it.
        // ---------------------------------------------------------
        Date expected = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-15");
        AtomicInteger errors3 = new AtomicInteger(0);

        Thread[] t3 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            t3[i] = new Thread(() -> {
                for (int j = 0; j < 5_000; j++) {
                    try {
                        Date d = SHARED_FORMAT.parse("2024-01-15");   // shared object
                        if (!d.equals(expected)) {
                            errors3.incrementAndGet();                // wrong date
                        }
                    } catch (Exception e) {
                        errors3.incrementAndGet();                    // exception
                    }
                }
            });
            t3[i].start();
        }
        for (Thread t : t3) {
            t.join();
        }
        System.out.println("Section 3 shared format     : errors = " + errors3.get() + " (expected 0)");

        // ---------------------------------------------------------
        // Section 4: FIXED - confinement. Each thread has its own SimpleDateFormat.
        // ---------------------------------------------------------
        AtomicInteger errors4 = new AtomicInteger(0);

        Thread[] t4 = new Thread[4];
        for (int i = 0; i < 4; i++) {
            t4[i] = new Thread(() -> {
                SimpleDateFormat myOwn = new SimpleDateFormat("yyyy-MM-dd");   // not shared
                for (int j = 0; j < 5_000; j++) {
                    try {
                        Date d = myOwn.parse("2024-01-15");
                        if (!d.equals(expected)) {
                            errors4.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errors4.incrementAndGet();
                    }
                }
            });
            t4[i].start();
        }
        for (Thread t : t4) {
            t.join();
        }
        System.out.println("Section 4 own format each  : errors = " + errors4.get() + " (expected 0)");
    }
}