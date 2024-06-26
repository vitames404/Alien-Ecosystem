/* autogenerated by Processing revision 1293 on 2024-05-08 */
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

ArrayList<Organism> organisms;
ArrayList<Food> foodItems;

int availableMinerals = 100;
int initialCount = 12;
int initialShroom = 8;
int foodSpawnRate = 200;
int shroomSpawnRate = 50;
long lastFoodSpawnTime = 0;
long lastShroomSpawnTime = 0;

long timer;
PImage preySprite;
PImage predatorSprite;

// Global variables for message display
String message = "";
long messageDisplayTime = 0;
int messageDuration = 2000; // Message display duration in milliseconds

int predatorsCount;
int preyCount;

int predSpeed = 1;

boolean showInstructions = false;

public void setup() {

    preySprite = loadImage("assets/Sprite-0001.png");
    predatorSprite = loadImage("assets/Sprite-0002.png");

    /* size commented out by preprocessor */;
    background(30);
    initializeEntities();

    timer = millis();
}

public void initializeEntities() {

    organisms = new ArrayList<Organism>();
    foodItems = new ArrayList<Food>();

    for (int i = 0; i < initialCount; i++) {
        organisms.add(new Prey(random(width), random(height), preySprite));
    }
    for (int i = 0; i < 1; i++) {
        organisms.add(new Predator(random(width), random(height), predatorSprite, predSpeed));
    }
    for (int i = 0; i < initialShroom; i++) {
        organisms.add(new Shroom(random(width), random(height), random(50, 150)));
    }
}

public void draw() {
    background(30);
    updateWorld();
    displayEntities();
    displayMessage();
    checkIfFinished();
    
    if (showInstructions) {
        displayInstructions();
    } else {
        displayMessage();
    }

    fill(255); // White text
    textSize(16);
    text("Number of preys: " + preyCount, 100, 20);
    text("Number of predators: " + predatorsCount, 100, 40);
}

public void keyPressed() {
    if (key == ESC) {
        showInstructions = !showInstructions; // Toggle instructions display
        key = 0; // Prevent default behavior of ESC key
    } else if(millis() - 0 > 500){
        switch (key) {
            case '+':
                foodSpawnRate -= 10;
                if (foodSpawnRate < 10) foodSpawnRate = 10; // Prevent too fast spawning
                message = "Increased Food Spawn Rate: " + foodSpawnRate;
                break;

            case '-':
                foodSpawnRate += 10;
                message = "Decreased Food Spawn Rate: " + foodSpawnRate;
                break;

            case 'j':
                availableMinerals -= 10;
                message = "Decreased minerals available | Shroom spawn rate: " + availableMinerals;
                break;

            case 'k':
                availableMinerals += 10;
                message = "Increased minerals available | Shroom spawn rate: " + availableMinerals;
                break;

            case 's':
                message = "Predators speed increased!";
                changePredatorSpeed(0.2f);
                break;

            case 'd':
                message = "Predators speed decreased";
                changePredatorSpeed(-0.2f);
                break;

            case 'e':
                message = "Preys speed increased!";
                changePreySpeed(0.2f);
                break;

            case 'r':
                message = "Preys speed decreased";
                changePreySpeed(-0.2f);
                break;

            default:
                break;
        }

        messageDisplayTime = millis();
    }
}

public void changePreySpeed(float amount){
    for(int i = 0; i < organisms.size(); i++){
    
        Organism org = organisms.get(i);

        if(org instanceof Prey){

            Prey p = (Prey) org;

            p.speed += amount;
        
        }
    }
}

public void changePredatorSpeed(float amount){

    for(int i = 0; i < organisms.size(); i++){
    
        Organism org = organisms.get(i);

        if(org instanceof Predator){

            Predator p = (Predator) org;

            p.speed += 0.2f;

        }
    }   
}

public void displayMessage() {
    if (millis() - messageDisplayTime < messageDuration) {
        fill(255); // White text
        textSize(16);
        textAlign(CENTER);
        text(message, width / 2, 20);
    }
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
    updateShroom(currentTime);
    updatePrey(currentTime);
    updatePredators(currentTime);
}

public void displayEntities() {
    for (Food food : foodItems) food.display();
    for (Organism org : organisms) org.display();
}

public void updatePrey(long currentTime) {
    for (int i = 0; i < organisms.size(); i++) {
       
        Organism org = organisms.get(i);

        if(org instanceof Prey){

            Prey p = (Prey) org;         
        
            p.move(organisms);
            checkCollisionsWithFood(p);
            p.checkReproduction(currentTime, organisms);
            
            if (p.health <= 0) {
                organisms.remove(i--);
            }
        }
    }
}

