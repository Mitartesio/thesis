package Listeners;


import java.util.Random;


import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

import utils.FoundViolation;

import gov.nasa.jpf.Config;


//Random scheduler that will simply take a random choice at each state with a non-deterministic choice

public class Random_Scheduler extends PropertyListenerAdapter {
    private Random random = new Random();
    private boolean found = false;
    private int valToFInd = 2;



    private final String className;
    private final String fieldName;
    private final int notAllowed;

    public Random_Scheduler(Config config) {
        className = config.getString("randomScheduler.className");
        fieldName = config.getString("randomScheduler.fieldName");

        if (!config.hasValue("randomScheduler.notAllowed")) {
            throw new JPFException("Missing required property: +randomScheduler.notAllowed");
        }
        notAllowed = config.getInt("randomScheduler.notAllowed");

        if (className == null || fieldName == null) {
            throw new JPFException("Missing required properties: +randomScheduler.className and/or +randomScheduler.fieldName");
        }

    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        // Check if cg is actully a ThreadChoiceGenerator

        int value = vm.getCurrentThread().getEnv().getStaticIntField(className, fieldName);

        if (value == notAllowed) {
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
        // If value has been found to violate the assertion
        if (found) {
            search.error(new FoundViolation(className,fieldName, notAllowed));
            search.terminate();
            return;
        }

        // If it's an end state we terminate
        if (search.isEndState()) {
            search.terminate();
        }
    }

}


