package com.pavelurusov.jtetris;

import javafx.scene.paint.Color;

/**
 * @author Pavel Urusov, me@pavelurusov.com
 * This class implements the tetromino (Tetris piece) for my Tetris clone.
 */

public class Tetromino {

    private int rotation = 0; // all new pieces spawn rotated 0 degrees
    private int type; // Tetromino type, 0 to 6
    private int x = 4, y = -3; // coordinates of the upper left corner of the piece
    private final String[] tetromino = { "0100010001000100",   // I-tetromino
                                         "0100011000100000",   // S-tetromino
                                         "0000011001100000",   // O-tetromino
                                         "0110010001000000",   // J-tetromino
                                         "0010011001000000",   // Z-tetromino
                                         "0110001000100000",   // L-tetromino
                                         "0100111000000000" }; // T-tetromino
    private final Color[] color = { Color.CYAN,
                                    Color.GREEN,
                                    Color.YELLOW,
                                    Color.BLUE,
                                    Color.RED,
                                    Color.ORANGE,
                                    Color.MAGENTA };

    public Tetromino(int type) {
        if (type < 0 || type > 6) {
            throw new IllegalArgumentException("Wrong tetromino type.");
        }
        this.type = type;
    }

    // returns the color at px,py
    // if the cell is empty, returns null

    public Color atPos(int px, int py) {
        return atPos(px, py, this.rotation);
    }

    public Color atPos(int px, int py, int r) {
        int n;
        switch(r % 4) {
            case 1: // rotation 90 degrees
                n = 12 - 4*px + py;
                break;
            case 2: // rotation 180 degrees
                n = 15 - px - 4*py;
                break;
            case 3: // rotation 270 degrees
                n = 3 + 4*px - py;
                break;
            default: // rotation 0 degrees
                n = px + 4*py;
        }
        if(tetromino[type].charAt(n) == '1') {
            return color[type];
        }
        return null;
    }

//    useful getters

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getType() {
        return type;
    }

    public int getRotation() {
        return rotation;
    }

//    movements

    public void advance() {
        y++;
    };

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }

    public void rotate() {
        rotation++;
    }
}
