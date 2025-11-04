package SUT;

public class TestForCounting {
    static int x;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            x++;
            x++;
            x++;
            x++;
            x++;
        }, "t1");

        Thread t2 = new Thread(() -> {
            x++;
            x++;
            x++;
            x++;
            x++;
            x++;
            x++;
        }, "t2");

        Thread t3 = new Thread(() -> {
            x++;
        }, "t3");

        Thread t4 = new Thread(() -> {
            x++;
            x++;
            x++;
        }, "t4");

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        System.out.println("The result of x is: " + x);
    }
}