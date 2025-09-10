package SearchAlgorithms;

import java.util.Random;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.heuristic.SimplePriorityHeuristic;
import gov.nasa.jpf.vm.VM;

public class Weighted_Random_Heuristic extends SimplePriorityHeuristic {

    protected Random random;

    public Weighted_Random_Heuristic(Config config, VM vm) {
        super(config, vm);

        random = new Random(config.getInt("choice.seed", 42));
    }

    @Override
    protected int computeHeuristicValue() {
        if (vm.getCurrentThread().isAlive()) {
            return 1;
        }
        return 2;
    }

}
