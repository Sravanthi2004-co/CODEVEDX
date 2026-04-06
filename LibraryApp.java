import java.sql.*;
import java.util.Scanner;

public class LibraryApp {

    static final String URL = "jdbc:mysql://127.0.0.1:3306/library"
        + "?allowPublicKeyRetrieval=true"
        + "&useSSL=false"
        + "&serverTimezone=UTC"
        + "&autoReconnect=true";
    static final String USER = "root";
    static final String PASS = "root123"; // ← Set your MySQL password here

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✔ Connected to Database Successfully!");

            // Create table if not exists
            createTable(con);

            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("\n==== Library Management System ====");
                System.out.println("1. Add Book");
                System.out.println("2. View Available Books");
                System.out.println("3. Issue Book");
                System.out.println("4. Return Book");
                System.out.println("5. Exit");
                System.out.print("Enter Choice: ");

                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1 -> addBook(con, sc);
                    case 2 -> viewBooks(con);
                    case 3 -> issueBook(con, sc);
                    case 4 -> returnBook(con, sc);
                    case 5 -> {
                        System.out.println("Exiting... Bye!");
                        con.close();
                        return;
                    }
                    default -> System.out.println("Invalid choice! Try again.");
                }
            }

        } catch (ClassNotFoundException e) {
            System.out.println("✘ MySQL Driver not found. Add mysql-connector-j JAR to classpath.");
        } catch (SQLException e) {
            System.out.println("✘ Connection Failed: " + e.getMessage());
            System.out.println("➡ Make sure MySQL is running and password is correct in PASS field.");
        }
    }

    // Auto-creates the library database table if it doesn't exist
    static void createTable(Connection con) {
        try {
            Statement st = con.createStatement();
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS books (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(100) NOT NULL, " +
                "author VARCHAR(100) NOT NULL, " +
                "available BOOLEAN DEFAULT TRUE)"
            );
        } catch (SQLException e) {
            System.out.println("✘ Table creation error: " + e.getMessage());
        }
    }

    static void addBook(Connection con, Scanner sc) {
        try {
            System.out.print("Enter Book Title: ");
            String title = sc.nextLine();
            System.out.print("Enter Author Name: ");
            String author = sc.nextLine();

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO books (title, author, available) VALUES (?, ?, TRUE)");
            ps.setString(1, title);
            ps.setString(2, author);
            ps.executeUpdate();
            System.out.println("✔ Book Added Successfully!");

        } catch (SQLException e) {
            System.out.println("✘ Error adding book: " + e.getMessage());
        }
    }

    static void viewBooks(Connection con) {
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT * FROM books WHERE available = TRUE");

            System.out.println("\n---------- Available Books ----------");
            System.out.printf("%-5s %-30s %-20s%n", "ID", "Title", "Author");
            System.out.println("-------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-30s %-20s%n",
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"));
            }
            if (!found) System.out.println("No books available right now.");

        } catch (SQLException e) {
            System.out.println("✘ Error viewing books: " + e.getMessage());
        }
    }

    static void issueBook(Connection con, Scanner sc) {
        try {
            viewBooks(con);
            System.out.print("\nEnter Book ID to Issue: ");
            int id = sc.nextInt();
            sc.nextLine();

            PreparedStatement ps = con.prepareStatement(
                "UPDATE books SET available = FALSE WHERE id = ? AND available = TRUE");
            ps.setInt(1, id);
            int rows = ps.executeUpdate();

            if (rows > 0) System.out.println("✔ Book Issued Successfully!");
            else System.out.println("✘ Book not available or ID not found.");

        } catch (SQLException e) {
            System.out.println("✘ Error issuing book: " + e.getMessage());
        }
    }

    static void returnBook(Connection con, Scanner sc) {
        try {
            System.out.print("Enter Book ID to Return: ");
            int id = sc.nextInt();
            sc.nextLine();

            PreparedStatement ps = con.prepareStatement(
                "UPDATE books SET available = TRUE WHERE id = ?");
            ps.setInt(1, id);
            int rows = ps.executeUpdate();

            if (rows > 0) System.out.println("✔ Book Returned Successfully!");
            else System.out.println("✘ Book ID not found.");

        } catch (SQLException e) {
            System.out.println("✘ Error returning book: " + e.getMessage());
        }
    }
}