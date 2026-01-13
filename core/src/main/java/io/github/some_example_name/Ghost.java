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
        this.center = startingPoint;
        this.direction = direction;
        this.level = level;
    }

    public void ChangeDirection() {
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
        while (!DoTimesMatch()) {
            int steps = 4;

            for (int i = 0; i < steps; i++) {
                boolean inCorner = false;
                for (Zone zone : level.zones) {
                    if (zone.getType() == Zone.Type.CHANGEDIRE && zone.isPointInZone(center.getX(), center.getY())) {
                        inCorner = true;

                        LineEquation playerProjection;

                        if (direction == Player.Direction.DOWN) {
                            playerProjection = new LineEquation(-1, center);
                        } else {
                            playerProjection = new LineEquation(1, center);
                        }

                        ArrayList<FloatPoint> intersections = new ArrayList<>();

                        for (int j = 0; j < zone.quad.points.length; j++) {
                            FloatPoint intersection = intersection(playerProjection, new LineEquation(zone.quad.points[j], zone.quad.points[(j + 1) % zone.quad.points.length]));

                            if (Clamp(zone.quad.points[j].getX(), zone.quad.points[(j + 1) % zone.quad.points.length].getX(), intersection.getX()) == intersection.getX()) {
                                intersections.add(intersection);
                            }
                        }

                        Zone newZone = level.zones.get(level.zones.indexOf(zone) + 1);
                        Zone oldZone = level.zones.get(level.zones.indexOf(zone) - 1);

                        LineEquation entryLine;
                        int leftPointIndex = 0;

                        for (int j = 0; j < zone.quad.points.length; j++) {
                            if (zone.quad.points[j].getX() <= zone.quad.points[leftPointIndex].getX()) {
                                leftPointIndex = j;
                            }
                        }

                        if (newZone.getType() == Zone.Type.RIGHT) {
                            if (oldZone.getType() == Zone.Type.UPDIAG) {
                                entryLine = new LineEquation(zone.quad.points[leftPointIndex], zone.quad.points[(leftPointIndex + 3) % zone.quad.points.length]);
                            } else {
                                entryLine = new LineEquation(zone.quad.points[leftPointIndex], zone.quad.points[(leftPointIndex + 1) % zone.quad.points.length]);
                            }
                        } else {
                            if (newZone.getType() == Zone.Type.UPDIAG) {
                                entryLine = new LineEquation(zone.quad.points[leftPointIndex], zone.quad.points[(leftPointIndex + 3) % zone.quad.points.length]);
                            } else {
                                entryLine = new LineEquation(zone.quad.points[leftPointIndex], zone.quad.points[(leftPointIndex + 1) % zone.quad.points.length]);
                            }
                        }

                        for (FloatPoint intersection : intersections) {
                            if (entryLine.isPointOnLine(intersection)) {
                                if (distance(intersection, center) / distance(intersections.get(0), intersections.get(1)) >= directionChangeScalar) {
                                    ChangeDirection();
                                }
                                break;
                            }
                        }
                        break;
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

                                if (center.getY() > bottomEdgeY + (0.8f * level.getLevelHeight()) || center.getY() < topEdgeY - (0.8f * level.getLevelHeight())) {
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
            }
        }

        FloatPoint finalPoint = new FloatPoint(center.getX(), 0);

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
            if (point.getY() > bottomEdgeY && point.getY() < finalPoint.getY()) {
                bottomEdgeY = point.getY();
            } else if (point.getY() < topEdgeY && point.getY() > finalPoint.getY()) {
                topEdgeY = point.getY();
            }
        }

        for (Zone zone : level.zones) {
            if (zone.getType() == Zone.Type.RIGHT && zone.isPointInZone(center.getX(), center.getY())) {
                finalPoint.setY(bottomEdgeY + (level.AverageHorPositionScalar.FindMean() * level.getLevelHeight()));
            } else if ((zone.getType() == Zone.Type.UPDIAG || zone.getType() == Zone.Type.DOWNDIAG) && zone.isPointInZone(center.getX(), center.getY())) {
                finalPoint.setY(bottomEdgeY + (level.AverageDiagPositionScalar.FindMean() * level.getLevelHeight()));
            }
        }

        return finalPoint;
    }

    public boolean DoTimesMatch() {
        final float delta = 0.01f;
        float MonsterToGhostDistance = 0, PlayerToGhostDistance;
        ThetaStarProcessor stepper = new ThetaStarProcessor(Level.TypeOfPath.GHOSTTRACKER, level.monster.midPoint, center, level);
        stepper.FindPath();

        PlayerToGhostDistance = center.getX() - level.player.midPoint.getX();
        for (LineSegment line : stepper.path) {
            MonsterToGhostDistance =+ line.Distance();
        }

        float MonsterToGhostTime = MonsterToGhostDistance / level.missileSpeed;
        float PlayerToGhostTime = PlayerToGhostDistance / level.worldSpeed;

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
