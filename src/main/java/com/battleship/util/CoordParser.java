package com.battleship.util;

public class CoordParser {

    public static int[] parse(String input, int size) {
        input = input.toUpperCase().trim();

        char letter = input.charAt(0);
        int col = letter - 'A';

        int row = Integer.parseInt(input.substring(1)) - 1;

        if (col < 0 || col >= size || row < 0 || row >= size) {
            throw new IllegalArgumentException("Invalid coordinate");
        }

        return new int[]{row, col};
    }
}