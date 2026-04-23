package com.battleship.player;

import com.battleship.model.*;

public class HardAIPlayer extends AIPlayer {

	public HardAIPlayer(int size) {
		super(size);
	}

	@Override
	public int[] makeMove() {

//  Se NON ho hit → SEARCH MODE
//  Se ho 1 hit → prova attorno
//  Se ho 2 hit → segui direzione
//  Se nave affondata → reset

//    	lastShot = moves.get(moves.size()-1)
//    	boolean targetingMode = (lastShot == hit && lastShot != sunk);
		boolean targetingMode = false;
		int[] move = targetingMode ? targetMode() : searchMode();
		
		moves.add(move);
		return move;
	}

	private int[] searchMode() {
//  SEARCH MODE:
//  spara a pattern (scacchiera)
//  (r + c) % 2 == 0
		int row, col;
		do {
		    row = rand.nextInt(board.getSize());
		    col = rand.nextInt(board.getSize());
		} while (trackingBoard.isAlreadyShot(row, col) || (row + col) % 2 != 0);
	
		int[] move = new int[]{row, col};
		moves.add(move);
	
		return move;
	}

	private int[] targetMode() {
//  TODO TARGET MODE:
//  Quando fai HIT:
//  salva posizione
//  prova attorno: ↑ ↓ ← →
//    
//  Quando fai secondo HIT
//  Hai direzione:
//  orizzontale → continua dx/sx
//  verticale → continua su/giù
		return super.makeMove();
	}

	@Override
	public void handleShotResult(int[] move, boolean hit, boolean sunk) {
		super.handleShotResult(move, hit, sunk);

		if (sunk) {
			markSurroundingsAsMiss(trackingBoard.getGrid()[move[0]][move[1]].getShip());
		}
	}

	private void markSurroundingsAsMiss(Ship lastShip) {
		for (int[] cell : lastShip.getPositions()) {
			int row = cell[0];
			int col = cell[1];
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {

					int r = row + i;
					int c = col + j;

					if (isInsideBoard(r, c)) {

						CellState state = trackingBoard.getState(r, c);

						if (state == CellState.EMPTY) {
							trackingBoard.setTrackingState(r, c, CellState.MISS);
						}
					}
				}
			}
		}
	}

	private boolean isInsideBoard(int r, int c) {
		return r >= 0 && r < trackingBoard.getSize() && c >= 0 && c < trackingBoard.getSize();
	}

//  ---------------------------
//  struttura dati ideale
//  List<int[]> hitQueue
//  Direction direction
//  boolean targetingMode
//   
//     
//  probabilistic AI (heatmap)
//  tracking lunghezze navi rimaste

}