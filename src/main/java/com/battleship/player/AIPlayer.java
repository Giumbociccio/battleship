package com.battleship.player;

import java.util.*;

import com.battleship.game.GameConfig;

public class AIPlayer extends Player {
	protected final Random rand = new Random();
	protected List<int[]> moves = new ArrayList<>();
	protected List<int[]> hits = new ArrayList<>();

	public AIPlayer(GameConfig config) {
	    super(config); // passa a Player
	}

	@Override
	public int[] makeMove() {
		int row, col;

		do {
		    row = rand.nextInt(board.getSize());
		    col = rand.nextInt(board.getSize());
		} while (trackingBoard.isAlreadyShot(row, col));

		int[] move = new int[]{row, col};
		moves.add(move);

		return move;
	}
	@Override
	public void handleShotResult(int[] move, boolean hit, boolean sunk) {
	    super.handleShotResult(move, hit, sunk);

	    if (hit) {
	        hits.add(move);
	    }
	}
}