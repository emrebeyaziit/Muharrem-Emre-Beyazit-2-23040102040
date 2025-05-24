package com.flappybird;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.*;

public class GameManager {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 660;
    private static final int BIRD_X_POSITION = 150;
    private static final double GRAVITY = 0.25;
    private static final double JUMP_FORCE = -6.5;
    private static final int GROUND_HEIGHT = 100;
    private static final int BIRD_WIDTH = 40;
    private static final int BIRD_HEIGHT = 30;
    private static final int PIPE_WIDTH = 70;
    private static final int PIPE_GAP = 200;
    private static final Double PIPE_SPEED = 2.6;
    private static final int PIPE_SPAWN_TIME = 140;
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private Canvas canvas;
    private GraphicsContext gc;
    private Bird bird;
    private List<Pipe> pipes;
    private boolean gameRunning;
    private boolean gameOver;
    private int score;
    private int highScore;
    private int pipeSpawnCounter;
    private Random random;

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    public GameManager() {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        bird = new Bird(BIRD_X_POSITION, HEIGHT / 2, BIRD_WIDTH, BIRD_HEIGHT);
        pipes = new ArrayList<>();
        gameRunning = false;
        gameOver = false;
        score = 0;
        highScore = loadHighScore();
        pipeSpawnCounter = 0;
        random = new Random();
    }

