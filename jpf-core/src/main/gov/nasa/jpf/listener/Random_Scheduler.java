package gov.nasa.jpf.listener;

import java.util.Random;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

public class Random_Scheduler extends PropertyListenerAdapter {
    private Random random;

    public Random_Scheduler(){
        this.random = new Random();
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg){
        
        if (cg instanceof ThreadChoiceGenerator) {
            ThreadChoiceGenerator tcg = (ThreadChoiceGenerator) cg;

            Object[] chosenThreads = tcg.getAllChoices();

            int choice = random.nextInt(chosenThreads.length);

            //for debugging
            // ThreadInfo ti = (ThreadInfo) chosenThreads[choice];
            // System.out.println("Chosen thread: " + ti.getName());

            tcg.select(choice);
        }
    }
}
