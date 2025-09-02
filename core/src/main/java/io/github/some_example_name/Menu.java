package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;

import static com.badlogic.gdx.Gdx.input;

public class Menu {
    List<Button> buttons;


}

class Button {
    private String text;
    private Runnable action;
    private Rect buttonShape;
    private float xText, yText;

    public Button(String text, Runnable action, int x, int y, int width, int height) {
        this.text = text;
        this.action = action;
        this.buttonShape = new Rect(x, y, width, height, new Color(0.42f, 0.42f, 0.43f, 0.25f));

        this.xText = 0;
        this.yText = 0;
    }
    public void CheckClick() {
        if (buttonShape.isPointInShape(new FloatPoint(Gdx.input.getX(), GameData.getInstance().getScreenHeight() - Gdx.input.getY()))) {
            if (input.isButtonJustPressed(Input.Buttons.LEFT)) {
                GameData.getInstance().timers.runAfter(0.1f, action);
            }
        }
    }

    public void FindTextPosition() {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text);
    }

    public void DrawButton(ShapeRenderer sr) {
        if (buttonShape.isPointInShape(new FloatPoint(Gdx.input.getX(), GameData.getInstance().getScreenHeight() - Gdx.input.getY()))) {
            sr.setColor(new Color(0.42f, 0.42f, 0.43f, 0.25f));
            buttonShape.Draw(sr);
        }
    }
    public void DrawText(SpriteBatch batch, BitmapFont font) {
        font.draw(batch, text, xText, yText);
    }
    public void Draw(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        DrawButton(sr);
        DrawText(batch, font);
    }
}
