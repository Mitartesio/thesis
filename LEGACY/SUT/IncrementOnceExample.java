package sut;


public class IncrementOnceExample {
    static int x;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            x++;
        }, "T1");

        Thread t2 = new Thread(() -> {
            x++;
        }, "T2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();


    }
}
