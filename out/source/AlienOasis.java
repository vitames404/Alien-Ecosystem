/* autogenerated by Processing revision 1293 on 2024-05-07 */
import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class AlienOasis extends PApplet {

// Ecosystem project
ArrayList<Shroom> glowshrooms;
ArrayList<Prey> prey;
ArrayList<Food> foodItems;
ArrayList<Predator> predators;

int availableMinerals = 100;
int initialCount = 12;
int initialShroom = 8;
int foodSpawnRate = 50;
int shroomSpawnRate = 50;
long lastFoodSpawnTime = 0;
long lastShroomSpawnTime = 0;

long timer;

public void setup() {
    /* size commented out by preprocessor */;
    background(30);
    initializeEntities();

    timer = millis();
}

public void initializeEntities() {
    predators = new ArrayList<Predator>();
    foodItems = new ArrayList<Food>();
    prey = new ArrayList<Prey>();
    glowshrooms = new ArrayList<Shroom>();

    for (int i = 0; i < initialCount; i++) {
        prey.add(new Prey(random(width), random(height)));
    }
    for (int i = 0; i < 1; i++) {
        predators.add(new Predator(random(width), random(height)));
    }
    for (int i = 0; i < initialShroom; i++) {
        glowshrooms.add(new Shroom(random(width), random(height), random(50, 150)));
    }
}

public void draw() {
    background(30);
    updateWorld();
    displayEntities();
    checkIfFinished();
}

public void updateWorld() {
    long currentTime = millis();
    if (currentTime - lastFoodSpawnTime > foodSpawnRate) {
        spawnFood();
        lastFoodSpawnTime = currentTime;
    }
    if (currentTime - lastShroomSpawnTime > shroomSpawnRate) {
        spawnShroom(currentTime);
        lastShroomSpawnTime = currentTime;
    }
    updatePrey(currentTime);
    updatePredators(currentTime);
    updateShroom(currentTime);
}

public void displayEntities() {
    for (Food food : foodItems) food.display();
    for (Shroom shroom : glowshrooms) shroom.display();
    for (Predator predator : predators) predator.display();
    for (Prey p : prey) p.display();
}

public void updatePrey(long currentTime) {
    for (int i = 0; i < prey.size(); i++) {
        Prey p = prey.get(i);
        p.move();
        checkCollisionsWithFood(p);
        p.checkReproduction(currentTime);
        if (p.health <= 0) {
            prey.remove(i--);
        }
    }
}

public void updateShroom(long currentTime){
    for(int i = 0; i < glowshrooms.size(); i++){
        Shroom s = glowshrooms.get(i);

        int preyQuantity = s.checkPreysInArea(prey);

        s.updateHealth(preyQuantity);

        if(s._health <= 0){
            glowshrooms.remove(s);
        }

    }
}

public void updatePredators(long currentTime) {

    for(int i = 0; i < predators.size(); i++){
        Predator p = predators.get(i);
        p.checkReproduction(currentTime);
        p.move();
        checkPredatorCollisions(p);
        if(p.health <= 0){
            predators.remove(p);
        }
    }
}

public void checkPredatorCollisions(Predator predator) {
    if(!predator.pregnant){
        for (int i = 0; i < prey.size(); i++) {
            if (checkCollision(prey.get(i), predator)) {
                predator.eat();
                prey.remove(i--);
                predator.targetPrey = null;
            }
        }
    }
}

public void spawnFood() {
    float x = random(width);
    float y = random(height);

    if (x < 0) x = 0;
    if (x > width) x = width;
    if (y < 0) y = 0;
    if (y > height) y = height;

    boolean inLight = false;

    // Check if the random position is within any shroom's light radius
    for (Shroom shroom : glowshrooms) {
        float distance = dist(x, y, shroom.position.x, shroom.position.y);
        if (distance < shroom._lightRadius) {
            inLight = true;
            break;
        }
    }

    // Higher chance to spawn food within the light radius
    if (inLight || random(1) < 0.1f) {  // 10% chance outside light radius
        foodItems.add(new Food(x, y));
    }
}

public void spawnShroom(long currentTime) {
    if (availableMinerals > glowshrooms.size() * 10) {
        float x = random(width);
        float y = random(height);

        if (x < 0) x = 0;
        if (x > width) x = width;
        if (y < 0) y = 0;
        if (y > height) y = height;

        glowshrooms.add(new Shroom(x, y, random(50, 150)));
    }
}

public boolean checkCollision(Organism a, Organism b) {
    float distance = dist(a.position.x, a.position.y, b.position.x, b.position.y);
    return distance < (a.radius + b.radius);
}

public void checkCollisionsWithFood(Prey p) {
    for (int j = 0; j < foodItems.size(); j++) {
        if (checkCollision(foodItems.get(j), p)) {
            p.eat(foodItems.remove(j)); // Assuming Prey has a method to 'eat'
            j--; // Adjust the index after removal
        }
    }
}

public void checkIfFinished(){

    long currentTime = millis();

    if(predators.size() < 1 || prey.size() <  1){
        println(((currentTime - timer) / 1000) / 60);
        exit();
    }
}

class Food extends Organism{

    Food(float x, float y) {
        super(x, y, 4);
    }

    public void display() {
        fill(0, 255, 0); // Green color for food
        noStroke();
        ellipse(position.x, position.y, radius * 2, radius * 2); // Draw food as a small circle
    }

    public void move(){

    }

}
abstract class Organism {
    PVector position;
    float radius;

    Organism(float x, float y, float r) {
        position = new PVector(x, y);
        radius = r;
    }

    public abstract void move();
    public abstract void display();

    public void wrapAround() {
        if (position.x > width) position.x = 0;
        else if (position.x < 0) position.x = width;
        if (position.y > height) position.y = 0;
        else if (position.y < 0) position.y = height;
    }
}
class Predator extends Organism {

    int radius = 20;
    int health = 100;
    Prey targetPrey = null;
    float detectionRadius = 150;
    float speed = 1;
    boolean pregnant = false;

    boolean reproducing = false;
    int preyEaten = 0;

    float lastHealthUpdateTime = 0;
    float lastEatTime = 0;
    float reproductionThreshold = 10000;

    // Adicionando atributos para a direção aleatória e controle de tempo
    PVector randomDirection = PVector.random2D();
    int directionChangeInterval = 2000; // tempo em milissegundos para mudar de direção
    long lastDirectionChangeTime = 0; // para rastrear a última mudança de direção

    Predator(float x, float y) {
        super(x, y, 20);
    }

    public void move() {
        findClosestPrey();
        PVector closestCorner = findClosestCorner(); // Declare e atribua a variável closestCorner

        if(pregnant){

            if(closestCorner != null){
                PVector directionToCorner = PVector.sub(closestCorner, position); // Corrija a atribuição aqui
                directionToCorner.normalize();
                directionToCorner.mult(speed);
                position.add(directionToCorner);

                if(PVector.dist(position, closestCorner) < 10)
                {
                    reproduce();
                    pregnant = false;
                }
            }
        }
        else{
            if (targetPrey != null) {
                PVector directionToPrey = PVector.sub(targetPrey.position, position);
                directionToPrey.normalize();
                directionToPrey.mult(speed);
                position.add(directionToPrey);

                if (PVector.dist(position, targetPrey.position) < 10) { // Assumindo um raio de "comer"
                    // Adicione lógica aqui para remover a presa da lista
                    targetPrey = null; // Limpa o alvo após comer
                    findClosestPrey(); // Procura por nova presa imediatamente
                }
            } else {
                long currentTime = millis();
                if (currentTime - lastDirectionChangeTime > directionChangeInterval) {
                    randomDirection = PVector.random2D();
                    randomDirection.mult(speed);
                    lastDirectionChangeTime = currentTime; // Atualiza o tempo da última mudança
                }
                position.add(randomDirection);
            }
        }

        updateHealth();
        wrapAround();
    }

    public void eat(){
        preyEaten++;
        lastEatTime = millis();
    }

    public void display() {
        fill(100, 100, 200, 15); // Cor de fundo semi-transparente para a área de detecção
        ellipse(position.x, position.y, radius * 2, radius * 2); // Desenha a área de detecção

        fill(255, 0, 0); // Cor vermelha para visibilidade

        if(pregnant){
            fill(255, 154, 154);
        }
        if(targetPrey != null){
            fill(151, 0, 0);
        }

        // Determina o ângulo atual de movimento
        float angle;
        if (targetPrey != null) {
            angle = atan2(targetPrey.position.y - position.y, targetPrey.position.x - position.x);
        } else {
            angle = atan2(randomDirection.y, randomDirection.x);
        }

        // Aplica transformações para rotacionar e desenhar o triângulo na direção correta
        pushMatrix();
        translate(position.x, position.y);
        rotate(angle + HALF_PI); // Adiciona HALF_PI para ajustar a orientação do triângulo
        triangle(0, -radius, -radius / 2, radius / 2, radius / 2, radius / 2);
        popMatrix();
    }

    public void findClosestPrey() {
        float minDistance = Float.MAX_VALUE;
        int counter = 0;
        Prey currentPrey = null;
        float distance = 0;

        for (Prey p : prey) {
            distance = PVector.dist(position, p.position);
            if (distance < detectionRadius && distance < minDistance) {
                currentPrey = p;
                counter++;        
            }
        }

        if(counter == 1 && currentPrey != null){
            minDistance = distance;
            targetPrey = currentPrey;
        }
        else
        {
            targetPrey = null;
        }
    }

    public void updateHealth() {
        float currentTime = millis();
        if (currentTime - lastHealthUpdateTime > 1000 && !pregnant) {
            health -= 3;
            lastHealthUpdateTime = currentTime;
        }
    }

    public void checkReproduction(float currentTime) {
        if (preyEaten >= 2) {
            pregnant = true;
            preyEaten = 0;
        } 
    }

    public void reproduce() {
        predators.add(new Predator(position.x + random(-10, 10), position.y + random(-10, 10)));
    }

    public PVector findClosestCorner(){
        // Calculate distances to each corner
        float dTopLeft = PVector.dist(position, new PVector(0, 0));
        float dTopRight = PVector.dist(position, new PVector(width, 0));
        float dBottomLeft = PVector.dist(position, new PVector(0, height));
        float dBottomRight = PVector.dist(position, new PVector(width, height));

        // Find the closest corner
        PVector closestCorner = null;
        float minDistance = Float.MAX_VALUE;
        if (dTopLeft < minDistance) {
            minDistance = dTopLeft;
            closestCorner = new PVector(0, 0);
        }
        if (dTopRight < minDistance) {
            minDistance = dTopRight;
            closestCorner = new PVector(width, 0);
        }
        if (dBottomLeft < minDistance) {
            minDistance = dBottomLeft;
            closestCorner = new PVector(0, height);
        }
        if (dBottomRight < minDistance) {
            closestCorner = new PVector(width, height);
        }
        return closestCorner;
    }
}
class Prey extends Organism {
    float health = 100;
    float lastHealthUpdateTime = 0;
    float healthDecayRate = 3;  // Health lost per second
    float speed = 1;
    float red = 100;
    float green = 255;
    boolean markedForRemoval = false;
    int foodEaten = 0;
    float lastEatTime = 0;  // Time since last eating event
    float detectionRadius = 150;  // Radius in which prey can detect food
    float reproductionThreshold = 10000;

    PVector randomDirection = PVector.random2D();
    int directionChangeInterval = 2000; // tempo em milissegundos para mudar de direção
    long lastDirectionChangeTime = 0; 

    Prey(float x, float y) {
        super(x, y, 8);
    }

    public void move() {
        updateMovement();
        updateHealth();
        wrapAround();
    }

    public void updateMovement() {
        
        PVector targetDirection = findTargetDirection();
        
        if(checkInLight()){
            
            if(targetDirection != null){

                targetDirection.mult(speed);
                position.add(targetDirection);
            }
            else{

                targetDirection = PVector.random2D();

                targetDirection.mult(speed * 2);
                position.add(targetDirection);
            }
        }
        else
        {
            long currentTime = millis();
            
            if(targetDirection != null){
                targetDirection.mult(speed);
                position.add(targetDirection);
            }
            else{
                if (currentTime - lastDirectionChangeTime > directionChangeInterval) {
                    randomDirection = PVector.random2D();
                    randomDirection.mult(speed);
                    lastDirectionChangeTime = currentTime; // Atualiza o tempo da última mudança
                }
                position.add(randomDirection);
            }
        }
    }

    public PVector findTargetDirection() {
        PVector closestFoodPosition = findClosestFoodWithinRadius();
        if (closestFoodPosition != null) {
            return PVector.sub(closestFoodPosition, position).normalize();
        }
        return null; // Default to random direction if no food is close
    }

    public PVector findClosestFoodWithinRadius() {
        PVector closestFood = null;
        float minDistance = Float.MAX_VALUE;
        for (Food food : foodItems) {
            float distance = PVector.dist(position, food.position);
            if (distance < detectionRadius && distance < minDistance) {
                minDistance = distance;
                closestFood = food.position;
            }
        }
        return closestFood;
    }

    public void updateHealth() {
        float currentTime = millis();

        float x = this.position.x;
        float y = this.position.y;

        boolean inLight = false;

        // Check if the random position is within any shroom's light radius
        for (Shroom shroom : glowshrooms) {
            float distance = dist(x, y, shroom.position.x, shroom.position.y);
            if (distance < shroom._lightRadius) {
                inLight = true;
                break;
            }
        }

        if(!inLight){
            if (currentTime - lastHealthUpdateTime > 1000) {
                health -= healthDecayRate;
                lastHealthUpdateTime = currentTime;
                updateColorsBasedOnHealth();
            }
        }
    }

    public void updateColorsBasedOnHealth() {
        green = constrain(green - healthDecayRate * 2, 0, 255);
        red = constrain(red + healthDecayRate * 2, 0, 255);
    }

    public void eat(Food food) {
        health = constrain(health + 20, 0, 100);
        green = constrain(green + 40, 0, 255);
        red = constrain(red - 40, 0, 255);
        foodEaten++;
        lastEatTime = millis();
    }

    public void checkReproduction(float currentTime) {
        if (foodEaten > 1 && currentTime - lastEatTime <= reproductionThreshold) {
            reproduce();
            foodEaten = 0;
        } else if (currentTime - lastEatTime > reproductionThreshold) {
            foodEaten = 0;
        }
    }

    public void reproduce() {
        prey.add(new Prey(position.x + random(-10, 10), position.y + random(-10, 10)));
    }

    public void display() {
        fill(red, green, 0);
        ellipse(position.x, position.y, radius * 2, radius * 2);
    }

    public boolean checkInLight(){
        float x = this.position.x;
        float y = this.position.y;

        // Check if the random position is within any shroom's light radius
        for (Shroom shroom : glowshrooms) {
            float distance = dist(x, y, shroom.position.x, shroom.position.y);
            if (distance < shroom._lightRadius) {
                return true;
            }
        }

        return false;

    }


}
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

    @Override public 
    void move() {

    }

    @Override public 
    void display() {

        fill(0, 250, 250);  // Cyan color for the shroom
        noStroke();
        ellipse(position.x, position.y, radius * 2, radius * 2);  // Draw shroom itself

        // Draw light area
        fill(255, 204, 0, 5);  // Semi-transparent light area, adjusted alpha for visibility
        ellipse(position.x, position.y, _lightRadius * 2, _lightRadius * 2);
    }

    public void updateHealth(int preyQuantity) {
        float currentTime = millis();

        if (currentTime - lastHealthUpdateTime > 1000) {
            _health -= 2 * preyQuantity;
            lastHealthUpdateTime = currentTime;
        }
    }

    public int checkPreysInArea(ArrayList<Prey> preys){
    
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


  public void settings() { size(1280, 720); }

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "AlienOasis" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}