package SUT;

public class TestTest {
    static int x;

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> x++, "t1");
        Thread t2 = new Thread(() -> x++, "t2");
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (Exception e) {
        }
        assert x > 1 : "not good";
    }
}