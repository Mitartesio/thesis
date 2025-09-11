package Listeners;

import java.util.Random;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

//Random scheduler that will simply take a random choice at each state with a non-deterministic choice

public class Random_Scehduler extends PropertyListenerAdapter {
    private Random random = new Random();

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
        // Check if cg is actully a ThreadChoiceGenerator
        if (cg instanceof ThreadChoiceGenerator) {
            // Convert to ThreadChoiceGenerator to use getTotalNumberOfChoices method
            ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;
            // Get the total number of choices
            int numberOfThreads = tcg.getTotalNumberOfChoices();

            // random choice
            int choice = random.nextInt(numberOfThreads);

            // Get all threads
            Object[] objs = tcg.getAllChoices();

            // Cast to ThreadInfo
            ThreadInfo chosenThread = (ThreadInfo) objs[choice];

            // Print thread for debugging
            System.out.println(chosenThread);

            // Randomly choose the next thread
            tcg.select(choice);

        }
    }

    // Every time a new state is found and it is actully an end state simply
    // terminate
    @Override
    public void stateAdvanced(Search search) {
        if (search.isEndState()) {
            search.terminate();
        }
    }
}
