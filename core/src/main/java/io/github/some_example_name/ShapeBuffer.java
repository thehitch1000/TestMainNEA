package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ShapeBuffer {
    private int capacity, frontPointer, rearPointer, size;
    Shape[] queue;
    public ShapeBuffer(int capacity) {
        this.capacity = capacity;
        this.frontPointer = 0;
        this.rearPointer = 0;
        this.size = 0;
        this.queue = new Shape[capacity];
        for (int i = 0; i < capacity; i++) {
            queue[i] = null;
        }
    }

    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFrontPointer() {
        return frontPointer;
    }
    public void setFrontPointer(int frontPointer) {
        this.frontPointer = frontPointer;
    }

    public int getRearPointer() {
        return rearPointer;
    }
    public void setRearPointer(int rearPointer) {
        this.rearPointer = rearPointer;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }

    public void add(Shape shape) {
        if (size == capacity) {
            System.out.println("Queue is full");
            Gdx.app.exit();
        }
        queue[rearPointer] = shape;
        rearPointer = (rearPointer + 1) % capacity;
        size++;
    }
    public void delete() {
        if (size == capacity) {
            System.out.println("Queue is empty");
            Gdx.app.exit();
        }
        queue[frontPointer] = null;
        frontPointer = (frontPointer + 1) % capacity;
        size--;
    }

    public void Draw(ShapeRenderer sr) {
        for (int i = 0; i < size; i++) {
            int index = (frontPointer + i) % capacity;
            if (queue[index] != null) {
                queue[index].Draw(sr);
            }
        }
    }

    public void MoveX(float x) {
        for (int i = 0; i < size; i++) {
            int index = (frontPointer + i) % capacity;
            queue[index].MoveX(x);
        }
    }
    public void MoveY(float y) {
        for (int i = 0; i < size; i++) {
            int index = (frontPointer + i) % capacity;
            queue[index].MoveY(y);
        }
    }
}
