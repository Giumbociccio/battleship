package com.battleship.model;

public class Move {
    int row;
    int col;
    boolean hit;

    public Move(int r, int c, boolean h) {
        row = r;
        col = c;
        hit = h;
    }
}