package phase3;

/*
 * CONCEPT
 * - Happens-before is Java's promise: if action A happens-before action B,
 *   then B sees everything A wrote.
 * - Without this link, Java promises nothing about what one thread sees of another.
 *
 * MAIN RULES
 * - Program order: in one thread, each line sees the lines before it
 * - volatile: a write happens-before every later read of the same field
 * - synchronized: unlock happens-before the next lock of the same monitor
 * - Thread.start(): everything before start() is visible to the new thread
 * - join(): everything the thread did is visible after join() returns
 *
 * KEY POINT
 * - A volatile write also publishes EVERY write made before it,
 *   not just the volatile field itself.
 *
 * GOAL
 * - Show that a plain field (data) becomes visible to another thread
 *   because of a volatile write (ready) made after it.
 * - Expected: reader always prints data = 42 (guaranteed)
 */
public class P3_HappensBeforeDemo {

    // plain field: published by the volatile write below
    static int data = 0;

    // volatile write/read creates the happens-before link
    static volatile boolean permissionGranted = false;

    public static void main(String[] args) throws InterruptedException {
        Thread reader = new Thread(() -> {
            System.out.println("reader: waiting for ready");
            while (!permissionGranted) {
                // spin until main sets ready
            }
            System.out.println("reader data = " + data);
        });

        reader.start();
        Thread.sleep(500);

        // order matters: data first, then the volatile write
        data = 42;
        permissionGranted = true;
        System.out.println("main: data written, ready set");

        // join: everything reader did is visible to main
        reader.join();
        System.out.println("main: reader finished");
    }
}