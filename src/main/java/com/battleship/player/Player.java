package com.battleship.player;

import com.battleship.model.Board;

public abstract class Player {
    protected Board board;
    protected int shots = 0;
    protected int hits = 0;
    protected int misses = 0;

    public Player(int size){ board = new Board(size); }

    public Board getBoard(){ return board; }

    public abstract int[] makeMove();
    
    public void recordShot(boolean hit) {
        shots++;
        if (hit) hits++;
        else misses++;
    }
    
    public int getShots() { return shots; }
    public int getHits() { return hits; }
    public int getMisses() { return misses; }

    public double getAccuracy() {
        return shots == 0 ? 0 : (hits * 100.0) / shots;
    }
}
