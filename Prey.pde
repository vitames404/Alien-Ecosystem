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

    void move(ArrayList<Organism> orgs) {
        updateMovement(orgs);
        updateHealth(orgs);
        wrapAround();
    }

    void updateMovement(ArrayList<Organism> orgs) {
        
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

    PVector findTargetDirection() {
        PVector closestFoodPosition = findClosestFoodWithinRadius();
        if (closestFoodPosition != null) {
            return PVector.sub(closestFoodPosition, position).normalize();
        }
        return null; // Default to random direction if no food is close
    }

    PVector findClosestFoodWithinRadius() {
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

    void updateHealth(ArrayList<Organism> organisms) {
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

    void updateColorsBasedOnHealth() {
        green = constrain(green - healthDecayRate * 2, 0, 255);
        red = constrain(red + healthDecayRate * 2, 0, 255);
    }

    void eat(Food food) {
        health = constrain(health + 20, 0, 100);
        if(health > 20){
            speed = 1;
        }
        green = constrain(green + 40, 0, 255);
        red = constrain(red - 40, 0, 255);
        foodEaten++;
        lastEatTime = millis();
    }

    void checkReproduction(float currentTime, ArrayList<Organism> orgs) {
        if (foodEaten >= 2 && currentTime - lastEatTime <= reproductionThreshold) {
            reproduce(orgs);
            foodEaten = 0;
        } else if (currentTime - lastEatTime > reproductionThreshold) {
            foodEaten = 0;
        }
    }

    void reproduce(ArrayList<Organism> orgs) {
        orgs.add(new Prey(position.x + random(-10, 10), position.y + random(-10, 10), sprite));
    }

    void display(){
        image(sprite, position.x, position.y);
    }

    boolean checkInLight(ArrayList<Organism> orgs) {
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
