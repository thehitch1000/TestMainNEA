package io.github.some_example_name;

import com.badlogic.gdx.math.Polygon;

import java.util.ArrayList;

public class ThetaStarStepper {
    private NodePrioQueue openList;
    Node[][] grid;
    Node start, end;
    ArrayList<LineSegment> path;
    Level.TypeOfPath type;

    int cellSize = 5;

    private boolean pathFound;
    private Level level;
    private float currentX, currentY;

    public ThetaStarStepper(Level.TypeOfPath type, FloatPoint startPoint, FloatPoint endPoint, Level level) {
        this.level = level;
        this.grid = level.grid;
        this.type = type;

        this.currentX = level.getXTravelled();
        this.currentY = level.getCurrentHeight();

        this.start = FindClosestNode(new Node ((int) startPoint.getX(), (int) startPoint.getY(), Node.NodeState.WALKABLE));
        this.end = FindClosestNode(new Node ((int) endPoint.getX(), (int) endPoint.getY(), Node.NodeState.WALKABLE));

        this.openList = new NodePrioQueue(1500);
        path = new ArrayList<>();

        pathFound = false;

        start.setG(0f);
        start.setH(distance(start, end));
        start.setF(start.getG() + start.getH());
        start.setParent(start);

        openList.enqueue(start);
    }

