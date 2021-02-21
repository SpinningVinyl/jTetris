package com.pavelurusov.jtetris;

import com.pavelurusov.squaregrid.SquareGrid;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Random;

/**
 * @author Pavel Urusov, me@pavelurusov.com
 * A quick and dirty Tetris clone.
 */

public class Tetris extends Application {
    private SquareGrid boardDisplay; // main board
    private SquareGrid nextPieceDisplay; // this small grid shows the next piece
    private AnimationTimer gameTimer; // this is the timer that makes the game tick
    private Label scoreLabel;
    private Label currentLevelLabel;
    private Button newGameButton;

    private double interval = 5e8; // 5*10^8 nanoseconds - this value determines the speed of the game

    private final int rows = 18;
    private final int columns = 12;
    private int score = 0;
    private int level = 1; // the game starts at level 1

    private Color[][] landed;

    private Tetromino currentPiece;
    private Tetromino nextPiece;

    private Random rnd = new Random();

    @Override
    public void start(Stage primaryStage) throws Exception{

        // setting up the game board
        boardDisplay = new SquareGrid(rows, columns, 20);
        landed = new Color[rows][columns];
        boardDisplay.setAlwaysDrawGrid(false);
        boardDisplay.setAutomaticRedraw(false);
        boardDisplay.setStyle("-fx-border-color: darkgray; -fx-border-width: 5px");

//      setting up the next piece display
        nextPieceDisplay = new SquareGrid(4,4,20);
        nextPieceDisplay.setAlwaysDrawGrid(false);

        Label nextPieceLabel = new Label("Next piece:");

//      setting up various UI elements
        Label controlsHintLabel = new Label("← Move left | → Move right | [SPC] / ↑ Rotate");
        scoreLabel = new Label();
        scoreLabel.setFont(Font.font("System", 22));
        scoreLabel.setStyle("-fx-text-fill: #770033; -fx-label-padding: 25px 0 0 0;");

        currentLevelLabel = new Label();
        currentLevelLabel.setStyle("-fx-label-padding: 25px 0 25px 0;");

        newGameButton = new Button("New game");
        newGameButton.setDisable(true);
        newGameButton.setOnMouseClicked(e -> mouseClicked(e));

        VBox rightPane = new VBox(3, nextPieceLabel,
                    nextPieceDisplay, scoreLabel, currentLevelLabel, newGameButton);
        rightPane.setStyle("-fx-padding: 8px;");

        HBox bottomPane = new HBox(3, controlsHintLabel);
        bottomPane.setStyle("-fx-padding: 5px;");

        BorderPane root = new BorderPane();
        root.setCenter(boardDisplay);
        root.setRight(rightPane);
        root.setBottom(bottomPane);

        // setting up the game loop
        gameTimer = new AnimationTimer() {
            long lastFrameTime;
            @Override
            public void handle(long time) {
                if (time - lastFrameTime >= interval) {
                    tick();
                    lastFrameTime = time;
                }
            }
        };

        // setting up the scene graph
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> keyPressed(e));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("jTetris");
        primaryStage.show();

        // setting the game state and starting the game loop
        currentPiece = new Tetromino(rnd.nextInt(7));
        nextPiece = new Tetromino(rnd.nextInt(7));
        showNextPiece();
        gameTimer.start();
    }

    // this method implements the main game loop
    private void tick() {
        // clear the landed pile of all filled lines
        clearFilledLines();
        // draw the landed pile on the game board
        drawLanded();
        // draw the current piece at its current position
        drawPiece();

        int nextY = currentPiece.getY() + 1;
        if (nextY >= 0) { // this check is necessary since the pieces spawn outside of the game board
            // if the piece can't advance, add it to the landed pile
            if (collision(currentPiece.getX(), nextY)) {
                for (int px = 0; px < 4; px++) {
                    for (int py = 0; py < 4; py++) {
                        int row = currentPiece.getY() + py;
                        int column = currentPiece.getX() + px;
                        if (currentPiece.atPos(px, py) != null && currentPiece.getY() >= 0) {
                            landed[row][column] = currentPiece.atPos(px, py);
                        }
                    }
                }
                // if the landed piece is too close to the top edge, go into the game over state
                if (currentPiece.getY() <= 1) {
                    gameTimer.stop();
                    currentLevelLabel.setText("Game over!");
                    newGameButton.setDisable(false);
                } else { // else create new tetrominoes
                    currentPiece = new Tetromino(nextPiece.getType());
                    nextPiece = new Tetromino(rnd.nextInt(7));
                    showNextPiece();
                }
            }
        }
        // move the current piece one row down
        currentPiece.advance();
        boardDisplay.redraw();
    }

