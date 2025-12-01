package sctbench.cs.origin;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BlueToothDriverBadTest extends TestJPF{
    BluetoothDriverBad test;
    public static String classPath;
    private Object device;

    @BeforeAll
    public static void init() {
        classPath = getClassPath();
    }

    @BeforeEach
    public void setup() throws Exception {
        
        // first declared class
        Class<?> deviceClass = BluetoothDriverBad.class.getDeclaredClasses()[0]; 
        device = deviceClass.getDeclaredConstructor().newInstance();

        setPrivateField(device, "pendingIo", 1);
        setPrivateField(device, "stoppingFlag", false);
        setPrivateField(device, "stoppingEvent", false);

        setStaticBoolean("stopped", false);
    }

    @RepeatedTest(100)
    public void runTest() throws Exception {
        Thread t1 = threadForStatic("BCSP_PnpStop", device);
        Thread t2 = threadForStatic("BCSP_PnpAdd", device);

        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        boolean stopped = getStaticBoolean("stopped");
        //System.out.println("Final state: stopped=" + stopped);
        
        // The "bad" concurrency result: stopped may become TRUE too early
        Assertions.assertFalse(stopped, 
            "Expected concurrency bug: stopped should not be true yet");
            
        System.out.println("RESULT:" + stopped);
    }




    public static String getClassPath() {
        String userDir = System.getProperty("user.dir"); //
        // System.out.println(userDir);
        String fs = File.separator; // For / or \ for win/osx, neet help which works, as \ escapes space in
                                    // absolutep aths on osx. This is where our problem is if we have problem on
                                    // windows.
        String ps = File.pathSeparator;

        String testClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "test";
        String mainClasses = userDir + fs + "build" + fs + "classes" + fs + "java" + fs + "main";

        return testClasses + ps + mainClasses;
    }

    // Reflection methods created to handle the private fields and methods of the
    // tested class
    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private void setStaticBoolean(String name, boolean value) throws Exception {
        Field f = AccountBad.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setBoolean(null, value);
    }

    private Thread threadForStatic(String methodName, Object arg) {
        return new Thread(() -> {
            try {
                Method m = BluetoothDriverBad.class.getDeclaredMethod(methodName, arg.getClass());
                m.setAccessible(true);
                m.invoke(null, arg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean getStaticBoolean(String name) throws Exception {
        Field f = BluetoothDriverBad.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getBoolean(null);
    }
}
