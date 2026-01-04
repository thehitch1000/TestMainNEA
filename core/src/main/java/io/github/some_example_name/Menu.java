package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.input;

public class Menu {
    private boolean open;
    ArrayList<Button> buttons;
    Body body;

    public Menu() {
        buttons = new ArrayList<>();
        body = null;
        this.open = false;
    }

    public void AddButton(Button button) {
        buttons.add(button);
    }
    public void AddBody(Body body) {
        this.body = body;
    }

    public void Draw(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        if (open) {
            buttons.forEach(button -> button.Draw(sr, batch, font));
            if (body != null) {
                body.Draw(batch, font);
            }
        }
    }
    public void CheckClick() {
        if (open) {
            buttons.forEach(button -> button.CheckClick());
        }
    }
    public void Open() {
        open = true;
    }
    public void Close() {
        open = false;
    }

}

class Button {
    private Text text;
    ArrayList<Runnable> actions;
    private Rect buttonShape;

    public Button(String message, ArrayList<Runnable> Actions, float centreX, float centreY, BitmapFont font) {
        this.text = new Text(message, centreX, centreY, font);

        actions = Actions;

        buttonShape = new Rect(text.getX() - 20, centreY - (text.getHeight() / 2f) - 10, text.getWidth() + 40, text.getHeight() + 20, new Color(0.42f, 0.42f, 0.43f, 0.1f));
    }
    public void CheckClick() {
        if (buttonShape.isPointInShape(new FloatPoint(Gdx.input.getX(), GameData.getInstance().getScreenHeight() - Gdx.input.getY()))) {
            if (input.isButtonJustPressed(Input.Buttons.LEFT)) {
                for (Runnable action : actions) {
                    GameData.getInstance().timers.runAfter(0.1f, action);
                }
            }
        }
    }

    public void DrawButton(ShapeRenderer sr) {
        if (buttonShape.isPointInShape(new FloatPoint(Gdx.input.getX(), GameData.getInstance().getScreenHeight() - Gdx.input.getY()))) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            sr.begin(ShapeRenderer.ShapeType.Filled);

            sr.setColor(new Color(0.42f, 0.42f, 0.43f, 0.1f));
            buttonShape.Draw(sr);

            sr.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }
    public void DrawText(SpriteBatch batch, BitmapFont font) {
        batch.begin();
        font.draw(batch, text.getText(), text.getX(), text.getY());
        batch.end();
    }
    public void Draw(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        DrawButton(sr);
        DrawText(batch, font);
    }
}

class Body {
    List<Text> lines;
    private float CentreX;

    public Body(float CentreX) {
        lines = new ArrayList<>();
        this.CentreX = CentreX;
    }

    public void AddLine(String text, float CentreY, BitmapFont font) {
        lines.add(new Text(text, CentreX, CentreY, font));
    }

    public void ClearLines() {
        lines.clear();
    }

    public void Draw(SpriteBatch batch, BitmapFont font) {
        batch.begin();
        for (Text line : lines) {
            line.Draw(batch, font);
        }
        batch.end();
    }
}

class Text {
    private String text;
    private float x, y;
    private float height, width;

    public Text(String text, float CentreX, float CentreY, BitmapFont font) {
        this.text = text;
        FindTextPosition(font, CentreX, CentreY);
    }
    public void FindTextPosition(BitmapFont font, float centreX, float centreY) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text);

        height = layout.height;
        width = layout.width;

        x = centreX - (width / 2f);
        y = centreY + (height / 2f);
    }

    public void Draw(SpriteBatch batch, BitmapFont font) {
        font.draw(batch, text, x, y);
    }

    public String getText() {
        return text;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getHeight() {
        return height;
    }
    public float getWidth() {
        return width;
    }

    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
}
