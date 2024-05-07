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

    boolean hunting = false;

    // Adicionando atributos para a direção aleatória e controle de tempo
    PVector randomDirection = PVector.random2D();
    int directionChangeInterval = 2000; // tempo em milissegundos para mudar de direção
    long lastDirectionChangeTime = 0; // para rastrear a última mudança de direção

    Predator(float x, float y) {
        super(x, y, 20);
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
                hunting = true;
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
                    counter++;        
                }
            }
        }

        if(counter <= 3 && currentPrey != null){
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
        orgs.add(new Predator(position.x + random(-10, 10), position.y + random(-10, 10)));
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
