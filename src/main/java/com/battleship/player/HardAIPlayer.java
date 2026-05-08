package com.battleship.player;

import com.battleship.model.*;
import java.util.*;

public class HardAIPlayer extends AIPlayer {

	private Queue<int[]> trackingMoves = new LinkedList<>();

	public HardAIPlayer(int size) {
		super(size);
	}

	@Override
	public int[] makeMove() {

//  Se NON ho hit → SEARCH MODE
//  Se ho 1 hit → prova attorno
//  Se ho 2 hit → segui direzione
//  Se nave affondata → reset

		int[] move;
		do {
			move = trackingMoves.isEmpty() ? searchMode() : targetMode();
		} while (!isInsideBoard(move[0], move[1]) || trackingBoard.isAlreadyShot(move));

//		Collections.reverse(trackingMoves);

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
		} while ((row + col) % 2 != 0);

		int[] move = { row, col };
		return move;
	}

//  TODO TARGET MODE:
	private int[] targetMode() {
		Random rand = new Random();
		
		int[] startMove = trackingMoves.peek();
		int row = startMove[0], col = startMove[1];
		// lista delle celle già colpite (di una nave)
		List<int[]> ship = getConnectedHits(startMove);

		// se ho colpito solo una cella
		if (ship.size() == 1) {
			int[][] directions = { { 0, 1 }, // Destra (right)
					{ 0, -1 }, // Sinistra (left)
					{ 1, 0 }, // Giù (down)
					{ -1, 0 } // Su (up)
			};

			// 2. Scegliamo un indice casuale tra 0 e 3
			int randomIndex = rand.nextInt(directions.length);
			int[] move = directions[randomIndex];

			// 3. Calcoliamo la nuova posizione
			row = startMove[0] + move[0];
			col = startMove[1] + move[1];
		} else {

			int minRow = Integer.MAX_VALUE;
			int minCol = Integer.MAX_VALUE;
			int maxRow = Integer.MIN_VALUE;
			int maxCol = Integer.MIN_VALUE;
			for (int[] cell : ship) {
				int r = cell[0];
				int c = cell[1];

				if (r < minRow)
					minRow = r;
				if (r > maxRow)
					maxRow = r;

				if (c < minCol)
					minCol = c;
				if (c > maxCol)
					maxCol = c;
			}

//		int addingValue = rand.nextBoolean() ? ship.size() : -1;
//		int row = (minRow < maxRow) ? minRow +addingValue : minRow;
//		int col = (minCol < maxCol) ? minCol +addingValue : minCol;
			boolean shipBorder = rand.nextBoolean();

			if (minRow == maxRow) {
				col = shipBorder ? (minCol - 1) : (maxCol + 1);
			} else if (minCol == maxCol) {
				row = shipBorder ? (minRow - 1) : (maxRow + 1);
			}

		}

		int[] move = { row, col };
		return move;
	}

	@Override
	public void handleShotResult(int[] lastMove, boolean hit, boolean sunk) {
		super.handleShotResult(lastMove, hit, sunk);
		
		if (sunk) {
			List<int[]> shipCells = getConnectedHits(lastMove);
			markSurroundingsAsMiss(shipCells);
			trackingMoves.clear();
		} else if (hit) {
			trackingMoves.offer(lastMove);
		}
	}

	private void markSurroundingsAsMiss(List<int[]> shipCells) {

		for (int[] cell : shipCells) {
			int row = cell[0];
			int col = cell[1];

			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {

					int r = row + i;
					int c = col + j;

					if (isInsideBoard(r, c)) {

						CellState state = trackingBoard.getState(r, c);

						// 🔥 NON toccare celle già note
						if (state != CellState.HIT && state != CellState.MISS) {
							trackingBoard.setTrackingState(r, c, CellState.MISS);
						}
					}
				}
			}
		}
	}

	private List<int[]> getConnectedHits(int[] start) {
		List<int[]> resultShip = new ArrayList<>();
		Queue<int[]> queue = new LinkedList<>();

		queue.add(start);
		resultShip.add(start);

		while (!queue.isEmpty()) {
			int[] current = queue.poll();

			int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

			for (int[] d : directions) {
				int r = current[0] + d[0];
				int c = current[1] + d[1];

				if (isInsideBoard(r, c) && !contains(resultShip, r, c)
						&& trackingBoard.getState(r, c) == CellState.HIT) {

					int[] next = { r, c };
					resultShip.add(next);
					queue.add(next);
				}
			}
		}

		return resultShip;
	}

	private boolean isInsideBoard(int r, int c) {
		return r >= 0 && r < trackingBoard.getSize() && c >= 0 && c < trackingBoard.getSize();
	}

	private boolean contains(List<int[]> list, int r, int c) {
		for (int[] cell : list) {
			if (cell[0] == r && cell[1] == c)
				return true;
		}
		return false;
	}
}