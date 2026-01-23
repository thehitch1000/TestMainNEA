package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;

import java.util.ArrayList;

public class Ghost {
    FloatPoint center;
    Player.Direction direction;
    Level level;
    LineEquation currentMoveLine;
    ArrayList<Polygon> barrierLines;
    public Player.Direction[] directions = Player.Direction.values();
    private float directionChangeScalar;
    ShapeRenderer sr;

    public Ghost(FloatPoint startingPoint, Player.Direction direction, Level level) {
        this.center = new FloatPoint(startingPoint.getX(), startingPoint.getY());
        this.direction = direction;
        this.level = level;

        barrierLines = new ArrayList<>();

        if (direction == Player.Direction.DOWN) {
            currentMoveLine = new LineEquation(-1, center);
        } else {
            currentMoveLine = new LineEquation(1, center);
        }

        sr = new ShapeRenderer();
    }

    public float getDirectionChangeScalar() {
        return directionChangeScalar;
    }

    public void ChangeDirection() {
        System.out.println("Changing direction");
        direction = directions[(direction.ordinal() + 1) % directions.length];
        currentMoveLine.setGradient(-1 * currentMoveLine.getGradient());
        currentMoveLine.CalcYIntercept(center);
    }

    public void MoveX(float X) {
        center.MoveX(X);
    }
    public void MoveY(float Y) {
        center.MoveY(Y);
    }

