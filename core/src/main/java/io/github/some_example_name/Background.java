package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class Background {
    List<ShapeColumn> columns;
    private final float columnGap = 10f; // horizontal space between columns

    public Background() {
        columns = new ArrayList<>();
    }

    public void addColumn() {
        if (columns.size() == 0) {
            columns.add(new ShapeColumn(0));
        } else {
            ShapeColumn last = columns.get(columns.size() - 1);
            columns.add(new ShapeColumn(last.getX() + last.getWidth() + columnGap));
        }
    }

    public void MoveX(float X) {
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

    public ShapeColumn(float x) {
        rects = new ArrayList<>();
        this.x = x;
        this.width = 0;
        CreateColumn(FindWidth(), x);
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

    public void CreateColumn(int width, float x) {
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
    public int FindWidth() {
        return 100 * (int) Math.ceil((Math.random() * 4));
    }

}
