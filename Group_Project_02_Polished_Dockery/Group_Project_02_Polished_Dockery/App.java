/*
Aiden Canady, Connor Hiltibidal, Santonio Dockery, Justin Tran
Baltimore Ravens Project v17 | Input/Output
*/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.*;

public class App {
    private static final Color RAVENS_PURPLE = new Color(36, 23, 115);
    private static final Color RAVENS_GOLD = new Color(158, 124, 12);
    private static final Color RAVENS_BLACK = new Color(0, 0, 0);
    private static final Color BG_COLOR = new Color(250, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color DETAIL_BG = new Color(248, 248, 250);
    private static final Color HOVER_COLOR = new Color(245, 243, 255);
    private static final Color DISABLED_BG = new Color(180, 180, 180);
    private static final Color DISABLED_FG = new Color(100, 100, 100);

    static class RosterEntry {
        String category;
        String name;
        String details;
        
        RosterEntry(String category, String name, String details) {
            this.category = category;
            this.name = name;
            this.details = details;
        }
    }
    
    private static List<ExpandableCard> allCards = new ArrayList<>();
    private static JTextField searchField;
    private static JPanel contentPanel;
    private static List<RosterEntry> currentRoster = new ArrayList<>();

    interface RosterAction {
        void apply();
        void undo();
        String getDescription();
    }

    private static final Deque<RosterAction> undoStack = new ArrayDeque<>();
    private static final Deque<RosterAction> redoStack = new ArrayDeque<>();
    private static JButton undoBtn;
    private static JButton redoBtn;

    private static void executeAction(RosterAction action) {
        action.apply();
        undoStack.push(action);
        redoStack.clear();
        refreshUndoRedoButtons();
        refreshContent();
    }

    private static void undoAction() {
        if (undoStack.isEmpty()) return;
        RosterAction action = undoStack.pop();
        action.undo();
        redoStack.push(action);
        refreshUndoRedoButtons();
        refreshContent();
    }

    private static void redoAction() {
        if (redoStack.isEmpty()) return;
        RosterAction action = redoStack.pop();
        action.apply();
        undoStack.push(action);
        refreshUndoRedoButtons();
        refreshContent();
    }

    private static void styleUndoRedoButton(JButton btn, boolean enabled) {
        btn.setEnabled(enabled);
        btn.setBackground(enabled ? RAVENS_GOLD : DISABLED_BG);
        btn.setForeground(enabled ? RAVENS_BLACK : DISABLED_FG);
        btn.setCursor(new Cursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private static void refreshUndoRedoButtons() {
        if (undoBtn != null) {
            styleUndoRedoButton(undoBtn, !undoStack.isEmpty());
            undoBtn.setToolTipText(undoStack.isEmpty() ? "Nothing to undo"
                : "Undo: " + undoStack.peek().getDescription());
        }
        if (redoBtn != null) {
            styleUndoRedoButton(redoBtn, !redoStack.isEmpty());
            redoBtn.setToolTipText(redoStack.isEmpty() ? "Nothing to redo"
                : "Redo: " + redoStack.peek().getDescription());
        }
    }

    private static void refreshContent() {
        allCards.clear();
        contentPanel.removeAll();
        populateContent(contentPanel, currentRoster);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createUI);
    }

    private static void createUI() {
        JFrame frame = new JFrame("Baltimore Ravens – Roster & Staff");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 900);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_COLOR);

        JPanel header = createHeader(frame);
        mainContainer.add(header, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        String csvPath = Paths.get("ravens_roster.csv").toAbsolutePath().toString();
        List<RosterEntry> roster = loadRosterFromCSV(csvPath);
        currentRoster = roster;
        populateContent(contentPanel, roster);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        frame.add(mainContainer);
        frame.setVisible(true);
        
        SwingUtilities.invokeLater(() -> showWelcomeDialog(frame));
    }

    private static JPanel createHeader(JFrame parentFrame) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(RAVENS_PURPLE);
        header.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("BALTIMORE RAVENS");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(RAVENS_GOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("2024-25 Roster & Coaching Staff");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));