    public void startGame(Stage primaryStage) {
        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        setupKeyControls(scene);

        primaryStage.setTitle("Flappy Bird - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        showStartScreen();
    }

    private void setupKeyControls(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                if (!gameRunning && !gameOver) {
                    startGameLoop();
                } else if (gameRunning) {
                    bird.jump();
                } else if (gameOver) {
                    resetGame();
                }
            }
        });
    }

    private void startGameLoop() {
        gameRunning = true;
        gameOver = false;

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameRunning) {
                    update();
                    render();

                    if (checkCollision()) {
                        gameRunning = false;
                        gameOver = true;

                        if (score > highScore) {
                            highScore = score;
                            saveHighScore(highScore);
                        }

                        showGameOverScreen();
                        this.stop();
                    }
                }
            }
        }.start();
    }

    private void update() {
        bird.update();

        pipeSpawnCounter++;
        if (pipeSpawnCounter >= PIPE_SPAWN_TIME) {
            int pipeHeight = random.nextInt(HEIGHT - PIPE_GAP - GROUND_HEIGHT - 100) + 50;
            pipes.add(new Pipe(WIDTH, pipeHeight, PIPE_WIDTH, PIPE_GAP));
            pipeSpawnCounter = 0;
        }

        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.update();

            if (!pipe.isScored() && pipe.getX() + PIPE_WIDTH < BIRD_X_POSITION) {
                pipe.setScored(true);
                score++;
            }

            if (pipe.getX() + PIPE_WIDTH < 0) {
                pipes.remove(i);
                i--;
            }
        }
    }

    private void render() {
        // Background
        gc.setFill(Color.rgb(98, 177, 234));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Pipes
        for (Pipe pipe : pipes) {
            pipe.render(gc);
        }

         // Ground
        gc.setFill(Color.rgb(203, 189, 147));
        gc.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);

        // Grass
        gc.setFill(Color.rgb(92, 152, 41));
        gc.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, 20);

        // Draw the bird
        bird.render(gc);

        // Draw your username on the bird
        if (username != null && !username.isEmpty()) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 18));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(username, bird.getX() + BIRD_WIDTH / 2, bird.getY() - 10);
        }

        // Score
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 30));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Score: " + score, WIDTH / 2, 50);

        gc.setFont(Font.font("Arial", 20));
        gc.fillText("High Score: " + highScore, WIDTH / 2, 80);
    }

    private boolean checkCollision() {
        if (bird.getY() + BIRD_HEIGHT > HEIGHT - GROUND_HEIGHT) return true;
        if (bird.getY() < 0) return true;

        for (Pipe pipe : pipes) {
            if (bird.getX() + BIRD_WIDTH > pipe.getX() &&
                    bird.getX() < pipe.getX() + PIPE_WIDTH &&
                    (bird.getY() < pipe.getHeight() || bird.getY() + BIRD_HEIGHT > pipe.getHeight() + PIPE_GAP)) {
                return true;
            }
        }
        return false;
    }

    private void showStartScreen() {
        gc.setFill(Color.rgb(98, 177, 234));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.rgb(203, 189, 147));
        gc.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);

        gc.setFill(Color.rgb(92, 152, 41));
        gc.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, 20);

        bird.render(gc);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 40));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Flappy Bird", WIDTH / 2, HEIGHT / 3);

        gc.setFont(Font.font("Arial", 20));
        gc.fillText("Press SPACE to start", WIDTH / 2, HEIGHT / 2);
        gc.fillText("Press SPACE to jump", WIDTH / 2, HEIGHT / 2 + 30);
    }

    private void showGameOverScreen() {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 40));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Game Over", WIDTH / 2, HEIGHT / 3);

        gc.setFont(Font.font("Arial", 30));
        gc.fillText("Score: " + score, WIDTH / 2, HEIGHT / 2);

        gc.setFont(Font.font("Arial", 20));
        gc.fillText("High Score: " + highScore, WIDTH / 2, HEIGHT / 2 + 40);

        gc.fillText("Press SPACE to try again", WIDTH / 2, HEIGHT * 2 / 3);
    }

    private void resetGame() {
        bird = new Bird(BIRD_X_POSITION, HEIGHT / 2, BIRD_WIDTH, BIRD_HEIGHT);
        pipes.clear();
        gameRunning = false;
        gameOver = false;
        score = 0;
        pipeSpawnCounter = 0;
        showStartScreen();
    }

    private int loadHighScore() {
        try {
            File file = new File(HIGH_SCORE_FILE);
            if (!file.exists()) return 0;

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            reader.close();
            return Integer.parseInt(line.trim());
        } catch (Exception e) {
            System.out.println("High score read error: " + e.getMessage());
            return 0;
        }
    }

    private void saveHighScore(int newHighScore) {
        try {
            FileWriter writer = new FileWriter(HIGH_SCORE_FILE);
            writer.write(String.valueOf(newHighScore));
            writer.close();
        } catch (Exception e) {
            System.out.println("High score write error: " + e.getMessage());
        }
    }

    private class Bird {
        private double x, y;
        private int width, height;
        private double velocity;

        public Bird(double x, double y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.velocity = 0;
        }

        public void update() {
            velocity += GRAVITY;
            y += velocity;
        }

        public void jump() {
            velocity = JUMP_FORCE;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(Color.YELLOW);
            gc.fillOval(x, y, width, height);
            gc.setFill(Color.WHITE);
            gc.fillOval(x + width - 15, y + 5, 10, 10);
            gc.setFill(Color.BLACK);
            gc.fillOval(x + width - 12, y + 7, 5, 5);
            gc.setFill(Color.ORANGE);
            gc.fillPolygon(
                    new double[]{x + width, x + width + 15, x + width},
                    new double[]{y + 10, y + height / 2, y + height - 10},
                    3
            );
        }

        public double getX() { return x; }
        public double getY() { return y; }
    }

    private class Pipe {
        private double x;
        private int height;
        private int width;
        private int gap;
        private boolean scored;

        public Pipe(double x, int height, int width, int gap) {
            this.x = x;
            this.height = height;
            this.width = width;
            this.gap = gap;
            this.scored = false;
        }

        public void update() {
            x -= PIPE_SPEED;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(Color.DARKGREEN);
            gc.fillRect(x, 0, width, height);
            gc.setFill(Color.rgb(6, 64, 43));
            gc.fillRect(x, height - 20, width, 20);
            gc.fillRect(x - 5, height - 20, width + 10, 20);

            gc.setFill(Color.DARKGREEN);
            gc.fillRect(x, height + gap, width, HEIGHT - (height + gap));
            gc.setFill(Color.rgb(6, 64, 43));
            gc.fillRect(x, height + gap, width, 20);
            gc.fillRect(x - 5, height + gap, width + 10, 20);
        }

        public double getX() { return x; }
        public int getHeight() { return height; }
        public boolean isScored() { return scored; }
        public void setScored(boolean scored) { this.scored = scored; }
    }
}
