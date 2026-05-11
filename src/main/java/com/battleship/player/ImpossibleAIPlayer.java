package com.battleship.player;

import java.util.*;

import com.battleship.game.GameConfig;
import com.battleship.model.CellState;

/**
 * ImpossibleAIPlayer — livello di difficoltà massimo.
 *
 * Strategia: Probability Density Heatmap. Per ogni mossa, l'AI calcola per ogni
 * cella non ancora sparata quante configurazioni valide delle navi rimanenti la
 * attraversano. La cella con densità di probabilità più alta viene selezionata.
 *
 * Non utilizza search mode / target mode tradizionali: la heatmap gestisce
 * implicitamente entrambe le situazioni, concentrandosi automaticamente sulle
 * zone di HIT non affondati.
 */
public class ImpossibleAIPlayer extends HardAIPlayer {

	/**
	 * Costruisce un'istanza di ImpossibleAIPlayer.
	 *
	 * @param size la dimensione della board di gioco (es. 10 per 10x10)
	 */
	public ImpossibleAIPlayer(GameConfig config) {
		super(config);
	}

	// -------------------------------------------------------------------------
	// OVERRIDE PRINCIPALE: makeMove()
	// -------------------------------------------------------------------------

	/**
	 * Calcola la mossa ottimale tramite heatmap probabilistica.
	 *
	 * Procedura: 1. Costruisce una griglia di punteggi (heatmap) 2. Per ogni nave
	 * rimanente e ogni possibile posizionamento compatibile, incrementa il
	 * punteggio delle celle coinvolte 3. Seleziona la cella con punteggio massimo
	 * tra quelle non ancora sparate
	 *
	 * @return int[] con [row, col] della mossa ottimale
	 */
	@Override
	public int[] makeMove() {
		int size = trackingBoard.getSize();
		int[][] heatmap = new int[size][size];

		// Ottieni le dimensioni delle navi ancora da affondare dalla configurazione di
		// gioco
		int[] remainingShipSizes = getRemainingShipSizes();

		// Calcolo UNA SOLA VOLTA per tutta la generazione della heatmap
		List<int[]> activeCluster = getLargestActiveHitCluster();

		// Popola la heatmap considerando ogni nave rimanente
		for (int shipSize : remainingShipSizes) {
			populateHeatmap(heatmap, shipSize, size, activeCluster);
		}

		// Seleziona la cella con punteggio massimo
		return selectBestMove(heatmap, size);
	}

	// -------------------------------------------------------------------------
	// COSTRUZIONE DELLA HEATMAP
	// -------------------------------------------------------------------------

	/**
	 * Popola la heatmap considerando tutti i posizionamenti validi per una nave di
	 * lunghezza {@code shipSize}.
	 *
	 * Un posizionamento è "valido" se: - Rimane all'interno dei limiti della board
	 * - Nessuna cella del percorso è MISS (la nave non può essere lì) - Non
	 * contraddice i HIT esistenti: se una cella del percorso è già HIT, essa deve
	 * essere inclusa in una configurazione coerente
	 *
	 * @param heatmap       la griglia di punteggi da aggiornare
	 * @param shipSize      la lunghezza della nave considerata
	 * @param size          la dimensione della board
	 * @param activeCluster cluster di HIT connessi più grande attualmente noto,
	 *                      usato per vincolare i placement alla probabilità
	 *                      condizionata. Lista vuota in assenza di HIT attivi
	 *                      (search mode)
	 */
	private void populateHeatmap(int[][] heatmap, int shipSize, int size, List<int[]> activeCluster) {

		// Prova tutti i posizionamenti orizzontali
		for (int row = 0; row < size; row++) {
			for (int col = 0; col <= size - shipSize; col++) {
				if (isValidPlacement(row, col, shipSize, true, size, activeCluster)) {
					for (int k = 0; k < shipSize; k++)
						heatmap[row][col + k]++;
				}
			}
		}

		// Prova tutti i posizionamenti verticali
		for (int row = 0; row <= size - shipSize; row++) {
			for (int col = 0; col < size; col++) {
				if (isValidPlacement(row, col, shipSize, false, size, activeCluster)) {
					for (int k = 0; k < shipSize; k++)
						heatmap[row + k][col]++;
				}
			}
		}
	}

