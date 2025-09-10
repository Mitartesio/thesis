package Listeners;

import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.GETSTATIC;
import gov.nasa.jpf.jvm.bytecode.PUTSTATIC;
import gov.nasa.jpf.search.Search;

public class Listener_For_Counting_States extends PropertyListenerAdapter {
    private int countT1;
    private int countT2;
    private int count = 0;

    // Plan of execution

    // Make it work:
    // By using knowledge of x lets just count increments here

    // Make it right:
    // Find a way to dynamically get all shared fields
    // Then store them and use same logic as above

    @Override
    public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInst,
            Instruction executedInsn) {
        if (executedInsn instanceof PUTSTATIC) {
            PUTSTATIC put = (PUTSTATIC) executedInsn;
            String tname = ti.getName();
            String fieldName = put.getFieldInfo().getName();
            if (fieldName.equals("answer") && tname.equals("t2")) {
                countT2++;
            }
        }
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        ThreadInfo thread = vm.getCurrentThread();
        cg.select(count);
        if (thread.getName().equals("t1")) {
            countT1++;
        } else if (thread.getName().equals("t2")) {
            countT2++;
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
        System.out.println("Number of operations t1: " + countT1);
        System.out.println("Number of operations t2: " + countT2);
    }
}
