package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Polygon;

import java.util.ArrayList;

public class Ghost {
    FloatPoint center;
    Player.Direction direction;
    Level level;
    LineEquation currentMoveLine;
    ArrayList<Polygon> barrierLines;
    public Player.Direction[] directions = Player.Direction.values();

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
    }

    public void ChangeDirection() {
        System.out.println("Changing direction");
        direction = directions[(direction.ordinal() + 1) % directions.length];
        currentMoveLine.setGradient(-1/ currentMoveLine.getGradient());
        currentMoveLine.CalcYIntercept(center);
    }

    public void MoveX(float X) {
        center.MoveX(X);
    }
    public void MoveY(float Y) {
        center.MoveY(Y);
    }

    public FloatPoint FindEndPoint(float directionChangeScalar) {
        System.out.println("Starting simulation...");

        System.out.println("Scalar: " + directionChangeScalar);
        while (!DoTimesMatch()) {
            int steps = 5;

            System.out.println("Cycle Started");

            for (int i = 0; i < steps; i++) {
                boolean inCorner = false;
                for (Zone zone : level.zones) {
                    if (zone.getType() == Zone.Type.CHANGEDIRE && zone.isPointInZone(center.getX(), center.getY())) {
                        inCorner = true;

                        if (!zone.isGhosted()) {
                            ArrayList<FloatPoint> intersections = new ArrayList<>();

                            Zone oldZone = level.zones.get(level.zones.indexOf(zone) - 1);
                            LineEquation entryLine;

                            if (oldZone.getType() == Zone.Type.DOWNDIAG) {
                                entryLine = new LineEquation(zone.quad.points[Math.floorMod(zone.getLeftPointIndex() - 1, zone.quad.points.length)], zone.quad.points[zone.getLeftPointIndex()]);
                            } else {
                                entryLine = new LineEquation(zone.quad.points[zone.getLeftPointIndex()], zone.quad.points[Math.floorMod(zone.getLeftPointIndex() + 1, zone.quad.points.length)]);
                            }

                            for (int j = 0; j < zone.quad.points.length; j++) {
                                FloatPoint intersection = intersection(currentMoveLine, new LineEquation(zone.quad.points[j], zone.quad.points[(j + 1) % zone.quad.points.length]));

                                float min = Math.min(zone.quad.points[j].getX(), zone.quad.points[(j + 1) % zone.quad.points.length].getX());
                                float max = Math.max(zone.quad.points[j].getX(), zone.quad.points[(j + 1) % zone.quad.points.length].getX());

                                if (Clamp(min, max, intersection.getX()) == intersection.getX()) {
                                    intersections.add(intersection);
                                }
                            }

                            for (FloatPoint intersection : intersections) {
                                if (entryLine.isPointOnLine(intersection)) {
                                    if (distance(intersection, center) / distance(intersections.get(0), intersections.get(1)) >= directionChangeScalar) {
                                        ChangeDirection();
                                        zone.ghosted();
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                if (!inCorner) {
                    for (Zone zone : level.zones) {
                        if (zone.getType() != Zone.Type.CHANGEDIRE && zone.isPointInZone(center.getX(), center.getY())) {
                            if (zone.getType() == Zone.Type.RIGHT) {
                                ArrayList<LineSegment> shapeLines = new ArrayList<>();

                                TransformShapeInLine(center.getX());

                                for (Polygon shape : barrierLines) {
                                    for (int j = 0; j < shape.getVertices().length; j += 2) {
                                        shapeLines.add(new LineSegment(shape.getVertices()[j], shape.getVertices()[j+1], shape.getVertices()[(j+2) % shape.getVertices().length], shape.getVertices()[(j+3) % shape.getVertices().length]));
                                    }
                                }

                                ArrayList<FloatPoint> intersections = new ArrayList<>();

                                for (LineSegment line : shapeLines) {
                                    if (line.getDirection() == LineSegment.Direction.VERTICAL && line.startPoint.getX() != level.player.midPoint.getX()) continue;

                                    if (Clamp(line.startPoint.getX(), line.endPoint.getX(), level.player.midPoint.getX()) == level.player.midPoint.getX()) {
                                        intersections.add(new FloatPoint(level.player.midPoint.getX(), line.FindY(level.player.midPoint.getX())));
                                    }
                                }

                                float bottomEdgeY = 0, topEdgeY = Float.MAX_VALUE;
                                for (FloatPoint point : intersections) {
                                    if (point.getY() > bottomEdgeY && point.getY() < level.player.midPoint.getY()) {
                                        bottomEdgeY = point.getY();
                                    } else if (point.getY() < topEdgeY && point.getY() > level.player.midPoint.getY()) {
                                        topEdgeY = point.getY();
                                    }
                                }

                                if ((center.getY() > bottomEdgeY + (0.8f * level.getLevelHeight()) && direction == Player.Direction.UP) || (center.getY() < topEdgeY - (0.8f * level.getLevelHeight()) && direction == Player.Direction.DOWN)) {
                                    ChangeDirection();
                                }
                            }
                            break;
                        }
                    }
                }

                if (direction == Player.Direction.DOWN) {
                    MoveY(-level.worldSpeed * Gdx.app.getGraphics().getDeltaTime());
                } else {
                    MoveY(level.worldSpeed * Gdx.app.getGraphics().getDeltaTime());
                }

                MoveX(level.worldSpeed * Gdx.app.getGraphics().getDeltaTime());

                System.out.println("Center X: " + center.getX() + " | Center Y: " + center.getY());

                System.out.println("Cycle Finished");
                System.out.println();
            }
        }

        System.out.println("Simulation finished");

        ArrayList<LineSegment> shapeLines = new ArrayList<>();

        TransformShapeInLine(center.getX());

        for (Polygon shape : barrierLines) {
            for (int j = 0; j < shape.getVertices().length; j += 2) {
                shapeLines.add(new LineSegment(shape.getVertices()[j], shape.getVertices()[j+1], shape.getVertices()[(j+2) % shape.getVertices().length], shape.getVertices()[(j+3) % shape.getVertices().length]));
            }
        }

        ArrayList<FloatPoint> intersections = new ArrayList<>();

        for (LineSegment line : shapeLines) {
            if (line.getDirection() == LineSegment.Direction.VERTICAL && line.startPoint.getX() != level.player.midPoint.getX()) continue;

            if (Clamp(line.startPoint.getX(), line.endPoint.getX(), level.player.midPoint.getX()) == level.player.midPoint.getX()) {
                intersections.add(new FloatPoint(level.player.midPoint.getX(), line.FindY(level.player.midPoint.getX())));
            }
        }

        float bottomEdgeY = 0, topEdgeY = Float.MAX_VALUE;
        for (FloatPoint point : intersections) {
            if (point.getY() > bottomEdgeY && point.getY() < center.getY()) {
                bottomEdgeY = point.getY();
            } else if (point.getY() < topEdgeY && point.getY() > center.getY()) {
                topEdgeY = point.getY();
            }
        }

        System.out.println("Center X: " + center.getX() + " | Center Y: " + center.getY());
        System.out.println("Player MidPoint X: " + level.player.midPoint.getX() + " | Player MidPoint Y: " + level.player.midPoint.getY());

        FloatPoint finalPoint = new FloatPoint(0,0);

        for (Zone zone : level.zones) {
            if (zone.getType() == Zone.Type.RIGHT && zone.isPointInZone(center.getX(), center.getY())) {
                finalPoint = new FloatPoint(center.getX(), bottomEdgeY + (level.AverageHorPositionScalar.FindMean() * level.getLevelHeight()));
                System.out.println("Y: " + finalPoint.getY());
                break;
            } else if ((zone.getType() == Zone.Type.UPDIAG || zone.getType() == Zone.Type.DOWNDIAG) && zone.isPointInZone(center.getX(), center.getY())) {
                finalPoint = new FloatPoint(center.getX(), bottomEdgeY + (level.AverageDiagPositionScalar.FindMean() * level.getLevelHeight()));
                System.out.println("Y: " + finalPoint.getY());
                break;
            }
        }

        level.setAllZoneNGhosted();

        System.out.println("Final Point: " + finalPoint.getX() + " | " + finalPoint.getY());

        level.setFinalPoint(center);

        return finalPoint;
    }

    public boolean DoTimesMatch() {
        final float delta = 0.01f;
        float MonsterToGhostDistance = 0, PlayerToGhostDistance;
        ThetaStarProcessor stepper = new ThetaStarProcessor(Level.TypeOfPath.GHOSTTRACKER, level.monster.midPoint, center, level, false);

        stepper.FindPath();

        PlayerToGhostDistance = center.getX() - level.player.midPoint.getX();
        for (LineSegment line : stepper.path) {
            System.out.println("Distance: " + line.Distance());
           MonsterToGhostDistance += line.Distance();
        }

        System.out.println("Monster to ghost distance: " + MonsterToGhostDistance + " | " + "Player to ghost distance: " + PlayerToGhostDistance);

        float MonsterToGhostTime = MonsterToGhostDistance / level.missileSpeed;
        float PlayerToGhostTime = PlayerToGhostDistance / level.worldSpeed;

        System.out.println("Monster to ghost time: " + MonsterToGhostTime + " | Player to ghost time: " + PlayerToGhostTime);

        System.out.println("Time Difference: " + (MonsterToGhostTime - PlayerToGhostTime));

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
        intersection.setX(line2.getYIntercept() - line1.getYIntercept() / line1.getGradient() - line2.getGradient());
        intersection.setY(line1.FindY(intersection.getX()));
        return intersection;
    }
    private float distance (FloatPoint point1, FloatPoint point2) {
        return (float) Math.sqrt(Math.pow(point1.getX() - point2.getX(), 2) + Math.pow(point1.getY() - point2.getY(), 2));
    }
}
