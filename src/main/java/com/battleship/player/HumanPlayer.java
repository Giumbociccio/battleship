package com.battleship.player;

import com.battleship.util.CoordParser;
import java.util.Scanner;

public class HumanPlayer extends Player {
    private final Scanner scanner = new Scanner(System.in);

    public HumanPlayer(int size){ super(size); }

    @Override
    public int[] makeMove(){
        while(true){
            try{
                System.out.print("Enter move (e.g. A5): ");
                String input = scanner.nextLine();
                return CoordParser.parse(input, board.getSize());
            } catch(Exception e){
                System.out.println("Invalid input, try again.");
            }
        }
    }
}