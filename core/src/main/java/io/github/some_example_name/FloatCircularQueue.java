package io.github.some_example_name;

public class FloatCircularQueue  {
    Float[] floats;
    private int frontPointer, rearPointer;

    public FloatCircularQueue(int capacity) {
        floats = new Float[capacity];
        frontPointer = rearPointer = 0;
    }

    public void enqueue(float f) {
        if (isFull()) {
            dequeue();
        }
        floats[frontPointer] = f;
        frontPointer = (frontPointer + 1) % floats.length;
    }
    public void dequeue() {
        if (isEmpty()) {
            return;
        }
        floats[rearPointer] = null;
        rearPointer++;
    }

    public boolean isFull() {
        return (frontPointer + 1) % floats.length == rearPointer;
    }
    public boolean isEmpty() {
        return rearPointer == frontPointer;
    }
}
