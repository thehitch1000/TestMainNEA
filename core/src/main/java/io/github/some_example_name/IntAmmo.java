package io.github.some_example_name;

public interface IntAmmo {
    void Draw(ShapeRenderer sr){}
}

abstract class Ammo implemtents IntAmmo {
    protected Shape shape;
    protected LineSegment[] path;
    
    public void Draw(ShapeRenderer sr){}
    
}

class bullet extends Ammo {

    
    public bullet() {
        shape = new Tri();
        path = new LineSegment[1]
    }
    
}

class midsile extends Ammo {


    publxic bullet(int pathLength) {
        shape = new Circle();
        path = new LineSegment[pathLength];
    }
    
}    