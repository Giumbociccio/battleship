package com.battleship.game;

import com.battleship.model.*;
import com.battleship.player.*;

import java.util.*;

public class Game {

	private final Player human;
	private final Player ai;
	private List<Move> history = new ArrayList<>();

	private final GameConfig config;

	public Game(GameConfig config) {
		this.config = config;

		human = new HumanPlayer(config.getSize());
		if (config.getDifficulty() == Difficulty.EASY) {
			ai = new AIPlayer(config.getSize());
		} else {
			ai = new HardAIPlayer(config.getSize());
		}

		setupShips(human);
		setupShips(ai);
	}

	private void setupShips(Player p) {
		int[] shipSizes = config.getShipSizes();

		if (config.isManualPlacement() && p instanceof HumanPlayer) {
			manualSetup(p, shipSizes);
		} else {
			randomSetup(p, shipSizes);
		}

	}

	private void manualSetup(Player p, int[] shipSizes) {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);

		for (int size : shipSizes) {
			boolean placed = false;

			p.getBoard().print(true);
			while (!placed) {
				try {
					System.out.println("\nPosiziona nave da " + size + " caselle");
					System.out.print("Inserisci posizione (es: A5): ");
					String pos = scanner.nextLine().toUpperCase();

					int col = pos.charAt(0) - 'A';
					int row = Integer.parseInt(pos.substring(1)) - 1;

					System.out.print("Orientamento (H=orizzontale, V=verticale): ");
					String dir = scanner.nextLine().toUpperCase();

					boolean horizontal = dir.equals("H");

					placed = p.getBoard().placeShip(new Ship(size), row, col, horizontal);

					if (!placed) {
						throw new Exception("❌ Posizione non valida (sovrapposta o fuori bordo). Riprova");
					}

				} catch (NumberFormatException e) {
					System.out.println("❌ Errore: Inserisci delle coordinate valide.");
				} catch (Exception e) {
					// Qui cadono sia i 'false' che eventuali altri errori imprevisti
					System.out.println("❌ Posizione non valida. Riprova.");
				}
			}
		}
	}

	private void randomSetup(Player p, int[] shipSizes) {
		Random rand = new Random();

		for (int s : shipSizes) {
			boolean placed = false;
			while (!placed) {
				placed = p.getBoard().placeShip(new Ship(s), rand.nextInt(p.getBoard().getSize()),
						rand.nextInt(p.getBoard().getSize()), rand.nextBoolean());
			}
		}
	}

	private void printRemainingShips(Player opponent) {
		System.out.println(opponent == ai ? "\nYOUR TURN" : "\nOPPONENT'S TURN");
		System.out.println("Navi rimanenti da affondare:");

		Map<Integer, Integer> ships = opponent.getBoard().getRemainingShips();

		if (ships.isEmpty()) {
			System.out.println("Nessuna 🚢");
			return;
		}

		// stampa ordinata per dimensione
		ships.keySet().stream().sorted(Comparator.reverseOrder()).forEach(size -> {
			int count = ships.get(size);
			System.out.println("Navi da " + size + ": " + count);
		});
	}

	public void start() {
		Player current = human;
		Player opponent = ai;

		while (true) {
			if (current == human) {
				System.out.println("\nYour board:");
				human.getBoard().print(true);

				System.out.println("\nEnemy board:");
				ai.getBoard().print(true); // true --> mostra posizioni navi nemiche
			}

			if (opponent.getBoard().allShipsSunk()) {
				if (current == human) {
					System.out.println("🎉 YOU WON!");
				} else {
					System.out.println("💀 YOU LOST!");
				}
				System.out.println("\n--- STATISTICHE ---");

				System.out.println("\nGiocatore:");
				System.out.println("Colpi: " + human.getShots());
				System.out.println("Hit: " + human.getHits());
				System.out.println("Miss: " + human.getMisses());
				System.out.printf("Precisione: %.2f%%\n", human.getAccuracy());

				System.out.println("\nAI:");
				System.out.println("Colpi: " + ai.getShots());
				System.out.println("Hit: " + ai.getHits());
				System.out.println("Miss: " + ai.getMisses());
				System.out.printf("Precisione: %.2f%%\n", ai.getAccuracy());
				break;
			}

			printRemainingShips(opponent);

			int[] move;
			String result;
			int row, col;
			do {
				move = current.makeMove();
				row = move[0];
				col = move[1];
				result = opponent.getBoard().shoot(row, col);
				System.out.println("" + ((char) ('A' + col)) + (row + 1) + " => " + result);
			} while (result.equalsIgnoreCase("Already shot"));

			boolean hit = result.contains("HIT") || result.contains("SUNK");

			current.recordShot(hit);
			history.add(new Move(row, col, hit));

			if (current instanceof AIPlayer) {
				((AIPlayer) current).processResult(move, hit);
			}

			Player temp = current;
			current = opponent;
			opponent = temp;
		}
	}
}