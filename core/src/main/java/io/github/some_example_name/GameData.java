package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameData {
    private int ScreenWidth, ScreenHeight, PlayerSpeedY, BackgroundBaseSpeed, ObstacleSpeed, TrailSpeed;
    private boolean Stop;
    private float BackgroundSpeed, elapsedTime;
    FunctionTimer timers;
    List<FunctionLock> locks = new ArrayList<>();

    private static GameData instance = null;

    public static GameData getInstance() {
        if (instance == null) {
            instance = new GameData(-8,0.25f,-8,-2, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        }
        return instance;
    }

    public GameData (int BackgroundBaseSpeed, float BackgroundSpeed, int ObstacleSpeed, int TrailSpeed,
                    int ScreenWidth, int ScreenHeight) {
        this.Stop = false;
        this.elapsedTime = 0;
        this.PlayerSpeedY = 0;
        this.BackgroundBaseSpeed = BackgroundBaseSpeed;
        this.BackgroundSpeed = BackgroundSpeed;
        this.ObstacleSpeed = ObstacleSpeed;
        this.TrailSpeed = TrailSpeed;
        this.ScreenWidth = ScreenWidth;
        this.ScreenHeight = ScreenHeight;

        this.timers = new FunctionTimer();
        this.locks = new ArrayList<>();
    }

    public void Maintenance() {
        elapsedTime += Gdx.graphics.getDeltaTime();
        resetLocks();
    }

    public void setStop(boolean stop) {
        this.Stop = stop;
    }
    public boolean isStop() {
        return Stop;
    }

    public void setElapsedTime(float elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    public float getElapsedTime() {
        return elapsedTime;
    }

    public int getPlayerSpeedY() {
        return PlayerSpeedY;
    }
    public void setPlayerSpeedY(int playerSpeed) {
        this.PlayerSpeedY = playerSpeed;
    }

    public int getBackgroundBaseSpeed() {
        return BackgroundBaseSpeed;
    }
    public void setBackgroundBaseSpeed(int backgroundBaseSpeed) {
        this.BackgroundBaseSpeed = backgroundBaseSpeed;
    }

    public float getBackgroundSpeed() {
        return BackgroundSpeed;
    }
    public void setBackgroundSpeed(float backgroundSpeed) {
        this.BackgroundSpeed = backgroundSpeed;
    }

    public int getObstacleSpeed() {
        return ObstacleSpeed;
    }
    public void setObstacleSpeed(int obstacleSpeed) {
        this.ObstacleSpeed = obstacleSpeed;
    }

    public int getTrailSpeed() {
        return TrailSpeed;
    }
    public void setTrailSpeed(int trailSpeed) {
        this.TrailSpeed = trailSpeed;
    }

    public int getScreenWidth() {
        return ScreenWidth;
    }
    public int getScreenHeight() {
        return ScreenHeight;
    }

    public void setDefaultSpeeds() {
        setBackgroundBaseSpeed(-8);
        setBackgroundSpeed(0.25f);
        setObstacleSpeed(-8);
        setTrailSpeed(-2);
    }
    public void setSpeedMulti(float multi) {
        setBackgroundBaseSpeed((int) (-8 * multi));
        setBackgroundSpeed(0.25f * multi);
        setObstacleSpeed((int) (-8 * multi));
        setTrailSpeed((int) (-2 * multi));
    }
    public void setAllSpeedsToZero() {
        setBackgroundBaseSpeed(0);
        setBackgroundSpeed(0);
        setObstacleSpeed(0);
        setTrailSpeed(0);
        setPlayerSpeedY(0);
    }

    public void EmptyFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void CreateFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            System.out.println("File created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void CopyFile(String startFile, String endFile) {
        Gdx.files.local(startFile).copyTo(Gdx.files.local(endFile));
    }

    public void resetLocks() {
        locks.forEach(lock -> lock.reset());
    }
}

class FunctionLock {
    private boolean used;

    public FunctionLock() {
        this.used = false;
    }

    public void used() {
        used = true;
    }
    public void reset() {
        used = false;
    }

    public boolean getState() {
        return used;
    }
}

class FunctionTimer {
    private List<Timer.Task> ActiveTasks = new ArrayList<>();

    public Timer.Task runAfter(float seconds, Runnable runnable) {
        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                runnable.run();
                ActiveTasks.remove(this);
            }
        };

        Timer.schedule(task, seconds);
        ActiveTasks.add(task);

        return task;
    }

    public Timer.Task runRepeating(float interval, float seconds, Runnable runnable) {
        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                runnable.run();
            }
        };

        Timer.schedule(task, interval, seconds);
        ActiveTasks.add(task);

        return task;
    }
}
