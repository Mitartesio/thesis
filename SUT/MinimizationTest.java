package SUT;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MinimizationTest {
    static int x = 0, y = 0, z = 0; // , w = 0;
    private static boolean conditionMet = false;

    public static void main(String[] args) throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();
        Condition w = lock.newCondition();

        Thread Tb = new Thread(() -> {
            int a = 0, b = 0;
            x = 1;
            a = x;
            y = a;
            lock.lock();
            try {
                while (!conditionMet) {
                    w.await();
                }
                b = y;
                z = a + b;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }, "t2");

        Thread Ta = new Thread(() -> {
            lock.lock();
            x++;
            y++;
            conditionMet = true;
            w.signal();
            lock.unlock();
            assert (z < 5) : "I found the error where !(z < 5)";
        }, "t1");

        Ta.start();
        Tb.start();

        Ta.join();
        Tb.join();

        // Only interleaving sequence in which the assert fails is B1, A1, B2, B3, A2,
        // A3, B4, B5, B6, A4
    }
}
