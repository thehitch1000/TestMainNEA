package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.io.FileWriter;
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
        try (FileWriter writer = new FileWriter("obstacles.txt", true)) {
            if (obstacle instanceof Box) {
                Rect rect = (Rect) obstacle.shape;
                writer.write("Box: " + rect.getX() + " " + rect.getY() + " " + rect.getWidth() + " " + rect.getHeight());
                writer.write("\n");
            } else if (obstacle instanceof Spike) {
                Tri tri = (Tri) obstacle.shape;
                writer.write("Spike: " + tri.points[0].getX() + " " + tri.points[0].getY() + " " +
                    tri.points[1].getX() + " " + tri.points[1].getY() + " " +
                    tri.points[2].getX() + " " + tri.points[2].getY());
                writer.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void MoveObstaclesX(float X) {
        for (Obstacle obstacle : obstacles) {
            obstacle.MoveX(X);
        }
    }
    public void MoveObstaclesY(float Y) {
        for (Obstacle obstacle : obstacles) {
            obstacle.MoveY(Y);
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
                            player.Rotate(-27, player.midPoint);
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
            player.Rotate(-9, player.midPoint);
        }
    }
    public void Tipping() {
        if (player.getState() == Player.State.TIPPING) {
            if (player.FindAngleTillFlat() == 0) {
                player.setState(Player.State.IDLE);
            } else if (player.isClockwise()) {
                player.Rotate(9, player.lowestPoint);
            } else {
                player.Rotate(-9, player.lowestPoint);
            }
        }
    }
    public void Jumping() {
        if (player.getState() == Player.State.JUMPING) {
            GameData.getInstance().setPlayerSpeedY(GameData.getInstance().getPlayerSpeedY() - 3);
            player.Rotate(-9, player.midPoint);
        }
    }
    public void PlayerMovement(){
        Falling();
        Tipping();
        Jumping();
        player.MoveY(GameData.getInstance().getPlayerSpeedY());
    }

    public void PlayerMaintenance() {
        CheckSlowDown();
        CheckPlayerMovement();
        PlayerMovement();
        player.setXDistance(0);
        player.FindLowestPoint();
        player.FindBottomLeft();
        player.CalcMidPoints();
        player.FindSurfaceY(obstacles);
//        player.FindXPoint();
//        player.FindYPoint();
//        player.CheckTrail();
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
                    PlayerRespawn(obstacle);
                    break;
                }
            } else if (obstacle instanceof Spike) {
                if (PlayerSpikeCollision((Tri) obstacle.shape)) {
                    PlayerRespawn(obstacle);
                    break;
                }
            }
        }
    }

    public void PlayerRespawn(Obstacle obstacle) {
        if (player.getState() != Player.State.RESPAWNING) {
            player.setState(Player.State.RESPAWNING);
            GameData.getInstance().setStop(true);
            GameData.getInstance().setAllSpeedsToZero();
            GameData.getInstance().timers.runAfter(0.5f, () -> Respawning(obstacle));
            GameData.getInstance().timers.runAfter(1f, () -> EndRespawning());
        }
    }
    public void Respawning(Obstacle obstacle) {
        if (obstacle instanceof Spike) {
            Tri tri = (Tri) obstacle.shape;
            Polygon poly = (Polygon) player.shape;
            MoveObstaclesX(-40);
            if (poly.points[player.getBL()].getY() == 200) {
                poly.points[player.getBL()].setPoint(player.getOriginPosX(), 200);
                poly.points[(player.getBL() + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), 200);
                poly.points[(player.getBL() + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), 200 + player.getWidth());
                poly.points[(player.getBL() + 3) % 4].setPoint(player.getOriginPosX(), 200 + player.getWidth());
//                poly.setX(player.getOriginPosX());
//                poly.setY(200);
            } else {
                poly.points[player.getBL()].setPoint(player.getOriginPosX(), player.getSurfaceLandingY());
                poly.points[(player.getBL() + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), player.getSurfaceLandingY());
                poly.points[(player.getBL() + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), player.getSurfaceLandingY() + player.getWidth());
                poly.points[(player.getBL() + 3) % 4].setPoint(player.getOriginPosX(), player.getSurfaceLandingY() + player.getWidth());
//                player.poly.setX(player.getOriginPosX());
//                player.poly.setY(player.getSurfaceLandingY());
            }
//            if (TriCheckRespawn()) {
//                GameData.getInstance().setDefaultSpeeds();
//            }
        } else if (obstacle instanceof Box) {
            Rect rect = (Rect) obstacle.shape;
            Polygon poly = (Polygon) player.shape;
            MoveObstaclesX(-30);
            poly.points[player.getBL()].setPoint(player.getOriginPosX(), rect.getY() + rect.getHeight());
            poly.points[(player.getBL() + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), rect.getY() + rect.getHeight());
            poly.points[(player.getBL() + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), rect.getY() + rect.getHeight() + player.getWidth());
            poly.points[(player.getBL() + 3) % 4].setPoint(player.getOriginPosX(), rect.getY() + rect.getHeight() + player.getWidth());
//            player.poly.setX(player.getOriginPosX());
//            player.poly.setY(rect.getY() + rect.getHeight());
//            if (TriCheckRespawn()) {
//                GameData.getInstance().setDefaultSpeeds();
//            }
        }
    }
//    public boolean TriCheckRespawn() {
//        while (ObstacleCollision()) {
//            MoveObstaclesX(-40);
//        }
//        return true;
//    }

    public void EndRespawning() {
        player.setState(Player.State.IDLE);
        GameData.getInstance().setDefaultSpeeds();
        PlayerMaintenance();
        GameData.getInstance().setStop(false);
    }

    public void Draw(ShapeRenderer sr) {
        player.Draw(sr);
    }
}
