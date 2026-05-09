package com.battleship.player;

import com.battleship.game.GameConfig;
import com.battleship.model.Board;
import com.battleship.model.CellState;

public abstract class Player {
	protected Board board;
	protected Board trackingBoard;
	protected int shots = 0;
	protected int hits = 0;
	protected int misses = 0;
	protected GameConfig config; // null per HumanPlayer, valorizzato per AI

	// Costruttore esistente rimane per HumanPlayer
	public Player(int size) {
	    this.board = new Board(size);
	    this.trackingBoard = new Board(size);
	}

	// Nuovo costruttore per i player AI che ricevono la config
	public Player(GameConfig config) {
	    this(config.getSize()); // riusa il costruttore base
	    this.config = config;
	}
	public Board getBoard() {
		return board;
	}
	
	public Board getTrackingBoard() {
		return trackingBoard;
	}
	// Aggiornamento puro del trackingBoard — non fare override di questo
	public void updateTrackingBoard(int[] move, boolean hit) {
	    int row = move[0];
	    int col = move[1];
	    trackingBoard.setTrackingState(row, col, hit ? CellState.HIT : CellState.MISS);
	}

	// Logica strategica — le sottoclassi fanno override di questo
	@SuppressWarnings("unused")
	public void handleShotResult(int[] move, boolean hit, boolean sunk) {
	    updateTrackingBoard(move, hit); // il padre lo chiama qui, una volta sola
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
