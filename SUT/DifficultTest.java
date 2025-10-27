package SUT;

public class DifficultTest {
    private static int x;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 250; i++) {
                assert (i <= x) : "x is smaller than i";
                x++;
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 250; i++) {
                x++;
                assert (i <= x) : "x is smaller than i";
            }
        }, "t2");

        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 250; i++) {
                assert (i <= x) : "x is smaller than i";
                x++;
            }
        }, "t3");

        Thread t4 = new Thread(() -> {
            for (int i = 0; i < 250; i++) {
                assert (i <= x) : "x is smaller than i";
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

        /*
         * This test is quite unlikely for a random walker. Every time a thread is
         * chosen another
         * thread can be scheduled right after meaning there is 3/4 chance of an
         * increment getting lost.
         * In a uniform setting this math is a bit more difficult, but the same
         * principle goes that it is very unlikely getting
         * more than 500
         */
        assert (x < 400 || x == 1000) : "Found error of x == 500";
    }

}
