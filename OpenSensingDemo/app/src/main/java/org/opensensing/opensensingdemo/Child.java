package org.opensensing.opensensingdemo;

/**
 * Created by arks on 8/28/15.
 */
public class Child {
    public String name;
    public String value;

    public Child(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Child(String name, Double value) {
        this.name = name;
        this.value = value.toString();
    }

    public Child(String name, Boolean value) {
        this.name = name;
        this.value = value.toString();
    }

    public String getValue() {
        return  value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Boolean getAsBoolean() {
        return new Boolean(value);
    }

    public Double getAsDouble() {
        return new Double(value);
    }

}