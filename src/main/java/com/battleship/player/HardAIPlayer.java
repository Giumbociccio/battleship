package com.battleship.player;

import com.battleship.model.*;
import java.util.*;

public class HardAIPlayer extends AIPlayer {

	private List<int[]> targetingMoves = new ArrayList<>();

	public HardAIPlayer(int size) {
		super(size);
	}

	@Override
	public int[] makeMove() {

//  Se NON ho hit → SEARCH MODE
//  Se ho 1 hit → prova attorno
//  Se ho 2 hit → segui direzione
//  Se nave affondata → reset

//		Collections.reverse(targetingMoves);

//		Cell lastMove = pairToCell(moves.get(0));
//		CellState lastShotState = lastMove.getState();
//		boolean lastShotSunk = lastMove.getShip().isSunk();
//
//		boolean targetingMode = (lastShotState == CellState.HIT && !lastShotSunk);
		int[] move;
		do {
//			move = targetingMode ? targetMode() : searchMode();
			move = targetMode();
		} while (trackingBoard.isAlreadyShot(move));

//		Collections.reverse(targetingMoves);

		moves.add(move);
		return move;
	}

//  TODO TARGET MODE:
	private int[] targetMode() {
		// controllo quante volte ho colpito la nave
		int hits = 0;
		int[] move = searchMode();
		boolean lastMiss = false;
		for (int i = 0; i < targetingMoves.size(); i++) {
			CellState currentState = pairToCell(targetingMoves.get(i)).getState();

			if (currentState == CellState.HIT) {
				hits++;
				lastMiss = false;
			} else if (lastMiss) {
				break;
			}
//  Quando fai 1 HIT:
//  salva posizione
//  prova attorno: ↑ ↓ ← →
			switch (hits) {
			case 1:

				break;
//  Quando fai 2 HIT
//  Hai direzione:
//  orizzontale → continua dx/sx
//  verticale → continua su/giù
			case 2:

				break;
			default:
				break;
			}
		}
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
		} while ((row + col) % 2 != 0);

		int[] move = new int[] { row, col };
		return move;
	}

	// trasforma moves.get(moves.size()-1) in una Cella
	private Cell pairToCell(int[] move) {
		return trackingBoard.getCell(move);
	}

	@Override
	public void handleShotResult(int[] move, boolean hit, boolean sunk) {
		super.handleShotResult(move, hit, sunk);

		if (sunk) {
			markSurroundingsAsMiss(pairToCell(move).getShip());
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