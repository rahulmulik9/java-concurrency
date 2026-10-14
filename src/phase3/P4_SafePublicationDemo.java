package phase3;

/*
 * CONCEPT
 * - Objects are shared through a plain array (no volatile): unsafe publication
 * - Reader may see an object before its constructor writes
 * - final field: never 0. plain field: 0 is allowed
 *
 * GOAL
 * - Create many objects, so the reader has many chances to catch a 0
 * - plainField 0 is likely on ARM (Apple M-series), rare on x86
 */
public class P4_SafePublicationDemo {

    static class Holder {
        final int finalField;
        int plainField;

        Holder() {
            finalField = 1;
            plainField = 1;
        }
    }

    // plain array: unsafe publication
    static Holder[] items = new Holder[1_000_000];

    public static void main(String[] args) throws InterruptedException {
        Thread writer = new Thread(() -> {
            for (int i = 0; i < items.length; i++) {
                items[i] = new Holder();
            }
        });
        writer.start();

        int plainZero = 0;
        int finalZero = 0;

        // main is the reader: scan while the writer fills the array
        while (writer.isAlive()) {
            for (int i = 0; i < items.length; i++) {
                Holder h = items[i];
                if (h != null) {
                    if (h.plainField == 0) plainZero++;
                    if (h.finalField == 0) finalZero++;
                }
            }
        }

        System.out.println("plainField saw 0: " + plainZero + " times");
        System.out.println("finalField saw 0: " + finalZero + " times");
    }
}