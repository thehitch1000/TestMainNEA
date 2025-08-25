package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public interface IntShape {
    void Draw(ShapeRenderer sr);
    void setShape(float x, float y);
    void MoveX(float X);
    void MoveY(float Y);
    boolean isPointInShape(FloatPoint point);
    boolean onScreen();
    void Rotate(float angle, FloatPoint point);
}

abstract class Shape implements IntShape {
    protected FloatPoint[] points;
    Matrix Angles, OldPoints, NewPoints;

    public void Draw(ShapeRenderer sr) {}
    public void setShape(float x, float y) {}
    public void MoveX(float X) {}
    public void MoveY(float Y) {}
    public boolean isPointInShape(FloatPoint point) {
        return false;
    }
    public boolean onScreen() {
        return false;
    }

    public void Rotate(float angle, FloatPoint point) {
        Angles = new Matrix(new float[4]);
        OldPoints = new Matrix(new float[8]);
        NewPoints = new Matrix(new float[8]);
        Angles.setMatrixSection(0, CosValue(Radians(angle)));
        Angles.setMatrixSection(1, -(SinValue(Radians(angle))));
        Angles.setMatrixSection(2, SinValue(Radians(angle)));
        Angles.setMatrixSection(3, CosValue(Radians(angle)));
        for (int i = 0; i < points.length; i++) {
            OldPoints.setMatrixSection(i, points[i].getX() - point.getX());
            OldPoints.setMatrixSection(i + 4, points[i].getY() - point.getY());
        }
        for (int i = 0; i < points.length; i++) {
            NewPoints.setMatrixSection(i, (OldPoints.getMatrixSection(i) * Angles.getMatrixSection(0)) + (OldPoints.getMatrixSection(i + 4) * Angles.getMatrixSection(1)));
            NewPoints.setMatrixSection(i + 4, (OldPoints.getMatrixSection(i) * Angles.getMatrixSection(2)) + (OldPoints.getMatrixSection(i + 4) * Angles.getMatrixSection(3)));
        }
        for (int i = 0; i < points.length; i++) {
            points[i].setX(NewPoints.getMatrixSection(i) + point.getX());
            points[i].setY(NewPoints.getMatrixSection(i + 4) + point.getY());
        }
    }
    private float CosValue(float radians) {
        return (float) Math.cos(radians);
    }
    private float SinValue(float radians) {
        return (float) Math.sin(radians);
    }
    private float Radians(float angle) {
        return (float) (angle * (Math.PI / 180));
    }

}

class Rect extends Shape implements Transparency {
    private float x, y, width, height, alpha;

    public Rect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.alpha = 1;
        points = null;
    }
    public Rect(float width, float height) {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
        this.alpha = 1;
        points = null;
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

    public float getWidth() {
        return width;
    }
    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }
    public void setHeight(float height) {
        this.height = height;
    }

    public void setAlpha(float Alpha) {
        alpha = Alpha;
    }
    public float getAlpha() {
        return alpha;
    }

    public void setShape(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void Draw(ShapeRenderer sr) {
        sr.setColor(1, 1, 1, alpha);
        if (points != null) {
            sr.triangle(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[2].getX(), points[2].getY());
            sr.triangle(points[2].getX(), points[2].getY(), points[3].getX(), points[3].getY(), points[0].getX(), points[0].getY());
        } else {
            sr.rect(x, y, width, height);
        }
    }
    public void MoveX(float X) {
        if (points != null) {
            for (int i = 0; i < points.length; i++) {
                points[i].setX(points[i].getX() + X);
            }
        } else {
            x += X;
        }
    }
    public void MoveY(float Y) {
        if (points != null) {
            for (int i = 0; i < points.length; i++) {
                points[i].setY(points[i].getY() + Y);
            }
        } else {
            y += Y;
        }
    }
    public boolean isPointInShape(FloatPoint point) {
        return point.getX() > x && point.getX() < x + width && point.getY() > y && point.getY() < y + height;
    }
    public boolean onScreen() {
        if (points != null) {
            boolean case1 = (points[0].getX() < GameData.getInstance().getScreenWidth() && points[0].getX() > 0);
            boolean case2 = (points[1].getX() < GameData.getInstance().getScreenWidth() && points[1].getX() > 0);
            boolean case3 = (points[2].getX() < GameData.getInstance().getScreenWidth() && points[2].getX() > 0);
            boolean case4 = (points[3].getX() < GameData.getInstance().getScreenWidth() && points[3].getX() > 0);

            return case1 && case2 && case3 && case4;
        } else {
            return (x < GameData.getInstance().getScreenWidth() && x > 0);
        }
    }
}

