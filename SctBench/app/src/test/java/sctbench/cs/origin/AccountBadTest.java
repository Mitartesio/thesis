package sctbench.cs.origin;

import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class AccountBadTest extends TestJPF {
    AccountBad test;
    public static String classPath;


    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }


    @BeforeEach
    public void setup() throws Exception{
        test = new AccountBad();

        // test.x = 1;
        // test.y = 2;
        // test.z = 4;
        // test.balance = test.x;
        setStaticInt("x", 1);
        setStaticInt("y", 2);
        setStaticInt("z", 4);
        setStaticInt("balance", 1);
        setStaticBoolean("deposit_done", false);
        setStaticBoolean("withdraw_done", false);

        // test.t3 = new Thread(() -> test.check_result());
        // test.t1 = new Thread(() -> test.deposit());
        // test.t2 = new Thread(() -> test.withdraw());
    }

    @RepeatedTest(100)
    public void runTest() throws Exception {
        Thread t1 = threadFor("deposit");
        Thread t2 = threadFor("withdraw");
        Thread t3 = threadFor("check_result");

        t1.start();
        t2.start();
        t3.start();

        int balance = getStaticInt("balance");
        int x = getStaticInt("x");
        int y = getStaticInt("y");
        int z = getStaticInt("z");

        //assert here ensures we check while the threads are still running
        Assertions.assertTrue(((x + y) - z) == balance);
        
        //assures completion of all threads
        t1.join();
        t2.join();
        t3.join();
        
        // The "bad" result as in the original code
        
        System.out.println("RESULT:" + test);
    }


//     @Test
//     public void testAccoutnBadWithJpf() throws InterruptedException {

//         if (verifyNoPropertyViolation(
// //                "+classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/CupTest/app/build/classes/java/test;/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/CupTest/app/build/classes/java/main",
//                 "+classpath=" + classPath,
// //                "+native_classpath=/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf.jar:/Users/frederikkolbel/ITU/fifth semester/Thesis/simpleExample/jpf-core/build/jpf-classes.jar",
//                 "+vm.args=-ea", "+listener = gov.nasa.jpf.listener.Listener_Uniform_Adapts,gov.nasa.jpf.listener.Listener_For_Counting_States,gov.nasa.jpf.listener.AssertionProperty",
//                 "+search.class = gov.nasa.jpf.search.Reset_Search",
// //                "+search_with_reset.k = 500",
//                 "+search_with_reset.probabilities = 0.999 0.001",
//                 "+search_with_reset.eps = 0.1",
//                 "+numberOfThreads = 2",
//                 "+search.multiple_errors = false",
//                 "+jpf.report.console.property_violation = error",
//                 "+report.console.finished = result,statistics,error",
//                 "+report.unique_errors = true"

//         )) {  // specifies the test goal, "jpfOptions" are optional
//             test = new AccountBad();
//             test.();

//             //gov.nasa.jpf.JPF.main(new String[]{"../../configs/MinimizationTest.jpf"});
//         }
//     }

    public static String getClassPath() {
        String userDir = System.getProperty("user.dir");   //
//        System.out.println(userDir);
        String fs = File.separator;                   // For / or \ for win/osx, neet help which works, as \ escapes space in absolutep aths on osx. This is where our problem is if we have problem on windows.
        String ps = File.pathSeparator;

        String testClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "test";
        String mainClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "main";

        return testClasses + ps + mainClasses;
    }

    // Reflection methods created to handle the private fields and methods of the tested class
    private void setStaticInt(String name, int value) throws Exception {
        Field f = AccountBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(null, value); // null because static
    }

    private void setStaticBoolean(String name, boolean value) throws Exception {
        Field f = AccountBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setBoolean(null, value);
}

    private Thread threadFor(String methodName) {
        return new Thread(() -> {
            try {
                Method m = AccountBad.class.getDeclaredMethod(methodName);
                m.setAccessible(true);
                m.invoke(test);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private int getStaticInt(String name) throws Exception {
        Field f = AccountBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(null);
    }

}
