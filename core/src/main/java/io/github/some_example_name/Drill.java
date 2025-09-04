package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class Drill {
    public enum Direction {
        UP_RIGHT, RIGHT, DOWN_RIGHT
    }

    List<Obstacle> currentShapes;
    FloatPoint[] IntersectionPoints;
    LineEquation[] lines, oldLines;
    Direction newDirection, oldDirection, direction;
    Polygon drillShape;
    public Drill.Direction[] directions = Drill.Direction.values();
    private boolean finished;
    int timesInARow = 0;

    // 0 - up right
    // 1 - right
    // 2 - down right

    // BUFFER:
    // 0 - top square
    // 1 - top triangle
    // 2 - bottom triangle
    // 3 - bottom square

    // INTERSECTION POINTS:
    // 0 - old 0 new 0
    // 1 - old 0 new 1
    // 2 - old 1 new 0
    // 3 - old 1 new 1

    public Drill() {
        this.direction = null;
        this.finished = false;
        this.newDirection = null;
        this.oldDirection = null;

        this.lines = new LineEquation[2];
        for (int i = 0; i < lines.length; i++) {
            this.lines[i] = new LineEquation(0, 0, LineEquation.LineDirection.DIAGONAL);
        }

        this.oldLines = new LineEquation[2];
        for (int i = 0; i < oldLines.length; i++) {
            this.oldLines[i] = new LineEquation(0, 0, LineEquation.LineDirection.DIAGONAL);
        }

        this.currentShapes = new ArrayList<>();

        this.IntersectionPoints = new FloatPoint[4];
        for (int i = 0; i < IntersectionPoints.length; i++) {
            this.IntersectionPoints[i] = new FloatPoint(0, 0);
        }

        drillShape = new Polygon(4, new Color(Color.WHITE));
    }

    public Direction getDirection() {
        return direction;
    }
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isFinished() {
        return finished;
    }
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Direction getNewDirection() {
        return newDirection;
    }
    public void setNewDirection(Direction newDirection) {
        this.newDirection = newDirection;
    }

    public Direction getOldDirection() {
        return oldDirection;
    }
    public void setOldDirection(Direction oldDirection) {
        this.oldDirection = oldDirection;
    }

    public void MoveX(float X) {
        drillShape.MoveX(X);
    }
    public void MoveY(float Y) {
        drillShape.MoveY(Y);
    }
    public void MoveXY(float x) {
        MoveX(x);
        if (direction == Direction.UP_RIGHT) {
            MoveY(x);
        } else {
            MoveY(-x);
        }
    }

    public void RotateDrill(int angle, FloatPoint point) {
        drillShape.Rotate(angle, point);
    }

    public void FindLines() {
        lines[0] = null;
        lines[1] = null;
        if (newDirection == Direction.RIGHT) {
            lines[0] = new LineEquation(0, drillShape.points[0].getY(), LineEquation.LineDirection.HORIZONTAL);
            lines[1] = new LineEquation(0, drillShape.points[2].getY(), LineEquation.LineDirection.HORIZONTAL);
        } else {
            if (newDirection == Direction.UP_RIGHT) {
                lines[0] = new LineEquation(1, drillShape.points[0].getY() - drillShape.points[0].getX(), LineEquation.LineDirection.DIAGONAL);
                lines[1] = new LineEquation(1, drillShape.points[2].getY() - drillShape.points[2].getX(), LineEquation.LineDirection.DIAGONAL);
            } else {
                lines[0] = new LineEquation(-1, drillShape.points[0].getY() + drillShape.points[0].getX(), LineEquation.LineDirection.DIAGONAL);
                lines[1] = new LineEquation(-1, drillShape.points[2].getY() + drillShape.points[2].getX(), LineEquation.LineDirection.DIAGONAL);
            }
        }
        FindIntersections();
    }
    public void FindIntersections() {
        for (int i = 0; i < IntersectionPoints.length; i++) {
            IntersectionPoints[i] = new FloatPoint(0,0);
        }
        if (newDirection == Direction.RIGHT) {
            IntersectionPoints[0].setPoint(oldLines[0].FindX(lines[0].getYIntercept()), lines[0].getYIntercept()); // line 0 and old line 0
            IntersectionPoints[1] = null;
            IntersectionPoints[2] = null;
            IntersectionPoints[3].setPoint(oldLines[1].FindX(lines[1].getYIntercept()), lines[1].getYIntercept()); // line 1 and old line 1
        } else {
            if (direction == Direction.RIGHT) {
                IntersectionPoints[0].setPoint(lines[0].FindX(oldLines[0].getYIntercept()), oldLines[0].getYIntercept());
                IntersectionPoints[1] = null;
                IntersectionPoints[2] = null;
                IntersectionPoints[3].setPoint(lines[1].FindX(oldLines[1].getYIntercept()), oldLines[1].getYIntercept());
            } else {
                IntersectionPoints[0].setWholePoint(LineIntersection(lines[0], oldLines[0]));
                IntersectionPoints[1].setWholePoint(LineIntersection(lines[0], oldLines[1]));
                IntersectionPoints[2].setWholePoint(LineIntersection(lines[1], oldLines[0]));
                IntersectionPoints[3].setWholePoint(LineIntersection(lines[1], oldLines[1]));
            }
        }
    }

    public void CalcDirection() {
        if (timesInARow >= 8) {
            int chance = (int) Math.ceil(Math.random() * 102);
            if (chance <= 81) {
                newDirection = direction;
            } else {
                timesInARow = 0;
                newDirection = directions[(int) (Math.random() * directions.length)];
            }
            CheckDirection();
        } else {
            timesInARow++;
            newDirection = direction;
        }
    }
    public void CheckDirection() {
        if (newDirection == Direction.UP_RIGHT) {
            if (drillShape.points[3].getY() > GameData.getInstance().getScreenHeight() - 100) {
                newDirection = directions[1 + ((int) Math.ceil(Math.random() * 100) % 2)];
            }
        } else if (newDirection == Direction.RIGHT) {
            if (drillShape.points[2].getY() > GameData.getInstance().getScreenHeight() - 100) {
                newDirection = directions[1 + ((int) Math.ceil(Math.random() * 100) % 2)];
            } else if (drillShape.points[0].getY() < 50) {
                newDirection = directions[((int) Math.ceil(Math.random() * 100) % 2)];
            }
        } else {
            if (drillShape.points[1].getY() < 100) {
                newDirection = directions[((int) Math.ceil(Math.random() * 100) % 2)];
            }
        }
    }

    public int CalcHeight() {
        return 150 + (int) Math.ceil(Math.random() * 125);
    }

    public void FirstStartShapes() {
        InitialiseShapes();
        switch (newDirection) {
            case UP_RIGHT:
                currentShapes.get(0).setX(700);
                currentShapes.get(1).shape.points[0].setWholePoint(drillShape.points[3]);
                currentShapes.get(1).shape.points[1].setX(drillShape.points[3].getX());
                currentShapes.get(2).shape.points[0].setPoint(700, lines[0].FindY(700));
                currentShapes.get(2).shape.points[1].setY(lines[0].FindY(700));
                currentShapes.get(3).setX(700);
                currentShapes.get(3).setHeight(lines[0].FindY(700));
                break;
            case RIGHT:
                currentShapes.get(0).setX(700);
                currentShapes.get(0).setY(drillShape.points[3].getY());
                currentShapes.get(0).setHeight(GameData.getInstance().getScreenHeight() - drillShape.points[3].getY());
                currentShapes.get(1).setX(700);
                currentShapes.get(1).setHeight(drillShape.points[0].getY());
                break;
            case DOWN_RIGHT:
                currentShapes.get(0).setX(700);
                currentShapes.get(0).setY(lines[1].FindY(700));
                currentShapes.get(0).setHeight(GameData.getInstance().getScreenHeight() - lines[1].FindY(700));
                currentShapes.get(1).shape.points[0].setPoint(700, lines[1].FindY(700));
                currentShapes.get(1).shape.points[1].setY(lines[1].FindX(700));
                currentShapes.get(2).shape.points[0].setPoint(700, lines[0].FindY(700));
                currentShapes.get(2).shape.points[1].setX(700);
                currentShapes.get(3).setX(700);
                break;
        }
    }

    public void StartShapes() {
        InitialiseShapes();
        switch (newDirection) {
            case UP_RIGHT:
                if (direction == Direction.RIGHT) {
                    currentShapes.get(0).setX(IntersectionPoints[3].getX());
                    currentShapes.get(1).shape.points[0].setWholePoint(IntersectionPoints[3]);
                    currentShapes.get(1).shape.points[1].setX(IntersectionPoints[3].getX());
                    currentShapes.get(2).shape.points[0].setWholePoint(IntersectionPoints[0]);
                    currentShapes.get(2).shape.points[1].setY(IntersectionPoints[0].getY());
                    currentShapes.get(3).setX(IntersectionPoints[0].getX());
                    currentShapes.get(3).setHeight(IntersectionPoints[0].getY());
                } else if (direction == Direction.DOWN_RIGHT) {
                    currentShapes.get(0).setX(IntersectionPoints[0].getX());
                    currentShapes.get(1).shape.points[0].setWholePoint(IntersectionPoints[3]);
                    currentShapes.get(1).shape.points[1].setX(IntersectionPoints[3].getX());
                    currentShapes.get(2).shape.points[0].setWholePoint(IntersectionPoints[0]);
                    currentShapes.get(2).shape.points[1].setY(IntersectionPoints[0].getY());
                    currentShapes.get(3).setX(IntersectionPoints[0].getX());
                    currentShapes.get(3).setHeight(IntersectionPoints[0].getY());
                }
                break;
            case RIGHT:
                currentShapes.get(0).setX(IntersectionPoints[3].getX());
                currentShapes.get(0).setY(IntersectionPoints[3].getY());
                currentShapes.get(0).setHeight(GameData.getInstance().getScreenHeight() - IntersectionPoints[3].getY());
                currentShapes.get(1).setX(IntersectionPoints[0].getX());
                currentShapes.get(1).setHeight(IntersectionPoints[0].getY());
                break;
            case DOWN_RIGHT:
                if (direction == Direction.UP_RIGHT) {
                    currentShapes.get(0).setX(IntersectionPoints[3].getX());
                    currentShapes.get(0).setY(IntersectionPoints[3].getY());
                    currentShapes.get(0).setHeight(GameData.getInstance().getScreenHeight() - IntersectionPoints[3].getY());
                    currentShapes.get(1).shape.points[0].setWholePoint(IntersectionPoints[3]);
                    currentShapes.get(1).shape.points[1].setY(IntersectionPoints[3].getY());
                    currentShapes.get(2).shape.points[0].setWholePoint(IntersectionPoints[0]);
                    currentShapes.get(2).shape.points[1].setX(IntersectionPoints[0].getX());
                    currentShapes.get(3).setX(IntersectionPoints[0].getX());
                } else if (direction == Direction.RIGHT) {
                    currentShapes.get(0).setX(IntersectionPoints[3].getX());
                    currentShapes.get(0).setY(IntersectionPoints[3].getY());
                    currentShapes.get(0).setHeight(GameData.getInstance().getScreenHeight() - IntersectionPoints[3].getY());
                    currentShapes.get(1).shape.points[0].setWholePoint(IntersectionPoints[3]);
                    currentShapes.get(1).shape.points[1].setY(IntersectionPoints[3].getY());
                    currentShapes.get(2).shape.points[0].setWholePoint(IntersectionPoints[0]);
                    currentShapes.get(2).shape.points[1].setX(IntersectionPoints[0].getX());
                    currentShapes.get(3).setX(IntersectionPoints[0].getX());
                }
                break;
        }
    }
    public void  InitialiseShapes() {
        switch (newDirection) {
            case RIGHT:
                currentShapes.add(0, new RectPath(false));
                currentShapes.add(1, new RectPath(true));
                break;
            case DOWN_RIGHT:
            case UP_RIGHT:
                currentShapes.add(0, new RectPath(false));
                currentShapes.add(1, new TriPath(false));
                currentShapes.add(2, new TriPath(true));
                currentShapes.add(3, new RectPath(true));
                break;
        }
    }
    public FloatPoint LineIntersection(LineEquation line1, LineEquation line2) {
        float x = (line1.getYIntercept() - line2.getYIntercept()) / (line2.getGradient() - line1.getGradient());
        float y = line1.getGradient() * x + line1.getYIntercept();
        return new FloatPoint(x, y);
    }

    public void EndShapes() {
        switch (direction)  {
            case UP_RIGHT:
                if (newDirection == Direction.RIGHT) {
                    currentShapes.get(0).setY(IntersectionPoints[3].getY());
                    currentShapes.get(0).setHeight(GameData.getInstance().getScreenHeight() - IntersectionPoints[3].getY());
                    currentShapes.get(0).setWidth(IntersectionPoints[3].getX() - currentShapes.get(0).getX());
                    currentShapes.get(1).shape.points[1].setY(IntersectionPoints[3].getY());
                    currentShapes.get(1).shape.points[2].setWholePoint(IntersectionPoints[3]);
                    currentShapes.get(2).shape.points[1].setX(IntersectionPoints[0].getX());
                    currentShapes.get(2).shape.points[2].setWholePoint(IntersectionPoints[0]);
                    currentShapes.get(3).setWidth(IntersectionPoints[0].getX() - currentShapes.get(3).getX());
                } else if (newDirection == Direction.DOWN_RIGHT) {
                    currentShapes.get(0).setY(IntersectionPoints[3].getY());
                    currentShapes.get(0).setHeight(GameData.getInstance().getScreenHeight() - IntersectionPoints[3].getY());
                    currentShapes.get(0).setWidth(IntersectionPoints[0].getX() - currentShapes.get(0).getX());
                    currentShapes.get(1).shape.points[1].setY(IntersectionPoints[3].getY());
                    currentShapes.get(1).shape.points[2].setWholePoint(IntersectionPoints[3]);
                    currentShapes.get(2).shape.points[1].setX(IntersectionPoints[0].getX());
                    currentShapes.get(2).shape.points[2].setWholePoint(IntersectionPoints[0]);
                    currentShapes.get(3).setWidth(IntersectionPoints[0].getX() - currentShapes.get(3).getX());
                }
                break;
            case RIGHT:
                currentShapes.get(0).setWidth(IntersectionPoints[3].getX() - currentShapes.get(0).getX());
                currentShapes.get(1).setWidth(IntersectionPoints[0].getX() - currentShapes.get(1).getX());
                break;
            case DOWN_RIGHT:
                if (newDirection == Direction.UP_RIGHT) {
                    currentShapes.get(0).setWidth(IntersectionPoints[3].getX() - currentShapes.get(0).getX());
                    currentShapes.get(1).shape.points[1].setX(IntersectionPoints[3].getX());
                    currentShapes.get(1).shape.points[2].setWholePoint(IntersectionPoints[3]);
                    currentShapes.get(2).shape.points[1].setY(IntersectionPoints[0].getY());
                    currentShapes.get(2).shape.points[2].setWholePoint(IntersectionPoints[0]);
                    currentShapes.get(3).setHeight(IntersectionPoints[0].getY());
                    currentShapes.get(3).setWidth(IntersectionPoints[0].getX() - currentShapes.get(3).getX());
                } else if (newDirection == Direction.RIGHT) {
                    currentShapes.get(0).setWidth(IntersectionPoints[3].getX() - currentShapes.get(0).getX());
                    currentShapes.get(1).shape.points[1].setX(IntersectionPoints[3].getX());
                    currentShapes.get(1).shape.points[2].setWholePoint(IntersectionPoints[3]);
                    currentShapes.get(2).shape.points[1].setY(IntersectionPoints[0].getY());
                    currentShapes.get(2).shape.points[2].setWholePoint(IntersectionPoints[0]);
                    currentShapes.get(3).setWidth(IntersectionPoints[0].getX() - currentShapes.get(3).getX());
                    currentShapes.get(3).setHeight(IntersectionPoints[0].getY());
                }
                break;
        }
    }

    public void PrintLines(ShapeRenderer sr) {
        if (lines[0].getDirection() == LineEquation.LineDirection.HORIZONTAL) {
            sr.setColor(Color.RED);
            sr.line(0, lines[0].getYIntercept(), 1500, lines[0].getYIntercept());
            sr.setColor(Color.GREEN);
            sr.line(0, lines[1].getYIntercept(), 1500, lines[1].getYIntercept());
        } else {
            // For diagonal lines with slope +1 or -1
            sr.setColor(Color.RED);
            sr.line(0, lines[0].FindY(0), 1500, lines[0].FindY(1500));
            sr.setColor(Color.GREEN);
            sr.line(0, lines[1].FindY(0), 1500, lines[1].FindY(1500));
        }

        // old lines
        if (oldLines[0].getDirection() == LineEquation.LineDirection.HORIZONTAL) {
            sr.setColor(Color.RED);
            sr.line(0, oldLines[0].getYIntercept(), 1500, oldLines[0].getYIntercept());
            sr.setColor(Color.GREEN);
            sr.line(0, oldLines[1].getYIntercept(), 1500, oldLines[1].getYIntercept());
        } else {
            // For diagonal lines with slope +1 or -1
            sr.setColor(Color.RED);
            sr.line(0, oldLines[0].FindY(0), 1500, oldLines[0].FindY(1500));
            sr.setColor(Color.GREEN);
            sr.line(0, oldLines[1].FindY(0), 1500, oldLines[1].FindY(1500));
        }
    }

    public void FinishPath() {
        oldDirection = direction;
        for (int i = 0; i < oldLines.length; i++) {
            oldLines[i] = null;
            oldLines[i] = new LineEquation(
                lines[i].getGradient(),
                lines[i].getYIntercept(),
                lines[i].getDirection()
            );
        }
        switch (direction) {
            case UP_RIGHT:
                MoveXY(20);
                RotateDrill(45, drillShape.points[0]);
                newDirection = Direction.RIGHT;
                FindLines();
                break;
            case RIGHT:
                MoveX(20);
                newDirection = Direction.RIGHT;
                break;
            case DOWN_RIGHT:
                MoveXY(20);
                RotateDrill(-45, drillShape.points[3]);
                newDirection = Direction.RIGHT;
                FindLines();
                break;
        }
    }

    public void setDrill(float x, float YLevel, float width, float height) {
        drillShape.points[0].setPoint(x, YLevel);
        drillShape.points[1].setPoint(x + width, YLevel);
        drillShape.points[2].setPoint(x + width, YLevel + height);
        drillShape.points[3].setPoint(x, YLevel + height);
    }
}