    public void FindPath() {
        if (LineOfSight(type, start, end)) {
            pathFound = true;
            ConstructSimplePath();
        } else {
            System.out.println("CRASH?");
            while (!pathFound) {
                if (openList.isEmpty()) {
                    System.out.println("No Path Found");
                    return;
                } else {
                    Node current = openList.dequeue();
                    if (current == end) {
                        pathFound = true;
                        ConstructComplexPath();
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
            if (zone.isPointInZone(missile.getX(), missile.getY())) {
                for (Polygon obstacle : level.shapes) {
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
        for (Zone zone : level.zones) {
            if (zone.isPointInZone(rect.getX() + rect.getWidth()/2f, rect.getY() + rect.getHeight()/2f)) {
                for (Polygon obstacle : level.shapes) {
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

    public boolean LineOfSight(Level.TypeOfPath type, Node start, Node end) {
        FloatPoint startPoint = new FloatPoint(start.getX(), start.getY());
        FloatPoint endPoint = new FloatPoint(end.getX(), end.getY());
        LineSegment segment = new LineSegment(startPoint, endPoint);
        segment.setSegment();
        Shape shape;
        int speed = 21;
        if (type == Level.TypeOfPath.PLAYERMISSILE || type == Level.TypeOfPath.MONSTERMISSILE) {
            shape = new Circle(startPoint.getX(), startPoint.getY(), 10);
        } else {
            shape = new Rect(startPoint.getX() - level.monster.shape.getWidth()/2f - 5, startPoint.getY() - level.monster.shape.getHeight()/2f - 5, level.monster.shape.getWidth() + 10, level.monster.shape.getHeight() + 10);
        }
        while (true) {
            if ((type == Level.TypeOfPath.PLAYERMISSILE || type == Level.TypeOfPath.MONSTERMISSILE) && !segment.isPointInSegment(shape.getX(), shape.getY())) break;
            if (type == Level.TypeOfPath.MONSTER && !segment.isPointInSegment(shape.getX() + level.monster.shape.getWidth()/2f + 5, shape.getY() + level.monster.shape.getHeight()/2f + 5)) break;

            if ((type == Level.TypeOfPath.PLAYERMISSILE || type == Level.TypeOfPath.MONSTERMISSILE && !isMissileSafeAt((Circle) shape))) return false;
            if (type == Level.TypeOfPath.MONSTER && !isMonsterSafeAt((Rect) shape)) return false;

            shape.MoveX(speed * (float) Math.cos(segment.getAngle()));
            shape.MoveY(speed * (float) Math.sin(segment.getAngle()));
        }
        return true;
    }
    public Node[] getNeighbours(Node currentNode) {
        Node[] Neighbours = new Node[8];
        int i = 0;
        int X = NodeFindXCoordinates(currentNode);
        int Y = NodeFindYCoordinates(currentNode);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                if (x == 0 && y == 0) continue;
                if (!inBound(X + x, Y + y)) continue;
                Neighbours[i] = grid[X + x][Y + y];
                i++;
            }
        }
        return Neighbours;
    }
    private boolean inBound(int X, int Y)    {
        return X >= 0 && X < grid.length && Y >= 0 && Y < grid[0].length;
    }
    public int NodeFindXCoordinates(Node node) {
        int index = (node.getX() - cellSize / 2) / cellSize;
        if (index < 0) index = 0;
        if (index >= grid.length) index = grid.length - 1;
        return index;
    }
    public int NodeFindYCoordinates(Node node) {
        int index = (node.getY() - level.getBottomOfLevel() - cellSize / 2) / cellSize;
        if (index < 0) index = 0;
        if (index >= grid[0].length) index = grid[0].length - 1;
        return index;
    }
    public void ProcessNeighbour(Node current, Node neighbour, Node end) {
        Node parent = current.getParent();
        if (parent == null) System.out.println("parent is null");
        float tempG;
        Node tempParent;

        if (LineOfSight(type, parent, neighbour)) {
            tempG = parent.getG() + distance(parent, neighbour);
            tempParent = parent;
        } else {
            tempG = current.getG() + distance(current,neighbour);
            tempParent = current;
        }

        if (tempG < neighbour.getG()) {
            neighbour.setG(tempG);
            neighbour.setH(distance(neighbour, end));
            neighbour.setF(neighbour.getG() + neighbour.getH());
            neighbour.setParent(tempParent);
            openList.enqueue(neighbour);
        }
    }
    public float distance(Node a, Node b) {
        return (float) Math.sqrt(Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2));
    }

    private Node FindClosestNode(Node node) {
        int gridX = (node.getX() - cellSize / 2) / cellSize;
        int gridY = (node.getY() - level.getBottomOfLevel() - cellSize / 2) / cellSize;
        if (inBound(gridX, gridY) ) {
            if (grid[gridX][gridY].getState() != Node.NodeState.UNWALKABLE) return grid[gridX][gridY];
            if (grid[gridX][gridY - 1].getState() != Node.NodeState.UNWALKABLE) return grid[gridX][gridY - 1];
            if (grid[gridX][gridY + 1].getState() != Node.NodeState.UNWALKABLE) return grid[gridX][gridY + 1];
            if (grid[gridX - 1][gridY].getState() != Node.NodeState.UNWALKABLE) return grid[gridX - 1][gridY];
            if (grid[gridX - 1][gridY - 1].getState() != Node.NodeState.UNWALKABLE) return grid[gridX - 1][gridY - 1];
        }
        return null;
    }

    public void ConstructComplexPath() {
        ArrayList<Node> nodes = new ArrayList<>();
        Node current = end;

        while (current != current.getParent()) {
            nodes.add(current);
            current = current.getParent();
        }
        nodes.add(current);

        for (int i = nodes.size() - 1; i > 0; i--) {
            FloatPoint point1 = new FloatPoint(nodes.get(i).getX(), nodes.get(i).getY());
            FloatPoint point2 = new FloatPoint(nodes.get(i - 1).getX(), nodes.get(i - 1).getY());
            path.add(new LineSegment(point1, point2));
        }

        Missile missile = new Missile(new FloatPoint(start.getX() - XDifference(), start.getY() - YDifference()), new FloatPoint(end.getX() - XDifference(), end.getY() - YDifference()), 6, level);
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
    private float XDifference() {
        return level.getXTravelled() - currentX;
    }
    private float YDifference() {
        return level.getCurrentHeight() - currentY;
    }

    public void ConstructSimplePath() {
        FloatPoint point1 = new FloatPoint(start.getX() - XDifference(), start.getY() - YDifference());
        FloatPoint point2 = new FloatPoint(end.getX() - XDifference(), end.getY() - YDifference());
        path.add(new LineSegment(point1, point2));

        Missile missile = new Missile(new FloatPoint(start.getX() - XDifference(), start.getY() - YDifference()), new FloatPoint(end.getX() - XDifference(), end.getY() - YDifference()), 6, level);
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