class Tri extends Shape {
    public Tri(FloatPoint point1, FloatPoint point2, FloatPoint point3) {
        points = new FloatPoint[3];
        points[0] = point1;
        points[1] = point2;
        points[2] = point3;
    }
    public Tri(FloatPoint[] Points) {
        points = Points;
    }

    public void Draw(ShapeRenderer sr) {
        sr.triangle(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[2].getX(), points[2].getY());
    }
    public void MoveX(float X) {
        for (int i = 0; i < points.length; i++) {
            points[i].setX(points[i].getX() + X);
        }
    }
    public void MoveY(float Y) {
        for (int i = 0; i < points.length; i++) {
            points[i].setY(points[i].getY() + Y);
        }
    }
    public boolean isPointInShape(FloatPoint point) {
        Vector2 A = new Vector2(points[0].getX(), points[0].getY());
        Vector2 B = new Vector2(points[1].getX(), points[1].getY());
        Vector2 C = new Vector2(points[2].getX(), points[2].getY());
        Vector2 P = new Vector2(point.getX(), point.getY());

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

        return (Negative || Positive);
    }
    public boolean onScreen() {
        boolean case1 = (points[0].getX() < GameData.getInstance().getScreenWidth() && points[0].getX() > 0);
        boolean case2 = (points[1].getX() < GameData.getInstance().getScreenWidth() && points[1].getX() > 0);
        boolean case3 = (points[2].getX() < GameData.getInstance().getScreenWidth() && points[2].getX() > 0);

        return case1 && case2 && case3;
    }
}

class Arrow extends Shape implements Transparency {
    private float alpha;
    Tri[] tris;

    public Arrow(FloatPoint[] Points) {
        points = Points;
        tris = new Tri[2];
        this.alpha = 1;
    }

    public void setAlpha(float Alpha) {
        alpha = Alpha;
    }
    public float getAlpha() {
        return alpha;
    }

    public void UpdateTriangles() {
        for (int i = 0; i < tris.length; i++) {
            for (int j = 0; j < tris[i].points.length; j++) {
                tris[i].points[j].setWholePoint(points[(2*i) + j]);
            }
        }
    }

    public void Draw (ShapeRenderer sr) {
        sr.triangle(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), points[2].getX(), points[2].getY());
    }

    public void MoveX(float X) {
        for (FloatPoint point : points) {
            point.MoveX(X);
        }
    }
    public void MoveY(float Y) {
        for (FloatPoint point : points) {
            point.MoveY(Y);
        }
    }

    public boolean IsPointInShape(FloatPoint point) {
        UpdateTriangles();
        for (Tri tri : tris) {
            if (tri.isPointInShape(point)) {
                return true;
            }
        }
        return false;
    }

    public boolean onScreen() {
        boolean[] cases = new boolean[4];
        for (int i = 0; i < cases.length; i++) {
            float x = points[i].getX();
            cases[i] = (x > 0 && x < GameData.getInstance().getScreenWidth());
        }
        return (cases[0] || cases[1] || cases[2] || cases[3]);
    }

}

class Circle extends Shape {
    private float radius, x, y;

    public Circle(float radius) {
        this.radius = radius;
        points = null;
        this.x = 0;
        this.y = 0;
    }

