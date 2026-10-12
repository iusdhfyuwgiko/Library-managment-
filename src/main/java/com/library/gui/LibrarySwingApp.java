package com.library.gui;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.LibraryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LibrarySwingApp extends JFrame {
    private final LibraryService service = new LibraryService();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Book ID", "Title", "Author", "Available", "Total"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable booksTable = new JTable(tableModel);
    private final JTextField searchField = new JTextField();
    private final JTextField memberIdField = new JTextField();
    private final JTextField bookIdField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JTextField categoryField = new JTextField();
    private final JTextField copiesField = new JTextField();
    private final JTextField memberNameField = new JTextField();
    private final JTextField memberEmailField = new JTextField();

    public LibrarySwingApp() {
        super("ISME Knowledge Hub");
        setLayout(new BorderLayout(12, 12));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);

        seedData();
        buildLayout();
        refreshBooks();
    }

    private void seedData() {
        if (service.getAllBooks().isEmpty() && service.getAllMembers().isEmpty()) {
            service.addBook(new Book("B-101", "Clean Code", "Robert Martin", "Programming", 2));
            service.addBook(new Book("B-102", "Java: The Complete Reference", "Herbert Schildt", "Programming", 3));
            service.addBook(new Book("B-103", "The Alchemist", "Paulo Coelho", "Fiction", 2));
            service.addMember(new Member("M-1001", "Alice", "alice@example.com"));
            service.addMember(new Member("M-1002", "Bob", "bob@example.com"));
        }
    }

    private void buildLayout() {
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel searchLabel = new JLabel("Search title:");
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> refreshBooks(searchField.getText()));

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        booksTable.setRowHeight(28);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(booksTable);
        add(tableScrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton refreshButton = new JButton("Refresh Books");
        refreshButton.addActionListener(e -> refreshBooks());

        JButton issueButton = new JButton("Issue Selected Book");
        issueButton.addActionListener(e -> issueSelectedBook());

        JButton returnButton = new JButton("Return Selected Book");
        returnButton.addActionListener(e -> returnSelectedBook());

        JButton addBookButton = new JButton("Add Book");
        addBookButton.addActionListener(e -> addBook());

        JButton addMemberButton = new JButton("Add Member");
        addMemberButton.addActionListener(e -> addMember());

        actionPanel.add(refreshButton);
        actionPanel.add(issueButton);
        actionPanel.add(returnButton);
        actionPanel.add(addBookButton);
        actionPanel.add(addMemberButton);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Book / Member Input"));

        formPanel.add(new JLabel("Book ID:"));
        formPanel.add(bookIdField);
        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Author:"));
        formPanel.add(authorField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel("Copies:"));
        formPanel.add(copiesField);
        formPanel.add(new JLabel("Member ID:"));
        formPanel.add(memberIdField);
        formPanel.add(new JLabel("Member Name:"));
        formPanel.add(memberNameField);
        formPanel.add(new JLabel("Member Email:"));
        formPanel.add(memberEmailField);

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.add(actionPanel, BorderLayout.NORTH);
        rightPanel.add(formPanel, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.EAST);
    }

    private void refreshBooks() {
        refreshBooks(searchField.getText());
    }

    private void refreshBooks(String keyword) {
        tableModel.setRowCount(0);
        List<Book> books = keyword == null || keyword.isBlank()
                ? service.getAllBooks()
                : service.searchBooks(keyword);

        for (Book book : books) {
            tableModel.addRow(new Object[]{
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getAvailableCopies(),
                    book.getTotalCopies()
            });
        }
    }

    private void addBook() {
        String id = bookIdField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String category = categoryField.getText().trim();
        String copiesText = copiesField.getText().trim();

        try {
            int copies = Integer.parseInt(copiesText);
            service.addBook(new Book(id, title, author, category, copies));
            JOptionPane.showMessageDialog(this, "Book added successfully.");
            clearBookFields();
            refreshBooks();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid book data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMember() {
        String id = memberIdField.getText().trim();
        String name = memberNameField.getText().trim();
        String email = memberEmailField.getText().trim();

        try {
            service.addMember(new Member(id, name, email));
            JOptionPane.showMessageDialog(this, "Member added successfully.");
            clearMemberFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid member data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void issueSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a book first.");
            return;
        }

        String bookId = (String) tableModel.getValueAt(selectedRow, 0);
        String memberId = memberIdField.getText().trim();
        if (memberId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a member ID to issue the book.");
            return;
        }

        boolean success = service.issueBook(bookId, memberId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Book issued successfully.");
            refreshBooks();
        } else {
            JOptionPane.showMessageDialog(this, "Issue failed. Check book availability or member limits.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a book first.");
            return;
        }

        String bookId = (String) tableModel.getValueAt(selectedRow, 0);
        boolean returned = service.returnBook(bookId, java.time.LocalDate.now());
        if (returned) {
            JOptionPane.showMessageDialog(this, "Book returned successfully.");
            refreshBooks();
        } else {
            JOptionPane.showMessageDialog(this, "Return failed. No active issue found for the selected book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearBookFields() {
        bookIdField.setText("");
        titleField.setText("");
        authorField.setText("");
        categoryField.setText("");
        copiesField.setText("");
    }

    private void clearMemberFields() {
        memberIdField.setText("");
        memberNameField.setText("");
        memberEmailField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySwingApp().setVisible(true));
    }
}
