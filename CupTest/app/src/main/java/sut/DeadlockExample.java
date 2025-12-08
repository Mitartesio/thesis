package sut;

public class DeadlockExample {

    public static final int answer = 5;

    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    /**
     * Returns true if we *suspect* a deadlock (threads didn't finish in time).
     */
    public boolean runForDeadlock() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
//                System.out.println("Thread 1: Holding lock 1...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
//                System.out.println("Thread 1: Waiting for lock 2...");
                synchronized (lock2) {
//                    System.out.println("Thread 1: Acquired lock 2!");
                }
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            synchronized (lock2) {
//                System.out.println("Thread 2: Holding lock 2...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
//                System.out.println("Thread 2: Waiting for lock 1...");
                synchronized (lock1) {
//                    System.out.println("Thread 2: Acquired lock 1!");
                }
            }
        }, "t2");


        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();

        // Wait a bit. We gota make sure that both threads have "finished"
        long timeoutMs = 2000;

        t1.join(timeoutMs);
        t2.join(timeoutMs);

        boolean t1Alive = t1.isAlive();
        boolean t2Alive = t2.isAlive();

        boolean suspectedDeadlock = t1Alive && t2Alive; // both stuck

        if (suspectedDeadlock) {
//            System.out.println("Deadlock suspected: both threads still alive after timeout.");
        }

        return suspectedDeadlock;
    }

    public static void main(String[] args) throws InterruptedException {
        DeadlockExample example = new DeadlockExample();
        boolean deadlocked = example.runForDeadlock();
//        System.out.println("Deadlock detected? " + deadlocked);
    }
}