public void updateShroom(long currentTime) {
    
    for (int i = 0; i < organisms.size(); i++) {
        
        Organism org = organisms.get(i);
        
        if (org instanceof Shroom) {
            
            Shroom s = (Shroom) org;
            
            int preyQuantity = 0;
            
            for (Organism otherOrg : organisms) {
                
                if (otherOrg instanceof Prey) {
                
                    preyQuantity += s.checkPreysInArea((Prey) otherOrg);
                
                }
            
            }
            
            s.updateHealth(preyQuantity);
            
            if (s._health <= 0) {
                organisms.remove(s);
            }
        }
    }
}

public void updatePredators(long currentTime) {

    for(int i = 0; i < organisms.size(); i++){
        
        Organism org = organisms.get(i);

        if(org instanceof Predator){

            Predator p = (Predator) org;

            p.checkReproduction(currentTime);
            p.move(organisms);
            checkPredatorCollisions(p);

            if(p.health <= 0){
                organisms.remove(p);
            }
        
        }
    }
}

public void checkPredatorCollisions(Predator predator) {
    if (!predator.pregnant) {
        for (int i = 0; i < organisms.size(); i++) {
            Organism org = organisms.get(i);
            if (org instanceof Prey && checkCollision(org, predator)) {
                //if(predator.hunting && org == predator.targetPrey){
                    predator.eat();
                    organisms.remove(i--);
                    predator.targetPrey = null;
                //}
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
    for (Organism org : organisms) {
        if(org instanceof Shroom){
            Shroom shroom = (Shroom) org;
            float distance = dist(x, y, shroom.position.x, shroom.position.y);
            if (distance < shroom._lightRadius) {
                inLight = true;
                break;
            }
        }
    }

    // Higher chance to spawn food within the light radius
    if (inLight || random(1) < 0.1f) {  // 10% chance outside light radius
        foodItems.add(new Food(x, y));
    }
}

public void spawnShroom(long currentTime) {

    int  shroomQuantity = 0;

    for (Organism org : organisms) {
        // Verifica se o organismo é um Shroom
        if (org instanceof Shroom) {
            shroomQuantity++;
        }
    }

    if (availableMinerals > shroomQuantity * 10) {
        float x = random(width);
        float y = random(height);

        if (x < 0) x = 0;
        if (x > width) x = width;
        if (y < 0) y = 0;
        if (y > height) y = height;

        organisms.add(new Shroom(x, y, random(50, 150)));
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

public void checkIfFinished() {
    long currentTime = millis();

    predatorsCount = 0;
    preyCount = 0;

    // Conta o número de predadores e presas na lista de organismos
    for (Organism org : organisms) {
        if (org instanceof Prey) {
            preyCount++;
        } else if (org instanceof Predator) {
            predatorsCount++;
        }
    }

    // Verifica se não há predadores ou presas restantes
    if (predatorsCount < 1 || preyCount < 1) {
        println(((currentTime - timer) / 1000) / 60);
        exit();
    }
}

public void displayInstructions() {
    fill(255);
    textSize(16);
    textAlign(CENTER);
    text("Instructions:\n" +
         "ESC: Toggle this instruction view\n" +
         "+   : Increase Food Spawn Rate by 10 (minimum rate: 10)\n" +
         "-   : Decrease Food Spawn Rate by 10\n" +
         "j   : Decrease available minerals by 10\n" +
         "k   : Increase available minerals by 10\n" +
         "s   : Increase predators' speed by 0.2 units\n" +
         "d   : Decrease predators' speed by 0.2 units\n" +
         "e   : Increase preys' speed by 0.2 units\n" +
         "r   : Decrease preys' speed by 0.2 units", width / 2, height / 2);
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

    public void move(ArrayList<Organism> orgs){

    }

}
abstract class Organism {
    protected PVector position;
    protected float radius;

    Organism(float x, float y, float r) {
        position = new PVector(x, y);
        radius = r;
    }

    public abstract void move(ArrayList<Organism> orgs);
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
    float speed;
    boolean pregnant = false;

    boolean reproducing = false;
    int preyEaten = 0;

    float lastHealthUpdateTime = 0;
    float lastEatTime = 0;
    float reproductionThreshold = 10000;

    boolean hunting = false;

    PImage sprite;

    // Adicionando atributos para a direção aleatória e controle de tempo
    PVector randomDirection = PVector.random2D();
    int directionChangeInterval = 2000; // tempo em milissegundos para mudar de direção
    long lastDirectionChangeTime = 0; // para rastrear a última mudança de direção

    Predator(float x, float y, PImage s, float spd) {
        super(x, y, 20);
        speed = spd;
        sprite = s;
    }

    public void move(ArrayList<Organism> orgs) {

        findClosestPrey(orgs);
        PVector closestCorner = findClosestCorner(); // Declare e atribua a variável closestCorner

        if(pregnant){

            if(closestCorner != null){
                PVector directionToCorner = PVector.sub(closestCorner, position); // Corrija a atribuição aqui
                directionToCorner.normalize();
                directionToCorner.mult(speed);
                position.add(directionToCorner);

                if(PVector.dist(position, closestCorner) < 10)
                {
                    reproduce(orgs);
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
                    findClosestPrey(orgs); // Procura por nova presa imediatamente
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

        // Set fill color based on predator state
        if (pregnant) {
            fill(255, 154, 154); // Pregnant color
        } else if (targetPrey != null) {
            fill(151, 0, 0); // Target prey color
        } else {
            noFill(); // No fill color
        }

        // Calculate angle based on target direction or random direction
        float angle;
        if (targetPrey != null) {
            angle = atan2(targetPrey.position.y - position.y, targetPrey.position.x - position.x);
        } else {
            angle = atan2(randomDirection.y, randomDirection.x);
        }

        // Apply transformations to rotate and draw the image in the correct direction
        pushMatrix();
        translate(position.x, position.y);
        rotate(angle); // Rotate the image to face the target direction
        imageMode(CENTER); // Set image mode to center
        sprite.resize(0, 30);
        image(sprite, 0, 0); // Draw the image at the translated and rotated position
        popMatrix();
    }


    public void findClosestPrey(ArrayList<Organism> orgs) {
        
        float minDistance = Float.MAX_VALUE;
        
        int counter = 0;
        Prey currentPrey = null;
        float distance = 0;

        for (Organism o : organisms) {
            if (o instanceof Prey) {
                Prey p = (Prey) o;
                distance = PVector.dist(position, p.position);
                if (distance < detectionRadius && distance < minDistance) {
                    currentPrey = p;
                    hunting = true;
                    counter++;        
                }
            }
        }

        if(counter <= 1 && currentPrey != null){
            minDistance = distance;
            targetPrey = currentPrey;
        }
        else
        {
            targetPrey = null;
            hunting = false;
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

    public void reproduce(ArrayList<Organism> orgs) {
        orgs.add(new Predator(position.x + random(-10, 10), position.y + random(-10, 10), predatorSprite, speed));
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

    PImage sprite;

    PVector randomDirection = PVector.random2D();
    int directionChangeInterval = 1000; // tempo em milissegundos para mudar de direção
    long lastDirectionChangeTime = 0; 

    Prey(float x, float y, PImage s) {
        super(x, y, 8);
        sprite = s;
    }

    public void move(ArrayList<Organism> orgs) {
        updateMovement(orgs);
        updateHealth(orgs);
        wrapAround();
    }

    public void updateMovement(ArrayList<Organism> orgs) {
        
        PVector targetDirection = findTargetDirection();
        
        if(checkInLight(orgs)){
            
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

    public void updateHealth(ArrayList<Organism> organisms) {
        float currentTime = millis();

        float x = this.position.x;
        float y = this.position.y;

        boolean inLight = false;

        // Check if the random position is within any shroom's light radius
        for (Organism org : organisms) {
            if (org instanceof Shroom) {
                Shroom shroom = (Shroom) org;
                float distance = dist(x, y, shroom.position.x, shroom.position.y);
                if (distance < shroom._lightRadius) {
                    inLight = true;
                    break;
                }
            }
        }

        if (!inLight) {
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
        if(health > 20){
            speed = 1;
        }
        green = constrain(green + 40, 0, 255);
        red = constrain(red - 40, 0, 255);
        foodEaten++;
        lastEatTime = millis();
    }

    public void checkReproduction(float currentTime, ArrayList<Organism> orgs) {
        if (foodEaten >= 2 && currentTime - lastEatTime <= reproductionThreshold) {
            reproduce(orgs);
            foodEaten = 0;
        } else if (currentTime - lastEatTime > reproductionThreshold) {
            foodEaten = 0;
        }
    }

    public void reproduce(ArrayList<Organism> orgs) {
        orgs.add(new Prey(position.x + random(-10, 10), position.y + random(-10, 10), sprite));
    }

    public void display(){
        image(sprite, position.x, position.y);
    }

    public boolean checkInLight(ArrayList<Organism> orgs) {
        float x = this.position.x;
        float y = this.position.y;

        for (Organism org : orgs) {
            if (org instanceof Shroom) {
                Shroom shroom = (Shroom) org;
                float distance = dist(x, y, shroom.position.x, shroom.position.y);
                if (distance < shroom._lightRadius) {
                    return true;
                }
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
    void move(ArrayList<Organism> orgs) {

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

    public int checkPreysInArea(Prey preys){
    
        int counter = 0;

        float distance = dist(preys.position.x, preys.position.y, this.position.x, this.position.y);

        if(distance < this._lightRadius){
            counter++;
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
