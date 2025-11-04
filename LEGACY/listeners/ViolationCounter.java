package Listeners;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;

public class ViolationCounter extends PropertyListenerAdapter {
    private int violations = 0;

    @Override
    public void propertyViolated(Search search) {
        // Called exactly when a property (e.g., Java assert) is violated on a path
        violations++;
        System.out.println("JPF_VIOLATION 1"); // easy to parse per run
    }

    @Override
    public void searchFinished(Search search) {
        System.out.println("Total violations: " + violations);
    }
}