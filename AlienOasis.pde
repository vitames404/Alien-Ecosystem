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
        organisms.add(new Predator(random(width), random(height), predatorSprite));
    }
    for (int i = 0; i < initialShroom; i++) {
        organisms.add(new Shroom(random(width), random(height), random(50, 150)));
    }
}

void draw() {
    background(30);
    updateWorld();
    displayEntities();
    checkIfFinished();
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
                if(predator.hunting && predator.targetPrey == org){
                    predator.eat();
                    organisms.remove(i--);
                    predator.targetPrey = null;
                }
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

    int predatorsCount = 0;
    int preyCount = 0;

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