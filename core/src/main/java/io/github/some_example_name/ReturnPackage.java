package io.github.some_example_name;

import java.util.ArrayList;

public class ReturnPackage {
    ArrayList<Float> floats;
    ArrayList<Integer> ints;
    ArrayList<Boolean> booleans;

    public ReturnPackage () {
        floats = new ArrayList<>();
        ints = new ArrayList<>();
        booleans = new ArrayList<>();
    }

    public int getInt(int i) {
        return ints.get(i);
    }
    public float getFloat(int i) {
        return floats.get(i);
    }
    public boolean getBoolean(int i) {
        return booleans.get(i);
    }
}
