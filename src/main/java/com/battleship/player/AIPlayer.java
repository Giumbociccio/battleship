package com.battleship.player;

import java.util.*;

public class AIPlayer extends Player {
	private final Random rand = new Random();
	protected List<int[]> moves = new ArrayList<>();
	protected List<int[]> hits = new ArrayList<>();

	public AIPlayer(int size) {
		super(size);
	}

	@Override
	public int[] makeMove() {
		int row = rand.nextInt(board.getSize());
		int col = rand.nextInt(board.getSize());
		int[] move = new int[] { row, col };
		moves.add(move);
		return move;
	}

	public void processResult(int[] move, boolean hit) {
		if (hit) {
			hits.add(move);
		}
	}
}