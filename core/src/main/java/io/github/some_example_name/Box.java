package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Box extends Obstacle {
    private int x, y, width, height;
    private boolean display;
    FloatPoint point;

    public Box(int x, int y, int width){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = 40;
        this.display = false;

        this.point = new FloatPoint(x,y + height);

        type = Type.BOX;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isDisplay() {
        return display;
    }

    public void Draw(ShapeRenderer sr) {
        sr.rect(x, y, width, height);
    }

    public void MoveX(float X) {
        x += X;
    }
    public void MoveY(int Y) {
        y += Y;
    }

    public void CheckDisplay() {
        if (x < GameData.getInstance().getScreenWidth() || x > -width) {
            display = true;
        } else {
            display = false;
        }
    }
}
