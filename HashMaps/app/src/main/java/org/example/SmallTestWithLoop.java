package org.example;

/*
This class is intended for the search_With_Reset to check that it can find the correct number of operations per thread
*/
public class SmallTestWithLoop{
    private static int x;
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Beginning!!!");
        //One operation for starting
        Thread t1 = new Thread(()->{
            for(int i = 0; i<5; i++){
                //2 operations per increment
                x++;
            }
        },"t1");
        //total = 11

        Thread t2 = new Thread(()->{
            for(int i = 0; i<2; i++){
                x++;
            }
        },"t2");
        //total = 5

        Thread t3 = new Thread(()->{
            for(int i = 0; i<10; i++){
                x++;
            }
        },"t3");
        //total = 21

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
    }
}