import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Random;

public class BankingSystem {
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    long loggedInAccount = -1; // Stores the currently logged-in account number

    public BankingSystem() {
        connectDB();
        showHomePage();
    }

    public void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/bank", "root", "@Ktmduke39t");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showHomePage() {
        JFrame frame = new JFrame("Banking System");
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        JButton createAccBtn = new JButton("Create New Account");
        JButton loginBtn = new JButton("Existing User Login");

        createAccBtn.addActionListener(e -> showRegisterPage());
        loginBtn.addActionListener(e -> showLoginPage());

        frame.add(createAccBtn);
        frame.add(loginBtn);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void showRegisterPage() {
        JFrame frame = new JFrame("Register Account");
        frame.setSize(400, 500);
        frame.setLayout(new GridLayout(10, 2));

        JTextField nameField = new JTextField();
        JTextField dobField = new JTextField();
        JTextField fatherNameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField aadharField = new JTextField();
        JTextField panField = new JTextField();
        JTextField mobileField = new JTextField();
        JTextField addressField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton registerBtn = new JButton("Register");

        frame.add(new JLabel("Name:")); frame.add(nameField);
        frame.add(new JLabel("DOB (YYYY-MM-DD):")); frame.add(dobField);
        frame.add(new JLabel("Father Name:")); frame.add(fatherNameField);
        frame.add(new JLabel("Age:")); frame.add(ageField);
        frame.add(new JLabel("Aadhar No:")); frame.add(aadharField);
        frame.add(new JLabel("PAN No:")); frame.add(panField);
        frame.add(new JLabel("Mobile No:")); frame.add(mobileField);
        frame.add(new JLabel("Address:")); frame.add(addressField);
        frame.add(new JLabel("Password:")); frame.add(passwordField);
        frame.add(registerBtn);

        registerBtn.addActionListener(e -> {
            String name = nameField.getText();
            String dob = dobField.getText();
            String fatherName = fatherNameField.getText();
            int age = Integer.parseInt(ageField.getText());
            String aadhar = aadharField.getText();
            String pan = panField.getText();
            String mobile = mobileField.getText();
            String address = addressField.getText();
            String password = new String(passwordField.getPassword());
            long accountNumber = generateAccountNumber();

            try {
                pst = con.prepareStatement("INSERT INTO users (account_number, name, dob, father_name, age, aadhar, pan, mobile, address, password, balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                pst.setLong(1, accountNumber);
                pst.setString(2, name);
                pst.setString(3, dob);
                pst.setString(4, fatherName);
                pst.setInt(5, age);
                pst.setString(6, aadhar);
                pst.setString(7, pan);
                pst.setString(8, mobile);
                pst.setString(9, address);
                pst.setString(10, password);
                pst.setDouble(11, 0.0);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Registration Successful! Your Account Number: " + accountNumber);
                frame.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    public long generateAccountNumber() {
        Random random = new Random();
        long accountNumber;

        while (true) {
            accountNumber = 1000000000L + (long) (random.nextDouble() * 9000000000L);

            try {
                pst = con.prepareStatement("SELECT account_number FROM users WHERE account_number = ?");
                pst.setLong(1, accountNumber);
                rs = pst.executeQuery();

                if (!rs.next()) {
                    break; // If account number does not exist, use it
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return accountNumber;
    }


    public void showLoginPage() {
        JFrame frame = new JFrame("Login");
        frame.setSize(300, 200);
        frame.setLayout(new GridLayout(3, 2));

        JTextField accNumField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        frame.add(new JLabel("Account Number:")); frame.add(accNumField);
        frame.add(new JLabel("Password:")); frame.add(passwordField);
        frame.add(loginBtn);

        loginBtn.addActionListener(e -> {
            long accNum = Long.parseLong(accNumField.getText());
            String password = new String(passwordField.getPassword());

            try {
                pst = con.prepareStatement("SELECT * FROM users WHERE account_number = ? AND password = ?");
                pst.setLong(1, accNum);
                pst.setString(2, password);
                rs = pst.executeQuery();

                if (rs.next()) {
                    loggedInAccount = accNum; // Set the session
                    JOptionPane.showMessageDialog(frame, "Login Successful!");
                    frame.dispose();
                    showDashboard();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid Credentials!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public void showDashboard() {
        if (loggedInAccount == -1) {
            JOptionPane.showMessageDialog(null, "No active session. Please log in.");
            return;
        }

        JFrame frame = new JFrame("Dashboard");
        frame.setSize(500, 400);
        frame.setLayout(new GridLayout(3, 2, 10, 10)); // 3 rows, 2 columns

        JButton accountBtn = new JButton("Accounts");
        JButton depositBtn = new JButton("Deposit");
        JButton checkBalanceBtn = new JButton("Check Balance");
        JButton transactionBtn = new JButton("Transaction History");
        JButton loanBtn = new JButton("Loans");
        JButton logoutBtn = new JButton("Logout");

        // Add event listeners
        accountBtn.addActionListener(e -> viewAccountDetails());
        depositBtn.addActionListener(e -> depositMoney());
        checkBalanceBtn.addActionListener(e -> checkBalance());
        transactionBtn.addActionListener(e -> viewTransactionHistory());
        loanBtn.addActionListener(e -> loanOptions());
        logoutBtn.addActionListener(e -> {
            loggedInAccount = -1;
            JOptionPane.showMessageDialog(frame, "Logged out successfully.");
            frame.dispose();
            showHomePage();
        });

        // Add buttons to frame
        frame.add(accountBtn);
        frame.add(depositBtn);
        frame.add(checkBalanceBtn);
        frame.add(transactionBtn);
        frame.add(loanBtn);
        frame.add(logoutBtn);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }


    public void checkBalance() {
        try {
            pst = con.prepareStatement("SELECT balance FROM users WHERE account_number = ?");
            pst.setLong(1, loggedInAccount);
            rs = pst.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                JOptionPane.showMessageDialog(null, "Your Balance: " + balance);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public void viewAccountDetails() {
        try {
            pst = con.prepareStatement("SELECT * FROM users WHERE account_number = ?");
            pst.setLong(1, loggedInAccount);
            rs = pst.executeQuery();

            if (rs.next()) {
                String details = "Name: " + rs.getString("name") +
                        "\nAccount Number: " + rs.getLong("account_number") +
                        "\nAadhar No: " + rs.getString("aadhar") +
                        "\nPAN No: " + rs.getString("pan") +
                        "\nMobile No: " + rs.getString("mobile") +
                        "\nAddress: " + rs.getString("address") +
                        "\nAvailable Balance: ₹" + rs.getDouble("balance");

                JOptionPane.showMessageDialog(null, details, "Account Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void depositMoney() {
        String[] options = {"Send Money", "Withdraw Money", "Add Money", "Cancel"};
        int choice = JOptionPane.showOptionDialog(null, "Choose an option:", "Deposit Session",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0: // Send Money
                sendMoney();
                break;
            case 1: // Withdraw Money
                withdrawMoney();
                break;
            case 2: // Add Money
                addMoney();
                break;
            case 3: // Cancel
                return;
            default:
                JOptionPane.showMessageDialog(null, "No option selected.");
        }
    }

    private void sendMoney() {
        String accountNumberStr = JOptionPane.showInputDialog("Enter account number to send money:");
        String amountStr = JOptionPane.showInputDialog("Enter amount to send:");

        if (accountNumberStr != null && !accountNumberStr.trim().isEmpty() && amountStr != null && !amountStr.trim().isEmpty()) {
            long accountNumber = Long.parseLong(accountNumberStr);
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(null, "Amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number for the amount.");
                return;
            }

            try {
                // Check if sufficient balance is available
                pst = con.prepareStatement("SELECT balance FROM users WHERE account_number = ?");
                pst.setLong(1, loggedInAccount);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    double currentBalance = rs.getDouble("balance");
                    if (currentBalance < amount) {
                        JOptionPane.showMessageDialog(null, "Insufficient balance.");
                        return;
                    }
                }

                // Deduct amount from current account
                pst = con.prepareStatement("UPDATE users SET balance = balance - ? WHERE account_number = ?");
                pst.setDouble(1, amount);
                pst.setLong(2, loggedInAccount);
                pst.executeUpdate();

                // Add amount to the recipient's account
                pst = con.prepareStatement("UPDATE users SET balance = balance + ? WHERE account_number = ?");
                pst.setDouble(1, amount);
                pst.setLong(2, accountNumber);
                int rowsAffected = pst.executeUpdate();

                if (rowsAffected > 0) {
                    // Log the transaction
                    pst = con.prepareStatement("INSERT INTO transactions (account_number, type, amount) VALUES (?, 'Send Money', ?)");
                    pst.setLong(1, loggedInAccount);
                    pst.setDouble(2, amount);
                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(null, "₹" + amount + " sent successfully to account " + accountNumber + "!");
                } else {
                    JOptionPane.showMessageDialog(null, "Account number not found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Account number and amount cannot be empty.");
        }
    }

    private void withdrawMoney() {
        String amountStr = JOptionPane.showInputDialog("Enter amount to withdraw:");

        if (amountStr != null && !amountStr.trim().isEmpty()) {
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(null, "Withdrawal amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.");
                return;
            }

            try {
                // Check if sufficient balance is available
                pst = con.prepareStatement("SELECT balance FROM users WHERE account_number = ?");
                pst.setLong(1, loggedInAccount);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    double currentBalance = rs.getDouble("balance");
                    if (currentBalance < amount) {
                        JOptionPane.showMessageDialog(null, "Insufficient balance.");
                        return;
                    }
                }

                // Deduct amount from the account
                pst = con.prepareStatement("UPDATE users SET balance = balance - ? WHERE account_number = ?");
                pst.setDouble(1, amount);
                pst.setLong(2, loggedInAccount);
                pst.executeUpdate();

                // Log the transaction
                pst = con.prepareStatement("INSERT INTO transactions (account_number, type, amount) VALUES (?, 'Withdraw', ?)");
                pst.setLong(1, loggedInAccount);
                pst.setDouble(2, amount);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "₹" + amount + " withdrawn successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Withdrawal amount cannot be empty.");
        }
    }

    private void addMoney() {
        String amountStr = JOptionPane.showInputDialog("Enter amount to add:");

        if (amountStr != null && !amountStr.trim().isEmpty()) {
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(null, "Amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.");
                return;
            }

            try {
                // Add amount to the user's account
                pst = con.prepareStatement("UPDATE users SET balance = balance + ? WHERE account_number = ?");
                pst.setDouble(1, amount);
                pst.setLong(2, loggedInAccount);
                pst.executeUpdate();

                // Log the transaction
                pst = con.prepareStatement("INSERT INTO transactions (account_number, type, amount) VALUES (?, 'Add Money', ?)");
                pst.setLong(1, loggedInAccount);
                pst.setDouble(2, amount);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(null, "₹" + amount + " added successfully to your account!");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Amount cannot be empty.");
        }
    }


    public void viewTransactionHistory() {
        try {
            pst = con.prepareStatement("SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_date DESC");
            pst.setLong(1, loggedInAccount);
            rs = pst.executeQuery();

            StringBuilder history = new StringBuilder("Transaction History:\n");
            while (rs.next()) {
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");

                // Displaying transaction type with appropriate message
                if (type.equals("Deposit")) {
                    history.append("Deposited: ₹").append(amount).append("\n");
                } else if (type.equals("Withdrawal")) {
                    history.append("Withdrew: ₹").append(amount).append("\n");
                } else if (type.equals("Received")) {
                    history.append("Received: ₹").append(amount).append("\n");
                } else {
                    history.append(type).append(": ₹").append(amount).append("\n");
                }
            }

            if (history.length() == 0) {
                history.append("No transactions found.");
            }

            JOptionPane.showMessageDialog(null, history.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loanOptions() {
        JFrame frame = new JFrame("Loan Options");
        frame.setSize(300, 300);
        frame.setLayout(new FlowLayout());

        JButton applyLoanBtn = new JButton("Apply for Loan");
        JButton viewLoanBtn = new JButton("View Loans");

        applyLoanBtn.addActionListener(e -> applyForLoan());
        viewLoanBtn.addActionListener(e -> viewLoans());

        frame.add(applyLoanBtn);
        frame.add(viewLoanBtn);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    public void applyForLoan() {
        try {
            // Get user age for eligibility check
            PreparedStatement pst = con.prepareStatement("SELECT age FROM users WHERE account_number = ?");
            pst.setLong(1, loggedInAccount);
            ResultSet rs = pst.executeQuery();

            int cibilScore = 0; // Initialize cibilScore

            if (rs.next()) {
                int age = rs.getInt("age");

                // Prompt user for CIBIL score
                String cibilScoreStr = JOptionPane.showInputDialog("Enter your CIBIL score:");
                if (cibilScoreStr == null || cibilScoreStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "CIBIL score cannot be empty.");
                    return;
                }

                try {
                    cibilScore = Integer.parseInt(cibilScoreStr);
                    if (cibilScore < 0) {
                        JOptionPane.showMessageDialog(null, "CIBIL score cannot be negative.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number for CIBIL score.");
                    return;
                }

                // Check age eligibility
                if (age < 21) {
                    JOptionPane.showMessageDialog(null, "Loan denied. Age must be above 21.");
                    return;
                }

                // Check CIBIL score eligibility
                if (cibilScore < 650) {
                    JOptionPane.showMessageDialog(null, "Loan denied. CIBIL score must be above 650.");
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(null, "User not found.");
                return;
            }

            // Prompt user for loan amount
            String amountStr = JOptionPane.showInputDialog("Enter loan amount:");
            if (amountStr == null || amountStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Loan amount cannot be empty.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(null, "Loan amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.");
                return;
            }

            // Prompt user for loan tenure
            String tenureStr = JOptionPane.showInputDialog("Enter loan tenure (in months):");
            if (tenureStr == null || tenureStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Loan tenure cannot be empty.");
                return;
            }

            int tenure;
            try {
                tenure = Integer.parseInt(tenureStr);
                if (tenure <= 0) {
                    JOptionPane.showMessageDialog(null, "Loan tenure must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number for tenure.");
                return;
            }

            // Collect additional details: annual income and profession
            String annualIncomeStr = JOptionPane.showInputDialog("Enter your annual income:");
            if (annualIncomeStr == null || annualIncomeStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Annual income cannot be empty.");
                return;
            }

            double annualIncome;
            try {
                annualIncome = Double.parseDouble(annualIncomeStr);
                if (annualIncome <= 0) {
                    JOptionPane.showMessageDialog(null, "Annual income must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number for annual income.");
                return;
            }

            String profession = JOptionPane.showInputDialog("Enter your profession (e.g., Farmer, Corporate Employee, Self-Employed):");
            if (profession == null || profession.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Profession cannot be empty.");
                return;
            }
            // Check length of the profession
            if (profession.length() > 50) { // Adjust this number based on your database column definition
                JOptionPane.showMessageDialog(null, "Profession cannot exceed 50 characters.");
                return;
            }

            // Set a default interest rate
            double interestRate = 5.5; // Change this as needed

            // Insert loan record with all necessary fields, including tenure, CIBIL score, annual income, and profession
            pst = con.prepareStatement("INSERT INTO loans (account_number, loan_amount, interest_rate, tenure, cibil_score, status, annual_income, profession) VALUES (?, ?, ?, ?, ?, 'Approved', ?, ?)");
            pst.setLong(1, loggedInAccount);
            pst.setDouble(2, amount);
            pst.setDouble(3, interestRate);
            pst.setInt(4, tenure);
            pst.setInt(5, cibilScore); // Use the manually entered CIBIL score
            pst.setDouble(6, annualIncome); // Include annual income
            pst.setString(7, profession);    // Include profession
            pst.executeUpdate();

            // Credit loan amount to user account
            pst = con.prepareStatement("UPDATE users SET balance = balance + ? WHERE account_number = ?");
            pst.setDouble(1, amount);
            pst.setLong(2, loggedInAccount);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(null, "Loan of ₹" + amount + " approved at " + interestRate + "% interest for " + tenure + " months and credited!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }

    public void viewLoans() {
        try {
            // Prepare the SQL statement to fetch loan details for the logged-in account
            pst = con.prepareStatement("SELECT loan_amount, interest_rate, tenure, status FROM loans WHERE account_number = ?");
            pst.setLong(1, loggedInAccount);
            rs = pst.executeQuery();

            StringBuilder loanDetails = new StringBuilder("Loans:\n");
            boolean hasLoans = false; // Flag to check if any loans exist

            while (rs.next()) {
                hasLoans = true; // Set flag to true if at least one loan is found
                loanDetails.append("Amount: ₹").append(rs.getDouble("loan_amount"))
                        .append(" - Interest Rate: ").append(rs.getDouble("interest_rate"))
                        .append("% - Tenure: ").append(rs.getInt("tenure"))
                        .append(" months - Status: ").append(rs.getString("status"))
                        .append("\n");
            }

            if (!hasLoans) {
                loanDetails.append("No loans found for this account.");
            }

            // Display the loan details in a message dialog
            JOptionPane.showMessageDialog(null, loanDetails.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching loan details: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        new BankingSystem();
    }
}
