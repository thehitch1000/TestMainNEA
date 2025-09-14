package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Node {
    public enum NodeState {
        REMOVE,
        WALKABLE,
        UNWALKABLE,
        UNKNOWN
    }

    private int x, y;
    private float g = Float.POSITIVE_INFINITY, h = 0, f = 0;
    private Node parent;
    private NodeState state;

    public Node(int x, int y, NodeState state) {
        this.x = x;
        this.y = y;
        this.state = state;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    public NodeState getState() {
        return state;
    }
    public void setState(NodeState walkable) {
        this.state = walkable;
    }

    public float getG() {
        return g;
    }
    public void setG(float g) {
        this.g = g;
    }

    public float getH() {
        return h;
    }
    public void setH(float h) {
        this.h = h;
    }

    public float getF() {
        return f;
    }
    public void setF(float f) {
        this.f = f;
    }

    public Node getParent() {
        return parent;
    }
    public void setParent(Node parent) {
        this.parent = parent;
    }

    public void Draw(ShapeRenderer sr) {
        switch (state) {
            case WALKABLE:
                sr.setColor(0, 1, 0, 1);  // Green
                sr.circle(x, y, 1);
                break;
            case UNWALKABLE:
                sr.setColor(1, 0, 0, 1);  // Red
                sr.circle(x, y, 1);
                break;
            case UNKNOWN:
                sr.setColor(0.5f, 0.5f, 0.5f, 1);  // Gray
                sr.circle(x, y, 1);
                break;

        }
    }
}
