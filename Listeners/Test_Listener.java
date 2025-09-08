package Listeners;

import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.MJIEnv;

public class Test_Listener extends PropertyListenerAdapter {

    @Override
    public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextIns, Instruction executedIns) {
        // Get jpf env
        MJIEnv env = ti.getEnv();
        // Method for getting the static field answer
        int answerValue = env.getStaticIntField("SimpleTestVolatile2", "answer");

        if (answerValue == 5) {
            System.out.println("The value of 5 was found!");
            vm.getSystemState().setIgnored(true);
        }

        // assert (answerValue != 5) : "the answer is" + answerValue;
    }

}
