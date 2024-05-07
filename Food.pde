class Food extends Organism{

    Food(float x, float y) {
        super(x, y, 4);
    }

    void display() {
        fill(0, 255, 0); // Green color for food
        noStroke();
        ellipse(position.x, position.y, radius * 2, radius * 2); // Draw food as a small circle
    }

    void move(ArrayList<Organism> orgs){

    }

}
