package org.example;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.jupiter.api.*;


public class StripedMapTest extends TestJPF{
    private static int x;
    private static int answer;

    @BeforeAll
    static void setup(){
        x = 0;
        answer = 0;
        StripedMap<Integer, Integer> myMap = new StripedMap<>(10);
    }

    @Test
    void test1() throws InterruptedException{
        if (verifyNoPropertyViolation(
            "+classpath=/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/test;/home/anmv/projects/jpf_thesis_work/Simple_Example_Thesis/HashMaps/app/build/classes/java/main",
//                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
                "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
                "+search.class = gov.nasa.jpf.search.Reset_Search",
//                "+search_with_reset.k = 500",
                "+search_with_reset.probabilities = 0.999 0.001",
                "+search_with_reset.eps = 0.1",
                "+numberOfThreads = 2",
                "+search.multiple_errors = false",
                "+jpf.report.console.property_violation = error",
                "+report.console.finished = result,statistics,error",
                "+report.unique_errors = true"
        )) {

            
            Thread t1 = new Thread(() -> {
                try{
                for(int i = 0; i<10; i++){
                    x++;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            });

            Thread t2 = new Thread(() -> {
                try{
                answer = x;
            }catch (Exception e){
                e.printStackTrace();
            }
            });

            t1.start();
            t2.start();

            t1.join();
            t2.join();

            Assertions.assertTrue(answer != 10);
            assert answer != 10 : "Found error";
            
}   
    }
}
