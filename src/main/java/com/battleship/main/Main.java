package com.battleship.main;

import com.battleship.game.Difficulty;
import com.battleship.game.Game;
import com.battleship.game.GameConfig;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {

		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);

		boolean playAgain = true;

		while (playAgain) {

			System.out.println("=== BATTLESHIP ===");
			System.out.println("Scegli modalità:");
			System.out.println("1. Classica: 10x10 (default)");
			System.out.println("2. Estesa: 26x26");
			System.out.print("Scelta (1 o 2): ");

			int choice = 1;
			try {
				choice = Integer.parseInt(scanner.nextLine());
			} catch (NumberFormatException e) {
				System.out.println("Inserita modalità di default");
			}

			System.out.println("\nScegli difficoltà:");
			System.out.println("1. Easy (default)");
			System.out.println("2. Hard");
			System.out.print("Scelta: ");

			int diffChoice = 1;
			try {
				diffChoice = Integer.parseInt(scanner.nextLine());
			} catch (NumberFormatException e) {
				System.out.println("Inserita modalità di default");
			}
			
			Difficulty difficulty = (diffChoice == 2) ? Difficulty.HARD : Difficulty.EASY;

			System.out.println("\nScegli sistemazione navi");
			System.out.println("1. Navi casuali (default)");
			System.out.println("2. Inserimento manuale");
			System.out.print("Scelta: ");

			int placementChoice = 1;
			try{
				placementChoice = Integer.parseInt(scanner.nextLine());
			} catch (NumberFormatException e) {
				System.out.println("Inserita modalità di default");
			}
			boolean manualPlacement = (placementChoice == 2);

			// ================ inizia la configurazione automatica
			GameConfig config;

			int size = 10;
			if (choice == 2)
				size = 26;

			config = new GameConfig(size, difficulty, manualPlacement);

			Game game = new Game(config);
			game.start();

			// 🔁 PLAY AGAIN
			System.out.print("\nVuoi giocare di nuovo? (Y/N): ");
			String answer = scanner.nextLine().trim().toUpperCase();

			playAgain = answer.equals("Y");
			System.out.println();
		}

		System.out.println("Grazie per aver giocato!");
	}
}