	/**
	 * Verifica se un posizionamento è compatibile con lo stato attuale del
	 * trackingBoard.
	 *
	 * Regole di compatibilità: - Una cella MISS esclude il posizionamento (la nave
	 * non può trovarsi lì) - Una cella EMPTY o HIT è accettabile - Le celle già
	 * SUNK (marcate come MISS dai surroundings) sono escluse implicitamente
	 *
	 * @param startRow   riga iniziale
	 * @param startCol   colonna iniziale
	 * @param shipSize   lunghezza della nave
	 * @param horizontal true = orizzontale, false = verticale
	 * @param size       dimensione della board
	 * @return true se il posizionamento è valido
	 */
	private boolean isValidPlacement(int startRow, int startCol, int shipSize, boolean horizontal, int size,
			List<int[]> activeCluster) {

		List<int[]> placementCells = buildPlacementCells(startRow, startCol, shipSize, horizontal, size);

		if (placementCells == null)
			return false; // fuori bounds o cella MISS

		if (activeCluster.isEmpty())
			return true; // search mode: nessun vincolo HIT

// Tutti gli HIT del cluster devono essere coperti dal placement
		for (int[] hit : activeCluster) {
			if (!containsCell(placementCells, hit[0], hit[1]))
				return false;
		}

		return true;
	}

	/**
	 * Costruisce la lista di celle occupate da un placement. Restituisce null se il
	 * placement è invalido: - fuori dai limiti della board - attraversa una cella
	 * MISS
	 *
	 * @param startRow   riga iniziale
	 * @param startCol   colonna iniziale
	 * @param shipSize   lunghezza della nave
	 * @param horizontal true = orizzontale, false = verticale
	 * @param size       dimensione della board
	 * @return lista di celle, oppure null se invalido
	 */
	private List<int[]> buildPlacementCells(int startRow, int startCol, int shipSize, boolean horizontal, int size) {
		List<int[]> cells = new ArrayList<>();

		for (int k = 0; k < shipSize; k++) {
			int r = horizontal ? startRow : startRow + k;
			int c = horizontal ? startCol + k : startCol;

			if (r < 0 || r >= size || c < 0 || c >= size)
				return null;
			if (trackingBoard.getState(r, c) == CellState.MISS)
				return null;

			cells.add(new int[] { r, c });
		}

		return cells;
	}

	/**
	 * Trova il gruppo di HIT connessi più numeroso nel trackingBoard. Questo
	 * rappresenta la nave parzialmente colpita su cui l'AI deve concentrarsi.
	 *
	 * Se ci sono più cluster della stessa dimensione, restituisce il primo trovato
	 * in ordine di scansione — sufficiente per la heatmap.
	 *
	 * @return lista di celle del cluster più grande, vuota se non esistono HIT
	 */
	private List<int[]> getLargestActiveHitCluster() {
		int size = trackingBoard.getSize();
		boolean[][] visited = new boolean[size][size];
		List<int[]> largest = new ArrayList<>();

		for (int r = 0; r < size; r++) {
			for (int c = 0; c < size; c++) {
				if (trackingBoard.getState(r, c) == CellState.HIT && !visited[r][c]) {
					List<int[]> cluster = getConnectedHits(new int[] { r, c }, size);
					// Marca tutte le celle del cluster come visitate
					for (int[] cell : cluster)
						visited[cell[0]][cell[1]] = true;
					if (cluster.size() > largest.size())
						largest = cluster;
				}
			}
		}
		return largest;
	}

	// -------------------------------------------------------------------------
	// SELEZIONE DELLA MOSSA MIGLIORE
	// -------------------------------------------------------------------------

	/**
	 * Scansiona la heatmap e restituisce la cella con punteggio massimo tra quelle
	 * non ancora sparate.
	 *
	 * In caso di parità tra più celle, viene selezionata la prima trovata in ordine
	 * di scansione (row-major). Una futura ottimizzazione potrebbe introdurre una
	 * scelta casuale tra i pareggianti.
	 *
	 * @param heatmap la griglia di punteggi calcolata
	 * @param size    la dimensione della board
	 * @return int[] con [row, col] della cella ottimale
	 * @throws IllegalStateException se non esistono celle disponibili (non dovrebbe
	 *                               accadere in una partita non ancora terminata)
	 */
	private int[] selectBestMove(int[][] heatmap, int size) {
		int bestScore = -1;
		List<int[]> bestCells = new ArrayList<>();

		for (int r = 0; r < size; r++) {
			for (int c = 0; c < size; c++) {
				if (trackingBoard.isAlreadyShot(r, c))
					continue;

				if (heatmap[r][c] > bestScore) {
					bestScore = heatmap[r][c];
					bestCells.clear();
					bestCells.add(new int[] { r, c });
				} else if (heatmap[r][c] == bestScore) {
					bestCells.add(new int[] { r, c });
				}
			}
		}

		if (bestCells.isEmpty()) {
			throw new IllegalStateException("ImpossibleAIPlayer: nessuna cella disponibile. "
					+ "Verifica che la partita non sia già terminata.");
		}

		return bestCells.get(rand.nextInt(bestCells.size()));
	}

	// -------------------------------------------------------------------------
	// GESTIONE DELLA FLOTTA RIMANENTE
	// -------------------------------------------------------------------------

