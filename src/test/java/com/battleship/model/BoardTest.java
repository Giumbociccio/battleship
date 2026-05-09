package com.battleship.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Map;

/**
 * Test unitari per Board.
 * Copre: piazzamento navi, controllo adiacenza, gestione colpi,
 * rilevamento affondamento, stato celle, vittoria.
 */
public class BoardTest {

    private Board board;

    @Before
    public void setUp() {
        // Arrange condiviso: board 10x10 pulita prima di ogni test
        board = new Board(10);
    }

    // =========================================================================
    // PIAZZAMENTO NAVI
    // =========================================================================

    @Test
    public void testPlaceShip_validHorizontal_returnsTrue() {
        // Arrange
        Ship ship = new Ship(3);
        // Act
        boolean result = board.placeShip(ship, 0, 0, true);
        // Assert
        assertTrue("Il piazzamento orizzontale valido deve restituire true", result);
    }

    @Test
    public void testPlaceShip_validVertical_returnsTrue() {
        Ship ship = new Ship(3);
        boolean result = board.placeShip(ship, 0, 0, false);
        assertTrue("Il piazzamento verticale valido deve restituire true", result);
    }

    @Test
    public void testPlaceShip_horizontalOutOfBounds_returnsFalse() {
        // Una nave da 4 che parte dalla colonna 8 su board 10 esce dai limiti
        Ship ship = new Ship(4);
        boolean result = board.placeShip(ship, 0, 8, true);
        assertFalse("Il piazzamento fuori dai limiti deve restituire false", result);
    }

    @Test
    public void testPlaceShip_verticalOutOfBounds_returnsFalse() {
        Ship ship = new Ship(4);
        boolean result = board.placeShip(ship, 8, 0, false);
        assertFalse("Il piazzamento verticale fuori dai limiti deve restituire false", result);
    }

    @Test
    public void testPlaceShip_exactlyAtBoundary_returnsTrue() {
        // Nave da 3 che termina esattamente all'ultima colonna — deve essere valida
        Ship ship = new Ship(3);
        boolean result = board.placeShip(ship, 0, 7, true);
        assertTrue("Il piazzamento esattamente al bordo deve essere valido", result);
    }

    @Test
    public void testPlaceShip_overlapping_returnsFalse() {
        // Arrange: prima nave già piazzata
        board.placeShip(new Ship(3), 0, 0, true);
        // Act: seconda nave che sovrappone la prima
        boolean result = board.placeShip(new Ship(2), 0, 1, true);
        assertFalse("Il piazzamento sovrapposto deve restituire false", result);
    }

    @Test
    public void testPlaceShip_adjacentHorizontal_returnsFalse() {
        // Le navi non possono essere adiacenti neanche orizzontalmente
        board.placeShip(new Ship(3), 0, 0, true);
        boolean result = board.placeShip(new Ship(2), 0, 3, true);
        assertFalse("Il piazzamento adiacente orizzontalmente deve essere rifiutato", result);
    }

    @Test
    public void testPlaceShip_adjacentVertical_returnsFalse() {
        board.placeShip(new Ship(3), 0, 0, true);
        // Nave immediatamente sotto la prima
        boolean result = board.placeShip(new Ship(2), 1, 0, true);
        assertFalse("Il piazzamento adiacente verticalmente deve essere rifiutato", result);
    }

    @Test
    public void testPlaceShip_adjacentDiagonal_returnsFalse() {
        // Vincolo diagonale: caso critico spesso dimenticato
        board.placeShip(new Ship(1), 2, 2, true);
        boolean result = board.placeShip(new Ship(1), 3, 3, true);
        assertFalse("Il piazzamento adiacente diagonalmente deve essere rifiutato", result);
    }

    @Test
    public void testPlaceShip_notAdjacentWithGap_returnsTrue() {
        // Con un gap di almeno una cella, il piazzamento è valido
        board.placeShip(new Ship(3), 0, 0, true);
        boolean result = board.placeShip(new Ship(2), 0, 5, true);
        assertTrue("Il piazzamento con gap sufficiente deve essere valido", result);
    }

    @Test
    public void testPlaceShip_setsCorrectCellStates() {
        // Verifica che le celle occupate abbiano stato SHIP
        Ship ship = new Ship(3);
        board.placeShip(ship, 2, 2, true);

        assertEquals(CellState.SHIP, board.getState(2, 2));
        assertEquals(CellState.SHIP, board.getState(2, 3));
        assertEquals(CellState.SHIP, board.getState(2, 4));
        // Celle adiacenti devono rimanere EMPTY
        assertEquals(CellState.EMPTY, board.getState(2, 5));
    }

    // =========================================================================
    // GESTIONE COLPI
    // =========================================================================

    @Test
    public void testShoot_emptyCell_returnsMiss() {
        String result = board.shoot(5, 5);
        assertEquals("MISS", result);
    }

    @Test
    public void testShoot_emptyCell_setsStateMiss() {
        board.shoot(5, 5);
        assertEquals(CellState.MISS, board.getState(5, 5));
    }