    public void StartSim(float directionChangeScalar) {
        System.out.println("Starting simulation...");

        System.out.println("Scalar: " + directionChangeScalar);

        this.directionChangeScalar = directionChangeScalar;

        GameData.getInstance().timers.runAfter(1f, () -> Step());
    }
    public void Step() {
        boolean inCorner = false, inGhostedCorner = false;
        for (Zone zone : level.zones) {
            if (zone.getType() == Zone.Type.CHANGEDIRE && zone.isPointInZone(center.getX(), center.getY())) {
                if (!zone.isGhosted()) {
                    inCorner = true;
                    ArrayList<FloatPoint> intersections = new ArrayList<>();

                    Zone oldZone = level.zones.get(level.zones.indexOf(zone) - 1);
                    Zone newZone = level.zones.get(level.zones.indexOf(zone) + 1);
                    LineEquation entryLine;

                    if (newZone.getType() == Zone.Type.UPDIAG && direction == Player.Direction.UP) {
                        zone.ghosted();
                        inCorner = false;
                        break;
                    }
                    if (newZone.getType() == Zone.Type.DOWNDIAG && direction == Player.Direction.DOWN) {
                        zone.ghosted();
                        inCorner = false;
                        break;
                    }

                    if (oldZone.getType() == Zone.Type.DOWNDIAG) {
                        entryLine = new LineEquation(zone.quad.points[Math.floorMod(zone.getLeftPointIndex() - 1, zone.quad.points.length)], zone.quad.points[zone.getLeftPointIndex()]);
                    } else {
                        entryLine = new LineEquation(zone.quad.points[zone.getLeftPointIndex()], zone.quad.points[Math.floorMod(zone.getLeftPointIndex() + 1, zone.quad.points.length)]);
                    }

                    for (int j = 0; j < zone.quad.points.length; j++) {
                        LineEquation zoneLine = new LineEquation(zone.quad.points[j], zone.quad.points[(j + 1) %  zone.quad.points.length]);

                        FloatPoint intersection = intersection(currentMoveLine, zoneLine);

                        float min = Math.min(zone.quad.points[j].getX(), zone.quad.points[(j + 1) % zone.quad.points.length].getX());
                        float max = Math.max(zone.quad.points[j].getX(), zone.quad.points[(j + 1) % zone.quad.points.length].getX());

                        if (Clamp(min, max, intersection.getX()) == intersection.getX()) {
                            intersections.add(intersection);
                        }
                    }

                    float distanceFromEntry = distance(center, intersection(currentMoveLine, entryLine));
                    float potDistance = distance(intersections.get(0), intersections.get(1));

                    float scalar = distanceFromEntry / potDistance;

                    if (scalar >= directionChangeScalar) {
                        ChangeDirection();
                        inCorner = false;
                        zone.ghosted();
                    }
                    break;
                } else {
                    inGhostedCorner = true;
                }
            }
        }
        if (!inCorner) {
            for (Zone zone : level.zones) {
                if (zone.getType() == Zone.Type.RIGHT && zone.isPointInZone(center.getX(), center.getY())) {
                    Zone nextDirectionZone = new Zone();

                    if (level.zones.indexOf(zone) + 2 < level.zones.size() - 1) {
                        nextDirectionZone = level.zones.get(level.zones.indexOf(zone) + 2);
                    }

                    if (nextDirectionZone.getType() == Zone.Type.UPDIAG && direction == Player.Direction.UP && inGhostedCorner) break;
                    if (nextDirectionZone.getType() == Zone.Type.DOWNDIAG && direction == Player.Direction.DOWN && inGhostedCorner) break;

                    ReturnPackage rp = FindEdges();

                    float topEdgeY = rp.getFloat(0);
                    float bottomEdgeY = rp.getFloat(1);

                    float currentLevelHeight = topEdgeY - bottomEdgeY;

                    if ((center.getY() >= bottomEdgeY + (0.8f * currentLevelHeight) && direction == Player.Direction.UP) || (center.getY() <= topEdgeY - (0.8f * currentLevelHeight) && direction == Player.Direction.DOWN)) {
                        ChangeDirection();
                    }
                    break;
                }
            }
        }

        if (direction == Player.Direction.DOWN) {
            MoveY(-5);
        } else {
            MoveY(5);
        }

        MoveX(5);

        level.setFinalPoint(center);

        System.out.println("Center X: " + center.getX() + " | Center Y: " + center.getY());

        if (DoTimesMatch()) FinishSim();
        else {
            System.out.println("Times don't match");
            GameData.getInstance().timers.runAfter(0.1f, () -> Step());
        }
    }
    public void FinishSim() {
        ReturnPackage rp = FindEdges();

        float topEdgeY = rp.floats.get(0);
        float bottomEdgeY = rp.floats.get(1);

        float currentLevelHeight = topEdgeY - bottomEdgeY;

        System.out.println("Center X: " + center.getX() + " | Center Y: " + center.getY());
        System.out.println("Player MidPoint X: " + level.player.midPoint.getX() + " | Player MidPoint Y: " + level.player.midPoint.getY());

        for (Zone zone : level.zones) {
            if (zone.getType() == Zone.Type.RIGHT && zone.isPointInZone(center.getX(), center.getY())) {
                level.finalPoint = new FloatPoint(center.getX(), bottomEdgeY + (level.AverageHorPositionScalar.FindMean() * currentLevelHeight));
                break;
            } else if ((zone.getType() == Zone.Type.UPDIAG || zone.getType() == Zone.Type.DOWNDIAG) && zone.isPointInZone(center.getX(), center.getY())) {
                level.finalPoint = new FloatPoint(center.getX(), bottomEdgeY + (level.AverageDiagPositionScalar.FindMean() * currentLevelHeight));
                break;
            }
        }

        level.setAllZoneNGhosted();

        System.out.println("Final Point: " + level.finalPoint.getX() + " | " + level.finalPoint.getY());
    }