	/**
	 * Restituisce le dimensioni della flotta completa dalla configurazione di
	 * gioco.
	 *
	 * La heatmap usa l'intera flotta perché isValidPlacement() esclude naturalmente
	 * i posizionamenti incompatibili con lo stato del trackingBoard: le navi già
	 * affondate hanno tutte le celle circostanti marcate come MISS, quindi non
	 * producono placement validi e non contribuiscono alla heatmap.
	 *
	 * TODO: tracciare esplicitamente le navi affondate per dimensione e sottrarle
	 * dalla flotta, eliminando iterazioni inutili sui placement.
	 *
	 * @return array delle dimensioni delle navi della flotta
	 */
	private int[] getRemainingShipSizes() {
		return config.getShipSizes();
	}

	// -------------------------------------------------------------------------
	// handleShotResult() — override per disabilitare la logica HardAI
	// -------------------------------------------------------------------------

	/**
	 * Override di handleShotResult per disabilitare la logica target/search
	 * ereditata da HardAIPlayer.
	 *
	 * ImpossibleAIPlayer non usa trackingMoves né logiche direzionali: tutto è
	 * gestito dalla heatmap. Chiamiamo però super della classe nonno (AIPlayer) per
	 * mantenere l'aggiornamento di hits e la chiamata a updateTrackingBoard().
	 *
	 * @param lastMove le coordinate dell'ultimo colpo [row, col]
	 * @param hit      true se il colpo ha colpito una nave
	 * @param sunk     true se il colpo ha affondato una nave
	 */
	@Override
	public void handleShotResult(int[] lastMove, boolean hit, boolean sunk) {
		// Aggiorna trackingBoard (HIT o MISS) tramite il metodo del nonno
		// NON chiamiamo super di HardAIPlayer perché esso gestirebbe
		// trackingMoves e markSurroundingsAsMiss() che non vogliamo qui.
		//
		// Chiamiamo direttamente AIPlayer.handleShotResult() saltando HardAIPlayer.
		// In Java non è possibile chiamare super.super direttamente,
		// quindi duplichiamo la logica minima di AIPlayer:

		// 1. Aggiorna trackingBoard
		updateTrackingBoard(lastMove, hit);

		// 2. Registra hit nella lista (logica di AIPlayer)
		if (hit) {
			hits.add(lastMove);
		}

		// 3. Se la nave è affondata, marca le celle circostanti come MISS
		// (questa logica è utile anche qui per restringere lo spazio
		// delle configurazioni valide nella heatmap successiva)
		if (sunk) {
			markSurroundingsOnSunk(lastMove);
		}
	}

	/**
	 * Marca come MISS tutte le celle adiacenti (incluse diagonali) al cluster di
	 * HIT connessi a {@code lastMove}.
	 *
	 * Questo migliora la precisione della heatmap nelle mosse successive: rimuove
	 * posizionamenti impossibili attorno alla nave appena affondata.
	 *
	 * @param lastMove coordinate dell'ultimo colpo che ha affondato una nave
	 */
	private void markSurroundingsOnSunk(int[] lastMove) {
		int size = trackingBoard.getSize();
		List<int[]> shipCells = getConnectedHits(lastMove, size); // riusa lo stesso metodo

		for (int[] cell : shipCells) {
			for (int dr = -1; dr <= 1; dr++) {
				for (int dc = -1; dc <= 1; dc++) {
					int r = cell[0] + dr;
					int c = cell[1] + dc;
					if (r >= 0 && r < size && c >= 0 && c < size) {
						CellState state = trackingBoard.getState(r, c);
						if (state != CellState.HIT && state != CellState.MISS) {
							trackingBoard.setTrackingState(r, c, CellState.MISS);
						}
					}
				}
			}
		}
	}

	/**
	 * BFS generica: raccoglie tutte le celle HIT connesse a {@code start}. Usata
	 * sia da getLargestActiveHitCluster() che da markSurroundingsOnSunk().
	 */
	private List<int[]> getConnectedHits(int[] start, int size) {
		List<int[]> result = new ArrayList<>();
		Queue<int[]> queue = new LinkedList<>();
		queue.add(start);
		result.add(start);

		int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
		while (!queue.isEmpty()) {
			int[] curr = queue.poll();
			for (int[] d : dirs) {
				int r = curr[0] + d[0];
				int c = curr[1] + d[1];
				if (r >= 0 && r < size && c >= 0 && c < size && !containsCell(result, r, c)
						&& trackingBoard.getState(r, c) == CellState.HIT) {
					int[] next = { r, c };
					result.add(next);
					queue.add(next);
				}
			}
		}
		return result;
	}

	/**
	 * Verifica se una lista di celle contiene già la coordinata (r, c). Necessario
	 * perché int[] non supporta equals() per valore in Java.
	 *
	 * @param list lista di coordinate
	 * @param r    riga da cercare
	 * @param c    colonna da cercare
	 * @return true se la cella è già presente nella lista
	 */
	private boolean containsCell(List<int[]> list, int r, int c) {
		for (int[] cell : list) {
			if (cell[0] == r && cell[1] == c)
				return true;
		}
		return false;
	}
}