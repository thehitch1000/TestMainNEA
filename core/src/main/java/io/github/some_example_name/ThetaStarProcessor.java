package io.github.some_example_name;

import com.badlogic.gdx.math.Polygon;
import java.util.ArrayList;

public class ThetaStarProcessor {
    final int cellSize = 16;

    private final int margin = 4 * cellSize;
    private int leftX, rightX;
    private NodePrioQueue openList;
    private boolean newGrid = true;
    Node[][] grid;
    Node start, end;
    ArrayList<LineSegment> path;
    Level.TypeOfPath type;

    private boolean pathFound, cancelled;
    private Level level;
    private float currentX, currentY;

    public ThetaStarProcessor(Level.TypeOfPath type, FloatPoint startPoint, FloatPoint endPoint, Level level, boolean newGrid) {
        this.newGrid = newGrid;

        this.leftX = (int) startPoint.getX() - margin;
        this.rightX = (int) endPoint.getX() + margin;

        this.level = level;
        this.type = type;

        this.currentX = level.getXTravelled();
        this.currentY = level.getCurrentHeight();

        this.start = FindClosestNodeToStart(new Node ((int) startPoint.getX(), (int) startPoint.getY(), Node.NodeState.WALKABLE));
        this.end = FindClosestNode(new Node ((int) endPoint.getX(), (int) endPoint.getY(), Node.NodeState.WALKABLE));

        if (!isEndpointValid()) {
            cancelled = true;
            return;
        }

        this.openList = new NodePrioQueue(1500);
        path = new ArrayList<>();

        pathFound = false;

        start.setG(0f);
        start.setH(distance(start, end));
        start.setF(start.getG() + start.getH());
        start.setParent(null);

        openList.enqueue(start);
    }
    private boolean isEndpointValid() {
        return isPointSafeAt(new FloatPoint(end.getX(), end.getY()));
    }
    public boolean isCancelled() {
        return cancelled;
    }

