package com.battleship.game;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test unitari per GameConfig.
 * Copre: correttezza delle flotte per entrambe le dimensioni di board,
 * coerenza dei parametri restituiti.
 */
public class GameConfigTest {

    @Test
    public void testGetSize_10_returnsCorrectSize() {
        GameConfig config = new GameConfig(10, Difficulty.EASY, false);
        assertEquals(10, config.getSize());
    }

    @Test
    public void testGetSize_26_returnsCorrectSize() {
        GameConfig config = new GameConfig(26, Difficulty.HARD, false);
        assertEquals(26, config.getSize());
    }

    @Test
    public void testGetShipSizes_10x10_correctFleet() {
        GameConfig config = new GameConfig(10, Difficulty.EASY, false);
        int[] sizes = config.getShipSizes();

        // Flotta attesa: {5,4,3,2,2,1,1,1}
        assertNotNull(sizes);
        assertEquals("La flotta 10x10 deve avere 8 navi", 8, sizes.length);

        // Verifica composizione
        int[] expected = {5, 4, 3, 2, 2, 1, 1, 1};
        assertArrayEquals("La flotta 10x10 deve corrispondere alla specifica",
                expected, sizes);
    }

    @Test
    public void testGetShipSizes_26x26_correctFleet() {
        GameConfig config = new GameConfig(26, Difficulty.HARD, false);
        int[] sizes = config.getShipSizes();

        // Flotta attesa: {5,4,4,3,3,3,2,2,2,2,1,1,1,1,1}
        assertNotNull(sizes);
        assertEquals("La flotta 26x26 deve avere 15 navi", 15, sizes.length);

        int[] expected = {5, 4, 4, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 1};
        assertArrayEquals("La flotta 26x26 deve corrispondere alla specifica",
                expected, sizes);
    }

    @Test
    public void testGetDifficulty_returnsCorrectDifficulty() {
        GameConfig config = new GameConfig(10, Difficulty.IMPOSSIBLE, false);
        assertEquals(Difficulty.IMPOSSIBLE, config.getDifficulty());
    }

    @Test
    public void testIsManualPlacement_true_returnsTrue() {
        GameConfig config = new GameConfig(10, Difficulty.EASY, true);
        assertTrue(config.isManualPlacement());
    }

    @Test
    public void testIsManualPlacement_false_returnsFalse() {
        GameConfig config = new GameConfig(10, Difficulty.EASY, false);
        assertFalse(config.isManualPlacement());
    }

    @Test
    public void testGetShipSizes_noShipLargerThanBoard() {
        GameConfig config = new GameConfig(10, Difficulty.EASY, false);
        for (int size : config.getShipSizes()) {
            assertTrue("Nessuna nave deve essere più grande della board",
                    size <= config.getSize());
        }
    }
}