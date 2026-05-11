package com.battleship.player;

import com.battleship.game.Difficulty;
import com.battleship.game.GameConfig;
import com.battleship.model.Board;
import com.battleship.model.CellState;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Test unitari per ImpossibleAIPlayer.
 *
 * Strategia: si costruisce manualmente lo stato del trackingBoard
 * tramite setTrackingState(), simulando scenari realistici
 * senza eseguire una partita completa.
 *
 * Reflection è usata per accedere a trackingBoard (campo protected
 * della superclasse) e per testare metodi privati critici.
 */
public class ImpossibleAIPlayerTest {

    private ImpossibleAIPlayer ai;
    private Board trackingBoard;
    private GameConfig config;

    // Dimensione board di default per i test
    private static final int SIZE = 10;

    @Before
    public void setUp() throws Exception {
        config = new GameConfig(SIZE, Difficulty.IMPOSSIBLE, false);
        ai = new ImpossibleAIPlayer(config);

        // Accede al trackingBoard tramite reflection
        // (campo protected in Player, superclasse di AIPlayer)
        trackingBoard = getTrackingBoard(ai);
    }

    // =========================================================================
    // UTILITY: reflection helpers
    // =========================================================================

    /**
     * Recupera il campo trackingBoard dalla gerarchia di superclassi.
     */
    private Board getTrackingBoard(ImpossibleAIPlayer player) throws Exception {
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
        throw new NoSuchFieldException("trackingBoard non trovato nella gerarchia");
    }

    /**
     * Invoca getLargestActiveHitCluster() tramite reflection.
     */
    @SuppressWarnings("unchecked")
    private java.util.List<int[]> invokeLargestCluster() throws Exception {
        Method method = ImpossibleAIPlayer.class
                .getDeclaredMethod("getLargestActiveHitCluster");
        method.setAccessible(true);
        return (java.util.List<int[]>) method.invoke(ai);
    }

    // =========================================================================
    // makeMove() — comportamento generale
    // =========================================================================

    @Test
    public void testMakeMove_emptyBoard_returnsValidCoordinate() {
        // Su board vuota, makeMove() deve restituire coordinate valide
        int[] move = ai.makeMove();

        assertNotNull("makeMove() non deve restituire null", move);
        assertEquals("Il move deve avere lunghezza 2", 2, move.length);
        assertTrue("row deve essere >= 0", move[0] >= 0);
        assertTrue("row deve essere < SIZE", move[0] < SIZE);
        assertTrue("col deve essere >= 0", move[1] >= 0);
        assertTrue("col deve essere < SIZE", move[1] < SIZE);
    }

    @Test
    public void testMakeMove_neverRepeatsSameCell() {
        // L'AI non deve mai sparare due volte nella stessa cella
        java.util.Set<String> fired = new java.util.HashSet<>();

        // Spara 30 volte e aggiorna il trackingBoard come farebbe Game.java
        for (int i = 0; i < 30; i++) {
            int[] move = ai.makeMove();
            String key = move[0] + "," + move[1];

            assertFalse("L'AI ha sparato due volte in " + key, fired.contains(key));
            fired.add(key);

            // Simula aggiornamento trackingBoard (MISS per semplicità)
            trackingBoard.setTrackingState(move[0], move[1], CellState.MISS);
        }
    }

    @Test
    public void testMakeMove_withHitPresent_targetsNearHit() {
        // Arrange: HIT in posizione centrale
        trackingBoard.setTrackingState(5, 5, CellState.HIT);

        // Act: l'AI deve scegliere una cella che fa parte
        // di un placement che copre (5,5)
        int[] move = ai.makeMove();

        // Verifica che la mossa non sia in una zona completamente lontana:
        // con un solo HIT, le uniche celle valide sono quelle in placement
        // che includono (5,5). Su board 10x10 con navi fino a 5,
        // la mossa deve essere nella stessa riga o colonna di (5,5)
        // entro la distanza massima della nave più lunga (5 celle)
        boolean sameRow = move[0] == 5;
        boolean sameCol = move[1] == 5;
        assertTrue("Con HIT in (5,5), la mossa deve essere nella stessa riga o colonna",
                sameRow || sameCol);
    }

    @Test
    public void testMakeMove_withHitCluster_targetsExtension() {
        // Arrange: cluster orizzontale HIT in (3,3) e (3,4)
        trackingBoard.setTrackingState(3, 3, CellState.HIT);
        trackingBoard.setTrackingState(3, 4, CellState.HIT);

        // Marca come MISS tutto tranne la riga 3 per isolare il comportamento
        // (non obbligatorio, ma rende il test più deterministico)

        int[] move = ai.makeMove();

        // Con cluster orizzontale, solo placement orizzontali sono validi
        // quindi la mossa deve essere sulla stessa riga del cluster
        assertEquals("Con cluster orizzontale su riga 3, la mossa deve essere in riga 3",
                3, move[0]);
    }

