package SUT;

public class DifficultTest {
    private static int x;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 250; i++) {
                x++;
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 250; i++) {
                x++;
            }
        }, "t2");

        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 250; i++) {
                x++;
            }
        }, "t3");

        Thread t4 = new Thread(() -> {
            for (int i = 0; i < 250; i++) {
                x++;
            }
        }, "t4");

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        System.out.println("x is equal to: " + x);

        assert (x != 500) : "Found error of x == 500";
    }

}
