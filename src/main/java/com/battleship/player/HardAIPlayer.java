package com.battleship.player;

public class HardAIPlayer extends AIPlayer {

    public HardAIPlayer(int size) {
        super(size);
    }

    @Override
    public int[] makeMove() {
    	
//  Se NON ho hit → SEARCH MODE
//  Se ho 1 hit → prova attorno
//  Se ho 2 hit → segui direzione
//  Se nave affondata → reset

//    	lastShot = moves.get(moves.size()-1)
//    	boolean targetingMode = (lastShot == hit && lastShot != sunk);
    	boolean targetingMode = false;
		int[] move;
		do {
			move = targetingMode ? targetMode() : searchMode();
		}
		while (board.isAlreadyShot(move[0], move[1]));
//		TODO vedi uselessMove() su Board.java
		moves.add(move);
		return move;
    }
    
    private int[] searchMode() {
//  evitare zone impossibili --> intorno alle navi
//		if () {	// se il precedente attacco era una sunk
//    		segna come mancati tutte le caselle intorno alla nave
//    	}
    	
//  SEARCH MODE:
//  spara a pattern (scacchiera)
//  evita celle già usate
//  (r + c) % 2 == 0
  	
    	return super.makeMove();
    }
    private int[] targetMode() {
//  TARGET MODE:
//  Quando fai HIT:
//  salva posizione
//  prova attorno: ↑ ↓ ← →
//    
//  Quando fai secondo HIT
//  Hai direzione:
//  orizzontale → continua dx/sx
//  verticale → continua su/giù
    	return super.makeMove();
    }
    
//  ---------------------------
//  struttura dati ideale
//  List<int[]> hitQueue
//  Direction direction
//  boolean targetingMode
//   
//     
//  probabilistic AI (heatmap)
//  tracking lunghezze navi rimaste
    
}