    public void Draw(ShapeRenderer sr) {
        sr.circle(x,y,radius);
    }

    public void setShape(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void MoveX(float X) {
        x += X;
    }
    public void MoveY(float Y) {
        y += Y;
    }

    public boolean IsPointInShape(FloatPoint point) {
        Vector2 C = new Vector2(x,y);
        Vector2 P = new Vector2(point.getX(), point.getY());

        return (C.dst(P) <= radius);
    }

    public boolean onScreen() {
        return x + radius >= 0 || x - radius <= GameData.getInstance().getScreenWidth();
    }
}

class Polygon extends Shape {
    private int numOfPoints;
    private FunctionLock lock;
    ArrayList<Tri> tris;

    public Polygon(int numOfPoints) {
        this.numOfPoints = numOfPoints;
        points = new FloatPoint[numOfPoints];
        tris = new ArrayList<>(numOfPoints - 2);
        lock = new FunctionLock();
    }
    public Polygon(FloatPoint[] Points) {
        points = Points;
        numOfPoints = points.length;
        tris = new ArrayList<>(numOfPoints - 2);
        lock = new FunctionLock();
    }

    public void UpdateTriangles() {
        if (!lock.getState()) {
            tris.clear();
            List<FloatPoint> vertices = new ArrayList<FloatPoint>(numOfPoints);

            for (FloatPoint point : points) {
                vertices.add(new FloatPoint(point.getX(), point.getY()));
            }

            while (vertices.size() > 3) {
                for (int i = 0; i < vertices.size(); i++) {
                    FloatPoint pre = vertices.get((i - 1 + vertices.size()) % vertices.size());
                    FloatPoint cur = vertices.get(i);
                    FloatPoint nex = vertices.get((i + 1) % vertices.size());

                    FloatPoint pc = new FloatPoint(cur.getX() - pre.getX(), cur.getY() - pre.getY());
                    FloatPoint cn = new FloatPoint(nex.getX() - cur.getX(), nex.getY() - cur.getY());

                    if (!isConvex(pc, cn)) {
                        continue;
                    }

                    Tri tri = new Tri(pre, cur, nex);

                    if (isVerticesInTri(tri, vertices, i)) continue;

                    tris.add(tri);

                    vertices.remove(i);
                    break;
                }
            }
            tris.add(new Tri(vertices.get(0), vertices.get(1), vertices.get(2)));
            lock.used();
        }
    }

    private boolean isConvex(FloatPoint v1, FloatPoint v2) {
        return ((v1.getX() * v2.getY()) - (v1.getY() * v2.getX())) < 0;
    }
    private boolean isVerticesInTri(Tri tri, List<FloatPoint> vertices, int currentPoint) {
        for (int n = 0; n < vertices.size() - 3; n++) {
            int index = (currentPoint + 2 + n);
            FloatPoint point = new FloatPoint(vertices.get(index).getX(), vertices.get(index).getY());
            if (tri.isPointInShape(point)) return true;
        }
        return false;
    }

    public void Draw(ShapeRenderer sr) {
        UpdateTriangles();
        for  (Tri tri : tris) {
            tri.Draw(sr);
        }
    }

    public void MoveX(float X) {
        for (FloatPoint point : points) {
            point.MoveX(X);
        }
    }
    public void MoveY(float Y) {
        for (FloatPoint point : points) {
            point.MoveY(Y);
        }
    }

    public boolean isPointInShape(FloatPoint point) {
        UpdateTriangles();
        for (Tri tri : tris) {
            if (tri.isPointInShape(point)) {
                return true;
            }
        }
        return false;
    }
    public boolean onScreen() {
        for (FloatPoint point : points) {
            if (point.getX() <= GameData.getInstance().getScreenWidth() && point.getX() >= 0) {
                return true;
            }
        }
        return false;
    }
}

interface Transparency {
    void setAlpha(float Alpha);
    float getAlpha();
}
interface Colour {
    void setColour(Color colour);
    Color getColour();
}
