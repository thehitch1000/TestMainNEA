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
    public enum ScreenHeight {
        TOP, BOTTOM, NEUTRAL, FIXED
    }
    public enum TypeOfPath {
        MONSTER, PLAYERMISSILE, MONSTERMISSILE
    }

    String currentFileName;
    List<Obstacle> obstacles;
    List<Zone> zones;
    List<Polygon> shapes;
    List<ThetaStarStepper> pathing;
    ScreenHeight screenHeight;
    Player player;
    Monster monster;
    Background background;
    Drill drill;
    Node[][] grid;
    int levelDistance = 3000, cellSize = 8, margin = 4 * cellSize;
    boolean monsterPathPending = false, playerMissilePathPending = false;

    private float xTravelled, currentHeight, StartTime, currentTopOfLevel, currentBottomOfLevel, levelHeight;
    private int shapeNumber;

    public Level() {
        player = new Player(100);
        monster = new Monster();
        background = new Background(20, 0);

        obstacles = new ArrayList<>();
        zones = new ArrayList<>();
        shapes = new ArrayList<>();
        pathing = new ArrayList<>();

        drill = new Drill();

        grid = new Node[][]{};

        this.xTravelled = 0;
        this.shapeNumber = 0;

        currentHeight = 0;
        StartTime = 0;

        screenHeight = ScreenHeight.NEUTRAL;
    }

    public void ResetLevel() {
        xTravelled = 0;
        player = new Player(100);
        monster = new Monster();
        shapeNumber = 0;
        currentHeight = 0;
        screenHeight = ScreenHeight.NEUTRAL;
        StartTime = 0;
        pathing.clear();
        GameData.getInstance().setStop(false);
        obstacles.clear();
        shapes.clear();
        zones.clear();
    }

    public boolean CheckLevelEnd() {
        return xTravelled > levelDistance;
    }

    public float getStartTime() {
        return StartTime;
    }
    public float getXTravelled() {
        return xTravelled;
    }
    public float getCurrentHeight() {
        return currentHeight;
    }
    public int getBottomOfLevel() {
        return (int) drill.getBottomOfLevel();
    }
    public Level.ScreenHeight getScreenHeight() {
        return screenHeight;
    }

    public void setCurrentFileName(String fileName) {
        currentFileName = fileName;
    }

    public void CreateZigZagLevel() {
        while (!drill.isFinished()) {
            if (drill.points[0].getX() + xTravelled >= levelDistance && drill.points[3].getX() + xTravelled >= levelDistance) {
                drill.setFinished(true);
                drill.FinishPath();
                drill.EndShapes();
                CreateZone();
                CreateCrossOverZone();
                TransferShapes();
                drill.StartShapes();
                drill.setDirection(Drill.Direction.RIGHT);
                drill.currentShapes.get(0).setWidth(levelDistance + 900 - drill.currentShapes.get(0).getX() - xTravelled);
                drill.currentShapes.get(1).setWidth(levelDistance + 900 - drill.currentShapes.get(1).getX() - xTravelled);
                CreateZone();
                CreateCrossOverZone();
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
                        if (temp > drill.getTopOfLevel()) {
                            drill.setTopOfLevel(temp);
                        }
                    }
                    if (drill.currentShapes.get(1) instanceof RectPath) {
                        if (drill.currentShapes.get(1).getHeight() < drill.getBottomOfLevel()) {
                            drill.setBottomOfLevel(drill.currentShapes.get(1).getHeight());
                        }
                    } else if (drill.currentShapes.get(3).getHeight() < 0) {
                        if (drill.currentShapes.get(3).getHeight() < drill.getBottomOfLevel()) {
                            drill.setBottomOfLevel(drill.currentShapes.get(3).getHeight());
                        }
                    }

                    CreateZone();
                    TransferShapes();

                    // NEW SHAPES
                    drill.StartShapes();

                    drill.setOldDirection(drill.getDirection());
                    drill.setDirection(drill.getNewDirection());


                    drill.FindLines();
                    drill.FindIntersections();

                    CreateCrossOverZone();
                }
            }
        }
        drill.setBottomOfLevel(drill.getBottomOfLevel() - 50);
        drill.setTopOfLevel(drill.getTopOfLevel() + 50);
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
        switch (drill.getDirection()) {
            case DOWN_RIGHT:
                zone = new Zone(Zone.Type.DOWNDIAG);
                break;
            case RIGHT:
                zone = new Zone(Zone.Type.RIGHT);
                break;
            default:
                zone = new Zone(Zone.Type.UPDIAG);
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
    public void CreateCrossOverZone() {
        Zone zone = new Zone(Zone.Type.CHANGEDIRE);

        zone.polygon.points[0].setPoint(drill.intersectionPoints[0].getX(), drill.intersectionPoints[0].getY());
        zone.polygon.points[1].setPoint(drill.intersectionPoints[1].getX(), drill.intersectionPoints[1].getY());
        zone.polygon.points[2].setPoint(drill.intersectionPoints[2].getX(), drill.intersectionPoints[2].getY());
        zone.polygon.points[3].setPoint(drill.intersectionPoints[3].getX(), drill.intersectionPoints[3].getY());

        AddZone(zone);
    }
    public void AdjustShapesHeights() {
        GameData.getInstance().EmptyFile("tempObstacles.txt");
        GameData.getInstance().CopyFile(currentFileName, "tempObstacles.txt");
        GameData.getInstance().EmptyFile(currentFileName);

        FunctionLock lock = new FunctionLock();

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(currentFileName, true))) {
            File file = new File("tempObstacles.txt");

            List<String> lines = Files.readAllLines(file.toPath());

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(":\\s+|\\s+");

                if (!lock.getState() && parts[0].equals("RectPath")) {
                    fileWriter.write("RectPath: " + parts[1] + " " + (drill.getBottomOfLevel() - 75) +
                        " " + parts[3] + " " + (drill.getTopOfLevel() - drill.getBottomOfLevel() + 150) + " " + Boolean.parseBoolean(parts[5]));
                    fileWriter.newLine();
                    lock.used();
                } else {
                    switch (parts[0]) {
                        case "RectPath":
                            if (Boolean.parseBoolean(parts[5])) {
                                fileWriter.write("RectPath: " + parts[1] + " " + (drill.getBottomOfLevel() - 150) +
                                    " " + parts[3] + " " + (Float.parseFloat(parts[4]) - drill.getBottomOfLevel() + 150) + " " + Boolean.parseBoolean(parts[5]));
                                fileWriter.newLine();
                            } else {
                                fileWriter.write("RectPath: " + Float.parseFloat(parts[1]) + " " + Float.parseFloat(parts[2]) +
                                    " " + Float.parseFloat(parts[3]) + " " + (drill.getTopOfLevel() - Float.parseFloat(parts[2]) + 150) +
                                    " " + Boolean.parseBoolean(parts[5]));
                                fileWriter.newLine();
                            }
                            break;
                        default:
                            fileWriter.write(line);
                            fileWriter.newLine();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean ReadFile() {
        try {
            File file = new File(currentFileName);
            List<String> lines = Files.readAllLines(file.toPath());

            if (lines.isEmpty() || shapeNumber >= lines.size()) {
                return false;
            }

            String line = lines.get(shapeNumber);
            String[] parts = line.split(":\\s+|\\s+");  // Split by ":" or whitespace

            if (shapeNumber > lines.size() - 1) {
                return false;
            }

            if (parts[0].equals("Zone") || parts[0].equals("TriPath") || parts[0].equals("RectPath")) {
                float x1 = Float.parseFloat(parts[1]);

                switch (parts[0]) {
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
                        if (x1 - xTravelled <= 1700 || X3 - xTravelled <= 1700) {
                            Obstacle TriPath = new TriPath(new FloatPoint(x1 - xTravelled, Float.parseFloat(parts[2]) - currentHeight),
                                new FloatPoint(X3 - xTravelled, Float.parseFloat(parts[4]) - currentHeight),
                                new FloatPoint(Float.parseFloat(parts[5]) - xTravelled, Float.parseFloat(parts[6]) - currentHeight), Boolean.parseBoolean(parts[7]));
                            obstacles.add(TriPath);
                            shapeNumber++;
                            return true;
                        } else {
                            return false;
                        }

                    case "Zone":
                        float x2 = Float.parseFloat(parts[3]);
                        float x3 = Float.parseFloat(parts[5]);
                        float x4 = Float.parseFloat(parts[7]);
                        if (x1 - xTravelled <= 1700 || x2 - xTravelled <= 1700 || x3 - xTravelled <= 1700 || x4 - xTravelled <= 1700) {
                            Zone zone = new Zone(Zone.Type.valueOf(parts[9]));
                            zone.polygon.points[0].setPoint(x1 - xTravelled, Float.parseFloat(parts[2]) - currentHeight);
                            zone.polygon.points[1].setPoint(x2 - xTravelled, Float.parseFloat(parts[4]) - currentHeight);
                            zone.polygon.points[2].setPoint(x3 - xTravelled, Float.parseFloat(parts[6]) - currentHeight);
                            zone.polygon.points[3].setPoint(x4 - xTravelled, Float.parseFloat(parts[8]) - currentHeight);
                            zones.add(zone);
                            shapeNumber++;
                            return true;
                        } else {
                            return false;
                        }

                    default:
                        System.out.println("Unknown obstacle type: " + parts[0]);
                }
            } else {
                shapeNumber++;
                return true;
            }
        } catch(IOException e) {
            e.printStackTrace();
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
            if (obstacle instanceof RectPath) {
                if (PlayerRectPathCollision((Rect) obstacle.shape)) {
                    player.LoseLife();
                    monster.setAwake(true);
                    PlayerRespawn(obstacle);
                }
            } else if (obstacle instanceof TriPath) {
                if (PlayerTriPathCollision((Tri) obstacle.shape)) {
                    player.LoseLife();
                    monster.setAwake(true);
                    PlayerRespawn(obstacle);
                }
            }
        }
    }

    public void MoveWorldX() {
        float X = -4;
        obstacles.forEach(obstacle -> obstacle.MoveX(X));
        player.zigTrail.forEach(trail -> trail.MoveX(X));
        player.MoveTempLinesX(X);
        xTravelled -= X;
        zones.forEach(zone -> zone.MoveX(X));
        monster.MoveX(X);
        player.missiles.forEach(ammo -> ammo.shape.MoveX(X));
        monster.missiles.forEach(ammo -> ammo.shape.MoveX(-X));
        MoveAllPathsX(X);
    }
    public void MoveWorldY(float Y) {
        currentHeight -= Y;
        player.MoveTempLinesY(Y);
        obstacles.forEach(obstacle -> obstacle.MoveY(Y));
        player.zigTrail.forEach(trail -> trail.MoveY(Y));
        zones.forEach(zone -> zone.MoveY(Y));
        MoveBackgroundY(Y / 10);
        monster.MoveY(Y);
        player.missiles.forEach(ammo -> ammo.shape.MoveY(Y));
        monster.missiles.forEach(ammo -> ammo.shape.MoveY(Y));
        MoveAllPathsY(Y);
    }
    public void MovePlayerY(float Y) {
        if (Y == 0) Y = (player.getDirection() == Player.Direction.DOWN) ? -4f : 4f;
        FloatPoint tempMidPoint = new FloatPoint(player.midPoint.getX(), player.midPoint.getY() + Y);

        float PlayerMove = 0, WorldMove = 0;
        int deadZone = 15;

        switch (screenHeight) {
            case FIXED:
                PlayerMove = Y;
                break;
            case TOP:
                if (tempMidPoint.getY() < GameData.getInstance().getScreenHeight()/2f - deadZone) {
                    screenHeight = ScreenHeight.NEUTRAL;
                }
                PlayerMove = Y;
                break;
            case BOTTOM:
                if (tempMidPoint.getY() > GameData.getInstance().getScreenHeight()/2f + deadZone) {
                    screenHeight = ScreenHeight.NEUTRAL;
                }
                PlayerMove = Y;
                break;
            case NEUTRAL:
                if (currentHeight + GameData.getInstance().getScreenHeight() >= currentTopOfLevel + deadZone) {
                    screenHeight = ScreenHeight.TOP;
                } else if (currentHeight <= currentBottomOfLevel - deadZone) {
                    screenHeight = ScreenHeight.BOTTOM;
                }
                WorldMove = -Y;
                break;
        }

        player.MoveY(PlayerMove);
        MoveWorldY(WorldMove);
    }
    public void MoveAllPathsX(float X) {
        monster.currentPath.forEach(path -> path.MoveX(X));
        monster.missiles.forEach(ammo -> ammo.path.forEach(line -> line.MoveX(X)));
        player.missiles.forEach(ammo -> ammo.path.forEach(line -> line.MoveX(X)));
    }
    public void MoveAllPathsY(float Y) {
        monster.currentPath.forEach(path -> path.MoveY(Y));
        monster.missiles.forEach(ammo -> ammo.path.forEach(line -> line.MoveY(Y)));
        player.missiles.forEach(ammo -> ammo.path.forEach(line -> line.MoveY(Y)));
    }

    public void MoveBackgroundX(float X) {
        background.MoveX(X);
    }
    public void MoveBackgroundY(float Y) {
        background.MoveY(Y);
    }
    public void CheckBackground() {
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
        player.calcHealthVisual();
    }
    public void CheckDisplay() {
        for (polygon polygon : player.zigTrail) {
            if (!polygon.onScreen()) {
                player.zigTrail.remove(polygon);
                return;
            }
        }
    }

    public void AddObstacle(Obstacle obstacle) {
        try (FileWriter writer = new FileWriter(currentFileName, true)) {
            if (obstacle instanceof RectPath) {
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
        zone.polygon.sortPoints();
        try (FileWriter writer = new FileWriter (currentFileName, true)) {
            writer.write("Zone: " + zone.polygon.points[0].getX() + " " + zone.polygon.points[0].getY() + " " +
                zone.polygon.points[1].getX() + " " + zone.polygon.points[1].getY() + " " +
                zone.polygon.points[2].getX() + " " + zone.polygon.points[2].getY() + " " +
                zone.polygon.points[3].getX() + " " + zone.polygon.points[3].getY() + " " +
                Zone.Type.valueOf(zone.type.toString()));
                writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void PlayerRespawn(Obstacle obstacle) {
        System.out.println("Respawning Player");
        GameData.getInstance().setStop(true);
        GameData.getInstance().timers.runAfter(0, () -> ZigZagRespawning(obstacle));
        GameData.getInstance().timers.runAfter(1f, () -> ZigZagEndRespawning());
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
        player.calcHealthVisual();
        GameData.getInstance().setStop(false);
    }

    public void BuildLevel() {
        GameData.getInstance().EmptyFile(currentFileName);

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

        try (FileWriter writer = new FileWriter(currentFileName, true)) {
            writer.write("Direction: " + drill.getDirection().toString() + "\n");
            writer.write("Monster: " + drill.points[0].getY() + "\n");
            writer.write("LevelHeight: " + (drill.points[3].getY() - drill.points[0].getY()) + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        AddObstacle(new RectPath(0, 0, 700, GameData.getInstance().getScreenHeight(), false));

        CreateZigZagLevel();
        AdjustShapesHeights();

        try (FileWriter writer = new FileWriter(currentFileName, true)) {
            writer.write("TopOfLevel: " + drill.getTopOfLevel() + "\n");
            writer.write("BottomOfLevel: " + drill.getBottomOfLevel() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpLevel(boolean newLevel) {
        if (newLevel) {
            BuildLevel();
        }

        Drill.Direction drillDirection = null;
        float starting0 = 0;
        try {
            File read = new File(currentFileName);

            List<String> lines = Files.readAllLines(read.toPath());

            String[] parts = lines.get(0).split(": ");
            drillDirection = Drill.Direction.valueOf(parts[1]);

            parts = lines.get(1).split(":\\s+|\\s+");
            starting0 = Float.parseFloat(parts[1]);

            parts = lines.get(2).split(":\\s+|\\s+");
            levelHeight = Float.parseFloat(parts[1]);

            parts = lines.get(lines.size() - 2).split(":\\s+|\\s+");
            currentTopOfLevel = Float.parseFloat(parts[1]);

            parts = lines.get(lines.size() - 1).split(":\\s+|\\s+");
            currentBottomOfLevel = Float.parseFloat(parts[1]);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (drillDirection == Drill.Direction.UP_RIGHT) {
            player.setDirection(Player.Direction.UP);
        } else if (drillDirection == Drill.Direction.DOWN_RIGHT) {
            player.setDirection(Player.Direction.DOWN);
        } else {
            player.setDirection(Player.Direction.UP);
        }

        player.setStartingPosition(starting0 + (0.3f * levelHeight));

        monster.setPosition(750, starting0 + (0.5f * levelHeight));
        monster.CalcMidPoint();
        monster.setAwake(true);

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
        player.calcHealthVisual();

        background = new Background(currentTopOfLevel - currentBottomOfLevel, currentBottomOfLevel);

        StartTime = GameData.getInstance().getElapsedTime();

        grid = new Node[GameData.getInstance().getScreenWidth() / cellSize][(int) (currentTopOfLevel - currentBottomOfLevel) / cellSize];

        while (background.columns.size() < 20) {
            background.addColumn();
        }

        while (ReadFile());
    }

    public void CreatePlayerMissile(FloatPoint startPoint, FloatPoint endPoint) {
        synchronized (this) {
            if (playerMissilePathPending) {
                return;
            }
            playerMissilePathPending = true;
        }

        ThetaStarStepper stepper = new ThetaStarStepper (TypeOfPath.PLAYERMISSILE, startPoint, endPoint, this);

        CheckMissileWalkabilityRegion((int) startPoint.getX() - margin, (int) endPoint.getX() + margin);

        Gdx.app.postRunnable(() -> {
            try {
                stepper.FindPath();
            } finally {
                synchronized (this) {
                    playerMissilePathPending = false;
                }
            }
        });
    }
    public void FindNewMonsterPath() {
        synchronized (this) {
            if (monsterPathPending) return;
            monsterPathPending = true;
        }

        monster.CalcMidPoint();

        ThetaStarStepper stepper;
        if (!monster.currentPath.isEmpty()) {
            stepper = new ThetaStarStepper(TypeOfPath.MONSTER, monster.currentPath.get(monster.currentPath.size() - 1).endPoint, FindMonsterEndPoint(), this);
        } else {
            stepper = new ThetaStarStepper(TypeOfPath.MONSTER, monster.midPoint, FindMonsterEndPoint(), this);
        }

        Gdx.app.postRunnable(() -> {
            try {
                stepper.FindPath();
            } finally {
                synchronized (this) {
                    monsterPathPending = false;
                }
            }
        });
    }
    public FloatPoint FindMonsterEndPoint(){
        Node[] Column;
        int X;
        if (!monster.currentPath.isEmpty()) {
            X = (int) (monster.currentPath.get(monster.currentPath.size() - 1).endPoint.getX() + 150);
        } else {
            X = (int) (monster.midPoint.getX() + 150);
        }

        SetGridToOneColumn(X);
        Column = grid[0];
        CheckMonsterWalkabilityColumn(Column);

        int WALKABLE_Nodes = 0;

        for (Node node : Column) {
            if (node.getState() == Node.NodeState.WALKABLE) {
                WALKABLE_Nodes++;
            }
        }

        System.out.println("WALKABLE NODES: " + WALKABLE_Nodes);

        boolean valid = false;
        do {
            float yLevel = (float) Math.random() * GameData.getInstance().getScreenHeight() - (cellSize/2f) - currentBottomOfLevel;
            float difference = yLevel % cellSize;
            float nodeY = (yLevel - difference) / cellSize;
            if (Column[(int) nodeY].getState() == Node.NodeState.WALKABLE) {
                return new FloatPoint(Column[(int) nodeY].getX(), Column[(int) nodeY].getY());
            }
        } while (!valid);
        return null;
    }

    public void TransformShapes(int leftBound, int rightBound) {
        shapes.clear();
        for (Obstacle obstacle : obstacles) {
            if (obstacle.shape instanceof Rect) {
                Rect rect = (Rect) obstacle.shape;
                Polygon box = new Polygon(new float[] {
                    rect.getX(), rect.getY(),
                    rect.getX() + rect.getWidth(), rect.getY(),
                    rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(),
                    rect.getX(), rect.getY() + rect.getHeight()
                });
                for (int i = 0; i < box.getVertices().length; i += 2) {
                    if (box.getVertices()[i] < leftBound || box.getVertices()[i] > rightBound) continue;
                    shapes.add(box);
                    break;
                }
            } else {
                Tri tri = (Tri) obstacle.shape;
                Polygon triangle = new Polygon(new float[]{
                    tri.points[0].getX(), tri.points[0].getY(),
                    tri.points[1].getX(), tri.points[1].getY(),
                    tri.points[2].getX(), tri.points[2].getY()
                });
                for (int i = 0; i < triangle.getVertices().length; i += 2) {
                    if (triangle.getVertices()[i] < leftBound || triangle.getVertices()[i] > rightBound) continue;
                    shapes.add(triangle);
                    break;
                }
            }
        }
    }
    public void ResetGridRegion(int start, int end) {
        grid = new Node[(end - start) / cellSize][(int) ((currentTopOfLevel - currentBottomOfLevel)/ cellSize)];
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                grid[x][y] = new Node(start + (x * cellSize) + (cellSize / 2), (int) currentBottomOfLevel + (y * cellSize) + (cellSize / 2), Node.NodeState.WALKABLE);
            }
        }
    }
    public void SetGridToOneColumn(int x){
        grid = new Node[1][(int) ((currentTopOfLevel - currentBottomOfLevel)/ cellSize)];
        for (int y = 0; y < grid[0].length; y++) {
            grid[0][y] = new Node(x, (int) currentBottomOfLevel + (y * cellSize) + (cellSize / 2), Node.NodeState.WALKABLE);
        }
    }

    public void CheckMissileWalkabilityRegion(int start, int end) {
        shapes.clear();
        TransformShapes(start, end);
        ResetGridRegion(start, end);
        for (Node[] nodes : grid) {
            for (Node node : nodes) {
                if (isMissileSafeAt(new Circle(node.getX(), node.getY(), 10))) {
                    node.setState(Node.NodeState.WALKABLE);
                } else {
                    node.setState(Node.NodeState.UNWALKABLE);
                }
            }
        }
    }
    public void CheckMonsterWalkabilityRegion(int start, int end) {
        shapes.clear();
        TransformShapes(start, end);
        ResetGridRegion(start,  end);
        for (Node[] nodes : grid) {
            CheckMonsterWalkabilityColumn(nodes);
        }
    }
    public void CheckMonsterWalkabilityColumn(Node[] nodes) {
        for (Node node : nodes) {
            if (isMonsterSafeAt(new Rect(node.getX() - monster.shape.getWidth()/2f, node.getY() - monster.shape.getHeight()/2f, monster.shape.getWidth(), monster.shape.getHeight()))) {
                node.setState(Node.NodeState.WALKABLE);
            } else {
                node.setState(Node.NodeState.UNWALKABLE);
            }
        }
    }

    public boolean isMissileSafeAt(Circle missile) {
        for (Zone zone : zones) {
            if (zone.isPointInZone(missile.getX(), missile.getY())) {
                for (Polygon obstacle : shapes) {
                    if (obstacle != null) {
                        if (missile.overlaps(obstacle)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
    public boolean isMonsterSafeAt(Rect rect) {
        for (Zone zone : zones) {
            if (zone.isPointInZone(rect.getX() + rect.getWidth()/2f, rect.getY() + rect.getHeight()/2f)) {
                for (Polygon obstacle : shapes) {
                    if (obstacle != null) {
                        if (rect.overlaps(obstacle)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void MoveMonsterAlongPath() {
        if (monster.currentPath.size() <= 1) {
            FindNewMonsterPath();
            return;
        }

        monster.CalcMidPoint();
        float angleRad = monster.currentPath.get(0).getAngle();

        float xDifference = monster.currentPath.get(0).endPoint.getX() - monster.midPoint.getX();
        float yDifference = monster.currentPath.get(0).endPoint.getY() - monster.midPoint.getY();
        float distanceToEnd = (float) Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2));

        if (distanceToEnd <= monster.getSpeed()) {
            monster.MoveX(xDifference);
            monster.MoveY(yDifference);
            monster.currentPath.remove(0);
        } else {
            monster.MoveX((float) Math.cos(angleRad) * monster.getSpeed());
            monster.MoveY((float) Math.sin(angleRad) * monster.getSpeed());
        }
    }
    public boolean inBound(int X, int Y)    {
        return X >= 0 && X < grid.length && Y >= 0 && Y < grid[0].length;
    }

    public void MoveMissiles() {
        for (Missile missile : player.missiles) {
            missile.MoveAlongPath();
            if (missile.path.isEmpty()) {
                Gdx.app.postRunnable(() -> player.missiles.remove(missile));
            }
        }
        for (Missile missile : monster.missiles) {
            missile.MoveAlongPath();
            if (missile.path.isEmpty()) {
                Gdx.app.postRunnable(() -> monster.missiles.remove(missile));
            }
        }
    }
}
