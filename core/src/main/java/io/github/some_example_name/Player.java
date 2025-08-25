package io.github.some_example_name;

public class Player {
    public enum State {
        IDLE, JUMPING, FALLING, TIPPING, DEAD, NULL,
    }
    public enum Direction {
        UP, DOWN, NULL
    }

    int targetTime = 0;

    private int totalAngle, LP, surfaceLandingY, originPosX, originPosY;
    private float x, y;
    FloatPoint midPoint, lowestPoint;
    ShapeBuffer trail;
    Shape shape, healthShape;
    State state;
    Direction direction;

    public Player() {
        this.totalAngle = 0;
        this.LP = 0;
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
        for (int i = 0; i < points.length; i++) {
            points[i].setY(points[i].getY() + y);
        }
        y += Y;
    }

    public void CalcMidPoints() {
        float x = points[0].getX() + points[2].getX();
        float y = points[0].getY() + points[2].getY();
        midPoint.setPoint(x/2, y/2);
    }
    public void FindXPoint() {
        int degrees = (totalAngle *-1) % 360;
        if (degrees < 45 || degrees > 315) {
            x = points[0].getX();
        } else if (degrees < 135) {
            x = points[1].getX();
        } else if (degrees < 225) {
            x = points[2].getX();
        } else if (degrees < 315) {
            x = points[3].getX();
        }
    }
    public void FindYPoint() {
        int degrees = (totalAngle *-1) % 360;
        if (degrees < 45 || degrees > 315) {
            y = points[0].getY();
        } else if (degrees < 135) {
            y = points[1].getY();
        } else if (degrees < 225) {
            y = points[2].getY();
        } else if (degrees < 315) {
            y = points[3].getY();
        }
    }
    public void FindLowestPoint() {
        LP = 0;
        for (int i = 0; i < points.length; i++) {
            if (i == 0) {
                lowestPoint.setPoint(points[i].getX(), points[i].getY());
            } else if (lowestPoint.getY() > points[i].getY()) {
                lowestPoint.setPoint(points[i].getX(), points[i].getY());
                LP = i;
            }
        }
    }
    public void FindSurfaceY(Box[] boxes) {
        int newSurfaceLandingY = originPosY;

        for (Box box : boxes) {
            if (x + width > box.getX() && x < box.getX() + box.getWidth()) {
                if (box.getY() + box.getHeight() > newSurfaceLandingY &&
                    box.getY() + box.getHeight() < lowestPoint.getY()) {
                    newSurfaceLandingY = box.getY() + box.getHeight();
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
            Shape shape = new Rect(false,8,8);
            shape.setShape(GameData.getInstance().getScreenWidth()/2, y + CreateYHeight());
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
}
