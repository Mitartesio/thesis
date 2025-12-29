package org.example;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.jupiter.api.*;


//This test class serves to have a few tests for our reset_search method with Listener_For_Counting_States and Listener_Uniform_Adapts
//This test class has been used as guiding for correctness, while most of our testing comes from the benchmarks and gradle tests we run
public class Reset_search_tests extends TestJPF{
    private static int x;

    @BeforeEach
    public void setup() {
        x = 0;
    }
    
    @Test
    public void smallTestJPF() throws Exception{
        //We use the Listener_For_Testing here to check whether 
        if(verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.Listener_For_Testing",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 100",
                "+numberOfThreads = 2",
                "+threads = t1 t2",
                "+operations = 6 8",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true",
                "+log.warning=gov.nasa.jpf",
                "+log.info=gov.nasa.jpf",
                "+vm.verbose=true",
                "+report.console.property_violation=all",
                "+log.info=gov.nasa.jpf",
                "+log.warning=gov.nasa.jpf"
        )) {
            int[] arr = new int[]{0};

            Thread t1 = new Thread(() -> {
                arr[0]++;
                arr[0]++;
            },"t1");

            Thread t2 = new Thread(() -> {
                arr[0]++;
                arr[0]++;
                arr[0]++;
            },"t2");

            t1.start();
            t2.start();

            t1.join();
            t2.join();
        }
    }

        @Test
    public void smallLoopTestJPF() throws Exception{
        //We use the Listener_For_Testing here to check whether 
        if(verifyNoPropertyViolation(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.Listener_For_Testing",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 100",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 3",
                "+threads = t1 t2 t3",
                "+operations = 13 5 21",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true",
                "+log.warning=gov.nasa.jpf",
                "+log.info=gov.nasa.jpf",
                "+vm.verbose=true",
                "+report.console.property_violation=all",
                "+log.info=gov.nasa.jpf",
                "+log.warning=gov.nasa.jpf"
        )) {
           Thread t1 = new Thread(()->{
            for(int i = 0; i<6; i++){
                //2 operations per increment
                x++;
            }
        },"t1");
        //total = 13

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

        @Test
    public void smallTest() throws InterruptedException{
        //This test is very difficult to get right with a completely random scheduler due to the fact that that thread t1 needs to
        //be called multiple time for x to be 10 and then execute thread t1
        if(verifyAssertionError(
                "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
                "+search_with_reset.k = 2500",
                // "+search_with_reset.probabilities = 0.99 0.01",
                // "+search_with_reset.eps = 0.5",
                "+numberOfThreads = 2",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true",
                "+log.warning=gov.nasa.jpf",
                "+log.info=gov.nasa.jpf",
                "+vm.verbose=true",
                "+report.console.property_violation=all",
                "+log.info=gov.nasa.jpf",
                "+log.warning=gov.nasa.jpf"
        )) {
            Thread t1 = new Thread(() ->{
                for(int i = 0; i<10; i++){
                    x++;
                } 
            });

            Thread t2 = new Thread(() -> {
                assert x != 10;
            });

            t1.start();
            t2.start();

            t1.join();
            t2.join();

        }
    }
}
