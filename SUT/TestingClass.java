package SUT;

public class TestingClass {
    private static int test;

    public static void main(String[] args) throws InterruptedException {

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                test++;
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                test++;
            }
        }, "t2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }
}
