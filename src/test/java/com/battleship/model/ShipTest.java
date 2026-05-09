package com.battleship.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test unitari per Ship.
 * Copre: costruzione, registrazione hit, rilevamento affondamento,
 * gestione posizioni.
 */
public class ShipTest {

    @Test
    public void testGetSize_returnsCorrectSize() {
        Ship ship = new Ship(4);
        assertEquals(4, ship.getSize());
    }

    @Test
    public void testIsSunk_newShip_returnsFalse() {
        Ship ship = new Ship(3);
        assertFalse("Una nave appena creata non deve essere affondata", ship.isSunk());
    }

    @Test
    public void testIsSunk_partialHits_returnsFalse() {
        Ship ship = new Ship(3);
        ship.hit();
        ship.hit();
        assertFalse("Con 2 hit su 3, la nave non deve essere affondata", ship.isSunk());
    }

    @Test
    public void testIsSunk_allHits_returnsTrue() {
        Ship ship = new Ship(3);
        ship.hit();
        ship.hit();
        ship.hit();
        assertTrue("Con tutti gli hit, la nave deve essere affondata", ship.isSunk());
    }

    @Test
    public void testIsSunk_sizeOne_oneHit_returnsTrue() {
        // Caso limite: nave da 1 cella
        Ship ship = new Ship(1);
        ship.hit();
        assertTrue("Una nave da 1 deve affondarsi al primo hit", ship.isSunk());
    }

    @Test
    public void testAddPosition_and_getPositions() {
        Ship ship = new Ship(2);
        ship.addPosition(0, 0);
        ship.addPosition(0, 1);

        assertEquals("La nave da 2 deve avere 2 posizioni registrate",
                2, ship.getPositions().size());
    }

    @Test
    public void testIsSunk_sizeMaxFleet_requiresAllHits() {
        // Nave da 5 (massima nella flotta): richiede esattamente 5 hit
        Ship ship = new Ship(5);
        for (int i = 0; i < 4; i++) ship.hit();
        assertFalse("Con 4 hit su 5, la nave non è ancora affondata", ship.isSunk());
        ship.hit();
        assertTrue("Con 5 hit su 5, la nave deve essere affondata", ship.isSunk());
    }
}