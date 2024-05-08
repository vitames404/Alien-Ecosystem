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

    void move(ArrayList<Organism> orgs) {

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

    void eat(){
        preyEaten++;
        lastEatTime = millis();
    }

    void display() {

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

    void updateHealth() {
        float currentTime = millis();
        if (currentTime - lastHealthUpdateTime > 1000 && !pregnant) {
            health -= 3;
            lastHealthUpdateTime = currentTime;
        }
    }

    void checkReproduction(float currentTime) {
        if (preyEaten >= 2) {
            pregnant = true;
            preyEaten = 0;
        } 
    }

    void reproduce(ArrayList<Organism> orgs) {
        orgs.add(new Predator(position.x + random(-10, 10), position.y + random(-10, 10), predatorSprite, speed));
    }

    PVector findClosestCorner(){
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
