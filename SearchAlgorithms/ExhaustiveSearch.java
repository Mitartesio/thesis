package SearchAlgorithms;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

public class ExhaustiveSearch extends Search {

    public ExhaustiveSearch(Config config, VM vm) {
        super(config, vm);
    }

    @Override
    public boolean requestBacktrack() {
        doBacktrack = true;

        return true;
    }

    @Override
    public void search() {
        depth = 0;

        notifySearchStarted();

    }

}
