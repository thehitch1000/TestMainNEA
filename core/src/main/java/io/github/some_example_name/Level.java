package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
        MONSTER, PLAYERMISSILE, MONSTERMISSILE, GHOSTTRACKER
    }

    String currentFileName;
    List<Barrier> barriers;
    List<Zone> zones;
    List<Polygon> shapes;
    FloatCircularQueue AverageHorPositionScalar, AverageDiagPositionScalar, AverageChangeDirection;
    ScreenHeight screenHeight;
    Player player;
    Monster monster;
    Background background;
    Drill drill;
    Node[][] grid;

    final int levelDistance = 3000, cellSize = 16, margin = 4 * cellSize, missileSpeed = 360, worldSpeed = 240;
    boolean monsterPathPending = false, playerMissilePathPending = false, monsterMissilePathPending = false;

    private float xTravelled, currentHeight, StartTime, currentTopOfLevel, currentBottomOfLevel, levelHeight, totalLevelTime;
    private int shapeNumber;

    public Level() {
        player = new Player(100);
        monster = new Monster();
        background = new Background(20, 0);

        barriers = new ArrayList<>();
        zones = new ArrayList<>();
        shapes = new ArrayList<>();

        AverageHorPositionScalar = new FloatCircularQueue(300);
        AverageDiagPositionScalar = new FloatCircularQueue(300);
        AverageChangeDirection = new FloatCircularQueue(25);

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
        totalLevelTime = 0;
        screenHeight = ScreenHeight.NEUTRAL;
        StartTime = 0;
        GameData.getInstance().setStop(false);
        barriers.clear();
        shapes.clear();
        zones.clear();
    }
    public void PauseLevel() {
        AddToTotalLevelTime();
        GameData.getInstance().setStop(true);
        GameData.getInstance().clearFrameTimers();
    }
    public void UnpauseLevel() {
        StartTime = GameData.getInstance().getElapsedTime();
        GameData.getInstance().timers.runAfter(0.1f, () -> GameData.getInstance().setStop(false));
        GameData.getInstance().runFunctionAfterFrames(GameData.getInstance().getFrameNumber() + Gdx.app.getGraphics().getFramesPerSecond() / 2, () -> AddAveragePositionScalar());
    }
    public void AddToTotalLevelTime() {
        totalLevelTime += GameData.getInstance().getElapsedTime() - StartTime;
    }
    public void CloseProgram() {
        writeTrends();
        Gdx.app.exit();
    }
    public void OpenProgram() {
        getTrends();
    }

    public boolean CheckLevelEnd() {
        return xTravelled > levelDistance;
    }

    public float getXTravelled() {
        return xTravelled;
    }
    public float getCurrentHeight() {
        return currentHeight;
    }
    public int getBottomOfLevel() {
        return (int) currentBottomOfLevel;
    }
    public float getTotalLevelTime() {
        return totalLevelTime;
    }
    public float getLevelHeight() {
        return levelHeight;
    }

    public void setCurrentFileName(String fileName) {
        currentFileName = fileName;
    }

    public void getTrends() {
        try {
            File file = new File("Trends");

            List<String> lines = Files.readAllLines(file.toPath());

            if (lines.isEmpty()) {
                return;
            }

            for (String line : lines) {
                String[] parts = line.split(":\\s+|\\s+");
                switch (parts[0]) {
                    case "AverageHorPosition":
                        for (int i = 1; i < parts.length - 1; i++) {
                            AverageHorPositionScalar.enqueue(Float.parseFloat(parts[i]));
                        }
                        break;
                    case "AverageDiagPosition":
                        for (int i = 1; i < parts.length - 1; i++) {
                            AverageDiagPositionScalar.enqueue(Float.parseFloat(parts[i]));
                        }
                        break;
                    case "AverageChangeDirection":
                        for (int i = 1; i < parts.length - 1; i++) {
                            AverageChangeDirection.enqueue(Float.parseFloat(parts[i]));
                        }
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeTrends() {
        GameData.getInstance().EmptyFile("Trends");
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter("Trends", true))){
            fileWriter.write("AverageHorPosition: ");
            if (!AverageHorPositionScalar.isEmpty()) {
                 for (int i = 0; i < AverageHorPositionScalar.floats.length; i++) {
                     fileWriter.write(AverageHorPositionScalar.floats[i] + " ");
                 }
            }
            fileWriter.newLine();
            fileWriter.write("AverageDiagPosition: ");
            if (!AverageDiagPositionScalar.isEmpty()) {
                for (int i = 0; i < AverageDiagPositionScalar.floats.length; i++) {
                    fileWriter.write(AverageDiagPositionScalar.floats[i] + " ");
                }
            }
            fileWriter.newLine();
            fileWriter.write("AverageChangeDirection: ");
            if (!AverageChangeDirection.isEmpty()) {
                for (int i = 0; i < AverageChangeDirection.floats.length; i++) {
                    fileWriter.write(AverageChangeDirection.floats[i] + " ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CreateZigZagLevel() {
        while (!drill.isFinished()) {
            if (drill.points[0].getX() + xTravelled >= levelDistance && drill.points[3].getX() + xTravelled >= levelDistance) {
                drill.setFinished(true);
                drill.FinishPath();
                drill.EndShapes();
                CreateCrossOverZone();
                CreateZone();
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
                    case "DOWN_RIGHT,DOWN_RIGHT":
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
                    default:
                        System.out.println("Unknown change in direction: " + key);
                        CloseProgram();
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
                AddBarrier(drill.currentShapes.get(0));
                AddBarrier(drill.currentShapes.get(1));
            } else {
                AddBarrier(drill.currentShapes.get(1));
                AddBarrier(drill.currentShapes.get(0));
            }
        } else if (drill.currentShapes.get(0).getX() > drill.currentShapes.get(3).getX()) {
            for (int i = drill.currentShapes.size() - 1; i >= 0; i--) {
                AddBarrier(drill.currentShapes.get(i));
            }
        } else {
            for (int i = 0; i < drill.currentShapes.size(); i++) {
                AddBarrier(drill.currentShapes.get(i));
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
                zone.quad.points[0].setWholePoint(drill.currentShapes.get(2).shape.points[0]);
                zone.quad.points[1].setWholePoint(drill.currentShapes.get(2).shape.points[2]);
                zone.quad.points[2].setWholePoint(drill.currentShapes.get(1).shape.points[2]);
                zone.quad.points[3].setWholePoint(drill.currentShapes.get(1).shape.points[0]);
                break;
            case RIGHT:
                zone.quad.points[0].setPoint(drill.currentShapes.get(1).getX(), drill.currentShapes.get(1).getHeight());
                zone.quad.points[1].setPoint(drill.currentShapes.get(1).getX() + drill.currentShapes.get(1).getWidth(), drill.currentShapes.get(1).getHeight());
                zone.quad.points[2].setPoint(drill.currentShapes.get(0).getX() + drill.currentShapes.get(0).getWidth(), drill.currentShapes.get(0).getY());
                zone.quad.points[3].setPoint(drill.currentShapes.get(0).getX(), drill.currentShapes.get(0).getY());
        }
        AddZone(zone);
    }
    public void CreateCrossOverZone() {
        Zone zone = new Zone(Zone.Type.CHANGEDIRE);

        zone.quad.points[0].setPoint(drill.intersectionPoints[0].getX(), drill.intersectionPoints[0].getY());
        zone.quad.points[1].setPoint(drill.intersectionPoints[1].getX(), drill.intersectionPoints[1].getY());
        zone.quad.points[2].setPoint(drill.intersectionPoints[2].getX(), drill.intersectionPoints[2].getY());
        zone.quad.points[3].setPoint(drill.intersectionPoints[3].getX(), drill.intersectionPoints[3].getY());

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

            for (String line : lines) {
                String[] parts = line.split(":\\s+|\\s+");

                if (!lock.isUsed() && parts[0].equals("RectPath")) {
                    fileWriter.write(line);
                    fileWriter.newLine();
                    lock.used();
                } else {
                    if (parts[0].equals("RectPath")) {
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
                    } else {
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
                            Barrier rectPath = new RectPath(x1 - xTravelled, Float.parseFloat(parts[2]) - currentHeight, Float.parseFloat(parts[3]), Float.parseFloat(parts[4]), Boolean.parseBoolean(parts[5]));
                            barriers.add(rectPath);
                            shapeNumber++;
                            return true;
                        } else {
                            return false;
                        }

                    case "TriPath":
                        float X3 = Float.parseFloat(parts[3]);
                        if (x1 - xTravelled <= 1700 || X3 - xTravelled <= 1700) {
                            Barrier TriPath = new TriPath(new FloatPoint(x1 - xTravelled, Float.parseFloat(parts[2]) - currentHeight),
                                new FloatPoint(X3 - xTravelled, Float.parseFloat(parts[4]) - currentHeight),
                                new FloatPoint(Float.parseFloat(parts[5]) - xTravelled, Float.parseFloat(parts[6]) - currentHeight), Boolean.parseBoolean(parts[7]));
                            barriers.add(TriPath);
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
                            zone.quad.points[0].setPoint(x1 - xTravelled, Float.parseFloat(parts[2]) - currentHeight);
                            zone.quad.points[1].setPoint(x2 - xTravelled, Float.parseFloat(parts[4]) - currentHeight);
                            zone.quad.points[2].setPoint(x3 - xTravelled, Float.parseFloat(parts[6]) - currentHeight);
                            zone.quad.points[3].setPoint(x4 - xTravelled, Float.parseFloat(parts[8]) - currentHeight);
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
        for (Barrier barrier : barriers) {
            if (barrier instanceof RectPath) {
                if (PlayerRectPathCollision((Rect) barrier.shape)) {
                    player.LoseLife();
                    monster.setAwake(true);
                    PlayerRespawn(barrier);
                }
            } else if (barrier instanceof TriPath) {
                if (PlayerTriPathCollision((Tri) barrier.shape)) {
                    player.LoseLife();
                    monster.setAwake(true);
                    PlayerRespawn(barrier);
                }
            }
        }
    }

    public void MoveWorldX() {
        float X = -worldSpeed * Gdx.app.getGraphics().getDeltaTime();
        barriers.forEach(barrier -> barrier.MoveX(X));
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
        barriers.forEach(barrier -> barrier.MoveY(Y));
        player.zigTrail.forEach(trail -> trail.MoveY(Y));
        zones.forEach(zone -> zone.MoveY(Y));
        MoveBackgroundY(Y / 10);
        monster.MoveY(Y);
        player.missiles.forEach(ammo -> ammo.shape.MoveY(Y));
        monster.missiles.forEach(ammo -> ammo.shape.MoveY(Y));
        MoveAllPathsY(Y);
    }
    public void MovePlayerY(float Y) {
        if (Y == 0) Y = (player.getDirection() == Player.Direction.DOWN) ? -(worldSpeed * Gdx.app.getGraphics().getDeltaTime()) : worldSpeed * Gdx.app.getGraphics().getDeltaTime();
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
            player.tempLines[0].setLine(player.lines[0].getGradient(), player.lines[0].getYIntercept());
            player.tempLines[1].setLine(player.lines[1].getGradient(), player.lines[1].getYIntercept());
            player.CalcMidPoints();
            for (Zone zone : zones) {
                if (zone.getType() == Zone.Type.CHANGEDIRE && !zone.isUsed() && zone.isPointInZone(player.midPoint.getX(), player.midPoint.getY())) {
                    AddChangeDirectionScalar(zone);
                    zone.used();
                    break;
                }
            }
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
    }
    public void CheckTrailPositioningDisplay() {
        for (polygon polygon : player.zigTrail) {
            if (!polygon.onScreen()) {
                player.zigTrail.remove(polygon);
                return;
            }
        }
    }
    public void CheckZonesOnScreen() {
        for (Zone zone : zones) {
            if (zone.isZoneLeftOfScreen()) {
                zones.remove(zone);
                return;
            }
        }
    }

    public void AddBarrier(Barrier barrier) {
        try (FileWriter writer = new FileWriter(currentFileName, true)) {
            if (barrier instanceof RectPath) {
                Rect rect = (Rect) barrier.shape;
                RectPath rectPath = (RectPath) barrier;
                writer.write("RectPath: " + rect.getX() + " " + rect.getY() + " " + rect.getWidth() + " " + rect.getHeight() + " " + rectPath.isBottom());
                writer.write("\n");
            } else if (barrier instanceof TriPath) {
                Tri tri = (Tri) barrier.shape;
                TriPath triPath = (TriPath) barrier;
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
        zone.quad.sortPoints();
        try (FileWriter writer = new FileWriter (currentFileName, true)) {
            writer.write("Zone: " + zone.quad.points[0].getX() + " " + zone.quad.points[0].getY() + " " +
                zone.quad.points[1].getX() + " " + zone.quad.points[1].getY() + " " +
                zone.quad.points[2].getX() + " " + zone.quad.points[2].getY() + " " +
                zone.quad.points[3].getX() + " " + zone.quad.points[3].getY() + " " +
                Zone.Type.valueOf(zone.type.toString()));
                writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void PlayerRespawn(Barrier barrier) {
        System.out.println("Respawning Player");
        PauseLevel();
        GameData.getInstance().timers.runAfter(0, () -> ZigZagRespawning(barrier));
        GameData.getInstance().timers.runAfter(1f, () -> ZigZagEndRespawning());
    }
    public void ZigZagRespawning(Barrier barrier) {
        float move = (levelHeight * 0.5f) / 10f;

        if (barrier instanceof RectPath) {
            RectPath rectPath = (RectPath) barrier;

            if (!rectPath.isBottom()) {
                GameData.getInstance().timers.runRepeatingUntil(0, 0.1f, 1f,() -> MovePlayerY(-move));
            } else {
                GameData.getInstance().timers.runRepeatingUntil(0, 0.1f, 1f,() -> MovePlayerY(move));
            }
        } else {
            TriPath triPath = (TriPath) barrier;
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
        UnpauseLevel();
        GameData.getInstance().setStop(false);
    }

    public void BuildLevel() {
        drill = new Drill();

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

        AddBarrier(new RectPath(0, -400, 700, 2000, false));

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
        if (newLevel) BuildLevel();

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
        monster.setAwake(false);

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

        grid = new Node[GameData.getInstance().getScreenWidth() / cellSize][(int) (currentTopOfLevel - currentBottomOfLevel) / cellSize];

        while (background.columns.size() < 20) {
            background.addColumn();
        }

        while (ReadFile());

        UnpauseLevel();
    }

    public void CreatePlayerMissile(FloatPoint startPoint, FloatPoint endPoint) {
        synchronized (this) {
            if (playerMissilePathPending) {
                return;
            }
            playerMissilePathPending = true;
        }

        ThetaStarProcessor stepper = new ThetaStarProcessor (TypeOfPath.PLAYERMISSILE, startPoint, endPoint, this);

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

        ThetaStarProcessor stepper;

        if (!monster.currentPath.isEmpty()) {
            stepper = new ThetaStarProcessor(TypeOfPath.MONSTER, monster.currentPath.get(monster.currentPath.size() - 1).endPoint, FindMonsterEndPoint(), this);
        } else {
            stepper = new ThetaStarProcessor(TypeOfPath.MONSTER, monster.midPoint, FindMonsterEndPoint(), this);
        }

        if (stepper.end == null) return;

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
    public FloatPoint FindMonsterEndPoint() {
        int X;
        if (!monster.currentPath.isEmpty()) {
            X = (int) (monster.currentPath.get(monster.currentPath.size() - 1).endPoint.getX() + 250);
        } else {
            X = (int) (monster.midPoint.getX() + 250);
        }

        CheckMonsterWalkabilityRegion(X - margin, X + margin);



        ArrayList<Node> walkableNodes = new ArrayList<>();

        for (Node[] column : grid) {
            for (Node node : column) {
                if (node.getState() == Node.NodeState.WALKABLE) {
                    walkableNodes.add(node);
                }
            }
        }

        Node node = walkableNodes.get((int) (Math.random() * walkableNodes.size()));

        return new FloatPoint(node.getX(), node.getY());
    }

    public void TransformShapes(int leftBound, int rightBound) {
        shapes.clear();
        for (Barrier barrier : barriers) {
            Polygon poly;
            if (barrier.shape instanceof Rect) {
                Rect rect = (Rect) barrier.shape;
                poly = new Polygon(new float[] {
                    rect.getX(), rect.getY(),
                    rect.getX() + rect.getWidth(), rect.getY(),
                    rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(),
                    rect.getX(), rect.getY() + rect.getHeight()
                });
            } else {
                Tri tri = (Tri) barrier.shape;
                poly = new Polygon(new float[]{
                    tri.points[0].getX(), tri.points[0].getY(),
                    tri.points[1].getX(), tri.points[1].getY(),
                    tri.points[2].getX(), tri.points[2].getY()
                });
            }

            float minX = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float[] vertices = poly.getVertices();
            for (int i = 0; i < vertices.length; i += 2) {
                minX = Math.min(minX, vertices[i]);
                maxX = Math.max(maxX, vertices[i]);
            }

            if (minX <= rightBound && maxX >= leftBound) {
                shapes.add(poly);
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

    public void CheckMissileWalkabilityRegion(int start, int end) {
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
        TransformShapes(start, end);
        ResetGridRegion(start,  end);
        for (Node[] nodes : grid) {
            CheckMonsterWalkabilityColumn(nodes);
        }
    }
    public void CheckMonsterWalkabilityColumn(Node[] nodes) {
        for (Node node : nodes) {
            if (isMonsterSafeAt(new Rect(node.getX() - monster.shape.getWidth()/2f - 5, node.getY() - monster.shape.getHeight()/2f - 5, monster.shape.getWidth() + 10, monster.shape.getHeight() + 10))) {
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
            if (zone.getType() != Zone.Type.CHANGEDIRE && zone.isPointInZone(rect.getX() + rect.getWidth()/2f, rect.getY() + rect.getHeight()/2f)) {
                for (Polygon barrier : shapes) {
                    if (rect.overlaps(barrier)) {
                        return false;
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

    public void AddChangeDirectionScalar(Zone currentZone) {
        Zone oldZone, newZone;

        oldZone = zones.get(zones.indexOf(currentZone) - 1);
        newZone = zones.get(zones.indexOf(currentZone) + 1);

        System.out.println("OldZone Type: " + oldZone.getType().toString());
        System.out.println("NewZone Type: " + newZone.getType().toString());

        int leftPointIndex = 0;

        for (int i = 0; i < currentZone.quad.points.length; i++) {
            if (currentZone.quad.points[i].getX() <= currentZone.quad.points[leftPointIndex].getX()) {
                leftPointIndex = i;
            }
        }

        System.out.println("leftPointIndex: " + leftPointIndex);

        LineEquation entryLine, playerLine;
        LineSegment zoneSizeLine;

        if (newZone.getType() == Zone.Type.RIGHT) {
            if (oldZone.getType() == Zone.Type.UPDIAG) {
                entryLine = new LineEquation(currentZone.quad.points[leftPointIndex], currentZone.quad.points[(leftPointIndex + 3) % currentZone.quad.points.length]);
            } else {
                entryLine = new LineEquation(currentZone.quad.points[leftPointIndex], currentZone.quad.points[(leftPointIndex + 1) % currentZone.quad.points.length]);
            }
        } else {
            if (newZone.getType() == Zone.Type.UPDIAG) {
                entryLine = new LineEquation(currentZone.quad.points[leftPointIndex], currentZone.quad.points[(leftPointIndex + 3) % currentZone.quad.points.length]);
            } else {
                entryLine = new LineEquation(currentZone.quad.points[leftPointIndex], currentZone.quad.points[(leftPointIndex + 1) % currentZone.quad.points.length]);
            }
        }

        playerLine = new LineEquation((-1/entryLine.getGradient()), player.midPoint);

        ArrayList<FloatPoint> intersections = new ArrayList<>();

        for (int i = 0; i < currentZone.quad.points.length; i++) {
            FloatPoint intersection = intersection(playerLine, new LineEquation(currentZone.quad.points[i], currentZone.quad.points[(i + 1) % currentZone.quad.points.length]));

            if (Clamp(currentZone.quad.points[i].getX(), currentZone.quad.points[(i + 1) % currentZone.quad.points.length].getX(), intersection.getX()) == intersection.getX()) {
                intersections.add(intersection);
            }
        }

        float potDistance = distance(intersections.get(0), intersections.get(1));
        float distanceFromEntry = distance(player.midPoint, intersection(playerLine, entryLine));

        float scalar = distanceFromEntry / potDistance;

        System.out.println("Distance From Entry: " + distanceFromEntry);
        System.out.println("PotDistance: " + potDistance);

        System.out.println("Scalar Added: " + scalar);
        System.out.println();

        AverageChangeDirection.enqueue(scalar);
    }
    public void AddAveragePositionScalar() {
        Zone currentZone = new Zone();
        for (Zone zone : zones) {
            if (zone.getType() != Zone.Type.CHANGEDIRE && zone.isPointInZone(player.midPoint.getX(), player.midPoint.getY())) {
                currentZone = zone;
            }
        }

        TransformShapes((int) player.midPoint.getX(), (int) player.midPoint.getX());

        ArrayList<LineSegment> shapeLines = new ArrayList<>();

        for (Polygon shape : shapes) {
            for (int i = 0; i < shape.getVertices().length; i += 2) {
                shapeLines.add(new LineSegment(shape.getVertices()[i], shape.getVertices()[i+1], shape.getVertices()[(i+2) % shape.getVertices().length], shape.getVertices()[(i+3) % shape.getVertices().length]));
            }
        }

        ArrayList<FloatPoint> intersections = new ArrayList<>();

        for (LineSegment line : shapeLines) {
            if (line.getDirection() == LineSegment.Direction.VERTICAL && line.startPoint.getX() != player.midPoint.getX()) continue;

            if (Clamp(line.startPoint.getX(), line.endPoint.getX(), player.midPoint.getX()) == player.midPoint.getX()) {
                intersections.add(new FloatPoint(player.midPoint.getX(), line.FindY(player.midPoint.getX())));
            }
        }

        float bottomEdgeY = 0, topEdgeY = Float.MAX_VALUE;
        for (FloatPoint point : intersections) {
            if (point.getY() > bottomEdgeY && point.getY() < player.midPoint.getY()) {
                bottomEdgeY = point.getY();
            } else if (point.getY() < topEdgeY && point.getY() > player.midPoint.getY()) {
                topEdgeY = point.getY();
            }
        }

        float currentLevelHeight = topEdgeY - bottomEdgeY;
        float averagePositionScalar = (player.midPoint.getY() - bottomEdgeY) / currentLevelHeight;

        if (currentZone.getType() == Zone.Type.RIGHT) {
            if (AverageHorPositionScalar.floats[AverageHorPositionScalar.getFrontPointer() - 1 % AverageHorPositionScalar.floats.length] == averagePositionScalar) return;
            AverageHorPositionScalar.enqueue(averagePositionScalar);
        } else if (currentZone.getType() == Zone.Type.UPDIAG || currentZone.getType() == Zone.Type.DOWNDIAG) {
            AverageDiagPositionScalar.enqueue(averagePositionScalar);
        }

        GameData.getInstance().runFunctionAfterFrames(GameData.getInstance().getFrameNumber() + Gdx.app.getGraphics().getFramesPerSecond() / 2, () -> AddAveragePositionScalar());
    }
    private float Clamp(float start, float end, float value) {
        return Math.max(start, Math.min(end, value));
    }
    private FloatPoint intersection (LineEquation line1, LineEquation line2) {
        FloatPoint intersection = new FloatPoint(0, 0);
        intersection.setX(line2.getYIntercept() - line1.getYIntercept() / line1.getGradient() - line2.getGradient());
        intersection.setY(line1.FindY(intersection.getX()));
        return intersection;
    }
    private float distance (FloatPoint point1, FloatPoint point2) {
        return (float) Math.sqrt(Math.pow(point1.getX() - point2.getX(), 2) + Math.pow(point1.getY() - point2.getY(), 2));
    }

    public void setAllZoneGhosted() {

    }

    public void CreateMonsterMissile() {
        synchronized (this) {
            if (monsterMissilePathPending) return;
            monsterMissilePathPending = true;
        }

        monster.CalcMidPoint();

        CheckMonsterWalkabilityRegion((int) monster.midPoint.getX() - margin, GameData.getInstance().getScreenWidth());

        Ghost scoutGhost = new Ghost(player.midPoint, player.getDirection(), this);

        ThetaStarProcessor stepper = new ThetaStarProcessor(TypeOfPath.MONSTERMISSILE, monster.midPoint, scoutGhost.FindEndPoint(AverageChangeDirection.FindMean()), this);

        Gdx.app.postRunnable(() -> {
            try {
                stepper.FindPath();
            } finally {
                synchronized (this) {
                    monsterMissilePathPending = false;
                }
            }
        });
    }
}
