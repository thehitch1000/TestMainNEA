package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import static com.badlogic.gdx.Gdx.input;

public class Level {
    Player player;
    Monster monster;
    ShapeBuffer[] obstacles;

    public Level() {
        player = new Player(40);
        monster = new Monster();
    }



    public void AddBox() {

    }
    public void AddSpike() {

    }

    public void CheckSlowDown() {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            GameData.getInstance().setSpeedMulti(0.5f);
        } else {
            GameData.getInstance().setDefaultSpeeds();
        }
    }

    public void CheckJumping(){
        if (input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (!player.isFalling() && !player.isJumping() && !player.isTipping()) {
                player.setJumping(true);
                GameData.getInstance().setPlayerSpeedY(30);
            }
        }
    }
    public void CheckTipping() {
        if (!player.isTipping() && (player.isJumping() || player.isFalling())) {
            player.CheckTipOver();
            if (player.isTipping()) {
                player.setJumping(false);
                player.setFalling(false);
                GameData.getInstance().setPlayerSpeedY(0);
                int angle = player.FindAngleTillFlat();
                if (angle > 45) {
                    angle = angle - 90;
                }
                if (angle > 0) {
                    player.setClockwise(true);
                } else {
                    player.setClockwise(false);
                }
            }
        }
    }
    public void CheckingFalling() {
        if (!player.isFalling() && !player.isJumping() && !player.isTipping()) {
            for (Box box : boxes) {
                if (box.isDisplay()) {
                    if (((player.getX() + player.getWidth() / 2f) + 4f >= box.getX() + box.getWidth()
                        && (player.getX() + player.getWidth() / 2f) - 4f <= box.getX() + box.getWidth())
                        && player.LowestPoint.getY() == box.getY() + box.getHeight()) {
                        player.Rotate(-27);
                        player.setFalling(true);
                    }
                }
            }
        }
    }
    public void CheckPlayerMovement() {
        CheckJumping();
        CheckingFalling();
        CheckTipping();
    }

    public void Falling() {
        if (player.getState() == Player.State.FALLING) {
            GameData.getInstance().setPlayerSpeedY(GameData.getInstance().getPlayerSpeedY() - 3);
            player.Rotate(-9, player.midPoint);
        }
    }
    public void Tipping() {
        if (player.getState() == Player.State.TIPPING) {
            if (player.FindAngleTillFlat() == 0) {
                player.setTipping(false);
                player.trail.setTargetTime(GameData.getInstance().getElapsedTime() + 200);
            }
            if (player.isClockwise()) {
                player.TipOver(9);
            } else {
                player.TipOver(-9);
            }
        }
    }
    public void Jumping() {
        if (player.getState() == Player.State.JUMPING) {
            GameData.getInstance().setPlayerSpeedY(GameData.getInstance().getPlayerSpeedY() - 3);
            player.Rotate(-9);
        }
    }
    public void PlayerMovement(){
        Falling();
        Tipping();
        Jumping();
    }
}
