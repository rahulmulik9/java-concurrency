package phase1;

class PrintTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Running on: " + Thread.currentThread().getName());
    }
}

public class P1_StartVsRunDemo {
    public static void main(String[] args) {
        Thread t1 = new Thread(new PrintTask(), "worker-1");
        t1.run();
        t1.start();

        //with lambda function
        //first example
        Runnable printTask2 = new Runnable() {
            @Override
            public void run() {
                System.out.println("Running on: " + Thread.currentThread().getName());
            }
        };

        Thread t2 = new Thread(printTask2, "worker-2");
        t2.run();
        t2.start();

        //second example
        Runnable printTask3 = () -> System.out.println("Running on: " + Thread.currentThread().getName());
        Thread t3 = new Thread(printTask3, "worker-3");
        t3.run();
        t3.start();
    }
}