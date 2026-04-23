package com.battleship.player;

import com.battleship.model.Board;
import com.battleship.model.CellState;

public abstract class Player {
	protected Board board;
	protected Board trackingBoard;
	protected int shots = 0;
	protected int hits = 0;
	protected int misses = 0;

	public Player(int size) {
		board = new Board(size);
		trackingBoard = new Board(size);
	}

	public Board getBoard() {
		return board;
	}
	
	public Board getTrackingBoard() {
		return trackingBoard;
	}
	public void handleShotResult(int[] move, boolean hit, boolean sunk) {
		int row = move[0];
		int col = move[1];
		
		if (hit) {
	        trackingBoard.setTrackingState(row, col, CellState.HIT);
	    } else {
	        trackingBoard.setTrackingState(row, col, CellState.MISS);
	    }
	}

	public abstract int[] makeMove();

	public void recordShot(boolean hit) {
		shots++;
		if (hit)
			hits++;
		else
			misses++;
	}

	public int getShots() {
		return shots;
	}

	public int getHits() {
		return hits;
	}

	public int getMisses() {
		return misses;
	}

	public double getAccuracy() {
		return shots == 0 ? 0 : (hits * 100.0) / shots;
	}
}
