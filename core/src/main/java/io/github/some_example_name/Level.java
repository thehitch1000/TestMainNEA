package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.ArrayList;

import static com.badlogic.gdx.Gdx.input;

public class Level {
    Player player;
    Monster monster;
    ArrayList<Obstacle> obstacles;

    public Level() {
        player = new Player();
        monster = new Monster();

        obstacles = new ArrayList<>();
    }



    public void AddObstacle(Obstacle obstacle) {
        if (obstacle instanceof Box) {

        } else if (obstacle instanceof Spike) {

        }
    }


    public void CheckSlowDown() {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            GameData.getInstance().setSpeedMulti(0.5f);
        } else {
            GameData.getInstance().setDefaultSpeeds();
        }
    }

    public void CheckJumping(){
        if (input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (player.getState() == Player.State.IDLE) {
                player.setState(Player.State.JUMPING);
                GameData.getInstance().setPlayerSpeedY(30);
            }
        }
    }
    public void CheckTipping() {
        if (player.getState() == Player.State.FALLING || player.getState() == Player.State.JUMPING) {
            if (player.lowestPoint.getY() + GameData.getInstance().getPlayerSpeedY() <= player.getSurfaceLandingY()) {
                player.HandleLanding();
                GameData.getInstance().setPlayerSpeedY(0);
                int angle = player.FindAngleTillFlat();
                if (angle > 45) {
                    angle = angle - 90;
                }
                if (angle > 0) {
                    player.setClockwise(true);
                } else {
                    player.setClockwise(false);
                }
            }
        }
    }
    public void CheckingFalling() {
        if (player.getState() == Player.State.IDLE) {
            for (Obstacle obstacle : obstacles) {
                if (obstacle instanceof Box) {
                    Rect rect = (Rect) obstacle.shape;
                    if (rect.onScreen()) {
                        if (((player.shape.points[player.getBL() + 1].getX() / 2f) + 4f >= rect.getX() + rect.getWidth()
                            && (player.shape.points[player.getBL() + 1].getX() / 2f) - 4f <= rect.getX() + rect.getWidth())
                            && player.lowestPoint.getY() == rect.getY() + rect.getHeight()) {
                            player.shape.Rotate(-27, player.midPoint);
                            player.setState(Player.State.FALLING);
                        }
                    }
                }
            }
        }
    }
    public void CheckPlayerMovement() {
        CheckJumping();
        CheckingFalling();
        CheckTipping();
    }

    public void Falling() {
        if (player.getState() == Player.State.FALLING) {
            GameData.getInstance().setPlayerSpeedY(GameData.getInstance().getPlayerSpeedY() - 3);
            player.shape.Rotate(-9, player.midPoint);
        }
    }
    public void Tipping() {
        if (player.getState() == Player.State.TIPPING) {
            if (player.FindAngleTillFlat() == 0) {
                player.setState(Player.State.IDLE);
                player.setTargetTime(GameData.getInstance().getElapsedTime() + 200);
            } else if (player.isClockwise()) {
                player.shape.Rotate(9, player.lowestPoint);
            } else {
                player.shape.Rotate(-9, player.lowestPoint);
            }
        }
    }
    public void Jumping() {
        if (player.getState() == Player.State.JUMPING) {
            GameData.getInstance().setPlayerSpeedY(GameData.getInstance().getPlayerSpeedY() - 3);
            player.shape.Rotate(-9, player.midPoint);
        }
    }
    public void PlayerMovement(){
        Falling();
        Tipping();
        Jumping();
    }

    public void PlayerMaintenance() {
        CheckSlowDown();
        CheckPlayerMovement();
        PlayerMovement();
        player.setXDistance(0);
        player.FindXPoint();
        player.FindYPoint();
        player.FindLowestPoint();
        player.CalcMidPoints();
        player.FindSurfaceY(obstacles);
        player.CheckTrail();
    }

    public boolean PlayerSpikeCollision(Tri tri) {
        for (FloatPoint point : tri.points) {
            if (player.shape.isPointInShape(point)) {
                return true;
            }
        }
        for (FloatPoint point : player.shape.points) {
            if (tri.isPointInShape(point)) {
                return true;
            }
        }
        return false;
    }
    public boolean PlayerBoxCollision(Rect rect) {
        for (FloatPoint point : rect.points) {
            if (player.shape.isPointInShape(point)) {
                return true;
            }
        }
        for (FloatPoint point : player.shape.points) {
            if (rect.isPointInShape(point)) {
                return true;
            }
        }
        return false;
    }
    public void ObstacleCollision() {
        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof Box) {
                if (PlayerBoxCollision((Rect) obstacle.shape)) {
                    BoxRespawn();
                }
            } else if (obstacle instanceof Spike) {
                if (PlayerSpikeCollision((Tri) obstacle.shape)) {
                    SpikeRespawn();
                }
            }
        }
    }
}