    public boolean DoTimesMatch() {
        System.out.println("Checking times...");
        final float delta = 0.01f;
        float MonsterToGhostDistance = 0, PlayerToGhostDistance;
        ThetaStarProcessor stepper = new ThetaStarProcessor(Level.TypeOfPath.GHOSTTRACKER, level.monster.midPoint, center, level, false);

        if (stepper.isCancelled()) {
            FinishSim();
            System.out.println("Did not finish");
        }

        stepper.FindPath();

        PlayerToGhostDistance = center.getX() - level.player.midPoint.getX();

        if (stepper.path.isEmpty()) {
            System.out.println("StartPoint: " + stepper.start.getX());
            System.out.println("Monster MidPoint: " + level.monster.midPoint.getX() + ", " + level.monster.midPoint.getY());
            System.out.println("EndPoint: " + stepper.end.getX());
            System.out.println("End State: " + stepper.end.getState());
        }

        for (LineSegment line : stepper.path) {
            System.out.println("Distance: " + line.Distance());
            MonsterToGhostDistance += line.Distance();

            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.line(line.startPoint.getX(), line.startPoint.getY(), line.endPoint.getX(), line.endPoint.getY());
            sr.end();
        }

        System.out.println("Monster to ghost distance: " + MonsterToGhostDistance + " | " + "Player to ghost distance: " + PlayerToGhostDistance);

        float MonsterToGhostTime = MonsterToGhostDistance / level.missileSpeed;
        float PlayerToGhostTime = PlayerToGhostDistance / level.worldSpeed;

        System.out.println("Monster to ghost time: " + MonsterToGhostTime + " | Player to ghost time: " + PlayerToGhostTime);

        System.out.println("Time Difference: " + (MonsterToGhostTime - PlayerToGhostTime));

        level.ResetGridNodes();

        if (MonsterToGhostTime > PlayerToGhostTime + delta) return false;
        return MonsterToGhostTime <= PlayerToGhostTime + delta;
    }
    public void TransformShapeInLine(float X) {
        barrierLines.clear();
        for (Barrier barrier : level.barriers) {
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

            if (minX <= X && maxX >= X) {
                barrierLines.add(poly);
            }
        }
    }
    private float Clamp(float start, float end, float value) {
        return Math.max(start, Math.min(end, value));
    }
    private FloatPoint intersection (LineEquation line1, LineEquation line2) {
        FloatPoint intersection = new FloatPoint(0, 0);
        intersection.setX((line2.getYIntercept() - line1.getYIntercept()) / (line1.getGradient() - line2.getGradient()));
        intersection.setY(line1.FindY(intersection.getX()));
        return intersection;
    }
    private float distance (FloatPoint point1, FloatPoint point2) {
        return (float) Math.sqrt(Math.pow(point1.getX() - point2.getX(), 2) + Math.pow(point1.getY() - point2.getY(), 2));
    }
    private ReturnPackage FindEdges() {
        ArrayList<LineSegment> shapeLines = new ArrayList<>();
        ReturnPackage rp = new ReturnPackage();

        TransformShapeInLine(center.getX());

        for (Polygon shape : barrierLines) {
            for (int j = 0; j < shape.getVertices().length; j += 2) {
                shapeLines.add(new LineSegment(shape.getVertices()[j], shape.getVertices()[j+1], shape.getVertices()[(j+2) % shape.getVertices().length], shape.getVertices()[(j+3) % shape.getVertices().length]));
            }
        }

        ArrayList<FloatPoint> intersections = new ArrayList<>();

        for (LineSegment line : shapeLines) {
            if (line.getDirection() == LineSegment.Direction.VERTICAL && line.startPoint.getX() != center.getX()) continue;

            float min = Math.min(line.startPoint.getX(), line.endPoint.getX());
            float max = Math.max(line.startPoint.getX(), line.endPoint.getX());

            if (Clamp(min, max, center.getX()) == center.getX()) {
                intersections.add(new FloatPoint(center.getX(), line.FindY(center.getX())));
            }
        }

        float topEdgeY = Float.MAX_VALUE, bottomEdgeY = 0;
        for (FloatPoint point : intersections) {
            if (point.getY() < topEdgeY && point.getY() > center.getY()) {
                topEdgeY = point.getY();
            } else if (point.getY() > bottomEdgeY && point.getY() < center.getY()) {
                bottomEdgeY = point.getY();
            }
        }

        rp.floats.add(topEdgeY);
        rp.floats.add(bottomEdgeY);

        return rp;
    }
}
