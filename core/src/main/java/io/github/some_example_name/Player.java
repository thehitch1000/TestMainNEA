package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public enum Direction {
        UP, DOWN, NULL
    }
    public enum State {
        IDLE, JUMPING, FALLING, TIPPING, RESPAWNING, DEAD, NULL
    }

    private int AngleTillFlat, lives, startingHealth, BL;
    private float surfaceLandingY, coolDownEndTime, originPosX, originPosY, currentHealth, width, totalAngle;
    private boolean Clockwise;
    List<Ammo> ammo;
    List<Rect> normalTrail;
    List<polygon> zigTrail;
    FloatPoint[] upPoints, downPoints, lineMidPoints;
    LineEquation[] lines, tempLines;

    Direction direction;
    State state;
    FloatPoint lowestPoint, midPoint;
    Shape healthShape, shape;
    LineEquation EndLine;

    public Player(int startingHealth) {
        this.startingHealth = startingHealth;
        this.AngleTillFlat = 0;
        this.BL = 0;
        this.lives = 5;
        this.totalAngle = 0;
        this.originPosX = 730;
        this.originPosY = 200;

        this.direction = Direction.NULL;
        this.state = State.NULL;

        this.surfaceLandingY = 0;
        this.currentHealth = startingHealth;
        this.width = 40;

        ammo = new ArrayList<>();
        normalTrail = new ArrayList<>();
        zigTrail = new ArrayList<>();

        lowestPoint = new FloatPoint(0, 0);
        midPoint = new FloatPoint(0, 0);

        healthShape = new polygon(4, Color.WHITE);
        shape = new polygon(4, Color.WHITE, 0.5f);

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

    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }

    public Direction getDirection() {
        return direction;
    }
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isClockwise() {
        return Clockwise;
    }
    public void setClockwise(boolean Clockwise) {
        this.Clockwise = Clockwise;
    }

    public float getCoolDownEndTime() {
        return coolDownEndTime;
    }
    public void setCoolDownEndTime(float CoolDownEndTime) {
        coolDownEndTime = CoolDownEndTime;
    }

    public int getBL() {
        return BL;
    }
    public float getWidth() {
        return width;
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
    }
    public void setUpPoints() {
        float move = midPoint.getY() - 400;
        MoveY(move);
        Rotate(45, midPoint);
        for (int i = 0; i < upPoints.length; i++) {
            upPoints[i].setPoint(shape.points[i].getX(), shape.points[i].getY());
        }
        Rotate(-45, midPoint);
        MoveY(-move);
    }

    public float getSurfaceLandingY() {
        return surfaceLandingY;
    }
    public float getOriginPosX() {
        return originPosX;
    }
    public float getAngleTillFlat() {
        return AngleTillFlat;
    }

    public void setStartingPosition(float x, float y) {
        if (state == State.IDLE) {
            this.shape.points[0].setPoint(x, y);
            this.shape.points[1].setPoint(x + width, y);
            this.shape.points[2].setPoint(x + width, y + width);
            this.shape.points[3].setPoint(x, y + width);
            for (int i = 0; i < shape.points.length; i++) {
                this.healthShape.points[i].setWholePoint(this.shape.points[i]);
            }
        } else {
            shape.points[0].setPoint(730, y);
            shape.points[1].setPoint(725, y + 15);
            shape.points[2].setPoint(755, y);
            shape.points[3].setPoint(725, y - 15);
            for (int i = 0; i < healthShape.points.length; i++) {
                this.healthShape.points[i].setWholePoint(this.shape.points[i]);
            }
        }
    }

    public void CorrectPoints() {
        shape.points[BL].setX(originPosX);
        shape.points[(BL + 1) % 4].setX(originPosX + width);
        shape.points[(BL + 2) % 4].setX(originPosX + width);
        shape.points[(BL + 3) % 4].setX(originPosX);
        for (int i = 0; i < shape.points.length; i++) {
            this.healthShape.points[i].setWholePoint(this.shape.points[i]);
        }
        ReCalcSolidPoints();
    }

    public void EntityUpdate(List<Obstacle> obstacles) {
        AngleTillFlat();
        FindSurfaceY(obstacles);
        FindBottomLeft();
        CalcMidPoints();
    }
    private void AngleTillFlat() {
        int angle = (int) totalAngle % 360;
        if (angle == 0 || angle == 90 || angle == 180 || angle == 270) {
            AngleTillFlat = 0;
        } else if (angle < 90) {
            AngleTillFlat = 90 - angle;
        } else if (angle < 180) {
            AngleTillFlat = 180 - angle;
        } else if (angle < 270) {
            AngleTillFlat = 270 - angle;
        } else {
            AngleTillFlat = 360 - angle;
        }
    }
    private void FindSurfaceY(List<Obstacle> obstacles) {
        float newSurfaceLandingY = originPosY;

        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof Box) {
                Rect rect = (Rect) obstacle.shape;
                if (midPoint.getX() > rect.getX() && midPoint.getX() < rect.getX() + rect.getWidth()) {
                    if (rect.getY() + rect.getHeight() > newSurfaceLandingY) {
                        newSurfaceLandingY = rect.getY() + rect.getHeight();
                    }
                }
            }
        }
        surfaceLandingY = newSurfaceLandingY;
    }
    public void CalcMidPoints() {
        float x = shape.points[0].getX() + shape.points[2].getX();
        float y = shape.points[0].getY() + shape.points[2].getY();
        midPoint.setPoint(x/2, y/2);

        if (state == State.NULL) {
            float x2 = shape.points[0].getX() + shape.points[3].getX();
            float y2 = shape.points[0].getY() + shape.points[3].getY();
            lineMidPoints[0].setPoint(x2/2, y2/2);

            float x3 = shape.points[0].getX() + shape.points[1].getX();
            float y3 = shape.points[0].getY() + shape.points[1].getY();
            lineMidPoints[1].setPoint(x3/2, y3/2);
        }
    }
    private void FindBottomLeft() {
        BL = 0;
        if (AngleTillFlat == 0) {
            BL = ((int) totalAngle % 360) / 90;
        } else {
            for (int i = 1; i < 4; i++) {
                if (shape.points[i].getX() < shape.points[BL].getX()) {
                    BL = i;
                }
            }
        }
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

    public void ReCalcSolidPoints() {
        if (currentHealth <= 0) {
            Gdx.app.exit();
        }
        float multi = currentHealth / (float) startingHealth;
        healthShape.points[(BL + 2) % 4].setY(shape.points[(BL + 1) % 4].getY() + (width * multi));
        healthShape.points[(BL + 3) % 4].setY(shape.points[BL].getY() + (width * multi));
    }

    public void AddToTrail() {
        if (state == State.IDLE) {
            normalTrail.add(new Rect(GameData.getInstance().getScreenWidth()/2, CreateYHeight(), 8,8, Color.WHITE));
        }
    }
    public int CreateYHeight() {
        return (int) ((Math.random() * (width/2)) + shape.points[BL].getY());
    }
    public void CheckTrail() {
        for (int i = 0; i < normalTrail.size(); i++) {
            if (normalTrail.get(i).getAlpha() <= 0) {
                normalTrail.remove(i);
            } else {
                normalTrail.get(i).setAlpha(normalTrail.get(i).getAlpha() - 0.04f);
                normalTrail.get(i).MoveX(-5f);
                normalTrail.get(i).MoveY(0.5f);
            }
        }
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
        totalAngle -= angle;
    }

    public void EqualPoints() {
        for (int i = 0; i < shape.points.length; i++) {
            this.healthShape.points[i].setWholePoint(this.shape.points[i]);
        }
        ReCalcSolidPoints();
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
}
