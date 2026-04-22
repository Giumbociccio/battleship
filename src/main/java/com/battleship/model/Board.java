package com.battleship.model;

import java.util.*;

public class Board {
	private final int size;
	private final Cell[][] grid;
	private final List<Ship> ships = new ArrayList<>();

	public Board(int size) {
		this.size = size;
		grid = new Cell[size][size];
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				grid[i][j] = new Cell();
	}

	public boolean placeShip(Ship ship, int row, int col, boolean horizontal) {
		if (horizontal) {
			if (col + ship.getSize() > size)
				return false;
			for (int i = 0; i < ship.getSize(); i++)
				if (grid[row][col + i].getState() != CellState.EMPTY || isNearShip(row, col + i))
					return false;

			for (int i = 0; i < ship.getSize(); i++) {
				Cell cell = grid[row][col + i];
				cell.setState(CellState.SHIP);
				cell.setShip(ship);
				ship.addPosition(row, col + i);
			}
		} else {
			if (row + ship.getSize() > size)
				return false;
			for (int i = 0; i < ship.getSize(); i++)
				if (grid[row + i][col].getState() != CellState.EMPTY || isNearShip(row + i, col))
					return false;

			for (int i = 0; i < ship.getSize(); i++) {
				Cell cell = grid[row + i][col];
				cell.setState(CellState.SHIP);
				cell.setShip(ship);
				ship.addPosition(row + i, col);
			}
		}
		ships.add(ship);
		return true;
	}

	public String shoot(int r, int c) {
		Cell cell = grid[r][c];

		if (cell.getState() == CellState.HIT || cell.getState() == CellState.MISS)
			return "Already shot";

		if (cell.getState() == CellState.SHIP) {
			cell.setState(CellState.HIT);
			cell.getShip().hit();
			return cell.getShip().isSunk() ? "SUNK" : "HIT";
		}

		cell.setState(CellState.MISS);
		return "MISS";
	}

	public boolean allShipsSunk() {
		return ships.stream().allMatch(Ship::isSunk);
	}

	private boolean isNearShip(int row, int col) {
		for (int r = row - 1; r <= row + 1; r++) {
			for (int c = col - 1; c <= col + 1; c++) {
				if (r < 0 || c < 0 || r >= size || c >= size)
					continue;
				if (grid[r][c].getState() == CellState.SHIP) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isAlreadyShot(int row, int col) {
	    CellState state = grid[row][col].getState();
	    return state == CellState.HIT || state == CellState.MISS;
	}
//	TODO uselessMove() --> toglie le celle intorno alla nave affondata
//	verificare se vada fatto su HardAIPlayer
	
	public Map<Integer, Integer> getRemainingShips() {
	    Map<Integer, Integer> map = new HashMap<>();

	    for (Ship ship : ships) {
	        if (!ship.isSunk()) {
	            int size = ship.getSize();
	            map.put(size, map.getOrDefault(size, 0) + 1);
	        }
	    }

	    return map;
	}

	public int getSize() {
		return size;
	}

	public void print(boolean reveal) {

		// Header lettere
		System.out.print("     ");
		for (int c = 0; c < size; c++) {
			System.out.printf("%c   ", (char) ('A' + c));
		}
		System.out.println();

		// Bordo superiore
		System.out.print("   ┌");
		for (int c = 0; c < size - 1; c++) {
			System.out.print("───┬");
		}
		System.out.println("───┐");

		for (int r = 0; r < size; r++) {

			// Riga contenuto
			System.out.printf("%2d │", r + 1);
			for (int c = 0; c < size; c++) {

				Cell cell = grid[r][c];
				String symbol = " ";

				switch (cell.getState()) {
				case EMPTY:
					symbol = " ";
					break;

				case SHIP:
					if (reveal) {
						Ship ship = cell.getShip();

						if (ship.isSunk()) {
							symbol = "█"; // nave affondata
						} else {
							symbol = String.valueOf(ship.getSize());
						}
					} else {
						symbol = " ";
					}
					break;

				case HIT:
				    if (cell.getShip() != null && cell.getShip().isSunk()) {
				        symbol = "█";  // trasforma hit in blocco se nave affondata
				    } else {
				        symbol = "X";
				    }
				    break;

				case MISS:
					symbol = "·";
					break;
				}

				System.out.printf(" %1s │", symbol);
			}
			System.out.println();

			// Separatori
			if (r < size - 1) {
				System.out.print("   ├");
				for (int c = 0; c < size - 1; c++) {
					System.out.print("───┼");
				}
				System.out.println("───┤");
			} else {
				System.out.print("   └");
				for (int c = 0; c < size - 1; c++) {
					System.out.print("───┴");
				}
				System.out.println("───┘");
			}
		}
	}
}