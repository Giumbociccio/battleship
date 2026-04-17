package com.battleship.model;

public class Cell {
    private CellState state;
    private Ship ship;

    public Cell() {
        this.state = CellState.EMPTY;
    }

    public CellState getState() { return state; }
    public void setState(CellState state) { this.state = state; }

    public Ship getShip() { return ship; }
    public void setShip(Ship ship) { this.ship = ship; }
}