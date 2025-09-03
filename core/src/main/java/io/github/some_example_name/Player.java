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

    private int AngleTillFlat, LP, startingHealth, BL;
    private float surfaceLandingY, coolDownEndTime, originPosX, originPosY, currentHealth, width, totalAngle;
    private boolean Clockwise;
    List<Ammo> ammo;
    List<Rect> normalTrail;
    List<Polygon> zigTrail;
    FloatPoint[] upPoints, downPoints;
    Direction direction;
    State state;
    FloatPoint lowestPoint, midPoint;
    Shape healthShape, shape;

    public Player(int startingHealth) {
        this.startingHealth = startingHealth;
        this.AngleTillFlat = 0;
        this.BL = 0;
        this.LP = 0;
        this.totalAngle = 0;
        this.originPosX = GameData.getInstance().getScreenWidth() / 2f - width / 2;
        this.originPosY = 200;

        this.direction = Direction.NULL;
        this.state = Player.State.NULL;

        this.surfaceLandingY = 0;
        this.currentHealth = startingHealth;
        this.width = 40;

        ammo = new ArrayList<>();
        normalTrail = new ArrayList<>();

        lowestPoint = new FloatPoint(0, 0);
        midPoint = new FloatPoint(0, 0);

        healthShape = new Polygon(4, Color.WHITE);
        shape = new Polygon(4, Color.WHITE, 0.5f);

        downPoints = new FloatPoint[4];
        for(int i = 0; i < downPoints.length; i++) {
            downPoints[i] = new FloatPoint(0, 0);
        }
        upPoints = new FloatPoint[4];
        for(int i = 0; i < upPoints.length; i++) {
            upPoints[i] = new FloatPoint(0, 0);
        }
    }

    public Player.State getState() {
        return state;
    }
    public void setState(Player.State state) {
        this.state = state;
    }

    public Player.Direction getDirection() {
        return direction;
    }
    public void setDirection(Player.Direction direction) {
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

    public void setDownPoints() {
        Rotate(-45, midPoint);
        for (int i = 0; i < downPoints.length; i++) {
            downPoints[i].setPoint(shape.points[i].getX(), shape.points[i].getY());
        }
        Rotate(45, midPoint);
    }
    public void setUpPoints() {
        Rotate(45, midPoint);
        for (int i = 0; i < upPoints.length; i++) {
            upPoints[i].setPoint(shape.points[i].getX(), shape.points[i].getY());
        }
        Rotate(-45, midPoint);
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
        this.shape.points[0].setPoint(x, y);
        this.shape.points[1].setPoint(x + width, y);
        this.shape.points[2].setPoint(x + width, y + width);
        this.shape.points[3].setPoint(x, y + width);
        for (int i = 0; i < shape.points.length; i++) {
            this.healthShape.points[i].setWholePoint(this.shape.points[i]);
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
        FindLowestPoint();
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
    private void FindLowestPoint() {
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
    private void CalcMidPoints() {
        float x = shape.points[0].getX() + shape.points[2].getX();
        float y = shape.points[0].getY() + shape.points[2].getY();
        midPoint.setPoint(x/2, y/2);
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

    public void ReCalcSolidPoints() {
        if (currentHealth <= 0) {
            Gdx.app.exit();
        }
        float multi = currentHealth / (float) startingHealth;
        healthShape.points[(BL + 2) % 4].setY(shape.points[(BL + 1) % 4].getY() + (width * multi));
        healthShape.points[(BL + 3) % 4].setY(shape.points[BL].getY() + (width * multi));
    }

    public void AddToTrail() {
        if (state == Player.State.IDLE) {
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

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
        healthShape.Draw(sr);
    }

    public void Rotate(float angle, FloatPoint pivot) {
        shape.Rotate(angle, pivot);
        healthShape.Rotate(angle, pivot);
        totalAngle -= angle;
    }

    public void MoveX(float X) {
        shape.MoveX(X);
        healthShape.MoveX(X);
    }
    public void MoveY(float Y) {
        shape.MoveY(Y);
        healthShape.MoveY(Y);
    }

    public void EqualPoints() {
        for (int i = 0; i < shape.points.length; i++) {
            this.healthShape.points[i].setWholePoint(this.shape.points[i]);
        }
        ReCalcSolidPoints();
    }
}
