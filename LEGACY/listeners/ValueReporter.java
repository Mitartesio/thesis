package Listeners;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.MJIEnv;

public class ValueReporter extends PropertyListenerAdapter {
    private final String className;
    private final String fieldName;
    private final String label;

    public ValueReporter(Config config) {
        className = config.getString("valueReporter.className");
        fieldName = config.getString("valueReporter.fieldName");
        label = config.getString("valueReporter.label", "JPF_ANSWER");

        if (className == null || fieldName == null) {
            throw new gov.nasa.jpf.JPFException(
                    "Missing required: +valueReporter.className and +valueReporter.fieldName");
        }
    }

    private void printValue(Search search) {
        try {
            MJIEnv env = search.getVM().getCurrentThread().getEnv();
            int v = env.getStaticIntField(className, fieldName);
            System.out.println(label + " " + v);
            System.out.flush();
        } catch (gov.nasa.jpf.vm.ClassInfoException e) {
            // Class may not be loaded yet...
        }
    }

    @Override
    public void propertyViolated(Search search) {
        // If another listener raised a violation and may terminate early,
        // ensure we still emit the value before the search stops.
        printValue(search);
    }

    @Override
    public void stateAdvanced(Search search) {
        if (search.isEndState()) {
            printValue(search);
        }
    }

}