//    handle mouse input
    private void mouseClicked(MouseEvent e) {
        startNewGame();
    }

    // handle keyboard input
    private void keyPressed(KeyEvent e) {
        KeyCode code = e.getCode();
        int nextX = currentPiece.getX();
        int nextY = currentPiece.getY();
        int nextRotation = currentPiece.getRotation();
        if (code == KeyCode.LEFT || code == KeyCode.KP_LEFT) {
            nextX--;
            if (!collision(nextX, nextY)) {
                currentPiece.moveLeft();
            }
        } else if (code == KeyCode.RIGHT || code == KeyCode.KP_RIGHT) {
            nextX++;
            if (!collision(nextX, nextY)) {
                currentPiece.moveRight();
            }
        } else if (code == KeyCode.SPACE || code == KeyCode.UP || code == KeyCode.KP_UP) {
            nextRotation++;
            if(!collision(nextX, nextY, nextRotation)) {
                currentPiece.rotate();
            }
        } else if (code == KeyCode.DOWN || code == KeyCode.KP_DOWN) {
            nextY++;
            if(!collision(nextX, nextY)) {
                currentPiece.advance();
            }
        }
        boardDisplay.redraw();
    }

//    draws the current piece on the screen at its current position
    private void drawPiece() {
        int startRow = currentPiece.getY();
        int startColumn = currentPiece.getX();
        for (int px = 0; px < 4; px++) {
            for (int py = 0; py < 4; py++) {
                int row = startRow + py;
                int column = startColumn + px;
                Color color = currentPiece.atPos(px, py);
                if (row >= 0 && row < boardDisplay.getRows() &&
                        column >= 0 && column < boardDisplay.getColumns()) {
                    if(color != null) {
                        boardDisplay.setCellColor(row, column, color);
                    }
                }
            }
        }
    }

//    draws the next piece on the nextPieceDisplay
    private void showNextPiece() {
        if (nextPiece != null) {
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    nextPieceDisplay.setCellColor(y, x, nextPiece.atPos(x, y));
                }
            }
        }
    }

//    collision detection
    private boolean collision(int x, int y, int r) {
        for (int px = 0; px < 4; px++) {
            for(int py = 0; py < 4; py++) {
                int row = y + py;
                int column = x + px;
                if (currentPiece.atPos(px, py, r) != null) {
                    if (row < 0 || row > (rows - 1)
                            || column < 0 || column > (columns - 1)) {
                        return true;
                    } else if (landed[row][column] != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean collision(int x, int y) {
        return collision(x, y, currentPiece.getRotation());
    }

    // draw the landed pile on the game board
    private void drawLanded() {
        for(int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                boardDisplay.setCellColor(row, column, landed[row][column]);
            }
        }
    }

    // clears the filled lines, increases the score counter and advances the current level
    private void clearFilledLines() {
        int clearedLines = 0;
        for (int row = rows - 1; row >= 0; row--) {
            boolean isFilled = true;
            for(int column = 0; column < columns; column++) {
                if(landed[row][column] == null) {
                    isFilled = false;
                    break;
                }
            }
            if (isFilled) {
                clearRow(row);
                row++;
                clearedLines++;
            }
        }
        // increase the score counter
        switch(clearedLines) {
            case 1:
                score += 100;
                break;
            case 2:
                score += 300;
                break;
            case 3:
                score += 500;
                break;
            case 4:
                score += 800;
        }
        // update the score display
        scoreLabel.setText(String.valueOf(score));

        // level advancement
        if(score >= 6000) {
            level = 5;
            interval = 1e8;
        } else if(score >= 4500) {
            level = 4;
            interval = 2e8;
        } else if(score >= 3000) {
            level = 3;
            interval = 3e8;
        } else if(score >= 1500) {
            level = 2;
            interval = 4e8;
        }
        currentLevelLabel.setText("Level: " + level);
    }

    // remove the indicated row and shift rows above it
    private void clearRow(int row) {
        for (int r = row; r > 0; r--) {
            System.arraycopy(landed[r - 1], 0, landed[r], 0, columns);
        }
    }

    private void startNewGame() {
        // clear the landed pile
        for(int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                landed[y][x] = null;
            }
        }
        // reset the game state
        level = 1;
        score = 0;
        interval = 5e8;
        newGameButton.setDisable(true);

        // generate new pieces
        currentPiece = new Tetromino(rnd.nextInt(7));
        nextPiece = new Tetromino(rnd.nextInt(7));
        showNextPiece();

        // restart the game loop
        gameTimer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
