package com.battleship.player;

import com.battleship.game.Difficulty;
import com.battleship.game.GameConfig;
import com.battleship.model.Board;
import com.battleship.model.CellState;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

/**
 * Test unitari per HardAIPlayer.
 * Copre: assenza di mosse duplicate, aggiornamento trackingMoves,
 * pulizia stato dopo affondamento.
 */
public class HardAIPlayerTest {

    private HardAIPlayer ai;
    private Board trackingBoard;

    @Before
    public void setUp() throws Exception {
        GameConfig config = new GameConfig(10, Difficulty.HARD, false);
        ai = new HardAIPlayer(config);
        trackingBoard = getTrackingBoard(ai);
    }

    private Board getTrackingBoard(HardAIPlayer player) throws Exception {
        Class<?> clazz = player.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("trackingBoard");
                field.setAccessible(true);
                return (Board) field.get(player);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("trackingBoard non trovato");
    }

    @Test
    public void testMakeMove_noShot_returnsValidCoordinate() {
        int[] move = ai.makeMove();
        assertNotNull(move);
        assertEquals(2, move.length);
        assertTrue(move[0] >= 0 && move[0] < 10);
        assertTrue(move[1] >= 0 && move[1] < 10);
    }

    @Test
    public void testMakeMove_neverShootsSameCell() {
        java.util.Set<String> fired = new java.util.HashSet<>();

        for (int i = 0; i < 50; i++) {
            int[] move = ai.makeMove();
            String key = move[0] + "," + move[1];
            assertFalse("HardAI ha sparato due volte in " + key, fired.contains(key));
            fired.add(key);
            trackingBoard.setTrackingState(move[0], move[1], CellState.MISS);
        }
    }

    @Test
    public void testHandleShotResult_hit_enqueuesMove() throws Exception {
        int[] move = {3, 3};
        ai.handleShotResult(move, true, false);

        // Verifica che trackingMoves non sia vuota tramite reflection
        Field field = HardAIPlayer.class.getDeclaredField("trackingMoves");
        field.setAccessible(true);
        java.util.Queue<?> queue = (java.util.Queue<?>) field.get(ai);

        assertFalse("Dopo un HIT, trackingMoves non deve essere vuota", queue.isEmpty());
    }

    @Test
    public void testHandleShotResult_sunk_clearsTrackingMoves() throws Exception {
        // Arrange: simula un HIT precedente
        trackingBoard.setTrackingState(3, 3, CellState.HIT);
        ai.handleShotResult(new int[]{3, 3}, true, false);

        // Act: affonda la nave
        ai.handleShotResult(new int[]{3, 3}, true, true);

        // Assert: trackingMoves deve essere vuota
        Field field = HardAIPlayer.class.getDeclaredField("trackingMoves");
        field.setAccessible(true);
        java.util.Queue<?> queue = (java.util.Queue<?>) field.get(ai);

        assertTrue("Dopo SUNK, trackingMoves deve essere vuota", queue.isEmpty());
    }
}