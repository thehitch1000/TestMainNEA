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
            obstacles.add(obstacle);
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
                    player.setState(Player.State.RESPAWNING);
                    BoxRespawn();
                    break;
                }
            } else if (obstacle instanceof Spike) {
                if (PlayerSpikeCollision((Tri) obstacle.shape)) {
                    player.setState(Player.State.RESPAWNING);
                    SpikeRespawn();
                    break;
                }
            }
        }
    }

    public void BoxRespawn() {

    }
    public void SpikeRespawn() {

    }

    public void InitRespawning(int deathType, int ObstacleNumber) {
        if (player.getState() != Player.State.RESPAWNING) {
            setTypeDeath(deathType);
            if (typeDeath == 1) {
                triNumber = ObstacleNumber;
            } else if (typeDeath == 2) {
                boxNumber = ObstacleNumber;
            }
            GameData.getInstance().setStop(true);
            GameData.getInstance().setAllSpeedsToZero();
        }
    }
    public void Respawning() {
        if (player.isRespawning()) {
            respawnCover.Flashing();
            if (player.MidPoint.getX() >= respawnCover.getElasped()) {
                if (typeDeath == 1) {
                    MoveObstacles(40);
                    int angle = FindPointsOrient();
                    if (player.points[0].getY() == 200 || player.points[2].getY() == 200) {
                        player.points[angle].setPoint(player.getOriginPosX(), 200);
                        player.points[(angle + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), 200);
                        player.points[(angle + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), 200 + player.getHeight());
                        player.points[(angle + 3) % 4].setPoint(player.getOriginPosX(), 200 + player.getHeight());
                        player.setX(player.getOriginPosX());
                        player.setY(200);
                    } else {
                        player.points[angle].setPoint(player.getOriginPosX(), player.getSurfaceLandingY());
                        player.points[(angle + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), player.getSurfaceLandingY());
                        player.points[(angle + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), player.getSurfaceLandingY() + player.getHeight());
                        player.points[(angle + 3) % 4].setPoint(player.getOriginPosX(), player.getSurfaceLandingY() + player.getHeight());
                        player.setX(player.getOriginPosX());
                        player.setY(player.getSurfaceLandingY());
                    }
                    if (TriCheckRespawn()) {
                        setTypeDeath(-1);
                        GameData.getInstance().setDefaultSpeeds();
                    }
                } else if (typeDeath == 2) {
                    MoveObstacles(30);
                    int angle = FindPointsOrient();
                    player.points[angle].setPoint(player.getOriginPosX(), boxes[boxNumber].getY() + boxes[boxNumber].getHeight());
                    player.points[(angle + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), boxes[boxNumber].getY() + boxes[boxNumber].getHeight());
                    player.points[(angle + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), boxes[boxNumber].getY() + boxes[boxNumber].getHeight() + player.getHeight());
                    player.points[(angle + 3) % 4].setPoint(player.getOriginPosX(), boxes[boxNumber].getY() + boxes[boxNumber].getHeight() + player.getHeight());
                    player.setX(player.getOriginPosX());
                    player.setY(boxes[boxNumber].getY() + boxes[boxNumber].getHeight());
                    if (TriCheckRespawn()) {
                        setTypeDeath(-1);
                        GameData.getInstance().setDefaultSpeeds();
                    }
                }
            }
            if (respawnCover.getElasped() >= 1500) {
                respawnCover.setElasped(0);
                player.setRespawning(false);
                GameData.getInstance().setDefaultSpeeds();
                playerMaintenance();
                GameData.getInstance().setStop(false);
            }
        }
    }
    public boolean TriCheckRespawn() {
        while (playerTriCollisions()) {
            MoveObstacles(40);
        }
        return true;
    }
}
