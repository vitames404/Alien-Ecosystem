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

void setup() {

    preySprite = loadImage("assets/Sprite-0001.png");
    predatorSprite = loadImage("assets/Sprite-0002.png");

    size(1280, 720);
    background(30);
    initializeEntities();

    timer = millis();
}

void initializeEntities() {

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

void draw() {
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

void keyPressed() {
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
                changePredatorSpeed(0.2);
                break;

            case 'd':
                message = "Predators speed decreased";
                changePredatorSpeed(-0.2);
                break;

            case 'e':
                message = "Preys speed increased!";
                changePreySpeed(0.2);
                break;

            case 'r':
                message = "Preys speed decreased";
                changePreySpeed(-0.2);
                break;

            default:
                break;
        }

        messageDisplayTime = millis();
    }
}

void changePreySpeed(float amount){
    for(int i = 0; i < organisms.size(); i++){
    
        Organism org = organisms.get(i);

        if(org instanceof Prey){

            Prey p = (Prey) org;

            p.speed += amount;
        
        }
    }
}

void changePredatorSpeed(float amount){

    for(int i = 0; i < organisms.size(); i++){
    
        Organism org = organisms.get(i);

        if(org instanceof Predator){

            Predator p = (Predator) org;

            p.speed += 0.2;

        }
    }   
}

void displayMessage() {
    if (millis() - messageDisplayTime < messageDuration) {
        fill(255); // White text
        textSize(16);
        textAlign(CENTER);
        text(message, width / 2, 20);
    }
}

void updateWorld() {
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

void displayEntities() {
    for (Food food : foodItems) food.display();
    for (Organism org : organisms) org.display();
}

void updatePrey(long currentTime) {
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

void updateShroom(long currentTime) {
    
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

void updatePredators(long currentTime) {

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

void checkPredatorCollisions(Predator predator) {
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

void spawnFood() {
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
    if (inLight || random(1) < 0.1) {  // 10% chance outside light radius
        foodItems.add(new Food(x, y));
    }
}

void spawnShroom(long currentTime) {

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

boolean checkCollision(Organism a, Organism b) {
    float distance = dist(a.position.x, a.position.y, b.position.x, b.position.y);
    return distance < (a.radius + b.radius);
}

void checkCollisionsWithFood(Prey p) {
    for (int j = 0; j < foodItems.size(); j++) {
        if (checkCollision(foodItems.get(j), p)) {
            p.eat(foodItems.remove(j)); // Assuming Prey has a method to 'eat'
            j--; // Adjust the index after removal
        }
    }
}

void checkIfFinished() {
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

void displayInstructions() {
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