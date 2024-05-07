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

void setup() {
    size(1280, 720);
    background(30);
    initializeEntities();

    timer = millis();
}

void initializeEntities() {
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
    updatePrey(currentTime);
    updatePredators(currentTime);
    updateShroom(currentTime);
}

void displayEntities() {
    for (Food food : foodItems) food.display();
    for (Shroom shroom : glowshrooms) shroom.display();
    for (Predator predator : predators) predator.display();
    for (Prey p : prey) p.display();
}

void updatePrey(long currentTime) {
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

void updateShroom(long currentTime){
    for(int i = 0; i < glowshrooms.size(); i++){
        Shroom s = glowshrooms.get(i);

        int preyQuantity = s.checkPreysInArea(prey);

        s.updateHealth(preyQuantity);

        if(s._health <= 0){
            glowshrooms.remove(s);
        }

    }
}

void updatePredators(long currentTime) {

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

void checkPredatorCollisions(Predator predator) {
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

void spawnFood() {
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
    if (inLight || random(1) < 0.1) {  // 10% chance outside light radius
        foodItems.add(new Food(x, y));
    }
}

void spawnShroom(long currentTime) {
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

void checkIfFinished(){

    long currentTime = millis();

    if(predators.size() < 1 || prey.size() <  1){
        println(((currentTime - timer) / 1000) / 60);
        exit();
    }
}

