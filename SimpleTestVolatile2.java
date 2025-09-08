//This test is supposed to check if jpf is able to execute a loop and find the one instance (here 1/500) case where
//another threads sets the value answer to x.
public class SimpleTestVolatile2 {
    static volatile int x = 0;
    static int answer;

    public static void main(String[] args) throws InterruptedException {

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                x++;
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            answer = x;
        }, "t2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        // assert (answer != 50) : "Found it answer = " + answer;

    }
}