    private Node FindClosestNodeToStart(Node node) {
        if (newGrid) {
            if (type == Level.TypeOfPath.MONSTER) {
                level.CheckMonsterWalkabilityRegion(leftX, rightX);
            } else {
                level.CheckMissileWalkabilityRegion(leftX, rightX);
            }
        }

        this.grid = level.grid;

        int gx = (node.getX() - leftX - (cellSize / 2)) / cellSize;
        int gy = (node.getY() - level.getBottomOfLevel() - (cellSize / 2)) / cellSize;

        if (inBound(gx, gy) && grid[gx][gy].getState() == Node.NodeState.WALKABLE) return node;

        for (Node n : getNeighbours(grid[gx][gy])) {
            if (n.getState() == Node.NodeState.WALKABLE) {
                return node;
            }
        }
        return null;
    }
    private Node FindClosestNode(Node node) {
        int startX = (node.getX() - leftX - (cellSize / 2)) / cellSize;
        int startY = (node.getY() - level.getBottomOfLevel() - (cellSize / 2)) / cellSize;

        if (!inBound(startX, startY)) return null;

        if (grid[startX][startY].getState() == Node.NodeState.WALKABLE) {
            return grid[startX][startY];
        }

        int maxRadius = Math.max(grid.length, grid[0].length);

        for (int r = 1; r <= maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (Math.abs(dx) != r && Math.abs(dy) != r) continue;

                    int x = startX + dx;
                    int y = startY + dy;

                    if (!inBound(x, y)) continue;

                    if (grid[x][y].getState() == Node.NodeState.WALKABLE) {
                        return grid[x][y];
                    }
                }
            }
        }
        return null;
    }

    public void FindPath() {
        System.out.println("Starting PathFinding");
        if (LineOfSight(type, start, end)) {
            System.out.println("Simple Path");
            ConstructPath(false);
            pathFound = true;
        } else {
            System.out.println("Complex Path");
            while (!pathFound) {
                if (openList.isEmpty()) {
                    System.out.println("No Path Found");
                    return;
                } else {
                    Node current = openList.dequeue();
                    if (current == end) {
                        pathFound = true;
                        ConstructPath(true);
                        return;
                    }
                    if (pathFound) return;
                    for (Node neighbour : getNeighbours(current)) {
                        ProcessNeighbour(current, neighbour, end);
                    }
                }
            }
        }
    }

    public boolean isMissileSafeAt(Circle missile) {
        for (Zone zone : level.zones) {
            if (zone.getType() != Zone.Type.CHANGEDIRE && zone.isPointInZone(missile.getX(), missile.getY())) {
                for (Polygon obstacle : level.shapes) {
                    if (missile.overlaps(obstacle)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    public boolean isMonsterSafeAt(Rect rect) {
        for (Zone zone : level.zones) {
            if (zone.isPointInZone(rect.getX() + rect.getWidth()/2f, rect.getY() + rect.getHeight()/2f)) {
                for (Polygon obstacle : level.shapes) {
                    if (rect.overlaps(obstacle)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    public boolean isPointSafeAt(FloatPoint point) {
        for (Zone zone : level.zones) {
            if (zone.getType() != Zone.Type.CHANGEDIRE && zone.isPointInZone(point.getX(), point.getY())) {
                return true;
            }
        }
        return false;
    }

    public boolean LineOfSight(Level.TypeOfPath type, Node startNode, Node endNode) {
        FloatPoint startPoint = new FloatPoint(startNode.getX(), startNode.getY());
        FloatPoint endPoint = new FloatPoint(endNode.getX(), endNode.getY());
        LineSegment segment = new LineSegment(startPoint, endPoint);
        Shape shape = null;
        FloatPoint ghostPoint = null;
        int speed = 5;
        if (type == Level.TypeOfPath.PLAYERMISSILE || type == Level.TypeOfPath.MONSTERMISSILE) {
            shape = new Circle(startPoint.getX(), startPoint.getY(), 10);
        } else if (type == Level.TypeOfPath.GHOSTTRACKER) {
            ghostPoint = new FloatPoint(startPoint.getX(), startPoint.getY());
        } else if (type == Level.TypeOfPath.MONSTER) {
            shape = new Rect(startPoint.getX() - level.monster.shape.getWidth()/2f - 5, startPoint.getY() - level.monster.shape.getHeight()/2f - 5, level.monster.shape.getWidth() + 10, level.monster.shape.getHeight() + 10);
        }
        while (true) {
            if ((type == Level.TypeOfPath.PLAYERMISSILE || type == Level.TypeOfPath.MONSTERMISSILE) && !segment.isPointInSegment(shape.getX(), shape.getY())) break;
            if (type == Level.TypeOfPath.MONSTER && !segment.isPointInSegment(shape.getX() + level.monster.shape.getWidth()/2f + 5, shape.getY() + level.monster.shape.getHeight()/2f + 5)) break;
            if (type == Level.TypeOfPath.GHOSTTRACKER && !segment.isPointInSegment(ghostPoint.getX(), ghostPoint.getY())) break;

            if ((type == Level.TypeOfPath.PLAYERMISSILE || type == Level.TypeOfPath.MONSTERMISSILE) && !isMissileSafeAt((Circle) shape)) return false;
            if (type == Level.TypeOfPath.MONSTER && !isMonsterSafeAt((Rect) shape)) return false;
            if (type == Level.TypeOfPath.GHOSTTRACKER && !isPointSafeAt(ghostPoint)) return false;

            if (type != Level.TypeOfPath.GHOSTTRACKER) {
                shape.MoveX(speed * (float) Math.cos(segment.getAngle()));
                shape.MoveY(speed * (float) Math.sin(segment.getAngle()));
            } else {
                ghostPoint.MoveX(speed * (float) Math.cos(segment.getAngle()));
                ghostPoint.MoveY(speed * (float) Math.sin(segment.getAngle()));
            }
        }
        return true;
    }
    public ArrayList<Node> getNeighbours(Node currentNode) {
        ArrayList<Node> neighbours = new ArrayList<>();
        int X = NodeFindXCoordinates(currentNode);
        int Y = NodeFindYCoordinates(currentNode);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                if (x == 0 && y == 0) continue;
                if (!inBound(X + x, Y + y)) continue;
                if (grid[X + x][Y + y].getState() == Node.NodeState.UNWALKABLE) continue;
                neighbours.add(grid[X + x][Y + y]);
            }
        }
        return neighbours;
    }
    private boolean inBound(int X, int Y)    {
        return X >= 0 && X < grid.length && Y >= 0 && Y < grid[0].length;
    }
    public int NodeFindXCoordinates(Node node) {
        int index = (node.getX() - leftX - (cellSize / 2)) / cellSize;
        if (index < 0) index = 0;
        if (index >= grid.length) index = grid.length - 1;
        return index;
    }
    public int NodeFindYCoordinates(Node node) {
        int index = (node.getY() - level.getBottomOfLevel() - (cellSize / 2)) / cellSize;
        if (index < 0) index = 0;
        if (index >= grid[0].length) index = grid[0].length - 1;
        return index;
    }
    public void ProcessNeighbour(Node current, Node neighbour, Node endNode) {
        Node parent = current.getParent();
        float tempG;
        Node tempParent;

        if (parent != null && distance(parent, neighbour) > (cellSize * 2.5f) && LineOfSight(type, parent, neighbour)) {
            tempG = parent.getG() + distance(parent, neighbour);
            tempParent = parent;
        } else {
            tempG = current.getG() + distance(current, neighbour);
            tempParent = current;
        }

        if (tempG < neighbour.getG()) {
            neighbour.setG(tempG);
            neighbour.setH(distance(neighbour, endNode));
            neighbour.setF(neighbour.getG() + neighbour.getH());
            neighbour.setParent(tempParent);
            openList.enqueue(neighbour);
        }
    }

    public float distance(Node a, Node b) {
        return (float) Math.sqrt(Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2));
    }

    private float XDifference() {
        return level.getXTravelled() - currentX;
    }
    private float YDifference() {
        return level.getCurrentHeight() - currentY;
    }

    public void ConstructPath(boolean complex) {
        if (complex) {
            ArrayList<Node> nodes = new ArrayList<>();
            Node current = end;

            while (current != null) {
                nodes.add(current);
                current = current.getParent();
            }

            for (int i = nodes.size() - 1; i > 0; i--) {
                FloatPoint point1 = new FloatPoint(nodes.get(i).getX(), nodes.get(i).getY());
                FloatPoint point2 = new FloatPoint(nodes.get(i - 1).getX(), nodes.get(i - 1).getY());
                path.add(new LineSegment(point1, point2));
            }
        } else {
            FloatPoint point1 = new FloatPoint(start.getX() - XDifference(), start.getY() - YDifference());
            FloatPoint point2 = new FloatPoint(end.getX() - XDifference(), end.getY() - YDifference());
            path.add(new LineSegment(point1, point2));
        }
        if (type == Level.TypeOfPath.GHOSTTRACKER) return;

        Missile missile = new Missile(new FloatPoint(start.getX() - XDifference(), start.getY() - YDifference()), new FloatPoint(end.getX() - XDifference(), end.getY() - YDifference()), 360, level);
        missile.setPath(path);
        missile.path.forEach(line -> line.MoveX(XDifference()));
        missile.path.forEach(line -> line.MoveY(YDifference()));

        if (type == Level.TypeOfPath.MONSTER) {
            level.monster.currentPath.add(path.get(0));
        } else if (type == Level.TypeOfPath.MONSTERMISSILE) {
            missile.setType(Missile.Type.MONSTER);
            level.monster.missiles.add(missile);
        } else if (type == Level.TypeOfPath.PLAYERMISSILE) {
            missile.setType(Missile.Type.PLAYER);
            level.player.missiles.add(missile);
        }
    }
}