    @Test(expected = IllegalStateException.class)
    public void testMakeMove_fullBoard_throwsIllegalStateException() {
        // Arrange: segna tutta la board come già sparata
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                trackingBoard.setTrackingState(r, c, CellState.MISS);
            }
        }
        // Act: deve lanciare IllegalStateException
        ai.makeMove();
    }

    // =========================================================================
    // getLargestActiveHitCluster() — BFS clustering
    // =========================================================================

    @Test
    public void testGetLargestCluster_noHits_returnsEmpty() throws Exception {
        java.util.List<int[]> cluster = invokeLargestCluster();
        assertTrue("Su board senza HIT, il cluster deve essere vuoto",
                cluster.isEmpty());
    }

    @Test
    public void testGetLargestCluster_singleHit_returnsSize1() throws Exception {
        trackingBoard.setTrackingState(2, 2, CellState.HIT);

        java.util.List<int[]> cluster = invokeLargestCluster();

        assertEquals("Un singolo HIT deve produrre cluster di dimensione 1",
                1, cluster.size());
        assertEquals(2, cluster.get(0)[0]);
        assertEquals(2, cluster.get(0)[1]);
    }

    @Test
    public void testGetLargestCluster_connectedHits_returnsAllConnected() throws Exception {
        // Cluster connesso orizzontale: (4,4), (4,5), (4,6)
        trackingBoard.setTrackingState(4, 4, CellState.HIT);
        trackingBoard.setTrackingState(4, 5, CellState.HIT);
        trackingBoard.setTrackingState(4, 6, CellState.HIT);

        java.util.List<int[]> cluster = invokeLargestCluster();

        assertEquals("Tre HIT connessi devono formare un cluster di dimensione 3",
                3, cluster.size());
    }

    @Test
    public void testGetLargestCluster_twoClusters_returnsLargest() throws Exception {
        // Cluster A: dimensione 3
        trackingBoard.setTrackingState(0, 0, CellState.HIT);
        trackingBoard.setTrackingState(0, 1, CellState.HIT);
        trackingBoard.setTrackingState(0, 2, CellState.HIT);

        // Cluster B: dimensione 1 (separato da MISS impliciti — non connesso)
        trackingBoard.setTrackingState(9, 9, CellState.HIT);

        java.util.List<int[]> cluster = invokeLargestCluster();

        assertEquals("Deve restituire il cluster più grande (dimensione 3)",
                3, cluster.size());
    }

    @Test
    public void testGetLargestCluster_hitsNotConnected_separateClusters() throws Exception {
        // Due HIT con gap tra loro: non devono formare un unico cluster
        trackingBoard.setTrackingState(0, 0, CellState.HIT);
        trackingBoard.setTrackingState(0, 2, CellState.HIT); // gap in (0,1)

        java.util.List<int[]> cluster = invokeLargestCluster();

        // Entrambi i cluster hanno dimensione 1, il risultato è uno dei due
        assertEquals("HIT non connessi non devono formare un cluster unico",
                1, cluster.size());
    }

    // =========================================================================
    // handleShotResult() — aggiornamento trackingBoard
    // =========================================================================

    @Test
    public void testHandleShotResult_hit_setsHitOnTrackingBoard() {
        int[] move = {3, 3};
        ai.handleShotResult(move, true, false);

        assertEquals("Dopo HIT, trackingBoard deve segnare HIT",
                CellState.HIT, trackingBoard.getState(3, 3));
    }

    @Test
    public void testHandleShotResult_miss_setsMissOnTrackingBoard() {
        int[] move = {3, 3};
        ai.handleShotResult(move, false, false);

        assertEquals("Dopo MISS, trackingBoard deve segnare MISS",
                CellState.MISS, trackingBoard.getState(3, 3));
    }

    @Test
    public void testHandleShotResult_sunk_marksSurroundingsAsMiss() {
        // Arrange: nave da 1 colpita e affondata in (5,5)
        trackingBoard.setTrackingState(5, 5, CellState.HIT);
        int[] move = {5, 5};

        // Act
        ai.handleShotResult(move, true, true);

        // Assert: le celle circostanti devono essere MISS
        assertEquals(CellState.MISS, trackingBoard.getState(4, 4));
        assertEquals(CellState.MISS, trackingBoard.getState(4, 5));
        assertEquals(CellState.MISS, trackingBoard.getState(4, 6));
        assertEquals(CellState.MISS, trackingBoard.getState(5, 4));
        assertEquals(CellState.MISS, trackingBoard.getState(5, 6));
        assertEquals(CellState.MISS, trackingBoard.getState(6, 4));
        assertEquals(CellState.MISS, trackingBoard.getState(6, 5));
        assertEquals(CellState.MISS, trackingBoard.getState(6, 6));
    }

    @Test
    public void testHandleShotResult_sunk_doesNotOverwriteHitCells() {
        // Le celle HIT della nave affondata non devono diventare MISS
        trackingBoard.setTrackingState(5, 5, CellState.HIT);
        ai.handleShotResult(new int[]{5, 5}, true, true);

        assertEquals("La cella HIT affondata non deve diventare MISS",
                CellState.HIT, trackingBoard.getState(5, 5));
    }
}