import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class App {
    // Team Colors
    private static final Color RAVENS_PURPLE = new Color(36, 23, 115);
    private static final Color RAVENS_GOLD = new Color(158, 124, 12);
    private static final Color RAVENS_BLACK = new Color(0, 0, 0);
    private static final Color BG_COLOR = new Color(250, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color DETAIL_BG = new Color(248, 248, 250);
    private static final Color HOVER_COLOR = new Color(245, 243, 255);
    
    // Data structure to hold roster information
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::createUI);
    }

    private static void createUI() {
        JFrame frame = new JFrame("Baltimore Ravens – Roster & Staff");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 900);

        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_COLOR);

        // Header
        JPanel header = createHeader(frame);
        mainContainer.add(header, BorderLayout.NORTH);

        // Content panel with cards
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Load roster data from CSV
        String csvPath = Paths.get("ravens_roster.csv").toAbsolutePath().toString();
        List<RosterEntry> roster = loadRosterFromCSV(csvPath);
        
        populateContent(contentPanel, roster);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        frame.add(mainContainer);
        frame.setVisible(true);
        
        // Show welcome dialog AFTER frame is visible
        SwingUtilities.invokeLater(() -> showWelcomeDialog(frame));
    }

    private static JPanel createHeader(JFrame parentFrame) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(RAVENS_PURPLE);
        header.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Title
        JLabel title = new JLabel("BALTIMORE RAVENS");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(RAVENS_GOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("2024-25 Roster & Coaching Staff");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));

        // Search and controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setBackground(RAVENS_PURPLE);
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        JButton expandAll = createControlButton("Expand All");
        expandAll.addActionListener(e -> toggleAll(true));

        JButton collapseAll = createControlButton("Collapse All");
        collapseAll.addActionListener(e -> toggleAll(false));

        JButton aboutBtn = createControlButton("About");
        aboutBtn.addActionListener(e -> showAboutDialog(parentFrame));

        JButton helpBtn = createControlButton("Help");
        helpBtn.addActionListener(e -> showHelpDialog(parentFrame));

        // Filter dropdown
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

        controls.add(new JLabel("Search: ") {{
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.PLAIN, 14));
        }});
        controls.add(searchField);
        controls.add(new JLabel("  Filter: ") {{
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.PLAIN, 14));
        }});
        controls.add(filterDropdown);
        controls.add(expandAll);
        controls.add(collapseAll);
        controls.add(aboutBtn);
        controls.add(helpBtn);

        header.add(title);
        header.add(subtitle);
        header.add(controls);

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
                btn.setBackground(RAVENS_GOLD.brighter());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(RAVENS_GOLD);
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
            contentPanel.removeAll();
            
            // Reload roster and repopulate with original structure
            String csvPath = Paths.get("ravens_roster.csv").toAbsolutePath().toString();
            List<RosterEntry> roster = loadRosterFromCSV(csvPath);
            populateContent(contentPanel, roster);
            
            contentPanel.revalidate();
            contentPanel.repaint();
            
            // Scroll to top
            SwingUtilities.invokeLater(() -> {
                contentPanel.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            });
            return;
        }
        
        // Separate cards into matching and non-matching
        List<ExpandableCard> matchingCards = new ArrayList<>();
        List<ExpandableCard> nonMatchingCards = new ArrayList<>();
        
        for (ExpandableCard card : allCards) {
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

    // Load roster from CSV file
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
                    continue; // Skip header
                }
                
                // Parse CSV line handling quoted fields
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
    
    // Parse a CSV line properly handling quoted fields
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

    // Fallback data if CSV not found
    private static List<RosterEntry> getDefaultRoster() {
        List<RosterEntry> roster = new ArrayList<>();
        roster.add(new RosterEntry("Player Roster - Quarterbacks", 
            "Lamar Jackson – QB", 
            "Stats:\n• NFL MVP: 2019, 2023\n• Career Passing Yards: 17,000+\n• Career Rushing Yards: 5,000+\n\nDescription:\nElite dual-threat quarterback and centerpiece of the Ravens offense."));
        roster.add(new RosterEntry("Player Roster - Quarterbacks", 
            "Cooper Rush – QB", 
            "Stats:\n• NFL Starts: 10+\n\nDescription:\nVeteran backup quarterback providing experience and stability."));
        roster.add(new RosterEntry("Player Roster - Running Backs", 
            "Derrick Henry – RB", 
            "Stats:\n• Career Rushing Yards: 9,000+\n• Career Rushing TDs: 90+\n\nDescription:\nPower back known for size, speed, and late-game dominance."));
        return roster;
    }

    private static void populateContent(JPanel panel, List<RosterEntry> roster) {
        // Group entries by category
        Map<String, List<RosterEntry>> categorizedRoster = new LinkedHashMap<>();
        for (RosterEntry entry : roster) {
            categorizedRoster.computeIfAbsent(entry.category, k -> new ArrayList<>()).add(entry);
        }

        // Track major sections for proper headers
        String lastMajorSection = "";
        
        // Add sections
        for (Map.Entry<String, List<RosterEntry>> categoryEntry : categorizedRoster.entrySet()) {
            String category = categoryEntry.getKey();
            
            // Determine major section (before the hyphen)
            String majorSection = category.contains(" - ") ? 
                category.substring(0, category.indexOf(" - ")) : category;
            
            // Add major section header if it's new
            if (!majorSection.equals(lastMajorSection)) {
                addSection(panel, majorSection.toUpperCase(), RAVENS_GOLD);
                lastMajorSection = majorSection;
            }
            
            // Add subsection if there's a subcategory
            if (category.contains(" - ")) {
                String subSection = category.substring(category.indexOf(" - ") + 3);
                addSubsection(panel, subSection);
            }
            
            // Add all entries in this category
            for (RosterEntry entry : categoryEntry.getValue()) {
                // Extract position/role from name if it exists
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

    private static void showAboutDialog(JFrame parent) {
        String message = "Baltimore Ravens Roster Application\n" +
                        "Version 2.0\n\n" +
                        "Complete roster and staff directory for the 2024-25 season.\n\n" +
                        "Features:\n" +
                        "• CSV-based data loading\n" +
                        "• Searchable player and staff database\n" +
                        "• Expandable detail cards\n" +
                        "• Position-based organization\n" +
                        "• Team colors and branding\n\n" +
                        "Go Ravens!";
        
        JOptionPane.showMessageDialog(parent, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showHelpDialog(JFrame parent) {
        String[] options = {"Search Help", "Navigation Help", "Close"};
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
        }
    }

    // Custom expandable card component
    static class ExpandableCard extends JPanel {
        private JPanel detailPanel;
        private JLabel arrowLabel;
        private JPanel headerPanel;
        private boolean expanded = false;
        private int width = 500;
        String searchText;

        public ExpandableCard(String name, String subtitle, String details, boolean isPlayer) {
            this.searchText = name + " " + subtitle + " " + details;
            
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
            ));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(width, 100));
            setPreferredSize(new Dimension(width, 70));

            // Header (clickable)
            headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(CARD_BG);
            headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            headerPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            headerPanel.setPreferredSize(new Dimension(width, 70));
            headerPanel.setMinimumSize(new Dimension(0, 70));

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

            // Detail panel
            detailPanel = new JPanel(new BorderLayout());
            detailPanel.setBackground(DETAIL_BG);
            detailPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 18, 18));
            detailPanel.setVisible(false);
            detailPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JTextArea detailText = new JTextArea(details);
            detailText.setEditable(false);
            detailText.setLineWrap(true);
            detailText.setWrapStyleWord(true);
            detailText.setBackground(DETAIL_BG);
            detailText.setFont(new Font("Arial", Font.PLAIN, 13));
            detailText.setForeground(new Color(60, 60, 70));

            detailPanel.add(detailText, BorderLayout.CENTER);

            // Click to expand/collapse
            headerPanel.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!expanded) headerPanel.setBackground(HOVER_COLOR);
                    leftPanel.setBackground(HOVER_COLOR);
                }
                public void mouseExited(MouseEvent e) {
                    if (!expanded) {
                        headerPanel.setBackground(CARD_BG);
                        leftPanel.setBackground(CARD_BG);
                    }
                }
                public void mouseClicked(MouseEvent e) {
                    toggleExpand();
                }
            });

            add(headerPanel);
            add(detailPanel);
        }

        private void toggleExpand() {
            setExpanded(!expanded);
        }

        public void setExpanded(boolean expand) {
            this.expanded = expand;
            detailPanel.setVisible(expand);
            arrowLabel.setText(expand ? "▲" : "▼");
            
            if (expand) {
                setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
                setPreferredSize(null);
            } else {
                setMaximumSize(new Dimension(width, 100));
                setPreferredSize(new Dimension(width, 70));
            }
            
            revalidate();
            repaint();
        }

        private void showQuickInfo(String name, String subtitle, String details) {
            String title = subtitle.isEmpty() ? name : name + " - " + subtitle;
            JOptionPane.showMessageDialog(null,
                details,
                title,
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}