import java.awt.Desktop;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExpenseTracker {

    private static Map<String, Double> expenses = new HashMap<>();
    private static Set<String> categories = new HashSet<>();  // Store unique categories
    private static int saveCounter = 0;  // To ensure unique filenames

    public static void main(String[] args) {
        loadExpenses();
        Scanner scanner = new Scanner(System.in);

        int numExpenses = getPositiveInteger(scanner, "How many expenses would you like to add to your list? ");

        for (int i = 1; i <= numExpenses; i++) {
            System.out.println("\nExpense #" + i);
            addExpense(scanner, i == 1 && categories.isEmpty());  // First expense case handling
        }

        while (true) {
            System.out.println("\nExpense Tracker Menu:");
            System.out.println("1. Add more expenses");
            System.out.println("2. View all expenses");
            System.out.println("3. View total spending by category");
            System.out.println("4. View total spending across all categories");
            System.out.println("5. Exit and save");

            System.out.print("Choose an option (1-5): ");
            int option = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            switch (option) {
                case 1:
                    numExpenses = getPositiveInteger(scanner, "How many more expenses would you like to add? ");
                    for (int i = 1; i <= numExpenses; i++) {
                        System.out.println("\nExpense #" + i);
                        addExpense(scanner, false);  // For subsequent expenses, the first flag is false
                    }
                    break;
                case 2:
                    viewExpenses();
                    break;
                case 3:
                    viewSpendingByCategory();
                    break;
                case 4:
                    viewTotalSpending();
                    break;
                case 5:
                    saveExpenses(scanner);
                    openFolder();
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    // New method to get a positive integer
    private static int getPositiveInteger(Scanner scanner, String prompt) {
        int number = -1;
        while (number <= 0) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                number = scanner.nextInt();
                if (number <= 0) {
                    System.out.println("Please enter a positive integer.");
                }
            } else {
                System.out.println("Invalid input! Please enter a positive integer.");
                scanner.next();  // Consume invalid input
            }
        }
        scanner.nextLine();  // Consume newline
        return number;
    }

    private static void addExpense(Scanner scanner, boolean isFirstExpense) {
        System.out.print("Enter the expense description: ");
        String description = scanner.nextLine();

        // Ensure only a valid positive number or zero is entered for the amount
        double amount = -1;
        while (amount < 0) {
            try {
                System.out.print("Enter the amount (0 or positive number): ");
                amount = scanner.nextDouble();
                if (amount < 0) {
                    System.out.println("Invalid input! Please enter a number that is zero or greater.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a valid number.");
                scanner.nextLine();  // Consume the invalid input
            }
        }
        scanner.nextLine();  // Consume newline after the amount

        // If it's the first expense and there are no categories, default to adding a new category
        String category = isFirstExpense ? addNewCategory(scanner) : chooseCategory(scanner);

        // Add the expense
        String key = description + " (" + category + ")";
        expenses.put(key, amount);

        // Add category to the set of categories if it's new
        categories.add(category);

        System.out.println("Expense added successfully!");
    }

    // Handle the addition of a new category
    private static String addNewCategory(Scanner scanner) {
        System.out.print("Enter the new category: ");
        return scanner.nextLine();
    }

    // Modified method to handle category selection with validation
    private static String chooseCategory(Scanner scanner) {
        while (true) {
            System.out.println("Do you want to:");
            System.out.println("1. Add a new category");
            System.out.println("2. Use an existing category");
            System.out.print("Choose 1 or 2: ");

            int choice = -1;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline
            } else {
                scanner.nextLine();  // Consume invalid input
            }

            if (choice == 1) {
                // Add a new category
                return addNewCategory(scanner);
            } else if (choice == 2) {
                if (!categories.isEmpty()) {
                    // Display existing categories
                    System.out.println("Existing categories:");
                    List<String> categoryList = new ArrayList<>(categories);
                    for (int i = 0; i < categoryList.size(); i++) {
                        System.out.println((i + 1) + ". " + categoryList.get(i));
                    }

                    // Let the user choose a category
                    System.out.print("Choose a category by number: ");
                    int categoryIndex = scanner.nextInt() - 1;
                    scanner.nextLine();  // Consume newline
                    if (categoryIndex >= 0 && categoryIndex < categoryList.size()) {
                        return categoryList.get(categoryIndex);
                    } else {
                        System.out.println("Invalid choice, please try again.");
                    }
                } else {
                    System.out.println("No categories available. Please add a new category.");
                }
            } else {
                System.out.println("Invalid input! You must choose either 1 or 2.");
            }
        }
    }

    private static void viewExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded.");
        } else {
            System.out.println("\nAll Expenses:");
            for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                System.out.println(entry.getKey() + " - $" + entry.getValue());
            }
        }
    }

    private static void viewSpendingByCategory() {
        Map<String, Double> categoryTotals = new HashMap<>();

        // Calculate total spending by category
        for (String key : expenses.keySet()) {
            String category = key.substring(key.indexOf("(") + 1, key.indexOf(")"));
            double amount = expenses.get(key);
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }

        if (categoryTotals.isEmpty()) {
            System.out.println("No expenses recorded.");
        } else {
            System.out.println("\nTotal Spending by Category:");
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                System.out.println(entry.getKey() + " - $" + entry.getValue());
            }
        }
    }

    private static void viewTotalSpending() {
        double totalSpending = 0;

        // Calculate total spending across all categories
        for (double amount : expenses.values()) {
            totalSpending += amount;
        }

        System.out.println("\nTotal Spending across all categories: $" + totalSpending);
    }

    private static void saveExpenses(Scanner scanner) {
        // Ask the user for a base file name
        System.out.print("Enter a name for the file (without extension): ");
        String baseName = scanner.nextLine();

        // Append a timestamp and counter to the file name to avoid duplicates
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = baseName + "_" + timestamp + "_" + (++saveCounter) + ".txt";

        // Get the Desktop folder path
        String userHome = System.getProperty("user.home");
        Path folderPath = Paths.get(userHome, "Desktop", "Expense Tracker Lists");

        // Create the folder if it doesn't exist
        try {
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
        } catch (IOException e) {
            System.out.println("Error creating folder: " + e.getMessage());
            return;
        }

        // Save expenses to the file in the specified folder
        Path filePath = folderPath.resolve(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            // Writing data in a format that can easily be copied to Excel (tab-delimited)
            writer.write("Description\tCategory\tAmount\n");
            for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                String[] splitKey = entry.getKey().split(" \\(");
                String description = splitKey[0];
                String category = splitKey[1].replace(")", "");
                writer.write(description + "\t" + category + "\t" + entry.getValue());
                writer.newLine();
            }
            System.out.println("Expenses saved to " + filePath);
        } catch (IOException e) {
            System.out.println("Error saving expenses: " + e.getMessage());
        }
    }

    // Opens the folder where the file was saved
    private static void openFolder() {
        try {
            String userHome = System.getProperty("user.home");
            Path folderPath = Paths.get(userHome, "Desktop", "Expense Tracker Lists");
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folderPath.toFile());
                System.out.println("Folder opened: " + folderPath);
            } else {
                System.out.println("Desktop is not supported on this system.");
            }
        } catch (IOException e) {
            System.out.println("Error opening folder: " + e.getMessage());
        }
    }

    private static void loadExpenses() {
        // You can load expenses from a file if needed
        // Currently, no load functionality is implemented here
        System.out.println("No saved expenses to load.");
    }
}

