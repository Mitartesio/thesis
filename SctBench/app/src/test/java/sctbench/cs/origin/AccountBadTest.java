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
    public void setup() throws Exception {
        test = new AccountBad();
    }

    @RepeatedTest(10000)
    public void booleantest() {
        //bug is found if test fails
        Assertions.assertFalse(AccountBad.runOnce());
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
}
