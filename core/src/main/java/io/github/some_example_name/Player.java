package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public enum Direction {
        UP, DOWN, NULL
    }

    private int lives, startingHealth;
    private float coolDownEndTime, currentHealth, minY, maxY;
    List<Missile> missiles;
    List<polygon> zigTrail;
    FloatPoint[] upPoints, downPoints, lineMidPoints;
    LineEquation[] lines, tempLines;

    Direction direction;
    FloatPoint midPoint;
    polygon healthShape, shape;
    LineEquation EndLine;

    public Player(int startingHealth) {
        this.startingHealth = startingHealth;
        this.lives = 3;
        this.currentHealth = startingHealth;
        this.minY = 0;
        this.maxY = 0;

        this.direction = Direction.NULL;

        missiles = new ArrayList<>();
        zigTrail = new ArrayList<>();

        midPoint = new FloatPoint(0, 0);

        healthShape = new polygon(4, new Color(0.16f, 0.49f, 0.42f, 1f));
        shape = new polygon(4, new Color(0.16f, 0.49f, 0.42f, 0.5f), 0.5f);

        downPoints = new FloatPoint[4];
        upPoints = new FloatPoint[4];
        lineMidPoints = new FloatPoint[2];

        for(int i = 0; i < downPoints.length; i++) {
            downPoints[i] = new FloatPoint(0, 0);
        }
        for(int i = 0; i < upPoints.length; i++) {
            upPoints[i] = new FloatPoint(0, 0);
        }
        for(int i = 0; i < lineMidPoints.length; i++) {
            lineMidPoints[i] = new FloatPoint(0, 0);
        }

        EndLine = new LineEquation(0,0, LineEquation.LineDirection.DIAGONAL);
        lines = new LineEquation[2];
        tempLines = new LineEquation[2];
        for(int i = 0; i < lines.length; i++) {
            lines[i] = new LineEquation(0,0, LineEquation.LineDirection.DIAGONAL);
            tempLines[i] = new LineEquation(0,0, LineEquation.LineDirection.DIAGONAL);
        }
    }

    public Direction getDirection() {
        return direction;
    }
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getLives() {
        return lives;
    }

    public void setDownPoints() {
        float move = midPoint.getY() - 400;
        MoveY(move);
        CalcMidPoints();
        Rotate(-45, midPoint);
        for (int i = 0; i < downPoints.length; i++) {
            downPoints[i].setPoint(shape.points[i].getX(), shape.points[i].getY());
        }
        Rotate(45, midPoint);
        MoveY(-move);
        CalcMidPoints();
    }
    public void setUpPoints() {
        float move = midPoint.getY() - 400;
        MoveY(move);
        CalcMidPoints();
        Rotate(45, midPoint);
        for (int i = 0; i < upPoints.length; i++) {
            upPoints[i].setPoint(shape.points[i].getX(), shape.points[i].getY());
        }
        Rotate(-45, midPoint);
        MoveY(-move);
        CalcMidPoints();
    }

    public void setStartingPosition(float y) {
        shape.points[0].setPoint(740, y);
        shape.points[1].setPoint(735, y + 15);
        shape.points[2].setPoint(765, y);
        shape.points[3].setPoint(735, y - 15);
        for (int i = 0; i < healthShape.points.length; i++) {
            this.healthShape.points[i].setWholePoint(this.shape.points[i]);
        }
    }

    public void CalcMidPoints() {
        float TotalX = 0, TotalY = 0;
        for (FloatPoint p : shape.points) {
            TotalX += p.getX();
            TotalY += p.getY();
        }
        midPoint.setPoint(TotalX/shape.points.length, TotalY/shape.points.length);

        float x2 = shape.points[0].getX() + shape.points[3].getX();
        float y2 = shape.points[0].getY() + shape.points[3].getY();
        lineMidPoints[0].setPoint(x2/2, y2/2);

        float x3 = shape.points[0].getX() + shape.points[1].getX();
        float y3 = shape.points[0].getY() + shape.points[1].getY();
        lineMidPoints[1].setPoint(x3/2, y3/2);

    }
    public void CreateLines() {
        if (direction == Direction.UP) {
            lines[0].setGradient(1);
            lines[1].setGradient(1);
            EndLine.setGradient(-1);
        } else {
            lines[0].setGradient(-1);
            lines[1].setGradient(-1);
            EndLine.setGradient(1);
        }
        lines[0].CalcYIntercept(lineMidPoints[0]);
        lines[1].CalcYIntercept(lineMidPoints[1]);
        EndLine.CalcYIntercept(midPoint);
    }

    public void FirstTrail() {
        polygon trail = new polygon(4, Color.WHITE);
        CreateLines();
        float intersectPoint2X, intersectPoint3X;
        if (direction == Direction.UP) {
            intersectPoint2X = (EndLine.getYIntercept() - lines[0].getYIntercept()) / 2;
            intersectPoint3X = (EndLine.getYIntercept() - lines[1].getYIntercept()) / 2;
            tempLines[0].setLine(EndLine.getGradient(), EndLine.getYIntercept());
        } else {
            intersectPoint2X = (EndLine.getYIntercept() - lines[0].getYIntercept()) / -2;
            intersectPoint3X = (EndLine.getYIntercept() - lines[1].getYIntercept()) / -2;
            tempLines[1].setLine(EndLine.getGradient(), EndLine.getYIntercept());
        }
        trail.points[0].setX(intersectPoint3X);
        trail.points[1].setX(intersectPoint2X);
        trail.points[2].setX(intersectPoint2X);
        trail.points[3].setX(intersectPoint3X);

        CalcTrailY(trail);
        zigTrail.add(trail);
    }
    public void CreateTrail() {
        polygon polygon = new polygon(4, Color.WHITE);
        int index = CalcIndex();
        float intersectPoint0X, intersectPoint1X, intersectPoint2X, intersectPoint3X, intersectPoint2XN, intersectPoint3XN;
        CreateLines();
        if (direction == Direction.UP) {
            intersectPoint0X = (tempLines[0].getYIntercept() - lines[1].getYIntercept()) / 2;
            intersectPoint1X = (tempLines[0].getYIntercept() - lines[0].getYIntercept()) / 2;
            intersectPoint2X = (EndLine.getYIntercept() - lines[0].getYIntercept()) / 2;
            intersectPoint3X = (EndLine.getYIntercept() - lines[1].getYIntercept()) / 2;

            intersectPoint2XN = (tempLines[0].getYIntercept() - lines[0].getYIntercept()) / 2;
            intersectPoint3XN = (tempLines[1].getYIntercept() - lines[0].getYIntercept()) / 2;

            zigTrail.get(zigTrail.size() - 1).points[2].setX(intersectPoint2XN);
            zigTrail.get(zigTrail.size() - 1).points[3].setX(intersectPoint3XN);

            zigTrail.get(zigTrail.size() - 1).points[2].setY(lines[0].FindY(zigTrail.get(zigTrail.size() - 1).points[2].getX()));
            zigTrail.get(zigTrail.size() - 1).points[3].setY(lines[0].FindY(zigTrail.get(zigTrail.size() - 1).points[3].getX()));
        } else {
            intersectPoint0X = (tempLines[1].getYIntercept() - lines[1].getYIntercept()) / -2;
            intersectPoint1X = (tempLines[1].getYIntercept() - lines[0].getYIntercept()) / -2;
            intersectPoint2X = (EndLine.getYIntercept() - lines[0].getYIntercept()) / -2;
            intersectPoint3X = (EndLine.getYIntercept() - lines[1].getYIntercept()) / -2;

            intersectPoint2XN = (tempLines[0].getYIntercept() - lines[1].getYIntercept()) / -2;
            intersectPoint3XN = (tempLines[1].getYIntercept() - lines[1].getYIntercept()) / -2;

            zigTrail.get(index).points[2].setX(intersectPoint2XN);
            zigTrail.get(index).points[3].setX(intersectPoint3XN);

            zigTrail.get(index).points[2].setY(lines[1].FindY(zigTrail.get(index).points[2].getX()));
            zigTrail.get(index).points[3].setY(lines[1].FindY(zigTrail.get(index).points[3].getX()));
        }
        polygon.points[0].setX(intersectPoint0X);
        polygon.points[1].setX(intersectPoint1X);
        polygon.points[2].setX(intersectPoint2X);
        polygon.points[3].setX(intersectPoint3X);

        CalcTrailY(polygon);
        zigTrail.add(polygon);
    }
    public void UpdatePoints() {
        CreateLines();
        int index = CalcIndex();

        zigTrail.get(index).points[2].setWholePoint(intersection(lines[0], EndLine));
        zigTrail.get(index).points[3].setWholePoint(intersection(lines[1], EndLine));
    }
    public int CalcIndex() {
        int index = -1;
        if (zigTrail.size() > 0) {
            index = zigTrail.size() - 1;
        }
        return index;
    }
    public void CalcTrailY(polygon polygon) {
        polygon.points[0].setY(lines[1].FindY(polygon.points[0].getX()));
        polygon.points[1].setY(lines[0].FindY(polygon.points[1].getX()));
        polygon.points[2].setY(lines[1].FindY(polygon.points[2].getX()));
        polygon.points[3].setY(lines[0].FindY(polygon.points[3].getX()));
    }
    public FloatPoint intersection(LineEquation line, LineEquation line2) {
        float x = (line2.getYIntercept() - line.getYIntercept()) / (line.getGradient() - line2.getGradient());
        float y = line.FindY(x);
        return new FloatPoint(x, y);
    }

    public void MoveX(float X) {
        shape.MoveX(X);
        healthShape.MoveX(X);
    }
    public void MoveY(float Y) {
        shape.MoveY(Y);
        healthShape.MoveY(Y);
        if (direction != Direction.NULL) {
            EndLine.MoveY(Y);
        }
    }
    public void MoveTempLinesX(float x) {
        for (LineEquation line : tempLines) {
            line.MoveX(x);
        }
    }
    public void MoveTempLinesY(float y) {
        for (LineEquation line : tempLines) {
            line.MoveY(y);
        }
    }

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
        healthShape.Draw(sr);
    }

    public void Rotate(float angle, FloatPoint pivot) {
        shape.Rotate(angle, pivot);
        healthShape.Rotate(angle, pivot);
    }

    public void PrintLines(ShapeRenderer sr) {
        sr.setColor(Color.RED);
        sr.line(0, lines[0].FindY(0), 1500, lines[0].FindY(1500));
        sr.setColor(Color.ORANGE);
        sr.line(0, lines[1].FindY(0), 1500, lines[1].FindY(1500));

        if (direction == Direction.DOWN) {
            sr.setColor(Color.RED);
            sr.line(0, tempLines[0].FindY(0), 1500, tempLines[0].FindY(1500));
            sr.setColor(Color.ORANGE);
            sr.line(0, tempLines[1].FindY(0), 1500, tempLines[1].FindY(1500));
        } else {
            sr.setColor(Color.RED);
            sr.line(0, tempLines[0].getYIntercept(), 1500, tempLines[0].FindY(1500));
            sr.setColor(Color.ORANGE);
            sr.line(0, tempLines[1].getYIntercept(), 1500, tempLines[1].FindY(1500));
        }

        sr.setColor(Color.YELLOW);
        sr.line(0, EndLine.FindY(0), 1500, EndLine.FindY(1500));
        sr.line(0, EndLine.FindY(0), 1500, EndLine.FindY(1500));
    }
    public void PrintPoints() {
        System.out.println("X: " + shape.points[0].getX() + " Y: " + shape.points[0].getY());
        System.out.println("X: " + shape.points[1].getX() + " Y: " + shape.points[1].getY());
        System.out.println("X: " + shape.points[2].getX() + " Y: " + shape.points[2].getY());
        System.out.println("X: " + shape.points[3].getX() + " Y: " + shape.points[3].getY());
        System.out.println();
    }

    public void TakeDamage(float damage) {
        currentHealth -= damage;
    }
    public boolean CheckHealth() {
        return currentHealth <= 0;
    }
    public void LoseLife() {
        lives--;
    }
    public void ResetHealth() {
        currentHealth = startingHealth;
    }

    public void calcHealthVisual() {
        FindMaxMinPoints();

        LineEquation healthLine = new LineEquation(0, minY + ((maxY - minY) * (currentHealth / startingHealth)), LineEquation.LineDirection.HORIZONTAL);


    }
    public void FindMaxMinPoints() {
        for (FloatPoint p : shape.points) {
            if (p.getY() > maxY) {
                maxY = p.getY();
            }
            if (p.getY() < minY) {
                minY = p.getY();
            }
        }
    }
}
