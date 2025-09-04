package Listeners;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class Test_Listener2 extends PropertyListenerAdapter {
    @Override
    public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextIns, Instruction executedIns) {

        MJIEnv env = ti.getEnv();
        int x = env.getStaticIntField("SimpleTest", "x");
        int y = env.getStaticIntField("SimpleTest", "y");

        if (x != y) {
            System.out.println("x = " + x + "y = " + y);
            vm.getSystemState().setIgnored(true);
        }
    }
}
