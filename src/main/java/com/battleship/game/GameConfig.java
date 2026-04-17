package com.battleship.game;

public class GameConfig {
	private final int size;
	private final Difficulty difficulty;
	private final boolean manualPlacement;
	private int[] shipSizes;

	public GameConfig(int size, Difficulty difficulty, boolean manualPlacement) {
		this.size = size;
		this.difficulty = difficulty;
		this.manualPlacement = manualPlacement;
		this.shipSizes = setShipSizes();
	}

	public boolean isManualPlacement() {
		return manualPlacement;
	}

	public int getSize() {
		return size;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public int[] getShipSizes() {
		return shipSizes;
	}

	private int[] setShipSizes() {
		int[] smallSize = { 5, 4, 3, 2, 2, 1, 1, 1 };
		int[] bigSize = { 5, 4, 4, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 1 };
		return (size == 10) ? smallSize : bigSize;
	}
}