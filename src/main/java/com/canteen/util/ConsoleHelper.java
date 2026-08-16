package com.canteen.util;

import java.util.Scanner;

public class ConsoleHelper {
    private final Scanner scanner = new Scanner(System.in);

    public void printLine(String text) {
        System.out.println(text);
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public String readRequiredString(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
            printLine("Value cannot be empty.");
        }
    }

    public int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt).trim());
            } catch (NumberFormatException exception) {
                printLine("Enter a valid integer.");
            }
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt).trim());
            } catch (NumberFormatException exception) {
                printLine("Enter a valid number.");
            }
        }
    }
}
