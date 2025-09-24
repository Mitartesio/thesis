package SUT;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MinimizationTest {
    static int x = 0, y = 0, z = 0; //, w = 0;
    
    public static void main(String[] args) throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition w = lock.newCondition();

        Thread Ta = new Thread(() -> {

            lock.lock();
            try {
                x++;
                y++;
                w.signal();
                assert (z < 5);
            } finally {
                lock.unlock();
            }
        }, "Ta");

        Thread Tb = new Thread(() -> {
            int a = 0, b = 0;
            lock.lock();
            try {
                x = 1;
                a = x;
                y = a;
                w.await();
                b = y;
                z = a + b;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }, "Tb");

        Ta.start();
        Tb.start();

        Ta.join();
        Tb.join();

        // Only interleaving sequence in which the assert fails is B1, A1, B2, B3, A2,
        // A3, B4, B5, B6, A4
    }
}
