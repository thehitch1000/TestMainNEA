package io.github.some_example_name;

public class FloatQueue  {
    Float[] floats;
    private int size;

    public FloatQueue(int capacity) {
        this.size = size;
        floats = new Float[capacity];
    }

    public void enqueue(float f) {
        if (isFull()) {
            RemoveOne();
        }
        floats[size] = f;
        size++;
    }

    public boolean isFull() {
        return size == floats.length;
    }
    public void RemoveOne() {
        for (int i = 0; i < size; i++) {
            floats[i] = floats[i + 1];
        }
        size--;
    }
}
