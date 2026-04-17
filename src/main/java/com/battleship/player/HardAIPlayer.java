package com.battleship.player;

public class HardAIPlayer extends AIPlayer {

    public HardAIPlayer(int size) {
        super(size);
    }

    @Override
    public int[] makeMove() {
    	int row = 0;
    	int col = 0;
    	
    	if(row == 0 || col == 0) {
    		return super.makeMove(); // fallback temporaneo
    	}
    	int[] move = new int[] { row, col };
		moves.add(move);
		return move;
    }
    
    /* SEARCH MODE:
     * spara a pattern (scacchiera)
     * evita celle già usate
     * (r + c) % 2 == 0
     * ---------------------------
     * TARGET MODE:
     * Quando fai HIT:
     * salva posizione
     * prova attorno: ↑ ↓ ← →
     * 
     * Quando fai secondo HIT
     * Hai direzione:
     * orizzontale → continua dx/sx
     * verticale → continua su/giù
     * ----------------------------
     * struttura dati ideale
     * List<int[]> hitQueue
     * Direction direction
     * boolean targetingMode
     * 
     * Se NON ho hit → SEARCH MODE
     * Se ho 1 hit → prova attorno
     * Se ho 2 hit → segui direzione
     * Se nave affondata → reset
     * 
     * probabilistic AI (heatmap)
     * evitare zone impossibili --> intorno alle navi
     * tracking lunghezze navi rimaste
     */
}