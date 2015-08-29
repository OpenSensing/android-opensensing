package org.opensensing.opensensingdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by arks on 8/27/15.
 */
public class Group {

    public String string;
    public Boolean active;
    public String fullName;
    public final List<Child> childrenList = new ArrayList<Child>();
    public final HashMap<String, Child> children = new HashMap<String, Child>();

    public Group(String fullName, Boolean active) {
        this.fullName = fullName;
        this.string = fullName.substring(fullName.lastIndexOf('.')+1);
        this.active = active;


    }

    public void addChild(String name, String value) {
        Child child = new Child(name, value);
        children.put(name, child);
        childrenList.add(child);
    }
    public void addChild(String name, Double value) {
        Child child = new Child(name, value);
        children.put(name, child);
        childrenList.add(child);
    }
    public void addChild(String name, Boolean value) {
        Child child = new Child(name, value);
        children.put(name, child);
        childrenList.add(child);
    }

}