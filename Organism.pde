abstract class Organism {
    protected PVector position;
    protected float radius;

    Organism(float x, float y, float r) {
        position = new PVector(x, y);
        radius = r;
    }

    abstract void move(ArrayList<Organism> orgs);
    abstract void display();

    void wrapAround() {
        if (position.x > width) position.x = 0;
        else if (position.x < 0) position.x = width;
        if (position.y > height) position.y = 0;
        else if (position.y < 0) position.y = height;
    }
}
