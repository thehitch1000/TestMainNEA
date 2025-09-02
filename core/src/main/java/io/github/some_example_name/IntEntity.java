package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public interface IntEntity {
    void Draw(ShapeRenderer sr);
    void EntityUpdate();
    void EntityUpdate(ArrayList<Obstacle> obstacles);
    void setStartingPosition(float x, float y);
    void MoveX(float X);
    void MoveY(float Y);
    void Rotate(float angle, FloatPoint pivot);
    void ReCalcSolidPoints();
    void AddToTrail();
    void CheckTrail();
    void EqualPoints();
}

abstract class Entity implements IntEntity {
    public enum State {
        IDLE, JUMPING, FALLING, TIPPING, RESPAWNING, DEAD, AWAKE, SLEEPING, NULL,
    }

    protected int startingHealth, BL;
    protected float currentHealth, width, totalAngle;
    protected List<Ammo> ammo;
    protected List<Rect> trail = new ArrayList<>();
    protected Shape healthShape, shape;
    protected FloatPoint midPoint;
    protected State state;

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
        healthShape.Draw(sr);
    }
    public void EntityUpdate() {}
    public void EntityUpdate(ArrayList<Obstacle> obstacles) {}
    public void setStartingPosition(float x, float y) {}
    public void MoveX(float X) {
        shape.MoveX(X);
        healthShape.MoveX(X);
    }
    public void MoveY(float Y) {
        shape.MoveY(Y);
        healthShape.MoveY(Y);
    }
    public void Rotate(float angle, FloatPoint pivot) {
        shape.Rotate(angle, pivot);
        healthShape.Rotate(angle, pivot);
        totalAngle -= angle;
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
            trail.add(new Rect(GameData.getInstance().getScreenWidth()/2, CreateYHeight(), 8,8, Color.WHITE));
        }
    }
    public int CreateYHeight() {
        return 0;
    }
    public void CheckTrail() {
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
    public State getState() {
        return state;
    }
    public void EqualPoints() {
        for (int i = 0; i < shape.points.length; i++) {
            this.healthShape.points[i].setWholePoint(this.shape.points[i]);
        }
        ReCalcSolidPoints();
    }
}

class Monster extends Entity {


    public Monster(int startingHealth) {
        this.startingHealth = startingHealth;
        this.width = 40;
        this.currentHealth = startingHealth;
        this.totalAngle = 0;

        this.state = State.SLEEPING;

        ammo = new ArrayList<>();

        midPoint = new FloatPoint(0, 0);

        healthShape = new Polygon(4, Color.RED);
        shape = new Polygon(4, Color.RED, 0.5f);
    }

    public void setStartingPosition(float x, float y) {
        this.shape.points[0].setPoint(x, y);
        this.shape.points[1].setPoint(x + width, y);
        this.shape.points[2].setPoint(x + width, y + width);
        this.shape.points[3].setPoint(x, y + width);
        for (int i = 0; i < shape.points.length; i++) {
            this.healthShape.points[i].setWholePoint(shape.points[i]);
        }
    }
    public void EntityUpdate() {}

}

class Player extends Entity {
    public enum Direction {
        UP, DOWN, NULL
    }

    private int AngleTillFlat, LP;
    private float surfaceLandingY, xDistance, coolDownEndTime, originPosX, originPosY;
    private boolean Clockwise;
    FloatPoint lowestPoint;
    Direction direction;

    public Player(int startingHealth) {
        this.startingHealth = startingHealth;
        this.AngleTillFlat = 0;
        this.BL = 0;
        this.LP = 0;
        this.totalAngle = 0;
        this.originPosX = GameData.getInstance().getScreenWidth() / 2f - width / 2;
        this.originPosY = 200;

        this.direction = Direction.NULL;
        this.state = State.IDLE;

        this.surfaceLandingY = 0;
        this.currentHealth = 80;
        this.width = 40;

        ammo = new ArrayList<>();

        lowestPoint = new FloatPoint(0, 0);
        midPoint = new FloatPoint(0, 0);

        healthShape = new Polygon(4, Color.WHITE);
        shape = new Polygon(4, Color.WHITE, 0.5f);
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

    public float getCurrentHealth() {
        return currentHealth;
    }
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public float getCoolDownEndTime() {
        return coolDownEndTime;
    }
    public void setCoolDownEndTime(float CoolDownEndTime) {
        coolDownEndTime = CoolDownEndTime;
    }

    public float getSurfaceLandingY() {
        return surfaceLandingY;
    }
    public float getOriginPosX() {
        return originPosX;
    }
    public float getWidth() {
        return width;
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

    public void EntityUpdate(ArrayList<Obstacle> obstacles) {
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
    private void FindSurfaceY(ArrayList<Obstacle> obstacles) {
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

    @Override
    public int CreateYHeight() {
        return (int) ((Math.random() * (width/2)) + shape.points[BL].getY());
    }
}
