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
    private boolean open;
    private float xTitle, yTitle, xCentre, yCentre;
    List<Button> buttons;
    String title;


    public Menu(String title, float xCentre, float yCentre, BitmapFont font) {
        buttons = null;
        this.title = title;
        this.open = false;
        this.xTitle = 0;
        this.yTitle = 0;

        this.xCentre = 0;
        this.yCentre = 0;

        setTitle(font);
    }

    public void setTitle(BitmapFont font) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, title);

        xTitle = xCentre - layout.width / 2;
        yTitle = yCentre - layout.height / 2;
    }


    public void AddButton(Button button) {
        buttons.add(button);
    }

    public void Draw(ShapeRenderer sr, SpriteBatch batch, BitmapFont font) {
        if (open) {
            buttons.forEach(button -> button.Draw(sr, batch, font));
            font.draw(batch, title, xTitle, yTitle);
        }
    }
    public void CheckClick() {
        buttons.forEach(button -> button.CheckClick());
    }
    public void Open() {
        open = true;
    }
    public void Close() {
        open = false;
    }

}

class Button {
    private String text;
    private Runnable action;
    private Rect buttonShape;
    private float xText, yText;
    private float centreX, centreY;

    public Button(String text, Runnable action, float centreX, float centreY) {
        this.text = text;
        this.action = action;
        this.buttonShape = null;

        this.xText = 0;
        this.yText = 0;

        this.centreX = centreX;
        this.centreY = centreY;
    }
    public void CheckClick() {
        if (buttonShape.isPointInShape(new FloatPoint(Gdx.input.getX(), GameData.getInstance().getScreenHeight() - Gdx.input.getY()))) {
            if (input.isButtonJustPressed(Input.Buttons.LEFT)) {
                GameData.getInstance().timers.runAfter(0.1f, action);
            }
        }
    }

    public void FindTextPosition(BitmapFont font) {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text);

        xText = centreX - layout.width / 2;
        yText = centreY - layout.height / 2;

        buttonShape = new Rect(xText - 10, yText - 10, layout.width + 20, layout.height + 20, new Color(0.42f, 0.42f, 0.43f, 0.25f));
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
