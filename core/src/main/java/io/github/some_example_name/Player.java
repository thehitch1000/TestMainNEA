package io.github.some_example_name;

public class Player {
    public enum State {
        IDLE, JUMPING, FALLING, TIPPING, DEAD, NULL,
    }
    public enum Direction {
        UP, DOWN, NULL
    }

    int targetTime = 0;

    private int totalAngle, LP, BL, originPosX, originPosY;
    private float x, y, surfaceLandingY;
    FloatPoint midPoint, lowestPoint;
    ShapeBuffer trail;
    Shape shape, healthShape;
    State state;
    Direction direction;

    public Player() {
        this.totalAngle = 0;
        this.LP = 0;
        this.BL = 0;
        this.surfaceLandingY = 0;
        this.originPosX = 0;
        this.originPosY = 0;

        this.x = 0;
        this.y = 0;

        this.state = State.NULL;
        this.direction = Direction.NULL;

        this.shape = null;
        this.healthShape = null;

        this.midPoint = new FloatPoint(0, 0);
        this.lowestPoint = new FloatPoint(0, 0);

        this.trail = new ShapeBuffer(10);
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

    public void MoveY(float Y) {
        for (int i = 0; i < shape.points.length; i++) {
            shape.points[i].setY(shape.points[i].getY() + y);
        }
        y += Y;
    }

    public void CalcMidPoints() {
        float x = shape.points[0].getX() + shape.points[2].getX();
        float y = shape.points[0].getY() + shape.points[2].getY();
        midPoint.setPoint(x/2, y/2);
    }
    public void FindXPoint() {
        int degrees = (totalAngle *-1) % 360;
        if (degrees < 45 || degrees > 315) {
            x = shape.points[0].getX();
        } else if (degrees < 135) {
            x = shape.points[1].getX();
        } else if (degrees < 225) {
            x = shape.points[2].getX();
        } else if (degrees < 315) {
            x = shape.points[3].getX();
        }
    }
    public void FindYPoint() {
        int degrees = (totalAngle *-1) % 360;
        if (degrees < 45 || degrees > 315) {
            y = shape.points[0].getY();
        } else if (degrees < 135) {
            y = shape.points[1].getY();
        } else if (degrees < 225) {
            y = shape.points[2].getY();
        } else if (degrees < 315) {
            y = shape.points[3].getY();
        }
    }
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
    public void FindSurfaceY(Obstacle[] obstacles) {
        float newSurfaceLandingY = originPosY;

        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof Box) {
                Rect rect = (Rect) obstacle.shape;
                if (shape.points[BL + 1].getX() > rect.getX() && x < rect.getX() + rect.getWidth()) {
                    if (rect.getY() + rect.getHeight() > newSurfaceLandingY &&
                        rect.getY() + rect.getHeight() < lowestPoint.getY()) {
                        newSurfaceLandingY = rect.getY() + rect.getHeight();
                    }
                }
            }
        }

        surfaceLandingY = newSurfaceLandingY;

        if (state == State.FALLING || state == State.JUMPING) {
            if (lowestPoint.getY() + GameData.getInstance().getPlayerSpeedY() <= surfaceLandingY) {
                HandleLanding();
            }
        }
    }
    public void HandleLanding() {
        MoveY(surfaceLandingY - lowestPoint.getY());
        state = State.TIPPING;
        GameData.getInstance().setPlayerSpeedY(0);
    }

    public void CheckTipOver() {
        if (lowestPoint.getY() + GameData.getInstance().getPlayerSpeedY() <= surfaceLandingY) {
            MoveY(surfaceLandingY - lowestPoint.getY());
            state = State.TIPPING;
            GameData.getInstance().setPlayerSpeedY(0);
        }
    }

    public int CreateYHeight() {
        return (int) Math.floor(Math.random() * 4) + (int) lowestPoint.getY();
    }
    public void CheckTrail() {
        for (int i = 0; i < trail.getSize(); i++) {
            int index = (trail.getFrontPointer() + i) % trail.getCapacity();
            Rect rect = (Rect) trail.shapes[index];
            if (rect.getAlpha() <= 0) {
                trail.delete();
            } else {
                rect.setAlpha(rect.getAlpha() - 0.04f);
                rect.MoveX(-5f);
                rect.MoveY(0.5f);
            }
        }
    }
    public void AddToTrail() {
        if (GameData.getInstance().getElapsedTime() >= targetTime) {
            Shape shape = new Rect(GameData.getInstance().getScreenWidth()/2, y + CreateYHeight(), 8,8);
            targetTime += 120;
        }
    }

    public int FindAngleTillFlat() {
        int angle = (totalAngle *-1) % 360;
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
            BL = totalAngle % 360 / 90;
        } else {
            BL = -1;
        }
    }
}
