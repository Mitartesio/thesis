package SUT;

public class SmallTest {
    static int x = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            x = 1;
            x = x * 2;
        }, "t1");

        Thread t2 = new Thread(() -> {
            x = 3;
            x = x + 1;
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assert x != 8 : "x is 8";
    }
}
