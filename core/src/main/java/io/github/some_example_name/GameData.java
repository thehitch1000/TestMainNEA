package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameData {
    private int ScreenWidth, ScreenHeight, frameNumber;
    private boolean Stop;
    private float BackgroundSpeed, elapsedTime;
    FunctionTimer timers;
    List<FunctionLock> locks;
    ArrayList<FunctionFrameTimer> frameTimers = new ArrayList<>();

    private static GameData instance = null;

    public static GameData getInstance() {
        if (instance == null) {
            instance = new GameData(0.25f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        return instance;
    }

    public GameData(float BackgroundSpeed,
                    int ScreenWidth, int ScreenHeight) {
        this.Stop = false;
        this.elapsedTime = 0;
        this.BackgroundSpeed = BackgroundSpeed;
        this.ScreenWidth = ScreenWidth;
        this.ScreenHeight = ScreenHeight;

        this.timers = new FunctionTimer();
        this.locks = new ArrayList<>();
    }

    public void Maintenance() {
        elapsedTime += Gdx.graphics.getDeltaTime() * 1000;
        frameNumber++;
        CheckFrameTimers();
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

    public float getBackgroundSpeed() {
        return BackgroundSpeed;
    }
    public void setBackgroundSpeed(float backgroundSpeed) {
        this.BackgroundSpeed = backgroundSpeed;
    }

    public int getScreenWidth() {
        return ScreenWidth;
    }
    public int getScreenHeight() {
        return ScreenHeight;
    }

    public int getFrameNumber() {
        return frameNumber++;
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

    public void CheckFrameTimers() {
        for (FunctionFrameTimer timer : frameTimers) {
            if (timer.getFrameNumber() >= frameNumber) {
                timer.function.run();
                frameTimers.remove(timer);
                return;
            }
        }
    }
    public void clearFrameTimers() {
        frameTimers.clear();
    }
    public void runFunctionAfterFrames(int frameNumber, Runnable function) {
        FunctionFrameTimer timer = new FunctionFrameTimer(function, frameNumber);
        frameTimers.add(timer);
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

    public boolean isUsed() {
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

    public Timer.Task runTasksAfter(float seconds, Runnable[] runnable) {
        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                for (Runnable r : runnable) {
                    r.run();
                }
                ActiveTasks.remove(this);
            }
        };

        Timer.schedule(task, seconds);
        ActiveTasks.add(task);

        return task;
    }

    public Timer.Task runRepeating(float seconds, float interval, Runnable runnable) {
        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                runnable.run();
            }
        };

        Timer.schedule(task, seconds, interval);
        ActiveTasks.add(task);

        return task;
    }

    public void runRepeatingUntil(float seconds, float interval, float ending, Runnable runnable) {
        Timer.Task task = new Timer.Task() {
            float elapsed = 0;
            @Override
            public void run() {
                runnable.run();
                elapsed += interval;
                if (elapsed >= ending) {
                    this.cancel();
                    ActiveTasks.remove(this);
                }
            }
        };

        Timer.schedule(task, seconds, interval);
        ActiveTasks.add(task);
    }
}

class FunctionFrameTimer {
    private int frameNumber;
    Runnable function;

    public FunctionFrameTimer(Runnable function, int frameNumber) {
        this.frameNumber = frameNumber;
        this.function = function;
    }

    public int getFrameNumber() {
        return frameNumber;
    }
}
