package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.input;

public class Level {
    enum Status {
        WALKABLE, UNWALKABLE
    }
    public enum levelStage {
        NORMAL, ZIGZAG
    }
    public enum ScreenHeight {
        TOP, BOTTOM, NEUTRAL, FIXED
    }

    List<Rect> baseRects;
    List<Obstacle> obstacles;
    List<Zone> zones;
    List<com.badlogic.gdx.math.Polygon> shapes;
    levelStage stage;
    ScreenHeight screenHeight;
    Player player;
    Monster monster;
    Background background;
    LineSegment BaseLine;
    Shape base;
    Drill drill;
    Node[][] grid;
    int levelDistance = 3000;
    int cellSize = 4;

    private float xTravelled, topOfLevel, bottomOfLevel, currentHeight;
    private int shapeNumber;

    public Level() {
        player = new Player(100);
        monster = new Monster();
        background = new Background(20, 0);
        base = new Rect(0, 0, new Color(0.2f, 0.38f, 0.66f, 1f));
        BaseLine = new LineSegment(new FloatPoint(0, 0), new FloatPoint(0, 0));

        obstacles = new ArrayList<>();
        baseRects = new ArrayList<>();
        zones = new ArrayList<>();
        shapes = new ArrayList<>();

        drill = new Drill();

        grid = new Node[GameData.getInstance().getScreenWidth()/cellSize][GameData.getInstance().getScreenHeight()/cellSize];

        this.xTravelled = 0;
        this.shapeNumber = 0;

        topOfLevel = GameData.getInstance().getScreenHeight();
        bottomOfLevel = 0;
        currentHeight = 0;

        screenHeight = ScreenHeight.NEUTRAL;
        stage = null;


    }

