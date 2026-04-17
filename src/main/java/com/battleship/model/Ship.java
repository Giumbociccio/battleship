package com.battleship.model;

import java.util.*;

public class Ship {
    private final int size;
    private int hits = 0;
    private final List<int[]> positions = new ArrayList<>();
    private boolean sunk = false;

    public Ship(int size) { this.size = size; }

    public void addPosition(int r, int c) { positions.add(new int[]{r,c}); }

    public void hit() {
        hits++;
        if (hits >= size) {
            sunk = true;
        }
    }

    public boolean isSunk() {
        return sunk;
    }
    
    public int getSize() {
        return size;
    }
}