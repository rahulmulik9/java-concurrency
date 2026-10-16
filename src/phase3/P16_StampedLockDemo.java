package phase3;

import java.util.concurrent.locks.StampedLock;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - StampedLock: a lock that gives you a "stamp" (a number) each time you lock
 *   - Optimistic read: read WITHOUT taking a lock, then check if a writer came in
 *   - validate(stamp): true = nobody wrote, false = a writer changed the data
 *   - Fallback: if validate fails, take a real read lock and read again
 *
 * PROBLEM:
 *   Even a read lock has a cost. When there are many readers and very few writers,
 *   readers spend time just taking and releasing the lock.
 *
 * SOLUTION:
 *   Read first with no lock at all (optimistic read). Then ask: "did a writer
 *   change anything while I was reading?" If no, you are done. If yes, read again
 *   with a real read lock.
 */
public class P16_StampedLockDemo {

    static int x = 1;
    static int y = 1;
    static StampedLock lock = new StampedLock();

    public static void main(String[] args) throws InterruptedException {

        // Section 1: optimistic read, nobody writes (VALID)
        long stamp = lock.tryOptimisticRead();       // no lock is taken
        int curX = x;
        int curY = y;
        boolean ok = lock.validate(stamp);           // did a writer come in? no
        System.out.println("1. No writer      : x = " + curX + ", y = " + curY + ", valid = " + ok);

        // Section 2: a writer changes the data in the middle of our read (BROKEN)
        stamp = lock.tryOptimisticRead();
        curX = x;                                    // we read x = 1
        Thread writer1 = new Thread(() -> {
            long ws = lock.writeLock();
            try {
                x = 10;
                y = 10;
            } finally {
                lock.unlockWrite(ws);
            }
        });
        writer1.start();
        writer1.join();                              // writer finishes between our two reads
        curY = y;                                    // we read y = 10
        ok = lock.validate(stamp);
        System.out.println("2. Writer came in : x = " + curX + ", y = " + curY + ", valid = " + ok
                + "  (x and y do not match, throw them away)");

        // Section 3: full pattern, optimistic read with fallback (FIXED)
        stamp = lock.tryOptimisticRead();
        curX = x;                                    // we read x = 10
        Thread writer2 = new Thread(() -> {
            long ws = lock.writeLock();
            try {
                x = 20;
                y = 20;
            } finally {
                lock.unlockWrite(ws);
            }
        });
        writer2.start();
        writer2.join();
        curY = y;                                    // we read y = 20

        if (!lock.validate(stamp)) {                 // a writer came in, so our values are not safe
            System.out.println("3. Writer came in, fall back to a real read lock");
            stamp = lock.readLock();                 // normal read lock
            try {
                curX = x;
                curY = y;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        System.out.println("   Final values   : x = " + curX + ", y = " + curY + "  (they match)");
    }
}