package sut;

public class SimpleTest {

    static int a = 0;
    static int b = 0;
    static int x = 0;
    static int y = 0;

    public static void main(String[] args) throws InterruptedException {

        Thread t1 = new Thread(() -> {
            a = 1;
            x = b;
        });

        Thread t2 = new Thread(() -> {
            b = 1;
            y = a;
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // if (x == 0 && y == 0) {
        // System.out.println("Found unwanted interleaving: x=0, y=0 at iteration " +
        // i);
        // break;
        // }

        // This might make it work in the sense that we can verify that JPF should flag
        // it every time x and y != 0
        // assert (x == 0 && y == 0) : "Interleaving exposes a bug where x = " + x + "
        // and y = " + y;

    }
}