        searchField = new JTextField(25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(RAVENS_GOLD, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterContent(searchField.getText());
            }
        });

        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel filterLabel = new JLabel("  Filter: ");
        filterLabel.setForeground(Color.WHITE);
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        String[] filterOptions = {
            "All Positions",
            "QB - Quarterbacks",
            "RB - Running Backs",
            "WR - Wide Receivers",
            "TE - Tight Ends",
            "OL - Offensive Line",
            "DL - Defensive Line",
            "LB - Linebackers",
            "DB - Defensive Backs",
            "ST - Special Teams",
            "Coaches"
        };
        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        filterDropdown.setFont(new Font("Arial", Font.PLAIN, 13));
        filterDropdown.setBackground(Color.WHITE);
        filterDropdown.setForeground(RAVENS_BLACK);
        filterDropdown.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterDropdown.setMaximumRowCount(11);
        filterDropdown.addActionListener(e -> {
            String selected = (String) filterDropdown.getSelectedItem();
            String filter = "";
            if (selected.startsWith("QB")) filter = "QB";
            else if (selected.startsWith("RB")) filter = "RB";
            else if (selected.startsWith("WR")) filter = "WR";
            else if (selected.startsWith("TE")) filter = "TE";
            else if (selected.startsWith("OL")) filter = "OL";
            else if (selected.startsWith("DL")) filter = "DL";
            else if (selected.startsWith("LB")) filter = "LB";
            else if (selected.startsWith("DB")) filter = "DB";
            else if (selected.startsWith("ST")) filter = "ST";
            else if (selected.equals("Coaches")) filter = "Coach";
            filterByCategory(filter);
        });

        JButton expandAll = createControlButton("Expand All");
        expandAll.addActionListener(e -> toggleAll(true));

        JButton collapseAll = createControlButton("Collapse All");
        collapseAll.addActionListener(e -> toggleAll(false));

        JButton aboutBtn = createControlButton("About");
        aboutBtn.addActionListener(e -> showAboutDialog(parentFrame));

        JButton helpBtn = createControlButton("Help");
        helpBtn.addActionListener(e -> showHelpDialog(parentFrame));

        JButton importBtn = createControlButton("Import CSV/Excel");
        importBtn.addActionListener(e -> importRoster(parentFrame));

        JButton exportBtn = createControlButton("Export CSV");
        exportBtn.addActionListener(e -> exportRoster(parentFrame));

        JButton addPlayerBtn = createControlButton("+ Add Player");
        addPlayerBtn.addActionListener(e -> showAddPlayerDialog(parentFrame));

        JButton removePlayerBtn = createControlButton("- Remove Player");
        removePlayerBtn.addActionListener(e -> showRemovePlayerDialog(parentFrame));

        undoBtn = createControlButton("<< Undo");
        undoBtn.setEnabled(false);
        undoBtn.setBackground(DISABLED_BG);
        undoBtn.setForeground(DISABLED_FG);
        undoBtn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        undoBtn.addActionListener(e -> undoAction());

        redoBtn = createControlButton(">> Redo");
        redoBtn.setEnabled(false);
        redoBtn.setBackground(DISABLED_BG);
        redoBtn.setForeground(DISABLED_FG);
        redoBtn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        redoBtn.addActionListener(e -> redoAction());        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row1.setBackground(RAVENS_PURPLE);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.add(searchLabel);
        row1.add(searchField);
        row1.add(filterLabel);
        row1.add(filterDropdown);
        row1.add(expandAll);
        row1.add(collapseAll);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setBackground(RAVENS_PURPLE);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        row2.add(aboutBtn);
        row2.add(helpBtn);
        row2.add(importBtn);
        row2.add(exportBtn);
        row2.add(addPlayerBtn);
        row2.add(removePlayerBtn);
        row2.add(undoBtn);
        row2.add(redoBtn);

        header.add(title);
        header.add(subtitle);
        header.add(row1);
        header.add(row2);

        final int SINGLE_ROW_THRESHOLD = 1280;
        final int ROW1_PERMANENT_ITEMS = 6;

        parentFrame.addComponentListener(new ComponentAdapter() {
            private boolean merged = false;

            @Override
            public void componentResized(ComponentEvent e) {
                int w = parentFrame.getWidth();
                boolean wantMerged = (w >= SINGLE_ROW_THRESHOLD);

                if (wantMerged && !merged) {
                    // Move row-2 buttons onto row 1
                    for (Component c : row2.getComponents()) {
                        row1.add(c);
                    }
                    row2.setVisible(false);
                    merged = true;
                    row1.revalidate();
                    header.revalidate();
                    header.repaint();
                } else if (!wantMerged && merged) {
                    // Move the extra buttons back to row 2, preserving order
                    Component[] r1 = row1.getComponents();
                    row2.removeAll();
                    for (int i = ROW1_PERMANENT_ITEMS; i < r1.length; i++) {
                        row2.add(r1[i]);
                        row1.remove(r1[i]);
                    }
                    row2.setVisible(true);
                    merged = false;
                    row1.revalidate();
                    row2.revalidate();
                    header.revalidate();
                    header.repaint();
                }
            }
        });

        return header;
    }

    private static JButton createControlButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setBackground(RAVENS_GOLD);
        btn.setForeground(RAVENS_BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(RAVENS_GOLD.brighter());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(btn.isEnabled() ? RAVENS_GOLD : DISABLED_BG);
            }
        });
        
        return btn;
    }

    // Filter cards by category/position and move to top
    private static void filterByCategory(String category) {
        searchField.setText(""); // Clear search field
        String lowerCategory = category.toLowerCase().trim();
        
        // If "All Positions" selected, restore original content
        if (lowerCategory.isEmpty()) {
            allCards.clear();
            contentPanel.removeAll();
            populateContent(contentPanel, currentRoster);
            
            contentPanel.revalidate();
            contentPanel.repaint();
            
            // Scroll to top
            SwingUtilities.invokeLater(() -> {
                contentPanel.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            });
            return;
        }
        
        // Separate cards into matching and non-matching
        // Save the current card list before clearing allCards for the rebuild
        List<ExpandableCard> currentCards = new ArrayList<>(allCards);
        List<ExpandableCard> matchingCards = new ArrayList<>();
        List<ExpandableCard> nonMatchingCards = new ArrayList<>();
        
        for (ExpandableCard card : currentCards) {
            boolean matches = card.searchText.toLowerCase().contains(lowerCategory);
            card.setVisible(true); // Show all cards
            
            if (matches) {
                matchingCards.add(card);
            } else {
                nonMatchingCards.add(card);
            }
        }
        
        // Reorder the content panel
        contentPanel.removeAll();
        
        // Add filtered section header
        String filterTitle = "";
        if (lowerCategory.equals("qb")) filterTitle = "QUARTERBACKS";
        else if (lowerCategory.equals("rb")) filterTitle = "RUNNING BACKS";
        else if (lowerCategory.equals("wr")) filterTitle = "WIDE RECEIVERS";
        else if (lowerCategory.equals("te")) filterTitle = "TIGHT ENDS";
        else if (lowerCategory.equals("ol")) filterTitle = "OFFENSIVE LINE";
        else if (lowerCategory.equals("dl")) filterTitle = "DEFENSIVE LINE";
        else if (lowerCategory.equals("lb")) filterTitle = "LINEBACKERS";
        else if (lowerCategory.equals("db")) filterTitle = "DEFENSIVE BACKS";
        else if (lowerCategory.equals("st")) filterTitle = "SPECIAL TEAMS";
        else if (lowerCategory.equals("coach")) filterTitle = "COACHING STAFF";
        
        if (!filterTitle.isEmpty() && !matchingCards.isEmpty()) {
            addSection(contentPanel, filterTitle, RAVENS_GOLD);
        }
        
        // Add matching cards first
        for (ExpandableCard card : matchingCards) {
            contentPanel.add(card);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        
        // Add a separator if there are both matching and non-matching
        if (!matchingCards.isEmpty() && !nonMatchingCards.isEmpty()) {
            JPanel separator = new JPanel();
            separator.setLayout(new BorderLayout());
            separator.setBackground(BG_COLOR);
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            separator.setAlignmentX(Component.LEFT_ALIGNMENT);
            separator.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            
            JLabel separatorLabel = new JLabel("─────  Other Positions  ─────");
            separatorLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            separatorLabel.setForeground(new Color(150, 150, 160));
            separatorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            separator.add(separatorLabel, BorderLayout.CENTER);
            contentPanel.add(separator);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        
        // Add non-matching cards after
        for (ExpandableCard card : nonMatchingCards) {
            contentPanel.add(card);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
        
        // Scroll to top
        SwingUtilities.invokeLater(() -> {
            contentPanel.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        });
    }

    private static List<RosterEntry> loadRosterFromCSV(String filename) {
        List<RosterEntry> roster = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = parseCSVLine(line);
                if (parts.length >= 3) {
                    roster.add(new RosterEntry(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim().replace("\\n", "\n")
                    ));
                } else {
                    System.err.println("Warning: Line " + lineNumber + " has only " + parts.length + " fields");
                }
            }
            
            System.out.println("Successfully loaded " + roster.size() + " entries from " + filename);
            
        } catch (FileNotFoundException e) {
            System.err.println("CSV file not found: " + filename);
            System.err.println("Using default roster data instead.");
            System.err.println("Make sure " + filename + " is in the correct directory");
            return getDefaultRoster();
        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
            return getDefaultRoster();
        }
        
        if (roster.isEmpty()) {
            System.err.println("Warning: CSV file was empty, using default data");
            return getDefaultRoster();
        }
        
        return roster;
    }
    
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }

    private static List<RosterEntry> getDefaultRoster() {
        String howTo =
            "No roster file was loaded.\n\n" +
            "To get started:\n" +
            "  1. Click the 'Import CSV/Excel' button in the toolbar above.\n" +
            "  2. Select a .csv or .xlsx roster file.\n" +
            "  3. The file must have three columns: Category, Name, Details.\n\n" +
            "This is a placeholder entry — it will be replaced once you import a file.";

        List<RosterEntry> roster = new ArrayList<>();
        roster.add(new RosterEntry(
            "\u26A0 No Roster Loaded \u2014 Please Import a File",
            "[ Placeholder ] \u2013 Import a roster file to begin",
            howTo));
        roster.add(new RosterEntry(
            "\u26A0 No Roster Loaded \u2014 Please Import a File",
            "[ Placeholder ] \u2013 Use the 'Import CSV/Excel' button above",
            howTo));
        roster.add(new RosterEntry(
            "\u26A0 No Roster Loaded \u2014 Please Import a File",
            "[ Placeholder ] \u2013 Supported formats: .csv, .xlsx, .xls",
            howTo));
        return roster;
    }

    private static void populateContent(JPanel panel, List<RosterEntry> roster) {
        Map<String, List<RosterEntry>> categorizedRoster = new LinkedHashMap<>();
        for (RosterEntry entry : roster) {
            categorizedRoster.computeIfAbsent(entry.category, k -> new ArrayList<>()).add(entry);
        }

        String lastMajorSection = "";
        
        for (Map.Entry<String, List<RosterEntry>> categoryEntry : categorizedRoster.entrySet()) {
            String category = categoryEntry.getKey();
            
            String majorSection = category.contains(" - ") ? 
                category.substring(0, category.indexOf(" - ")) : category;
            
            if (!majorSection.equals(lastMajorSection)) {
                addSection(panel, majorSection.toUpperCase(), RAVENS_GOLD);
                lastMajorSection = majorSection;
            }
            
            if (category.contains(" - ")) {
                String subSection = category.substring(category.indexOf(" - ") + 3);
                addSubsection(panel, subSection);
            }
            
            for (RosterEntry entry : categoryEntry.getValue()) {
                String displayName = entry.name;
                String subtitle = "";
                
                if (entry.name.contains("–")) {
                    String[] parts = entry.name.split("–", 2);
                    displayName = parts[0].trim();
                    subtitle = parts[1].trim();
                } else if (entry.name.contains("-")) {
                    String[] parts = entry.name.split("-", 2);
                    displayName = parts[0].trim();
                    subtitle = parts[1].trim();
                }
                
                boolean isPlayer = category.toLowerCase().contains("player");
                addCard(panel, displayName, subtitle, entry.details, isPlayer);
            }
        }
    }

    private static void addSection(JPanel panel, String title, Color color) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(color);
        label.setBorder(BorderFactory.createEmptyBorder(30, 0, 15, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
    }

    private static void addSubsection(JPanel panel, String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(RAVENS_PURPLE);
        label.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
    }

    private static void addCard(JPanel panel, String name, String subtitle, String details, boolean isPlayer) {
        ExpandableCard card = new ExpandableCard(name, subtitle, details, isPlayer);
        allCards.add(card);
        panel.add(card);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private static void exportRoster(JFrame parent) {
        if (currentRoster.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                "No roster data to export.",
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Roster as CSV");
        chooser.setSelectedFile(new File("ravens_roster_export.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        // Ensure .csv extension
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            // Header row
            pw.println("Category,Name,Details");

            for (RosterEntry entry : currentRoster) {
                pw.print(escapeCsvField(entry.category));
                pw.print(",");
                pw.print(escapeCsvField(entry.name));
                pw.print(",");
                // Store newlines as \n literal so they survive the round-trip
                pw.println(escapeCsvField(entry.details.replace("\n", "\\n")));
            }

            JOptionPane.showMessageDialog(parent,
                "Roster exported successfully to:\n" + file.getAbsolutePath(),
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent,
                "Failed to export roster:\n" + ex.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String escapeCsvField(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static void importRoster(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Roster File");
        chooser.setFileFilter(new FileNameExtensionFilter(
            "Roster Files (*.csv, *.xlsx, *.xls)", "csv", "xlsx", "xls"));

        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        String name = file.getName().toLowerCase();
        List<RosterEntry> imported;

        try {
            if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                imported = loadRosterFromExcel(file);
            } else {
                imported = loadRosterFromCSV(file.getAbsolutePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            StringBuilder errorMsg = new StringBuilder();
            Throwable cause = ex;
            int depth = 0;
            while (cause != null && depth < 6) {
                String msg = cause.getClass().getSimpleName() +
                    (cause.getMessage() != null ? ": " + cause.getMessage() : " (no message)");
                errorMsg.append(depth == 0 ? msg : "\nCaused by: " + msg);
                cause = cause.getCause();
                depth++;
            }

            JOptionPane.showMessageDialog(parent,
                "Failed to import file:\n\n" + errorMsg.toString() +
                "\n\nCheck the terminal for the full stack trace.",
                "Import Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (imported.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                "The selected file contained no valid roster entries.\n\n" +
                "Make sure the file has three columns: Category, Name, Details.",
                "Import Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Reload the UI with the new data
        currentRoster = imported;
        undoStack.clear();
        redoStack.clear();
        refreshUndoRedoButtons();
        allCards.clear();
        contentPanel.removeAll();
        populateContent(contentPanel, currentRoster);
        contentPanel.revalidate();
        contentPanel.repaint();

        JOptionPane.showMessageDialog(parent,
            "Imported " + imported.size() + " entries from:\n" + file.getName(),
            "Import Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Load roster entries from an Excel file (.xlsx / .xls).
     * Requires Apache POI on the classpath. If POI is not available, a clear
     * error message is shown so the user knows what dependency to add.
     *
     * Expected columns (row 0 = header, skipped):
     *   col 0 – Category
     *   col 1 – Name
     *   col 2 – Details  (use literal \n for newlines, same as CSV format)
     */
    private static List<RosterEntry> loadRosterFromExcel(File file) throws Exception {
        try {
            Class.forName("org.apache.poi.ss.usermodel.WorkbookFactory");
        } catch (ClassNotFoundException e) {
            throw new Exception(
                "Apache POI is not on the classpath.\n\n" +
                "To enable Excel import, add these JARs to your project:\n" +
                "  poi-5.x.x.jar\n" +
                "  poi-ooxml-5.x.x.jar\n" +
                "  commons-collections4-4.x.jar\n\n" +
                "Download from: https://poi.apache.org/download.html\n\n" +
                "Alternatively, save your spreadsheet as .csv and import that.");
        }

        List<RosterEntry> roster = new ArrayList<>();

        Object workbook = Class.forName("org.apache.poi.ss.usermodel.WorkbookFactory")
            .getMethod("create", File.class)
            .invoke(null, file);

        Object sheet = workbook.getClass()
            .getMethod("getSheetAt", int.class)
            .invoke(workbook, 0);

        java.util.Iterator<?> rowIter = ((Iterable<?>) sheet).iterator();
        boolean firstRow = true;

        while (rowIter.hasNext()) {
            Object row = rowIter.next();
            if (firstRow) { firstRow = false; continue; }

            String category = getCellString(row, 0);
            String name     = getCellString(row, 1);
            String details  = getCellString(row, 2);

            if (category != null && !category.isBlank()
                    && name != null && !name.isBlank()) {
                String detailsClean = (details == null ? "" : details)
                    .replace("\\n", "\n");
                roster.add(new RosterEntry(category.trim(), name.trim(), detailsClean));
            }
        }

        workbook.getClass().getMethod("close").invoke(workbook);
        return roster;
    }

    private static String getCellString(Object row, int col) throws Exception {
        Object cell = row.getClass()
            .getMethod("getCell", int.class)
            .invoke(row, col);
        if (cell == null) return "";

        Object cellType = cell.getClass().getMethod("getCellType").invoke(cell);
        String typeName = cellType.toString();

        if (typeName.equals("STRING") || typeName.equals("BLANK")) {
            return (String) cell.getClass().getMethod("getStringCellValue").invoke(cell);
        } else if (typeName.equals("NUMERIC")) {
            double val = (double) cell.getClass().getMethod("getNumericCellValue").invoke(cell);
            return (val == Math.floor(val)) ? String.valueOf((long) val) : String.valueOf(val);
        } else if (typeName.equals("BOOLEAN")) {
            return String.valueOf(cell.getClass().getMethod("getBooleanCellValue").invoke(cell));
        } else {
            try {
                return (String) cell.getClass().getMethod("getStringCellValue").invoke(cell);
            } catch (Exception e) {
                return "";
            }
        }
    }

    private static void filterContent(String query) {
        String lowerQuery = query.toLowerCase().trim();
        for (ExpandableCard card : allCards) {
            boolean matches = lowerQuery.isEmpty() || 
                card.searchText.toLowerCase().contains(lowerQuery);
            card.setVisible(matches);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private static void toggleAll(boolean expand) {
        for (ExpandableCard card : allCards) {
            if (card.isVisible()) {
                card.setExpanded(expand);
            }
        }
    }

    // JOptionPane dialogs
    private static void showWelcomeDialog(JFrame parent) {
        JOptionPane.showMessageDialog(parent,
            "Welcome to the Baltimore Ravens Roster App!\n\n" +
            "Browse the complete 2024-25 roster including:\n" +
            "• Players by position\n" +
            "• Coaching staff\n" +
            "• Front office\n" +
            "• Medical & performance staff\n\n" +
            "Use the search feature to find specific people quickly!",
            "Welcome to Ravens Roster",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showAddPlayerDialog(JFrame parent) {
        Set<String> existingCategories = new LinkedHashSet<>();
        for (RosterEntry e : currentRoster) existingCategories.add(e.category);
        String[] categoryArray = existingCategories.toArray(new String[0]);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_COLOR);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        form.add(new JLabel("Category:"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        JComboBox<String> catBox = new JComboBox<>(categoryArray);
        catBox.setEditable(true);
        catBox.setPreferredSize(new Dimension(320, 28));
        form.add(catBox, gc);

        gc.gridx = 0; gc.gridy = 1; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        form.add(new JLabel("Name:"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(320, 28));
        form.add(nameField, gc);

        gc.gridx = 0; gc.gridy = 2; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        form.add(new JLabel("Position / Role:"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        JTextField roleField = new JTextField();
        roleField.setPreferredSize(new Dimension(320, 28));
        roleField.setToolTipText("e.g. QB, WR, Head Coach  (optional – shown as card subtitle)");
        form.add(roleField, gc);

        gc.gridx = 1; gc.gridy = 3; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        JLabel roleHint = new JLabel("Optional – shown as the card subtitle (e.g. QB, Head Coach)");
        roleHint.setFont(new Font("Arial", Font.ITALIC, 11));
        roleHint.setForeground(new Color(120, 120, 130));
        form.add(roleHint, gc);

        gc.gridx = 0; gc.gridy = 4; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Details:"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.BOTH; gc.weightx = 1; gc.weighty = 1;
        JTextArea detailsArea = new JTextArea(6, 30);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setPreferredSize(new Dimension(320, 120));
        form.add(detailsScroll, gc);

        int result = JOptionPane.showConfirmDialog(parent, form,
            "Add Player / Staff Member", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String category = ((JTextField) catBox.getEditor().getEditorComponent()).getText().trim();
        String name     = nameField.getText().trim();
        String role     = roleField.getText().trim();
        String details  = detailsArea.getText().trim();

        if (category.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                "Category and Name are required.", "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String storedName = role.isEmpty() ? name : name + " \u2013 " + role;

        RosterEntry newEntry = new RosterEntry(category, storedName, details);

        executeAction(new RosterAction() {
            public void apply()  { currentRoster.add(newEntry); }
            public void undo()   { currentRoster.remove(newEntry); }
            public String getDescription() { return "Add \"" + name + "\""; }
        });

        JOptionPane.showMessageDialog(parent,
            "\"" + name + "\" has been added to the roster.",
            "Player Added", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showRemovePlayerDialog(JFrame parent) {
        if (currentRoster.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "The roster is empty.",
                "Remove Player", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build display names for the list
        String[] displayNames = currentRoster.stream()
            .map(e -> e.name + "  [" + e.category + "]")
            .toArray(String[]::new);

        JList<String> list = new JList<>(displayNames);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(480, 300));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_COLOR);
        panel.add(new JLabel("Select one or more entries to remove (Ctrl/Shift to multi-select):"),
            BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(parent, panel,
            "Remove Player / Staff Member", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        int[] selected = list.getSelectedIndices();
        if (selected.length == 0) {
            JOptionPane.showMessageDialog(parent, "No entries selected.",
                "Remove Player", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Collect entries to remove (in reverse index order to keep indices stable)
        List<RosterEntry> toRemove = new ArrayList<>();
        for (int idx : selected) toRemove.add(currentRoster.get(idx));

        // Confirm
        StringBuilder names = new StringBuilder();
        for (RosterEntry e : toRemove) names.append("  • ").append(e.name).append("\n");
        int confirm = JOptionPane.showConfirmDialog(parent,
            "Remove the following " + toRemove.size() + " entry(ies)?\n\n" + names,
            "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        List<Integer> originalIndices = new ArrayList<>();
        for (RosterEntry e : toRemove) originalIndices.add(currentRoster.indexOf(e));

        String desc = toRemove.size() == 1
            ? "Remove \"" + toRemove.get(0).name + "\""
            : "Remove " + toRemove.size() + " entries";

        executeAction(new RosterAction() {
            public void apply() { currentRoster.removeAll(toRemove); }
            public void undo()  {
                for (int i = 0; i < toRemove.size(); i++) {
                    int idx = Math.min(originalIndices.get(i), currentRoster.size());
                    currentRoster.add(idx, toRemove.get(i));
                }
            }
            public String getDescription() { return desc; }
        });

        JOptionPane.showMessageDialog(parent,
            toRemove.size() + " entry(ies) removed from the roster.",
            "Removed", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showAboutDialog(JFrame parent) {
        String message = "Baltimore Ravens Roster Application\n" +
                        "Version 4.0\n\n" +
                        "Complete roster and staff directory for the 2024-25 season.\n\n" +
                        "Features:\n" +
                        "• CSV-based data loading\n" +
                        "• Import roster from CSV or Excel (.xlsx/.xls)\n" +
                        "• Export current roster to CSV\n" +
                        "• Add and remove players / staff members\n" +
                        "• Undo and Redo for add/remove operations\n" +
                        "• Searchable player and staff database\n" +
                        "• Expandable detail cards\n" +
                        "• Position-based organization\n" +
                        "• Team colors and branding\n\n" +
                        "Go Ravens!";
        
        JOptionPane.showMessageDialog(parent, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showHelpDialog(JFrame parent) {
        String[] options = {"Search Help", "Navigation Help", "Import/Export Help", "Roster Editing Help", "Close"};
        int choice = JOptionPane.showOptionDialog(parent,
            "What do you need help with?",
            "Help Menu",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 0) {
            JOptionPane.showMessageDialog(parent,
                "SEARCH HELP\n\n" +
                "• Type in the search box to filter results\n" +
                "• Search by name, position, or role\n" +
                "• Examples: 'Lamar', 'QB', 'Coach', 'Medical'\n" +
                "• Clear the search box to show all entries",
                "Search Help",
                JOptionPane.INFORMATION_MESSAGE);
        } else if (choice == 1) {
            JOptionPane.showMessageDialog(parent,
                "NAVIGATION HELP\n\n" +
                "• Click any card to expand and view details\n" +
                "• Click again to collapse\n" +
                "• Double-click for quick info popup\n" +
                "• Use 'Expand All' to open all visible cards\n" +
                "• Use 'Collapse All' to close all cards\n" +
                "• Scroll to browse all sections",
                "Navigation Help",
                JOptionPane.INFORMATION_MESSAGE);
        } else if (choice == 2) {
            JOptionPane.showMessageDialog(parent,
                "IMPORT / EXPORT HELP\n\n" +
                "EXPORT CSV:\n" +
                "• Click 'Export CSV' to save the current roster to a .csv file.\n" +
                "• A file chooser will appear — pick a name and location.\n" +
                "• The exported file has three columns: Category, Name, Details.\n\n" +
                "IMPORT CSV:\n" +
                "• Click 'Import CSV/Excel' and select a .csv file.\n" +
                "• The file must have three columns in order: Category, Name, Details.\n" +
                "• Row 1 is treated as a header and skipped automatically.\n" +
                "• Use \\n (backslash-n) inside the Details field for line breaks.\n\n" +
                "IMPORT EXCEL (.xlsx / .xls):\n" +
                "• Same column layout as CSV (Category, Name, Details).\n" +
                "• Requires Apache POI JARs on the classpath.\n" +
                "• If POI is missing, the app will tell you exactly which JARs to add.",
                "Import/Export Help",
                JOptionPane.INFORMATION_MESSAGE);
        } else if (choice == 3) {
            JOptionPane.showMessageDialog(parent,
                "ROSTER EDITING HELP\n\n" +
                "ADD PLAYER (+ Add Player):\n" +
                "• Fill in Category (pick existing or type a new one), Name, Position/Role, and Details.\n" +
                "• Category and Name are required; Position/Role and Details are optional.\n" +
                "• The Position/Role appears as a subtitle on the card (e.g. QB, Head Coach).\n" +
                "• The new entry is added to the bottom of the roster.\n\n" +
                "REMOVE PLAYER (- Remove Player):\n" +
                "• A list of all current entries is shown.\n" +
                "• Click one entry, or hold Ctrl/Shift to select multiple.\n" +
                "• Confirm the removal in the follow-up dialog.\n\n" +
                "UNDO (<< Undo):\n" +
                "• Reverses the most recent add or remove operation.\n" +
                "• Grayed out when there is nothing to undo.\n" +
                "• Hover over the button to see what will be undone.\n\n" +
                "REDO (>> Redo):\n" +
                "• Re-applies an operation that was undone.\n" +
                "• The redo history is cleared whenever a new add or remove is performed.\n\n" +
                "Note: Undo/Redo history is cleared when you import a new roster file.",
                "Roster Editing Help",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Custom expandable card component
    static class ExpandableCard extends JPanel {

        private static final Color CARD_BG = Color.WHITE;
        private static final Color DETAIL_BG = new Color(248, 248, 250);
        private static final Color HOVER_COLOR = new Color(245, 243, 255);
        private static final Color RAVENS_GOLD = new Color(158, 124, 12);

        private JPanel detailPanel;
        private JLabel arrowLabel;
        private JPanel headerPanel;

        private boolean expanded = false;
        public String searchText;
        private JTextArea detailText;

        private Timer animationTimer;
        private int animationHeight = 0;
        private int targetHeight = 0;
        private int fullDetailHeight = 0;

        private static final int ANIMATION_SPEED = 20;

        public ExpandableCard(String name, String subtitle, String details, boolean isPlayer) {
            this.searchText = name + " " + subtitle + " " + details;

            setLayout(new BorderLayout());
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 235), 1),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
            ));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(CARD_BG);
            headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            headerPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBackground(CARD_BG);

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
            nameLabel.setForeground(RAVENS_BLACK);

            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            subtitleLabel.setForeground(new Color(100, 100, 120));
            subtitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

            leftPanel.add(nameLabel);
            if (!subtitle.isEmpty()) {
                leftPanel.add(subtitleLabel);
            }

            arrowLabel = new JLabel("▼");
            arrowLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            arrowLabel.setForeground(RAVENS_GOLD);

            headerPanel.add(leftPanel, BorderLayout.CENTER);
            headerPanel.add(arrowLabel, BorderLayout.EAST);

            detailPanel = new JPanel(new BorderLayout());
            detailPanel.setBackground(DETAIL_BG);
            detailPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 18, 18));

            detailText = new JTextArea(details);
            detailText.setEditable(false);
            detailText.setLineWrap(true);
            detailText.setWrapStyleWord(true);
            detailText.setBackground(DETAIL_BG);
            detailText.setFont(new Font("Arial", Font.PLAIN, 13));
            detailText.setForeground(new Color(60, 60, 70));

            detailPanel.add(detailText, BorderLayout.CENTER);

            detailPanel.setPreferredSize(new Dimension(0, 0));
            detailPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
            detailPanel.setMinimumSize(new Dimension(0, 0));

            headerPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!expanded) {
                        headerPanel.setBackground(HOVER_COLOR);
                        leftPanel.setBackground(HOVER_COLOR);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!expanded) {
                        headerPanel.setBackground(CARD_BG);
                        leftPanel.setBackground(CARD_BG);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    toggleExpand();
                }
            });

            add(headerPanel, BorderLayout.NORTH);
            add(detailPanel, BorderLayout.CENTER);
        }

    private void toggleExpand() {
        setExpanded(!expanded);
    }

    public void setExpanded(boolean expand) {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        if (expand && fullDetailHeight == 0) {
            int availableWidth = getWidth() - 36;
            detailText.setSize(new Dimension(availableWidth, Short.MAX_VALUE));
            fullDetailHeight = detailText.getPreferredSize().height + 33;
        }

        this.expanded = expand;
        arrowLabel.setText(expand ? "▲" : "▼");

        targetHeight = expand ? fullDetailHeight : 0;
        animationHeight = detailPanel.getHeight();

        animationTimer = new Timer(10, e -> {
            if (animationHeight < targetHeight) {
                animationHeight = Math.min(animationHeight + ANIMATION_SPEED, targetHeight);
            } else if (animationHeight > targetHeight) {
                animationHeight = Math.max(animationHeight - ANIMATION_SPEED, targetHeight);
            }

            detailPanel.setPreferredSize(new Dimension(0, animationHeight));
            detailPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, animationHeight));

            revalidate();
            repaint();

            if (animationHeight == targetHeight) {
                animationTimer.stop();
            }
        });

        animationTimer.start();
    }
}

}