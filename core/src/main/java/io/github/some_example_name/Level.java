package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.input;

public class Level {
    public enum levelStage {
        NORMAL, ZIGZAG
    }
    public enum ScreenHeight {
        TOP, BOTTOM, NEUTRAL, FIXED
    }

    List<Rect> baseRects;
    List<Obstacle> obstacles;
    List<Zone> zones;
    levelStage stage;
    ScreenHeight screenHeight;
    Player player;
    Monster monster;
    Background background;
    LineSegment BaseLine;
    Shape base;
    Drill drill;
    int levelDistance = 3000;

    private float xTravelled, topOfLevel, bottomOfLevel, currentHeight;
    private boolean ended;
    private int shapeNumber;

    public Level() {
        player = new Player(100);
        monster = new Monster();
        background = new Background();
        base = new Rect(0,0, new Color(0.2f, 0.38f, 0.66f, 1f));
        BaseLine = new LineSegment(new FloatPoint(0, 0), new FloatPoint(0, 0));

        obstacles = new ArrayList<>();
        baseRects = new ArrayList<>();

        drill = new Drill();

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

    public void CreateNormalLevel() {
        for (int i = 0; i < 5; i++) {
            AddObstacle(new Box(1800 + (i * 800), 200, 200, 40));
        }
    }
    public void CreateZigZagLevel() {
        while (!drill.isFinished()) {
            if (drill.drillShape.points[0].getX() + xTravelled >= levelDistance && drill.drillShape.points[3].getX() + xTravelled >= levelDistance) {
                drill.setFinished(true);
                drill.FinishPath();
                drill.EndShapes();
                CreateZone();
                TransferShapes();
                drill.StartShapes();
                drill.setDirection(Drill.Direction.RIGHT);
                drill.currentShapes.get(0).setWidth(levelDistance + 900 - drill.currentShapes.get(0).getX() - xTravelled);
                drill.currentShapes.get(1).setWidth(levelDistance + 900 - drill.currentShapes.get(1).getX() - xTravelled);
                CreateZone();
                TransferShapes();
            } else {
                drill.CalcDirection();
                boolean continued = drill.getNewDirection() == drill.getDirection();
                if (!continued) {
                    for (int i = 0; i < drill.oldLines.length; i++) {
                        drill.oldLines[i] = null;
                        drill.oldLines[i] = new LineEquation(
                            drill.lines[i].getGradient(),
                            drill.lines[i].getYIntercept(),
                            drill.lines[i].getDirection()
                        );
                    }
                }
                String key = drill.getDirection() + "," + drill.getNewDirection();
                switch (key) {
                    case "UP_RIGHT,UP_RIGHT":
                        drill.MoveXY(20);
                        break;
                    case "UP_RIGHT,RIGHT":
                        drill.MoveXY(20);
                        drill.RotateDrill(45, drill.drillShape.points[0]);
                        break;
                    case "UP_RIGHT,DOWN_RIGHT":
                        drill.MoveXY(20);
                        drill.RotateDrill(90, drill.drillShape.points[0]);
                        break;
                    case "RIGHT,UP_RIGHT":
                        drill.MoveX(20);
                        drill.RotateDrill(-45, drill.drillShape.points[3]);
                        break;
                    case "RIGHT,RIGHT":
                        drill.MoveX(20);
                        break;
                    case "RIGHT,DOWN_RIGHT":
                        drill.MoveX(20);
                        drill.RotateDrill(45, drill.drillShape.points[0]);
                        break;
                    case "DOWN_RIGHT,UP_RIGHT":
                        drill.MoveXY(20);
                        drill.RotateDrill(-90, drill.drillShape.points[3]);
                        break;
                    case "DOWN_RIGHT,RIGHT":
                        drill.MoveXY(20);
                        drill.RotateDrill(-45, drill.drillShape.points[3]);
                        break;
                    case "DOWN_RIGHT,DOWN_RIGHT":
                        drill.MoveXY(20);
                        break;
                    default:
                        System.out.println("Unknown change in direction: " + key);
                        Gdx.app.exit();
                }
                if (!continued) {
                    // Find and store intersection points
                    drill.FindLines();
                    drill.FindIntersections();

                    // OLD SHAPES
                    drill.EndShapes();

                    if (drill.currentShapes.get(0).getHeight() < 0) {
                        float temp = (drill.currentShapes.get(0).getHeight() * -1) + drill.currentShapes.get(0).getY();
                        if (temp > topOfLevel) {
                            topOfLevel = temp;
                        }
                    }
                    if (drill.currentShapes.get(1) instanceof RectPath) {
                        if (drill.currentShapes.get(1).getHeight() < bottomOfLevel) {
                            bottomOfLevel = drill.currentShapes.get(1).getHeight();
                        }
                    } else if (drill.currentShapes.get(3).getHeight() < 0) {
                        if (drill.currentShapes.get(3).getHeight() < bottomOfLevel) {
                            bottomOfLevel = drill.currentShapes.get(3).getHeight();
                        }
                    }

                    CreateZone();

                    TransferShapes();

                    // NEW SHAPES
                    drill.StartShapes();

                    drill.setOldDirection(drill.getDirection());
                    drill.setDirection(drill.getNewDirection());
                }
            }
        }
    }
    public void TransferShapes() {
        if (drill.currentShapes.size() == 2) {
            AddObstacle(drill.currentShapes.get(0));
            AddObstacle(drill.currentShapes.get(1));
        } else if (drill.currentShapes.get(0).getX() > drill.currentShapes.get(3).getX()) {
            AddObstacle(drill.currentShapes.get(3));
            if (drill.currentShapes.get(1) != null) {
                AddObstacle(drill.currentShapes.get(2));
                AddObstacle(drill.currentShapes.get(1));
            }
            AddObstacle(drill.currentShapes.get(0));
        } else {
            for (int i = 0; i < drill.currentShapes.size(); i++) {
                if (drill.currentShapes.get(i) != null) {
                    AddObstacle(drill.currentShapes.get(i));
                }
            }
        }
        drill.currentShapes.clear();
    }
    public void CreateZone() {
        Zone zone = new Zone(false, drill.getDirection());
        for (int i = 0; i < zone.polygon.points.length; i++) {
            zone.polygon.points[i] = new FloatPoint(0, 0);
        }
        switch (drill.getDirection()) {
            case UP_RIGHT:
            case DOWN_RIGHT:
                zone.polygon.points[0].setWholePoint(drill.currentShapes.get(2).shape.points[0]);
                zone.polygon.points[1].setWholePoint(drill.currentShapes.get(2).shape.points[2]);
                zone.polygon.points[2].setWholePoint(drill.currentShapes.get(1).shape.points[2]);
                zone.polygon.points[3].setWholePoint(drill.currentShapes.get(1).shape.points[0]);
                break;
            case RIGHT:
                zone.polygon.points[0].setPoint(drill.currentShapes.get(1).getX(), drill.currentShapes.get(1).getHeight());
                zone.polygon.points[1].setPoint(drill.currentShapes.get(1).getX() + drill.currentShapes.get(1).getWidth(), drill.currentShapes.get(1).getHeight());
                zone.polygon.points[2].setPoint(drill.currentShapes.get(0).getX() + drill.currentShapes.get(0).getWidth(), drill.currentShapes.get(0).getY());
                zone.polygon.points[3].setPoint(drill.currentShapes.get(0).getX(), drill.currentShapes.get(0).getY());
        }
        AddZone(zone);
    }
    public void AdjustShapesHeights() {
        GameData.getInstance().EmptyFile("tempObstacles.txt");
        GameData.getInstance().CopyFile("obstacles.txt", "tempObstacles.txt");
        GameData.getInstance().EmptyFile("obstacles.txt");

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter("obstacles.txt", false))) {
            File file = new File("tempObstacles.txt");

            List<String> lines = Files.readAllLines(file.toPath());

            for (String line : lines) {
                String[] parts = line.split(":\\s+|\\s+");

                switch (parts[0]) {
                    case "RectPath":
                        if (Boolean.parseBoolean(parts[5])) {
                            fileWriter.write("RectPath: " + parts[1] + " " + (bottomOfLevel - 150) +
                                " " + parts[3] + " " + (Float.parseFloat(parts[4]) - bottomOfLevel + 150) + " " + Boolean.parseBoolean(parts[5]));
                            fileWriter.newLine();
                        } else {
                            fileWriter.write("RectPath: " + Float.parseFloat(parts[1]) + " " + Float.parseFloat(parts[2]) +
                                " " + Float.parseFloat(parts[3]) + " " + (topOfLevel - Float.parseFloat(parts[2]) + 150) +
                                " " + Boolean.parseBoolean(parts[5]));
                            fileWriter.newLine();
                        }
                        break;
                    case "TriPath":
                    case "Zone":
                        fileWriter.write(line);
                        fileWriter.newLine();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                    if (x1 - xTravelled <= 1500) {
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
                    if (x1 - xTravelled <= 1500 || x3 - xTravelled <= 1500 || x5 - xTravelled <= 1500) {
                        Obstacle spike = new Spike(new FloatPoint(x1 - xTravelled, Float.parseFloat(parts[2])),
                                                   new FloatPoint(x3 - xTravelled, Float.parseFloat(parts[4])),
                                                   new FloatPoint(x5 - xTravelled, Float.parseFloat(parts[6])));
                        obstacles.add(spike);
                        shapeNumber++;
                    } else {
                        ended = true;
                    }
                    break;
                case "RectPath":
                    if (x1 - xTravelled <= 1500) {
                        Obstacle rectPath = new RectPath(x1 - xTravelled, Float.parseFloat(parts[2]), Float.parseFloat(parts[3]), Float.parseFloat(parts[4]), Boolean.parseBoolean(parts[5]));
                        obstacles.add(rectPath);
                        shapeNumber++;
                    } else {
                        ended = true;
                    }
                    break;
                case "TriPath":
                    float X3 = Float.parseFloat(parts[3]);
                    float X5 = Float.parseFloat(parts[5]);
                    if (x1 - xTravelled <= 1500 || X3 - xTravelled <= 1500 || X5 - xTravelled <= 1500) {
                        Obstacle TriPath = new TriPath(new FloatPoint(x1 - xTravelled, Float.parseFloat(parts[2])),
                            new FloatPoint(X3 - xTravelled, Float.parseFloat(parts[4])),
                            new FloatPoint(X5 - xTravelled, Float.parseFloat(parts[6])), Boolean.parseBoolean(parts[7]));
                        obstacles.add(TriPath);
                        shapeNumber++;
                    } else {
                        ended = true;
                    }
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
        if (stage == levelStage.NORMAL) {
            player.EntityUpdate(obstacles);
        } else {

        }
    }
    public void Checking() {
        if (stage == levelStage.NORMAL) {
            CheckObstacleCollision();
            CheckPlayerMovement();
            CheckSlowDown();
            CheckBulletContact();
            player.CheckTrail();
        } else {

        }
    }
    public void Move() {
        if (stage == levelStage.NORMAL) {
            MoveAmmo();
            PlayerMovement();
            MoveObstaclesX(GameData.getInstance().getObstacleSpeed());
            MovePlayerY();
        } else {
            MoveWorldX();
//            MovePlayerY();
        }
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
        if (rect.getX() <= player.shape.points[player.getBL()].getX() + player.getWidth() && rect.getX() >= player.shape.points[player.getBL()].getX() + (player.getWidth() / 2)) {
            return player.shape.points[player.getBL()].getY() <= rect.getY() + rect.getHeight() && player.shape.points[player.getBL()].getY() + player.getWidth() >= rect.getY();
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
                player.MoveY(player.getSurfaceLandingY() - player.lowestPoint.getY());
                player.setState(Player.State.TIPPING);
                GameData.getInstance().setPlayerSpeedY(0);
                int angle = (int) player.getAngleTillFlat();
                player.setClockwise(angle > 45);
            }
        }
    }
    public void CheckingFalling() {
        if (player.getState() == Player.State.IDLE) {
            for (Obstacle obstacle : obstacles) {
                if (obstacle instanceof Box) {
                    Rect rect = (Rect) obstacle.shape;
                    if (rect.onScreen()) {
                        if (((player.shape.points[player.getBL()].getX() + player.getWidth() / 2f) + 4f >= rect.getX() + rect.getWidth()
                            && (player.shape.points[player.getBL()].getX() + player.getWidth() / 2f) - 4f <= rect.getX() + rect.getWidth())
                            && Math.abs(player.lowestPoint.getY() - rect.getY() - rect.getHeight()) <= 0.1) {
                            player.Rotate(-27, player.midPoint);
                            player.setState(Player.State.FALLING);

                        }
                    }
                }
            }
        }
    }
    public void CheckFiring() {
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
        CheckJumping();
        CheckTipping();
        CheckingFalling();
        CheckFiring();
    }

    public void Falling() {
        if (player.getState() == Player.State.FALLING) {
            GameData.getInstance().setPlayerSpeedY(GameData.getInstance().getPlayerSpeedY() - 3);
            player.Rotate(-9, player.midPoint);
        }
    }
    public void Tipping() {
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
    public void Jumping() {
        if (player.getState() == Player.State.JUMPING) {
            GameData.getInstance().setPlayerSpeedY(GameData.getInstance().getPlayerSpeedY() - 3);
            player.Rotate(-9, player.midPoint);
        }
    }
    public void PlayerMovement() {
        Falling();
        Tipping();
        Jumping();
    }

    public void MoveWorldX() {
        if (stage == levelStage.ZIGZAG) {
            MoveObstaclesX(-3.5f);
//            player.zigTrail.forEach(trail -> trail.MoveX(-3.5f));
//            player.MoveTempLinesX(-3.5f);
            xTravelled += 3.5f;
            zones.forEach(zone -> zone.MoveX(-3.5f));
        } else {
            MoveObstaclesX(GameData.getInstance().getObstacleSpeed());
        }
    }
    public void MoveWorldY(float Y) {
        if (stage == levelStage.ZIGZAG) {
            currentHeight -= Y;
            MoveObstaclesY(Y);
//            player.MoveTempLinesY(Y);
//            player.zigTrail.forEach(trail -> trail.MoveY(Y));
            zones.forEach(zone -> zone.MoveY(Y));
        }
    }
    public void MovePlayerY() {
        if (stage == levelStage.NORMAL) {
            player.MoveY(GameData.getInstance().getPlayerSpeedY());
        } else {
            float Y = (player.getDirection() == Player.Direction.DOWN) ? -3.5f : 3.5f;
            switch (screenHeight) {
                case FIXED:
                    player.MoveY(Y);
                    break;
                case TOP:
                    player.MoveY(Y);
                    if (player.shape.points[0].getY() <= player.downPoints[0].getY()) {
                        screenHeight = ScreenHeight.NEUTRAL;
                    }
                    break;
                case BOTTOM:
                    player.MoveY(Y);
                    if (player.shape.points[0].getY() >= player.upPoints[0].getY()) {
                        screenHeight = ScreenHeight.NEUTRAL;
                    }
                    break;
                case NEUTRAL:
                    MoveWorldY(-Y);
                    if (currentHeight <= bottomOfLevel) {
                        screenHeight = ScreenHeight.BOTTOM;
                    } else if (currentHeight + 800 >= topOfLevel) {
                        screenHeight = ScreenHeight.TOP;
                    }
                    break;
            }
        }
    }

    public void MoveBackgroundX(float X) {
        background.MoveX(X);
    }
    public void MoveBackgroundY(float Y) {
        background.MoveY(Y);
    }
    public void CheckBackground() {
        if (stage == levelStage.NORMAL) {
            for (Rect rect : baseRects) {
                rect.MoveX(GameData.getInstance().getBackgroundBaseSpeed());
            }
            if (baseRects.get(0).getX() < -baseRects.get(0).getWidth()) {
                baseRects.remove(0);
                baseRects.add(new Rect(1770, 10, 150, 180, new Color(0.12f, 0.28f, 0.51f, 1f)));
            }
        }
        background.MoveX(-GameData.getInstance().getBackgroundSpeed());
        for (int i = background.columns.size() - 1; i >= 0; i--) {
            if (!background.columns.get(i).onScreen()) {
                background.columns.remove(i);
            }
        }
        if (background.columns.size() < 20) {
            background.addColumn();
        }
    }

    public void MoveAmmo() {
        for (Ammo ammo : player.ammo) {
            ammo.MoveAlongPath();
        }
    }
    public void CheckBulletContact()  {
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
            } else if (obstacle instanceof RectPath) {
                Rect rect = (Rect) obstacle.shape;
                RectPath rectPath = (RectPath) obstacle;
                writer.write("RectPath: " + rect.getX() + " " + rect.getY() + " " + rect.getWidth() + " " + rect.getHeight() + " " + rectPath.isBottom());
                writer.write("\n");
            } else if (obstacle instanceof TriPath) {
                Tri tri = (Tri) obstacle.shape;
                TriPath triPath = (TriPath) obstacle;
                writer.write("TriPath: " + tri.points[0].getX() + " " + tri.points[0].getY() + " " +
                    tri.points[1].getX() + " " + tri.points[1].getY() + " " +
                    tri.points[2].getX() + " " + tri.points[2].getY() + " " + triPath.isBottom());
                writer.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void AddZone(Zone zone) {
        try (FileWriter writer = new FileWriter ("obstacles.txt", true)) {
            writer.write("Zone: " + zone.polygon.points[0].getX() + " " + zone.polygon.points[0].getY() + " " +
                zone.polygon.points[1].getX() + " " + zone.polygon.points[1].getY() + " " +
                zone.polygon.points[2].getX() + " " + zone.polygon.points[2].getY() + " " +
                zone.polygon.points[3].getX() + " " + zone.polygon.points[3].getY() + " " +
                zone.getType());
            writer.write("\n");
        } catch (IOException e) {
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
            if (Math.abs(poly.points[player.getBL()].getY() - 200f) <= 0.5f) {
                poly.points[player.getBL()].setPoint(player.getOriginPosX(), 200);
                poly.points[(player.getBL() + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), 200);
                poly.points[(player.getBL() + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), 200 + player.getWidth());
                poly.points[(player.getBL() + 3) % 4].setPoint(player.getOriginPosX(), 200 + player.getWidth());
            } else {
                poly.points[player.getBL()].setPoint(player.getOriginPosX(), player.getSurfaceLandingY());
                poly.points[(player.getBL() + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), player.getSurfaceLandingY());
                poly.points[(player.getBL() + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), player.getSurfaceLandingY() + player.getWidth());
                poly.points[(player.getBL() + 3) % 4].setPoint(player.getOriginPosX(), player.getSurfaceLandingY() + player.getWidth());
            }

            if (TriCheckRespawn()) {
                GameData.getInstance().setDefaultSpeeds();
            }
        } else if (obstacle instanceof Box) {
            Rect rect = (Rect) obstacle.shape;
            Polygon poly = (Polygon) player.shape;
            MoveObstaclesX(-30);
            poly.points[player.getBL()].setPoint(player.getOriginPosX(), rect.getY() + rect.getHeight());
            poly.points[(player.getBL() + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), rect.getY() + rect.getHeight());
            poly.points[(player.getBL() + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), rect.getY() + rect.getHeight() + player.getWidth());
            poly.points[(player.getBL() + 3) % 4].setPoint(player.getOriginPosX(), rect.getY() + rect.getHeight() + player.getWidth());
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

    public void setUpLevel(levelStage stage) {
        if (stage == levelStage.NORMAL) {
            player.setStartingPosition(730,200);
            player.ReCalcSolidPoints();
            player.state = Player.State.IDLE;
            GameData.getInstance().timers.runRepeating(0.5f, 0.1f, () -> player.AddToTrail());
            for (int i = 0; i < 12; i++) {
                baseRects.add(new Rect(160 * i, 10, 150, 180, new Color(0.12f, 0.28f, 0.51f, 1f)));
            }
            BaseLine = new LineSegment(new FloatPoint(0, 200), new FloatPoint(GameData.getInstance().getScreenWidth(), 200));
            base = new Rect(0,0,1500, 200, new Color(0.2f, 0.38f, 0.66f, 1f));
            CreateNormalLevel();
        } else if (stage == levelStage.ZIGZAG) {
            AddObstacle(new RectPath(0, 0, 700, 800, false));

            drill.setNewDirection(drill.directions[(int) Math.ceil(Math.random() * 3) % 3]);

            if (drill.getNewDirection() == Drill.Direction.RIGHT) {
                drill.setDrill(700, 240, 20, drill.CalcHeight());
            } else {
                drill.setDrill(700, 300, 20, drill.CalcHeight());
            }

            switch (drill.getNewDirection()) {
                case UP_RIGHT: // up right
                    drill.RotateDrill(-45, drill.drillShape.points[3]);
                    break;
                case DOWN_RIGHT: // down right
                    drill.RotateDrill(45, drill.drillShape.points[0]);
                    break;
            }

            drill.FindLines();
            drill.FirstStartShapes();
            drill.setDirection(drill.getNewDirection());

            CreateZigZagLevel();
            AdjustShapesHeights();

            player.setDownPoints();
            player.setUpPoints();
        }
        this.stage = stage;

        while(background.columns.size() < 20) {
            background.addColumn();
        }

        while (!ended) {
            ReadFile();
        }
        ended = false;
    }



    public void MoveObstaclesY(float Y) {
        for (Obstacle obstacle : obstacles) {
            obstacle.MoveY(Y);
        }
    }
}
