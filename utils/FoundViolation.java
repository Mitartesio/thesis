package utils;

import gov.nasa.jpf.Property;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

import java.io.PrintWriter;

public class FoundViolation implements Property {
    private final String className;
    private final String fieldName;
    private final int notAllowedValue;


    public FoundViolation(String className, String fieldName, int notAllowedValue) {
        this.className = className;
        this.fieldName = fieldName;
        this.notAllowedValue = notAllowedValue;
    }


    @Override
    public boolean check (Search search, VM vm){

        int val = vm.getCurrentThread().getEnv().getStaticIntField(className, fieldName);
        return val != notAllowedValue;

    }

    @Override
    public String getErrorMessage (){
        return String.format("%s.value == %d", className, notAllowedValue);
    }

    @Override
    public String getExplanation(){
        return String.format("Static field %s.%s reached %d", className, fieldName, notAllowedValue);
    }

    @Override
    public void reset (){

    }

    @Override
    public Property clone() throws CloneNotSupportedException{
        return new FoundViolation(className, fieldName, notAllowedValue);
    } // so that we can store multiple errors


    @Override
    public void printOn(PrintWriter ps) {
            ps.println(getErrorMessage());
            ps.println(getExplanation());
    }
}