package phase2;

/*
 * WHAT WE ARE GOING TO LEARN IN THIS STEP:
 *   - ThreadLocal: every thread gets its OWN copy of a variable
 *   - ThreadLocal.withInitial(): a default value for each thread
 *   - remove(): clean up when you are done
 *
 * PROBLEM:
 *   Two threads share one static variable (for example "current user").
 *   One thread overwrites the value, and the other thread reads the wrong one.
 *
 * SOLUTION:
 *   Store the value in a ThreadLocal. Each thread has a private copy,
 *   so no sharing, no overwriting, and no locks needed.
 */
public class P10_ThreadLocalDemo {

    static String sharedUser = "Not Initiated";                                   // one copy for ALL threads
    static ThreadLocal<String> localUser = new ThreadLocal<>(); // one copy PER thread
    static ThreadLocal<String> role = ThreadLocal.withInitial(() -> "guest");

    static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // Section 1: shared variable (BROKEN)
        System.out.println("1. Shared static variable");
        Thread a1 = new Thread(() -> {
            sharedUser = "Alice";
            pause(500);                                         // Alice does some work
            System.out.println("   Alice's thread reads: " + sharedUser);
        });
        Thread a2 = new Thread(() -> {
            pause(100);
            sharedUser = "Bob";                                 // overwrites Alice
            pause(500);
            System.out.println("   Bob's thread reads  : " + sharedUser);
        });
        a1.start();
        a2.start();
        a1.join();
        a2.join();

        // Section 2: ThreadLocal (FIXED)
        System.out.println("\n2. ThreadLocal variable");
        Thread b1 = new Thread(() -> {
            localUser.set("Alice");
            pause(500);
            System.out.println("   Alice's thread reads: " + localUser.get());
        });
        Thread b2 = new Thread(() -> {
            pause(100);
            localUser.set("Bob");                               // only changes Bob's copy
            pause(500);
            System.out.println("   Bob's thread reads  : " + localUser.get());
        });
        b1.start();
        b2.start();
        b1.join();
        b2.join();

        // Section 3: default value and remove()
        System.out.println("\n3. withInitial() and remove()");
        System.out.println("   main reads role       : " + role.get());       // guest
        Thread c = new Thread(() -> {
            System.out.println("   thread starts with    : " + role.get());   // guest
            role.set("admin");
            System.out.println("   thread sets role      : " + role.get());   // admin
            role.set("user");
            System.out.println("   thread sets role      : " + role.get());   // user
            role.remove();
            // remove() deletes THIS thread's copy only (main's copy is not touched).
            // The next get() finds no copy, so it creates a brand new one:
            //   - withInitial(...) -> runs the supplier again (here it gives "guest")
            // The new copy comes from the default value, never from main or another thread.
            System.out.println("   remove thread copy now thread read role : " + role.get());   // guest
            role.set("manager");
            System.out.println("   thread sets role      : " + role.get());   // manager

        });
        c.start();
        c.join();
        System.out.println("   main reads role again : " + role.get());       // still guest
    }
}