    public void CreateNormalLevel() {
        for (int i = 0; i < 5; i++) {
            AddObstacle(new Box(1800 + (i * 800), 200, 200, 40));
        }
    }
    public void CreateZigZagLevel() {
        while (!drill.isFinished()) {
            if (drill.points[0].getX() + xTravelled >= levelDistance && drill.points[3].getX() + xTravelled >= levelDistance) {
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
                        drill.RotateDrill(45, drill.points[0]);
                        break;
                    case "UP_RIGHT,DOWN_RIGHT":
                        drill.MoveXY(20);
                        drill.RotateDrill(90, drill.points[0]);
                        break;
                    case "RIGHT,UP_RIGHT":
                        drill.MoveX(20);
                        drill.RotateDrill(-45, drill.points[3]);
                        break;
                    case "RIGHT,RIGHT":
                        drill.MoveX(20);
                        break;
                    case "RIGHT,DOWN_RIGHT":
                        drill.MoveX(20);
                        drill.RotateDrill(45, drill.points[0]);
                        break;
                    case "DOWN_RIGHT,UP_RIGHT":
                        drill.MoveXY(20);
                        drill.RotateDrill(-90, drill.points[3]);
                        break;
                    case "DOWN_RIGHT,RIGHT":
                        drill.MoveXY(20);
                        drill.RotateDrill(-45, drill.points[3]);
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
        bottomOfLevel -= 50;
        topOfLevel += 50;
    }
    public void TransferShapes() {
        if (drill.currentShapes.size() == 2) {
            if (drill.currentShapes.get(0).getX() < drill.currentShapes.get(1).getX()) {
                AddObstacle(drill.currentShapes.get(0));
                AddObstacle(drill.currentShapes.get(1));
            } else {
                AddObstacle(drill.currentShapes.get(1));
                AddObstacle(drill.currentShapes.get(0));
            }
        } else if (drill.currentShapes.get(0).getX() > drill.currentShapes.get(3).getX()) {
            for (int i = drill.currentShapes.size() - 1; i >= 0; i--) {
                AddObstacle(drill.currentShapes.get(i));
            }
        } else {
            for (int i = 0; i < drill.currentShapes.size(); i++) {
                AddObstacle(drill.currentShapes.get(i));
            }
        }
        drill.currentShapes.clear();
    }
    public void CreateZone() {
        Zone zone;
        if (drill.currentShapes.size() == 2) {
            zone = new Zone(1);
        } else {
            zone = new Zone(0);
        }
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

    public boolean ReadFile() {
        try {
            File file = new File("obstacles.txt");
            List<String> lines = Files.readAllLines(file.toPath());

            if (lines.isEmpty() || shapeNumber >= lines.size()) {
                return false;
            }

            String line = lines.get(shapeNumber);
            String[] parts = line.split(":\\s+|\\s+");  // Split by ":" or whitespace

            float x1 = Float.parseFloat(parts[1]);

            switch (parts[0]) {
                case "Box":
                    if (x1 - xTravelled <= 1700) {
                        Obstacle box = new Box(x1 - xTravelled, Float.parseFloat(parts[2]), Float.parseFloat(parts[3]), Float.parseFloat(parts[4]));
                        obstacles.add(box);
                        shapeNumber++;
                        return true;
                    } else {
                        return false;
                    }

                case "Spike":
                    float x3 = Float.parseFloat(parts[3]);
                    float x5 = Float.parseFloat(parts[5]);
                    if (x1 - xTravelled <= 1700 || x3 - xTravelled <= 1700 || x5 - xTravelled <= 1700) {
                        Obstacle spike = new Spike(new FloatPoint(x1 - xTravelled, Float.parseFloat(parts[2])),
                            new FloatPoint(x3 - xTravelled, Float.parseFloat(parts[4])),
                            new FloatPoint(x5 - xTravelled, Float.parseFloat(parts[6])));
                        obstacles.add(spike);
                        shapeNumber++;
                        return true;
                    } else {
                        return false;
                    }

                case "RectPath":
                    if (x1 - xTravelled <= 1700) {
                        Obstacle rectPath = new RectPath(x1 - xTravelled, Float.parseFloat(parts[2]) - currentHeight, Float.parseFloat(parts[3]), Float.parseFloat(parts[4]), Boolean.parseBoolean(parts[5]));
                        obstacles.add(rectPath);
                        shapeNumber++;
                        return true;
                    } else {
                        return false;
                    }

                case "TriPath":
                    float X3 = Float.parseFloat(parts[3]);
                    float X5 = Float.parseFloat(parts[5]);
                    if (x1 - xTravelled <= 1700 || X3 - xTravelled <= 1700 || X5 - xTravelled <= 1700) {
                        Obstacle TriPath = new TriPath(new FloatPoint(x1 - xTravelled, Float.parseFloat(parts[2]) - currentHeight),
                                                       new FloatPoint(X3 - xTravelled, Float.parseFloat(parts[4]) - currentHeight),
                                                       new FloatPoint(X5 - xTravelled, Float.parseFloat(parts[6]) - currentHeight), Boolean.parseBoolean(parts[7]));
                        obstacles.add(TriPath);
                        shapeNumber++;
                        return true;
                    } else {
                        return false;
                    }

                case "Zone":
                    float x4 = Float.parseFloat(parts[7]);
                    if (x1 - xTravelled <= 1700 || x4 - xTravelled <= 1700) {
                        Zone zone = new Zone(Integer.parseInt(parts[9]));
                        zone.polygon.points[0].setPoint(x1 - xTravelled, Float.parseFloat(parts[2]) - currentHeight);
                        zone.polygon.points[1].setPoint(Float.parseFloat(parts[3]) - xTravelled, Float.parseFloat(parts[4]) - currentHeight);
                        zone.polygon.points[2].setPoint(Float.parseFloat(parts[5]) - xTravelled, Float.parseFloat(parts[6]) - currentHeight);
                        zone.polygon.points[3].setPoint(x4 - xTravelled, Float.parseFloat(parts[8]) - currentHeight);
                        zones.add(zone);
                        shapeNumber++;
                    }
                    break;

                default:
                    System.out.println("Unknown obstacle type: " + parts[0]);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return false;
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
        }
    }
    public void Checking() {
        if (stage == levelStage.NORMAL) {
            CheckObstacleCollision();
            CheckPlayerMovement();
            CheckSlowDown();
            CheckBulletContact();
            player.CheckTrail();
        }
    }
    public void Move() {
        if (stage == levelStage.NORMAL) {
            MoveAmmo();
            PlayerMovement();
            MoveObstaclesX(GameData.getInstance().getObstacleSpeed());
            MovePlayerY(0);
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
    public boolean PlayerRectPathCollision(Rect rect) {
        for (FloatPoint point : player.shape.points) {
            if (rect.isPointInShape(point)) {
                return true;
            }
        }
        return false;
    }
    public boolean PlayerTriPathCollision(Tri tri) {
        for (FloatPoint point : player.shape.points) {
            if (tri.isPointInShape(point)) {
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
            } else if (obstacle instanceof RectPath) {
                if (PlayerRectPathCollision((Rect) obstacle.shape)) {
                    PlayerRespawn(obstacle);
                }
            } else if (obstacle instanceof TriPath) {
                if (PlayerTriPathCollision((Tri) obstacle.shape)) {
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
            if (GameData.getInstance().getElapsedTime() >= player.getCoolDownEndTime()) {
                FloatPoint mouse = new FloatPoint(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
                Bullet bullet = new Bullet(3, new FloatPoint(player.midPoint.getX(), player.midPoint.getY()), mouse);
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
            obstacles.forEach(obstacle -> obstacle.MoveX(-3.5f));
            player.zigTrail.forEach(trail -> trail.MoveX(-3.5f));
            player.MoveTempLinesX(-3.5f);
            xTravelled += 3.5f;
            zones.forEach(zone -> zone.MoveX(-3.5f));
        } else {
            obstacles.forEach(obstacle -> obstacle.MoveX(-GameData.getInstance().getObstacleSpeed()));
        }
    }
    public void MoveWorldY(float Y) {
        if (stage == levelStage.ZIGZAG) {
            currentHeight -= Y;
            player.MoveTempLinesY(Y);
            obstacles.forEach(obstacle -> obstacle.MoveY(Y));
            player.zigTrail.forEach(trail -> trail.MoveY(Y));
            zones.forEach(zone -> zone.MoveY(Y));
            MoveBackgroundY(Y/10);
        }
    }
    public void MovePlayerY(float Y) {
        if (stage == levelStage.NORMAL) {
            player.MoveY(GameData.getInstance().getPlayerSpeedY());
        } else {
            if (Y == 0) Y = (player.getDirection() == Player.Direction.DOWN) ? -3.5f : 3.5f;
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
        MoveBackgroundX(-GameData.getInstance().getBackgroundSpeed());
        for (int i = background.columns.size() - 1; i >= 0; i--) {
            if (!background.columns.get(i).onScreen()) {
                background.columns.remove(i);
            }
        }
        if (background.columns.size() < 20) {
            background.addColumn();
        }
    }

    public void CheckChangePlayerDirection() {
        if (input.isKeyJustPressed(Input.Keys.SPACE)) {
            ChangeDirection();
        }
    }
    public void ChangeDirection() {
        player.tempLines[0].setLine(player.lines[0].getGradient(), player.lines[0].getYIntercept());
        player.tempLines[1].setLine(player.lines[1].getGradient(), player.lines[1].getYIntercept());
        player.CalcMidPoints();
        if (player.getDirection() == Player.Direction.DOWN) {
            player.Rotate(90, player.midPoint);
            player.setDirection(Player.Direction.UP);
        } else {
            player.Rotate(-90, player.midPoint);
            player.setDirection(Player.Direction.DOWN);
        }
        player.CalcMidPoints();
        player.CreateTrail();
    }
    public void CheckDisplay() {
        for (polygon polygon : player.zigTrail) {
            if (!polygon.onScreen()) {
                player.zigTrail.remove(polygon);
                return;
            }
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
        if (obstacle instanceof Box || obstacle instanceof Spike) {
            player.setState(Player.State.RESPAWNING);
            GameData.getInstance().setStop(true);
            GameData.getInstance().setAllSpeedsToZero();
            GameData.getInstance().timers.runAfter(0.5f, () -> NormalRespawning(obstacle));
            GameData.getInstance().timers.runAfter(1f, () -> EndNormalRespawning());
        } else {
            GameData.getInstance().setStop(true);
            GameData.getInstance().timers.runAfter(0, () -> ZigZagRespawning(obstacle));
            GameData.getInstance().timers.runAfter(1f, () -> ZigZagEndRespawning());
        }
    }
    public void NormalRespawning(Obstacle obstacle) {
        if (obstacle instanceof Spike) {
            polygon poly = (polygon) player.shape;
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
            polygon poly = (polygon) player.shape;
            MoveObstaclesX(-30);
            poly.points[player.getBL()].setPoint(player.getOriginPosX(), rect.getY() + rect.getHeight());
            poly.points[(player.getBL() + 1) % 4].setPoint(player.getOriginPosX() + player.getWidth(), rect.getY() + rect.getHeight());
            poly.points[(player.getBL() + 2) % 4].setPoint(player.getOriginPosX() + player.getWidth(), rect.getY() + rect.getHeight());
            poly.points[(player.getBL() + 3) % 4].setPoint(player.getOriginPosX(), rect.getY() + rect.getHeight());
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
    public void EndNormalRespawning() {
        player.setState(Player.State.IDLE);
        GameData.getInstance().setDefaultSpeeds();
        GameData.getInstance().setStop(false);
    }
    public void ZigZagRespawning(Obstacle obstacle) {
        float move = ((drill.points[3].getY() - drill.points[0].getY()) * 0.5f) / 10f;

        if (obstacle instanceof RectPath) {
            RectPath rectPath = (RectPath) obstacle;

            if (!rectPath.isBottom()) {
                GameData.getInstance().timers.runRepeatingUntil(0, 0.1f, 1f,() -> MovePlayerY(-move));
            } else {
                GameData.getInstance().timers.runRepeatingUntil(0, 0.1f, 1f,() -> MovePlayerY(move));
            }
        } else {
            TriPath triPath = (TriPath) obstacle;
            if (!triPath.isBottom()) {
                GameData.getInstance().timers.runRepeatingUntil(0, 0.1f, 1f,() -> MovePlayerY(-move));
            } else {
                GameData.getInstance().timers.runRepeatingUntil(0, 0.1f, 1f,() -> MovePlayerY(move));
            }
        }
    }
    public void ZigZagEndRespawning() {
        player.CalcMidPoints();
        if (player.getDirection() == Player.Direction.DOWN) {
            player.Rotate(90, player.midPoint);
            player.setDirection(Player.Direction.UP);
        } else {
            player.Rotate(-90, player.midPoint);
            player.setDirection(Player.Direction.DOWN);
        }
        player.CalcMidPoints();
        player.zigTrail.clear();
        player.FirstTrail();
        GameData.getInstance().setStop(false);
    }

    public void setUpLevel(levelStage stage) {
        if (stage == levelStage.NORMAL) {
            player.state = Player.State.IDLE;
            player.setStartingPosition(730,200);
            player.ReCalcSolidPoints();
            GameData.getInstance().timers.runRepeating(0.5f, 0.1f, () -> player.AddToTrail());
            for (int i = 0; i < 12; i++) {
                baseRects.add(new Rect(160 * i, 10, 150, 180, new Color(0.12f, 0.28f, 0.51f, 1f)));
            }
            BaseLine = new LineSegment(new FloatPoint(0, 200), new FloatPoint(GameData.getInstance().getScreenWidth(), 200));
            base = new Rect(0,0,1500, 200, new Color(0.2f, 0.38f, 0.66f, 1f));
            background = new Background(600, BaseLine.startPoint.getY());
            CreateNormalLevel();
        } else if (stage == levelStage.ZIGZAG) {
            AddObstacle(new RectPath(0, bottomOfLevel, 700, topOfLevel - bottomOfLevel, false));

            drill.setNewDirection(drill.directions[(int) Math.ceil(Math.random() * 3) % 3]);

            if (drill.getNewDirection() == Drill.Direction.RIGHT) {
                drill.setDrill(700, 240, 20, drill.CalcHeight());
            } else {
                drill.setDrill(700, 300, 20, drill.CalcHeight());
            }


            switch (drill.getNewDirection()) {
                case UP_RIGHT: // up right
                    drill.RotateDrill(-45, drill.points[3]);
                    break;
                case DOWN_RIGHT: // down right
                    drill.RotateDrill(45, drill.points[0]);
                    break;
            }

            drill.FindLines();
            drill.FirstStartShapes();
            drill.setDirection(drill.getNewDirection());

            if (drill.getDirection() == Drill.Direction.UP_RIGHT) {
                player.setDirection(Player.Direction.UP);
            } else if (drill.getDirection() == Drill.Direction.DOWN_RIGHT) {
                player.setDirection(Player.Direction.DOWN);
            }

            if (player.getDirection() == Player.Direction.UP) {
                player.setStartingPosition(750, drill.points[0].getY() + 30);
            } else {
                player.setStartingPosition(750, drill.points[3].getY() - 30);
            }

            CreateZigZagLevel();
            AdjustShapesHeights();

            player.CalcMidPoints();
            player.setUpPoints();
            player.setDownPoints();
            if (player.getDirection() == Player.Direction.DOWN) {
                player.Rotate(-45, player.midPoint);
            } else {
                player.Rotate(45, player.midPoint);
            }
            player.CalcMidPoints();
            player.CreateLines();
            player.FirstTrail();

            background = new Background(topOfLevel - bottomOfLevel, bottomOfLevel);
        }
        this.stage = stage;

        while(background.columns.size() < 20) {
            background.addColumn();
        }
        while (ReadFile());
    }

    public void CreatePlayerMissile(FloatPoint startPoint, FloatPoint endPoint) {
        Missile missile = new Missile(startPoint, endPoint, 6);

        CheckWalkability();

        CreateMissilePath(missile, missile.startPoint, endPoint);
        player.ammo.add(missile);
    }
    public void CreateMissilePath(Missile missile, FloatPoint startPoint, FloatPoint endPoint) {
        Node[] pathNodes = FindPathNodes(startPoint, endPoint);

        if (pathNodes == null || pathNodes.length < 2) {
            LineSegment[] directPath = new LineSegment[1];
            directPath[0] = new LineSegment(startPoint, endPoint);
            missile.setPath(directPath);
            return;
        }

        LineSegment[] pathSegments = new LineSegment[pathNodes.length - 1];
        int j = 0;

        for (int i = pathNodes.length - 1; i > 0; i--) {
            FloatPoint point1 = new FloatPoint(pathNodes[i].getX(), pathNodes[i].getY());
            FloatPoint point2 = new FloatPoint(pathNodes[i - 1].getX(), pathNodes[i - 1].getY());
            pathSegments[j] = new LineSegment(point1, point2);
            j++;
        }

        missile.setPath(pathSegments);
    }
    public Node[] FindPathNodes(FloatPoint startPoint, FloatPoint endPoint) {
        Node start = new Node ((int) startPoint.getX(), (int) startPoint.getY(), Node.NodeState.WALKABLE);
        Node end = new Node ((int) endPoint.getX(), (int) endPoint.getY(), Node.NodeState.WALKABLE);

        NodePrioQueue openList = new NodePrioQueue(150);
        openList.enqueue(start);

        while (!openList.isEmpty()) {
            Node current = openList.dequeue();

            if (current == end) {
                return ConstructMissilePath(end);
            }

            for (Node neighbour : getNeighbours(current)) {
                if (neighbour != null) {
                    if (neighbour.getState() == Node.NodeState.WALKABLE) {
                        ProcessNeighbour(current, neighbour, end, openList);
                    }
                }
            }
        }
        System.out.println("No path found");
        return null;
    }
    public void TransformShapes() {
        shapes.clear();
        for (Obstacle obstacle : obstacles) {
            if (obstacle.shape instanceof Rect) {
                Rect rect = (Rect) obstacle.shape;
                shapes.add(new Polygon(new float[] {
                    rect.getX(), rect.getY(),
                    rect.getX() + rect.getWidth(), rect.getY(),
                    rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(),
                    rect.getX(), rect.getY() + rect.getHeight()
                }));
            } else {
                Tri tri = (Tri) obstacle.shape;
                shapes.add(new Polygon(new float[] {
                    tri.points[0].getX(), tri.points[0].getY(),
                    tri.points[1].getX(), tri.points[1].getY(),
                    tri.points[2].getX(), tri.points[2].getY()
                }));
            }
        }
    }
    public void CheckWalkability() {
        TransformShapes();
        ResetGrid();
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Node.NodeState tempState = isMissileSafeAt(grid[x][y].getX(), grid[x][y].getY());
                switch (tempState) {
                    case UNWALKABLE:
                    case WALKABLE:
                    case UNKNOWN:
                        grid[x][y].setState(tempState);
                        break;
                    case REMOVE:
                        grid[x][y] = null;
                        break;
                }
            }
        }
    }
    public void ResetGrid() {
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                grid[x][y] = new Node(x, y, Node.NodeState.WALKABLE);
            }
        }
    }
    public Node.NodeState isMissileSafeAt(float x, float y) {
        Circle missile = new Circle(x, y, 10);
        Node.NodeState tempState;
        for (Zone zone : zones) {
            if (zone.isPointInZone(x,y)) {
                for (Polygon shape : shapes) {
                    tempState = missile.overlaps(shape);
                    switch (tempState) {
                        case UNWALKABLE:
                        case REMOVE:
                            return tempState;
                    }
                }
                return Node.NodeState.WALKABLE;
            }
        }
        return Node.NodeState.REMOVE;
    }
    public boolean LineOfSight(Node start, Node end) {
        FloatPoint startPoint = new FloatPoint(start.getX(), start.getY());
        FloatPoint endPoint = new FloatPoint(end.getX(), end.getY());
        LineSegment segment = new LineSegment(startPoint, endPoint);
        segment.setSegment();
        Missile testMissile = new Missile(startPoint, endPoint, 6);
        boolean ended = false;
        Status status = Status.WALKABLE;
        while (!ended) {
            testMissile.MoveX(testMissile.getSpeed() * (float) Math.cos(segment.getAngle()));
            testMissile.MoveY(testMissile.getSpeed() * (float) Math.sin(segment.getAngle()));
            if (!segment.isPointInSegment(new FloatPoint(testMissile.shape.getX(), testMissile.shape.getY()))) {
                ended = true;
            }
            Node.NodeState state = isMissileSafeAt(testMissile.shape.getX(), testMissile.shape.getY());
            switch (state) {
                case UNWALKABLE:
                case REMOVE:
                    status = Status.UNWALKABLE;
                    ended = true;
                    break;
            }
        }
        switch (status) {
            case UNWALKABLE:
                return false;
            case WALKABLE:
                return true;
        }
        return false;
    }
    public Node[] getNeighbours(Node currentNode) {
        Node[] Neighbours = new Node[8];
        int i = 0;
        int X = NodeFindXCoordinates(currentNode);
        int Y = NodeFindYCoordinates(currentNode);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                if (x == 0 && y == 0) continue;
                Neighbours[i] = grid[X + x][Y + y];
                i++;
            }
        }
        return Neighbours;
    }
    public int NodeFindXCoordinates(Node node) {
        return (node.getX() - 4) / 8;
    }
    public int NodeFindYCoordinates(Node node) {
        return (node.getY() - 4) / 8;
    }
    public void ProcessNeighbour(Node current, Node neighbour, Node end, NodePrioQueue openList) {
        Node parent = current.getParent();
        float tempG;
        Node tempParent;

        if (LineOfSight(parent, neighbour)) {
            tempG = parent.getG() + distance(parent, neighbour);
            tempParent = parent;
        } else {
            tempG = current.getG() + distance(current,neighbour);
            tempParent = current;
        }

        if (tempG < neighbour.getG()) {
            neighbour.setG(tempG);
            neighbour.setH(heuristic(neighbour, end));
            neighbour.setF(neighbour.getG() + neighbour.getH());
            neighbour.setParent(tempParent);
            openList.enqueue(neighbour);
        }
    }
    public float distance(Node a, Node b) {
        return (float) Math.sqrt(Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2));
    }
    public float heuristic(Node a, Node b) {
        return (float) Math.sqrt(Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2));
    }
    public Node[] ConstructMissilePath(Node end) {
        Node[] path = new Node[15];
        Node current = end;
        int i = 0;

        while (current != current.getParent()) {
            path[i] = current;
            i++;
            current = current.getParent();
        }
        path[i] = current; // Add the start node
        return path;
    }
}
