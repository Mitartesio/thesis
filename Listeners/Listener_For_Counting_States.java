package Listeners;

import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.PUTSTATIC;
import gov.nasa.jpf.search.Search;

public class Listener_For_Counting_States extends PropertyListenerAdapter {
    private int countT1;
    private int countT2;

    @Override
    public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInst, Instruction executedInsn) {
        if (executedInsn instanceof PUTSTATIC) {
            PUTSTATIC put = (PUTSTATIC) executedInsn;
            String tname = ti.getName();
            String fieldName = put.getFieldInfo().getName();
            if (fieldName.equals("x") && tname.equals("t1")) {
                countT1++;
                // System.out.println("Thread " + tname + " wrote " + fieldName);
                // MJIEnv env = ti.getEnv();
                // System.out.println(env.getStaticIntField("SimpleTest2", "x"));
            } else if (fieldName.equals("answer") && tname.equals("t2")) {
                // System.out.println("Thread " + tname + " wrote " + fieldName);
                countT2++;
            }
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        if (search.isEndState()) {
            search.terminate();
        }
    }

    @Override
    public void searchFinished(Search search) {
        System.out.println("The search is done!");
        System.out.println("Number of operations t2: " + countT2);
        System.out.println("Number of operations t1: " + countT1);
    }
}