    @Test
    public void testShoot_shipCell_returnsHit() {
        board.placeShip(new Ship(3), 0, 0, true);
        String result = board.shoot(0, 0);
        assertEquals("HIT", result);
    }

    @Test
    public void testShoot_shipCell_setsStateHit() {
        board.placeShip(new Ship(3), 0, 0, true);
        board.shoot(0, 0);
        assertEquals(CellState.HIT, board.getState(0, 0));
    }

    @Test
    public void testShoot_sinkShip_returnsSunk() {
        // Nave da 1 cella: un singolo colpo deve affondarla
        board.placeShip(new Ship(1), 3, 3, true);
        String result = board.shoot(3, 3);
        assertEquals("SUNK", result);
    }

    @Test
    public void testShoot_partialHitOnLargeShip_returnsHitNotSunk() {
        // Nave da 3: i primi due colpi devono restituire HIT, non SUNK
        board.placeShip(new Ship(3), 0, 0, true);
        assertEquals("HIT", board.shoot(0, 0));
        assertEquals("HIT", board.shoot(0, 1));
        // Solo il terzo affonda
        assertEquals("SUNK", board.shoot(0, 2));
    }

    @Test
    public void testShoot_alreadyShot_returnsAlreadyShot() {
        board.shoot(5, 5); // primo colpo su cella vuota
        String result = board.shoot(5, 5); // secondo colpo sulla stessa cella
        assertEquals("Already shot", result);
    }

    @Test
    public void testShoot_alreadyHit_returnsAlreadyShot() {
        board.placeShip(new Ship(3), 0, 0, true);
        board.shoot(0, 0); // HIT
        String result = board.shoot(0, 0); // stesso punto
        assertEquals("Already shot", result);
    }

    // =========================================================================
    // RILEVAMENTO VITTORIA
    // =========================================================================

    @Test
    public void testAllShipsSunk_noShips_returnsTrue() {
        // Board senza navi: allShipsSunk() deve restituire true (nessuna nave rimanente)
        assertTrue("Board senza navi deve risultare 'tutto affondato'",
                board.allShipsSunk());
    }

    @Test
    public void testAllShipsSunk_shipNotSunk_returnsFalse() {
        board.placeShip(new Ship(2), 0, 0, true);
        assertFalse("Con nave intatta, allShipsSunk() deve restituire false",
                board.allShipsSunk());
    }

    @Test
    public void testAllShipsSunk_allShipsSunk_returnsTrue() {
        board.placeShip(new Ship(2), 0, 0, true);
        board.shoot(0, 0);
        board.shoot(0, 1);
        assertTrue("Con tutte le navi affondate, allShipsSunk() deve restituire true",
                board.allShipsSunk());
    }

    @Test
    public void testAllShipsSunk_multipleShips_partialSunk_returnsFalse() {
        board.placeShip(new Ship(2), 0, 0, true);
        board.placeShip(new Ship(2), 5, 5, true);
        // Affonda solo la prima
        board.shoot(0, 0);
        board.shoot(0, 1);
        assertFalse("Con una nave ancora intatta, allShipsSunk() deve restituire false",
                board.allShipsSunk());
    }

    // =========================================================================
    // NAVI RIMANENTI
    // =========================================================================

    @Test
    public void testGetRemainingShips_afterPlacement_correctCount() {
        board.placeShip(new Ship(3), 0, 0, true);
        board.placeShip(new Ship(2), 5, 5, true);

        Map<Integer, Integer> remaining = board.getRemainingShips();

        assertEquals("Deve esserci 1 nave da 3", Integer.valueOf(1), remaining.get(3));
        assertEquals("Deve esserci 1 nave da 2", Integer.valueOf(1), remaining.get(2));
    }

    @Test
    public void testGetRemainingShips_afterSinking_removedFromMap() {
        board.placeShip(new Ship(2), 0, 0, true);
        board.placeShip(new Ship(3), 5, 5, true);
        // Affonda la nave da 2
        board.shoot(0, 0);
        board.shoot(0, 1);

        Map<Integer, Integer> remaining = board.getRemainingShips();

        assertFalse("La nave da 2 affondata non deve comparire nella mappa",
                remaining.containsKey(2));
        assertTrue("La nave da 3 intatta deve comparire nella mappa",
                remaining.containsKey(3));
    }

    // =========================================================================
    // isAlreadyShot
    // =========================================================================

    @Test
    public void testIsAlreadyShot_freshCell_returnsFalse() {
        assertFalse(board.isAlreadyShot(3, 3));
    }

    @Test
    public void testIsAlreadyShot_afterMiss_returnsTrue() {
        board.shoot(3, 3);
        assertTrue(board.isAlreadyShot(3, 3));
    }

    @Test
    public void testIsAlreadyShot_afterHit_returnsTrue() {
        board.placeShip(new Ship(1), 3, 3, true);
        board.shoot(3, 3);
        assertTrue(board.isAlreadyShot(3, 3));
    }

    @Test
    public void testIsAlreadyShot_intArray_consistentWithRowCol() {
        board.shoot(4, 4);
        // Le due firme del metodo devono essere coerenti
        assertTrue(board.isAlreadyShot(new int[]{4, 4}));
    }
}