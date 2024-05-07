// Shroom.pde
class Shroom extends Organism { 

    protected float _lightRadius;
    protected float _health;
    protected float _lightIntensity; // Novo atributo para intensidade da luz

    float lastHealthUpdateTime = 0;

    Shroom(float x, float y, float lightRadius) {
        super(x, y, 10);
        _lightRadius = lightRadius;
        _health = 100;  // Pass the radius as part of the constructor to the Organism class   
    }

    @Override
    void move() {

    }

    @Override
    void display() {

        fill(0, 250, 250);  // Cyan color for the shroom
        noStroke();
        ellipse(position.x, position.y, radius * 2, radius * 2);  // Draw shroom itself

        // Draw light area
        fill(255, 204, 0, 5);  // Semi-transparent light area, adjusted alpha for visibility
        ellipse(position.x, position.y, _lightRadius * 2, _lightRadius * 2);
    }

    void updateHealth(int preyQuantity) {
        float currentTime = millis();

        if (currentTime - lastHealthUpdateTime > 1000) {
            _health -= 2 * preyQuantity;
            lastHealthUpdateTime = currentTime;
        }
    }

    int checkPreysInArea(ArrayList<Prey> preys){
    
        int counter = 0;

        for(Prey p : preys){

            float distance = dist(p.position.x, p.position.y, this.position.x, this.position.y);

            if(distance < this._lightRadius){
                counter++;
            }
      }

    return counter;

    }

}