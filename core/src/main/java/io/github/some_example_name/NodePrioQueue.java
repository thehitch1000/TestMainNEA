package io.github.some_example_name;

public class NodePrioQueue {
    Node[] nodes;
    int size;

    public NodePrioQueue(int capacity) {
        this.nodes = new Node[capacity];
    }
    public int getSize() {
        return size;
    }

    public void enqueue(Node node) {
        int i = size;
        while (i > 0 && nodes[i - 1].getF() > node.getF()) {
            nodes[i] = nodes[i - 1];
            i--;
        }
        nodes[i] = node;
        size++;
    }
    public Node dequeue() {
        Node node = nodes[0];
        size--;
        for (int i = 0; i < size; i++) {
            nodes[i] = nodes[i + 1];
        }
        return node;
    }
    public Node peek() {
        return nodes[0];
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
