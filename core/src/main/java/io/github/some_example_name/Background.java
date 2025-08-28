package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class Background {
    private int XTotal;
    List<ShapeColumn> columns;

    public Background() {
        columns = new ArrayList<>();
    }

    public void addColumn() {
        int width = FindWidth();
        ShapeColumn column = new ShapeColumn();
        column.CreateColumn(width, XTotal);
        XTotal += width + 10;
        columns.add(column);
    }

    public int getXTotal() {
        return XTotal;
    }
    public void setXTotal(int XTotal) {
        this.XTotal = XTotal;
    }

    public void MoveX(float X) {
        XTotal += X;
        for (ShapeColumn column : columns) {
            for (Rect rect : column.rects) {
                rect.MoveX(X);
            }
            column.setX(column.getX() + X);
        }
    }
    public void MoveY(float Y) {
        for (ShapeColumn column : columns) {
            for (Rect rect : column.rects) {
                rect.MoveY(Y);
            }
        }
    }

    public int FindWidth() {
        return 100 * (int) Math.ceil((Math.random() * 4));
    }

    public void Draw(ShapeRenderer sr) {
        for (ShapeColumn column : columns) {
            for (Rect rect : column.rects) {
                rect.Draw(sr);
            }
        }
    }
}

class ShapeColumn {
    private float x, width;
    List<Rect> rects;
    Color BackgroundColour = new Color(0.12f, 0.28f, 0.51f, 1f);

    public ShapeColumn() {
        rects = new ArrayList<>();
        this.x = 0;
        this.width = 0;
    }

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }

    public float getWidth() {
        return width;
    }
    public void setWidth(float width) {
        this.width = width;
    }

    public boolean onScreen() {
        for (Rect rect : rects) {
            if (rect.onScreen()) {
                return true;
            }
        }
        return false;
    }

    public void CreateColumn(int width, int x) {
        this.width = width;
        this.x = x;
        int CurrentHeight = 590;
        boolean finished = false;
        while (!finished) {
            CurrentHeight -= 10;
            if (CurrentHeight != 0) {
                int XSplit = (int) (Math.random() * 2);
                int LevelHeight = FindLevelHeight();
                if ((CurrentHeight - LevelHeight) < 10) {
                    LevelHeight = CurrentHeight - 10;
                }
                CurrentHeight -= LevelHeight;
                if (XSplit == 0) {
                    rects.add(new Rect(x + 10,210 + CurrentHeight, width,LevelHeight, BackgroundColour));
                } else {
                    int divider = (int) (randomBetween(0.2f, 0.8f) * width);
                    rects.add(new Rect(x + 10, 210 + CurrentHeight, divider - 5, LevelHeight, BackgroundColour));
                    rects.add(new Rect(x + divider + 15, 210 + CurrentHeight, width - divider - 5, LevelHeight, BackgroundColour));
                }
            } else {
                finished = true;
            }
        }
    }

    public int FindLevelHeight() {
        return 100 * (int) Math.ceil((Math.random() * 3));
    }
    public float randomBetween(float min, float max) {
        return (float) Math.random() * (max - min) + min;
    }

}
