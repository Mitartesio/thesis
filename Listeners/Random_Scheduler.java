package Listeners;

import java.util.Random;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

//Random scheduler that will simply take a random choice at each state with a non-deterministic choice

public class Random_Scheduler extends PropertyListenerAdapter {
    private Random random = new Random();
    private boolean found = false;
    private int valToFInd = 2;

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        // Check if cg is actully a ThreadChoiceGenerator

        ThreadInfo ti = vm.getCurrentThread();
        MJIEnv env = ti.getEnv();
        if (env.getStaticIntField("SimpleTest2", "answer") == valToFInd) {
            found = true;
        }

        if (cg instanceof ThreadChoiceGenerator) {
            // Convert to ThreadChoiceGenerator to use getTotalNumberOfChoices method
            ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;
            // Get the total number of choices
            int numberOfThreads = tcg.getTotalNumberOfChoices();

            // random choice
            int choice = random.nextInt(numberOfThreads);

            // Uncomment this section for debugging: Will show which thread is chosen at
            // each point!

            // Get all threads
            // Object[] objs = tcg.getAllChoices();

            // Cast to ThreadInfo
            // ThreadInfo chosenThread = (ThreadInfo) objs[choice];

            // System.out.println(chosenThread);

            // Randomly choose the next thread
            tcg.select(choice);

        }
    }

    // Every time a new state is found and it is actully an end state simply
    // terminate
    @Override
    public void stateAdvanced(Search search) {
        // If value has been found
        if (found) {
            System.out.println("1");
            search.terminate();
        }
        // If search is at end state we check if the current field of x == value to find
        // to print 1 or 0 and the terminate
        if (search.isEndState()) {
            if (found || search.getVM().getCurrentThread().getEnv().getStaticIntField("SimpleTest2",
                    "answer") == valToFInd) {
                System.out.println("JPF_FOUND 1");
            } else {
                System.out.println("JPF_FOUND 2");
            }
            search.terminate();
        }
    }
}
