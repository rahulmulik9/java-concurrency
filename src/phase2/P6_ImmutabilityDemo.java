package phase2;
/*
 * CONCEPT
 * - An immutable object never changes after it is created, so it is always thread-safe.
 *
 * MAIN RULES
 * - final class, final fields, set only in the constructor, no setters
 * - To "change" it, create a new object and replace the reference
 * - Make the shared reference volatile so readers see the new object
 * - String and Integer are immutable, that is why they are safe to share
 *
 * KEY POINT
 * - Reader must copy the reference ONCE (ImmutablePoint p = immutable)
 *   and then use only p. Reading the field twice could give two different objects.
 *
 * GOAL
 * - Example 1: mutable point, writer sets x then y, reader can see x new and y old.
 * - Example 2: immutable point, whole object replaced in one step.
 * - Expected: Example 1 may print a mismatch (a != b), timing dependent, not every run
 *   Example 2 always prints "No mismatch found"
 */
/*
An immutable object never changes after it is created.
Rules for an immutable class:
  1) final class, so nobody can subclass it and add changes
  2) final fields, set only in the constructor
  3) no setters
An immutable object is always thread-safe. There is nothing to race on, and no lock is needed.
To “change” it, create a new object and replace the reference.
The reference itself is shared, so make it volatile so readers see the new object.
String and Integer are immutable. That is why they are safe to share.
* */
public class P6_ImmutabilityDemo {

    // mutable: x and y are changed in two separate steps
    static class MutablePoint {
        int x = 0;
        int y = 0;
    }

    // immutable: final fields, no setters, never changes after creation
    static final class ImmutablePoint {
        final int x;
        final int y;

        ImmutablePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static final MutablePoint mutable = new MutablePoint();

    // volatile so readers see the newest object
    static volatile ImmutablePoint immutable = new ImmutablePoint(0, 0);


    public static void main(String[] args) throws InterruptedException {

        Thread writer1 = new Thread(() -> {
            for (int i = 1; i <= 1_000_000; i++) {
                // two steps, a reader can look in between
                mutable.x = i;
                mutable.y = i;
            }
        }, "writer-1");

        Thread reader1 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                int a = mutable.x;
                int b = mutable.y;
                // writer and reader run at the same time
                // reader read x after writer set x, but before writer set y
                // so a is the new value and b is the old value
                if (a != b) {
                    System.out.println("reader-1 found mismatch a = " + a + ", b = " + b + " -> x new, y old");
                    // stop at the first bad read so it prints only once
                    break;
                }
            }
        }, "reader-1");

        Thread writer2 = new Thread(() -> {
            for (int i = 1; i <= 1_000_000; i++) {
                // one step, whole new object replaces the old one
                immutable = new ImmutablePoint(i, i);
            }
        }, "writer-2");

        Thread reader2 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                // read the reference once, then use only this copy
                ImmutablePoint p = immutable;
                if (p.x != p.y) {
                    System.out.println("reader-2 found mismatch a = " + p.x + ", b = " + p.y + " -> x new, y old");
                }
            }
            System.out.println("No mismatch found");
        }, "reader-2");

        System.out.println("EXAMPLE 1 mutable point");
        writer1.start();
        reader1.start();
        writer1.join();
        reader1.join();

        System.out.println("EXAMPLE 2 immutable point");
        writer2.start();
        reader2.start();
        writer2.join();
        reader2.join();
    }
}