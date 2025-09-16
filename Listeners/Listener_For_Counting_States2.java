package Listeners;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.DIRECTCALLRETURN;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class Listener_For_Counting_States2 extends PropertyListenerAdapter {
    private Map<String, Integer> threads = new HashMap<>();
    private String nextThread;

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        if (cg instanceof ThreadChoiceGenerator) {
            // Convert to threadChoiceGenerator
            ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

            // Get all threads
            Object[] objs = tcg.getAllChoices();
            // System.out.println("The instruction taken is: " + cg.getInsn());
            for (Object threadInfo : objs) {
                ThreadInfo threadInfo2 = (ThreadInfo) threadInfo;
                System.out.println(threadInfo2.getName());
                if (!threadInfo2.getName().equals("main") && !threads.containsKey(threadInfo2.getName())) {
                    threads.put(threadInfo2.getName(), 0);
                    nextThread = threadInfo2.getName();
                }
            }

            int totalChoices = tcg.getTotalNumberOfChoices();

            for (int i = 0; i < totalChoices; i++) {
                ThreadInfo thread = tcg.getChoice(i);
                if (thread.getName().equals(nextThread) && !(cg.getInsn() instanceof DIRECTCALLRETURN)) {
                    System.out.println("The thread: " + thread.getName() + " has tis operation " + cg.getInsn());
                    threads.put(thread.getName(), threads.get(thread.getName()) + 1);
                    tcg.select(i);
                }
            }
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        if (search.isEndState()) {
            boolean found = false;
            for (String string : threads.keySet()) {
                if (threads.get(string) == 0) {
                    nextThread = string;
                    found = true;
                }
            }
            if (found) {
                search.requestBacktrack();
            } else {
                for (String string : threads.keySet()) {
                    System.out.println("The thread: " + string + "has this many operations: " + threads.get(string));
                }
                search.terminate();
            }
        }
    }
}
