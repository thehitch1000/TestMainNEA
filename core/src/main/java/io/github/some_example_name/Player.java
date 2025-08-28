package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public enum State {
        IDLE, JUMPING, FALLING, TIPPING, RESPAWNING, DEAD, NULL,
    }
    public enum Direction {
        UP, DOWN, NULL
    }

    private int totalAngle, LP, BL, originPosX, originPosY;
    private float xDistance, /* x,  y,*/ surfaceLandingY, width, ySpeed;
    private boolean Clockwise;
    FloatPoint midPoint, lowestPoint;
    List<Rect> trail;
    Shape shape, healthShape;
    State state;
    Direction direction;

    public Player() {
        this.totalAngle = 0;
        this.LP = 0;
        this.BL = 0;
        this.surfaceLandingY = 0;
        this.originPosX = 0;
        this.originPosY = 200;
        this.width = 40;
        this.ySpeed = 0;

//        this.x = 0;
//        this.y = 0;

        this.xDistance = 0;

        this.state = State.IDLE;
        this.direction = Direction.NULL;

        this.shape = new Polygon(4, Color.WHITE, 0.5f);
        this.healthShape = new Polygon(4, Color.WHITE);

        this.midPoint = new FloatPoint(0, 0);
        this.lowestPoint = new FloatPoint(0, 0);

        this.trail = new ArrayList<>();

        this.Clockwise = true;
    }

    public void setStartingPosition(float x, float y) {
        shape.points[0].setPoint(x, y);
        shape.points[1].setPoint(x + width, y);
        shape.points[2].setPoint(x + width, y + width);
        shape.points[3].setPoint(x, y + width);
        linkHealthShape();

    }
    private void linkHealthShape() {
        this.healthShape.points[0].setPoint(shape.points[0].getX(), shape.points[0].getY());
        this.healthShape.points[1].setPoint(shape.points[1].getX(), shape.points[1].getY());
        this.healthShape.points[2].setPoint(shape.points[2].getX(), shape.points[2].getY());
        this.healthShape.points[3].setPoint(shape.points[3].getX(), shape.points[3].getY());
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

    public float getSurfaceLandingY() {
        return surfaceLandingY;
    }
    public int getBL() {
        return BL;
    }
    public int getLP() {
        return LP;
    }
    public int getOriginPosX() {
        return originPosX;
    }
    public float getWidth() {
        return width;
    }
    public float getXDistance() {
        return xDistance;
    }

    public void setXDistance(float xDistance) {
        this.xDistance = xDistance;
    }

    public void MoveY(float Y) {
        shape.MoveY(Y);
        healthShape.MoveY(Y);
    }

    public void CalcMidPoints() {
        float x = shape.points[0].getX() + shape.points[2].getX();
        float y = shape.points[0].getY() + shape.points[2].getY();
        midPoint.setPoint(x/2, y/2);
    }
//    public void FindXPoint() {
//        int degrees = (totalAngle *-1) % 360;
//        if (degrees < 45 || degrees > 315) {
//            x = shape.points[0].getX();
//        } else if (degrees < 135) {
//            x = shape.points[1].getX();
//        } else if (degrees < 225) {
//            x = shape.points[2].getX();
//        } else if (degrees < 315) {
//            x = shape.points[3].getX();
//        }
//    }
//    public void FindYPoint() {
//        int degrees = (totalAngle *-1) % 360;
//        if (degrees < 45 || degrees > 315) {
//            y = shape.points[0].getY();
//        } else if (degrees < 135) {
//            y = shape.points[1].getY();
//        } else if (degrees < 225) {
//            y = shape.points[2].getY();
//        } else if (degrees < 315) {
//            y = shape.points[3].getY();
//        }
//    }
    public void FindLowestPoint() {
        LP = 0;
        for (int i = 0; i < shape.points.length; i++) {
            if (i == 0) {
                lowestPoint.setPoint(shape.points[i].getX(), shape.points[i].getY());
            } else if (lowestPoint.getY() > shape.points[i].getY()) {
                lowestPoint.setPoint(shape.points[i].getX(), shape.points[i].getY());
                LP = i;
            }
        }
    }
    public void FindSurfaceY(ArrayList<Obstacle> obstacles) {
        float newSurfaceLandingY = originPosY;

        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof Box) {
                Rect rect = (Rect) obstacle.shape;
                if (shape.points[BL + 1].getX() > rect.getX() && shape.points[BL].getX() < rect.getX() + rect.getWidth()) {
                    if (rect.getY() + rect.getHeight() > newSurfaceLandingY &&
                        rect.getY() + rect.getHeight() < lowestPoint.getY()) {
                        newSurfaceLandingY = rect.getY() + rect.getHeight();
                    }
                }
            }
        }

        surfaceLandingY = newSurfaceLandingY;
    }

    public int CreateYHeight() {
        return (int) ((Math.random() * (width/2)) + shape.points[BL].getY());
    }
    public void CheckTrail() {
        System.out.println(trail.size());
        for (int i = 0; i < trail.size(); i++) {
            if (trail.get(i).getAlpha() <= 0) {
                trail.remove(i);
            } else {
                trail.get(i).setAlpha(trail.get(i).getAlpha() - 0.04f);
                trail.get(i).MoveX(-5f);
                trail.get(i).MoveY(0.5f);
            }
        }
    }
    public void AddToTrail() {
        if (state == State.IDLE) {
            trail.add(new Rect(GameData.getInstance().getScreenWidth()/2, CreateYHeight(), 8,8, Color.WHITE));
        }
    }

    public int FindAngleTillFlat() {
        int angle = totalAngle % 360;
        if (angle == 0 || angle == 90 || angle == 180 || angle == 270) {
            return 0;
        } else if (angle < 90) {
            return 90 - angle;
        } else if (angle < 180) {
            return 180 - angle;
        } else if (angle < 270) {
            return 270 - angle;
        } else {
            return 360 - angle;
        }
    }

    public void FindBottomLeft() {
        if (FindAngleTillFlat() == 0) {
            BL = (totalAngle % 360) / 90;
        } else {
            BL = -1;
        }
    }

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
//        healthShape.Draw(sr);
    }

    public void Rotate(int angle, FloatPoint point) {
        shape.Rotate(angle, point);
        healthShape.Rotate(angle, point);
        totalAngle -= angle;
    }
}
