package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.input;

public class Level {
    Entity player, monster;
    ArrayList<Obstacle> obstacles;

    private float xTravelled;
    private boolean ended;
    private int shapeNumber = 0;

    public Level() {
        player = new Player(100);
        monster = new Monster(100);

        obstacles = new ArrayList<>();

        this.ended = false;
        this.xTravelled = 0;
        this.shapeNumber = 0;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public void CreateLevel() {
        for (int i = 0; i < 5; i++) {
            AddObstacle(new Box(1800 + (i * 800), 200, 200, 40));
        }
    }

    public void ReadFile() {
        try {
            File file = new File("obstacles.txt");
            List<String> lines = Files.readAllLines(file.toPath());

            if (lines.isEmpty() || shapeNumber >= lines.size()) {
                ended = true;
                return;
            }

            String line = lines.get(shapeNumber);
            String[] parts = line.split(":\\s+|\\s+");  // Split by ": " or whitespace

            float x1 = Float.parseFloat(parts[1]);
            switch (parts[0]) {
                case "Box":
                    if (x1 - xTravelled < 1500) {
                        Obstacle box = new Box(x1 - xTravelled, Float.parseFloat(parts[2]), Float.parseFloat(parts[3]), Float.parseFloat(parts[4]));
                        obstacles.add(box);
                        shapeNumber++;
                    } else {
                        ended = true;
                    }
                    break;
                case "Triangle":
                    float x3 = Float.parseFloat(parts[3]);
                    float x5 = Float.parseFloat(parts[5]);
                    if (x1 - xTravelled < 1500 || x3 - xTravelled < 1500 || x5 - xTravelled < 1500) {
                        Obstacle spike = new Spike(new FloatPoint(x1 - xTravelled, Float.parseFloat(parts[2])),
                                                   new FloatPoint(x3 - xTravelled, Float.parseFloat(parts[4])),
                                                   new FloatPoint(x5 - xTravelled, Float.parseFloat(parts[6])));
                        obstacles.add(spike);
                        shapeNumber++;
                    } else {
                        ended = true;
                    }
                    break;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void CheckSlowDown() {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            GameData.getInstance().setSpeedMulti(0.5f);
        } else {
            GameData.getInstance().setDefaultSpeeds();
        }
    }

    public void Update() {
        player.EntityUpdate(obstacles);
    }
    public void Checking() {
        CheckObstacleCollision();
        CheckPlayerMovement();
        CheckSlowDown();
        CheckBulletContact();
        player.CheckTrail();
    }
    public void Move() {
        MoveAmmo();
        PlayerMovement();
        MoveObstaclesX(GameData.getInstance().getObstacleSpeed());
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
        if (player.shape.isPointInShape(new FloatPoint(rect.getX(), rect.getY() + rect.getHeight()))) return true;
        if (player.shape.isPointInShape(new FloatPoint(rect.getX(), rect.getY()))) return true;
        if (rect.getX() <= player.shape.points[player.BL].getX() + player.width && rect.getX() >= player.shape.points[player.BL].getX() + (player.width / 2)) {
            if (player.shape.points[player.BL].getY() <= rect.getY() + rect.getHeight() && player.shape.points[player.BL].getY() + player.width >= rect.getY()) {
                return true;
            }
        }
        return false;
    }

    public void CheckObstacleCollision() {
        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof Box) {
                if (PlayerBoxCollision((Rect) obstacle.shape)) {
                    PlayerRespawn(obstacle);
                }
            } else if (obstacle instanceof Spike) {
                if (PlayerSpikeCollision((Tri) obstacle.shape)) {
                    PlayerRespawn(obstacle);
                }
            }
        }
    }
    public void CheckJumping(Player player){
        if (input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (player.getState() == Player.State.IDLE) {
                player.setState(Player.State.JUMPING);
                GameData.getInstance().setPlayerSpeedY(30);
            }
        }
    }
    public void CheckTipping(Player player) {
        if (player.getState() == Player.State.FALLING || player.getState() == Player.State.JUMPING) {
            if (player.lowestPoint.getY() + GameData.getInstance().getPlayerSpeedY() <= player.getSurfaceLandingY()) {
                player.MoveY(player.getSurfaceLandingY() - player.lowestPoint.getY());
                player.setState(Player.State.TIPPING);
                GameData.getInstance().setPlayerSpeedY(0);
                int angle = (int) player.getAngleTillFlat();
                if (angle > 45) {
                    player.setClockwise(true);
                } else {
                    player.setClockwise(false);
                }
            }
        }
    }
    public void CheckingFalling(Player player) {
        if (player.getState() == Player.State.IDLE) {
            for (Obstacle obstacle : obstacles) {
                if (obstacle instanceof Box) {
                    Rect rect = (Rect) obstacle.shape;
                    if (rect.onScreen()) {
                        if (((player.shape.points[player.BL].getX() + player.getWidth() / 2f) + 4f >= rect.getX() + rect.getWidth()
                            && (player.shape.points[player.BL].getX() + player.getWidth() / 2f) - 4f <= rect.getX() + rect.getWidth())
                            && Math.abs(player.lowestPoint.getY() - rect.getY() - rect.getHeight()) <= 0.1) {
                            player.Rotate(-27, player.midPoint);
                            player.setState(Player.State.FALLING);

                        }
                    }
                }
            }
        }
    }
    public void CheckFiring(Player player) {
        if (input.isButtonJustPressed(Input.Buttons.LEFT)) {
            System.out.println(GameData.getInstance().getElapsedTime());
            if (GameData.getInstance().getElapsedTime() >= player.getCoolDownEndTime()) {
                FloatPoint mouse = new FloatPoint(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
                Bullet bullet = new Bullet(3, player.midPoint, mouse);
                bullet.setBullet(player.midPoint);
                player.ammo.add(bullet);
                player.setCoolDownEndTime(GameData.getInstance().getElapsedTime() + 150);
            }
        }
    }
    public void CheckPlayerMovement() {
        Player player = (Player) this.player;
        CheckJumping(player);
        CheckTipping(player);
        CheckingFalling(player);
        CheckFiring(player);
    }

    public void Falling(Player player) {
        if (player.getState() == Player.State.FALLING) {
            GameData.getInstance().setPlayerSpeedY(GameData.getInstance().getPlayerSpeedY() - 3);
            player.Rotate(-9, player.midPoint);
        }
    }
    public void Tipping(Player player) {
        if (player.getState() == Player.State.TIPPING) {
            if (player.getAngleTillFlat() == 0) {
                player.setState(Player.State.IDLE);
                player.CorrectPoints();
            } else if (player.isClockwise()) {
                player.Rotate(9, player.lowestPoint);
            } else {
                player.Rotate(-9, player.lowestPoint);
            }
        }
    }
    public void Jumping(Player player) {
        if (player.getState() == Player.State.JUMPING) {
            GameData.getInstance().setPlayerSpeedY(GameData.getInstance().getPlayerSpeedY() - 3);
            player.Rotate(-9, player.midPoint);
        }
    }
    public void PlayerMovement() {
        Player player = (Player) this.player;
        Falling(player);
        Tipping(player);
        Jumping(player);
        player.MoveY(GameData.getInstance().getPlayerSpeedY());
    }

    public void MoveAmmo() {
        for (Ammo ammo : player.ammo) {
            ammo.MoveAlongPath();
        }
    }
    public void CheckBulletContact() {
        for (int i = 0; i < player.ammo.size(); i++) {
            if (CheckAmmoCollision(player.ammo.get(i), monster.shape)) {
                player.ammo.remove(i);
                i--;
            }
        }
    }
    public boolean CheckAmmoCollision(Ammo ammo, Shape shape) {
        if (ammo instanceof Bullet) {
            Bullet bullet = (Bullet) ammo;
            if (bullet.shape.points[0].getX() == bullet.endPoint.getX() && bullet.shape.points[0].getY() == bullet.endPoint.getY()) {
                return true;
            }
            if (!ammo.shape.onScreen()) {
                return true;
            }
            for (FloatPoint point : shape.points) {
                if (ammo.shape.isPointInShape(point)) {
                    return true;
                }
            }
            for (FloatPoint point : ammo.shape.points) {
                if (shape.isPointInShape(point)) {
                    return true;
                }
            }
        }
        return false;
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
        xTravelled -= X;
    }

    public void PlayerRespawn(Obstacle obstacle) {
        Player player = (Player) this.player;
        if (player.getState() != Player.State.RESPAWNING) {
            player.setState(Player.State.RESPAWNING);
            GameData.getInstance().setStop(true);
            GameData.getInstance().setAllSpeedsToZero();
            GameData.getInstance().timers.runAfter(0.5f, () -> Respawning(obstacle, player));
            GameData.getInstance().timers.runAfter(1f, () -> EndRespawning(player));
        }
    }
    public void Respawning(Obstacle obstacle, Player player) {
        if (obstacle instanceof Spike) {
            Polygon poly = (Polygon) player.shape;
            MoveObstaclesX(-40);
            if (Math.abs(poly.points[player.BL].getY() - 200f) <= 0.5f) {
                poly.points[player.BL].setPoint(player.getOriginPosX(), 200);
                poly.points[(player.BL + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), 200);
                poly.points[(player.BL + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), 200 + player.getWidth());
                poly.points[(player.BL + 3) % 4].setPoint(player.getOriginPosX(), 200 + player.getWidth());
            } else {
                poly.points[player.BL].setPoint(player.getOriginPosX(), player.getSurfaceLandingY());
                poly.points[(player.BL + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), player.getSurfaceLandingY());
                poly.points[(player.BL + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), player.getSurfaceLandingY() + player.getWidth());
                poly.points[(player.BL + 3) % 4].setPoint(player.getOriginPosX(), player.getSurfaceLandingY() + player.getWidth());
            }

            if (TriCheckRespawn()) {
                GameData.getInstance().setDefaultSpeeds();
            }
        } else if (obstacle instanceof Box) {
            Rect rect = (Rect) obstacle.shape;
            Polygon poly = (Polygon) player.shape;
            MoveObstaclesX(-30);
            poly.points[player.BL].setPoint(player.getOriginPosX(), rect.getY() + rect.getHeight());
            poly.points[(player.BL + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), rect.getY() + rect.getHeight());
            poly.points[(player.BL + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), rect.getY() + rect.getHeight() + player.getWidth());
            poly.points[(player.BL + 3) % 4].setPoint(player.getOriginPosX(), rect.getY() + rect.getHeight() + player.getWidth());
            if (TriCheckRespawn()) {
                GameData.getInstance().setDefaultSpeeds();
            }
        }
        player.CorrectPoints();
        player.EqualPoints();
        player.EntityUpdate(obstacles);
    }
    public boolean TriCheckRespawn() {
        while (TriCheck()) {
            MoveObstaclesX(-40);
        }
        return true;
    }
    public boolean TriCheck() {
        for (Obstacle obstacle : obstacles) {
            if (obstacle instanceof Spike) {
                for (FloatPoint point : player.shape.points) {
                    if (obstacle.shape.isPointInShape(point)) return true;
                }
                for (FloatPoint point : obstacle.shape.points) {
                    if (player.shape.isPointInShape(point)) return true;
                }
            }
        }

        return false;
    }
    public void EndRespawning(Player player) {
        player.setState(Player.State.IDLE);
        GameData.getInstance().setDefaultSpeeds();
        GameData.getInstance().setStop(false);
    }






    public void MoveObstaclesY(float Y) {
        for (Obstacle obstacle : obstacles) {
            obstacle.MoveY(Y);
        }
    }

}
