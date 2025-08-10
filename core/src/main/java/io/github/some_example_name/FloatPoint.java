package io.github.some_example_name;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class FloatPoint {
    private float x,y;

    public FloatPoint (float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public void setPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void setWholePoint(FloatPoint point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    public void MoveX(float X) {
        this.x += X;
    }
    public void MoveY(float Y) {
        this.y += Y;
    }

    public Node.NodeState isPointInTri(Polygon obstacle) {
        Vector2 A = new Vector2(obstacle.getVertices()[0], obstacle.getVertices()[1]);
        Vector2 B = new Vector2(obstacle.getVertices()[2], obstacle.getVertices()[3]);
        Vector2 C = new Vector2(obstacle.getVertices()[4], obstacle.getVertices()[5]);
        Vector2 P = new Vector2(x, y);

        Vector2 AB = B.cpy().sub(A);
        Vector2 BC = C.cpy().sub(B);
        Vector2 CA = A.cpy().sub(C);

        Vector2 AP = P.cpy().sub(A);
        Vector2 BP = P.cpy().sub(B);
        Vector2 CP = P.cpy().sub(C);

        float cross1 = AB.crs(AP);  // AB x AP
        float cross2 = BC.crs(BP);  // BC x BP
        float cross3 = CA.crs(CP);  // CA x CP

        boolean Negative = cross1 < 0 && cross2 < 0 && cross3 < 0;
        boolean Positive = cross1 > 0 && cross2 > 0 && cross3 > 0;

        if (Negative || Positive) {
            return Node.NodeState.REMOVE;
        }

        return Node.NodeState.WALKABLE;
    }
}
