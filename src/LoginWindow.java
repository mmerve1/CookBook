import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class LoginWindow extends JFrame implements ActionListener {

    private JButton addRecipe;
    private JPanel recipeCardPanel;
    private JScrollPane scrollPane;
    private JTextField filter, ingredientFilter,suggestFilter;
    private JComboBox<String> sorting;
    private JButton sortButton;
    private JButton suggestButton;
    private ArrayList<JPanel> allCards;

    public LoginWindow(boolean firstTime) {

        Font font3 = new Font("Consolas", Font.BOLD, 14);
        setTitle("Cook Recipes");
        setSize(800, 750);
        if (firstTime) {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        setResizable(false);
        setLocationRelativeTo(null);
        // Panel Background
        getContentPane().setBackground(new Color(227, 227, 203));
        setLayout(null);

        // Recipe cards area
        recipeCardPanel = new JPanel();
        recipeCardPanel.setLayout(new BoxLayout(recipeCardPanel, BoxLayout.Y_AXIS));

        // Scroll Pane for recipe cards
        scrollPane = new JScrollPane(recipeCardPanel);
        scrollPane.setBounds(100, 150, 600, 500);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane);

        // Button for adding a recipe
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setBounds(100, 670, 600, 40);
        buttonPanel.setBackground(getContentPane().getBackground());

        addRecipe = new JButton("Add Recipe");
        addRecipe.setPreferredSize(new Dimension(150, 40));
        addRecipe.setBackground(new Color(42, 157, 143));
        addRecipe.setFont(font3);
        addRecipe.setFocusable(false);
        addRecipe.addActionListener(this);

        buttonPanel.add(addRecipe);
        add(buttonPanel);

        // Search bar and sorting
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBounds(100, 50, 600, 100);
        add(searchPanel);

       // Filter for recipe name (First Part)
        filter = new JTextField(50);
        filter.setFont(new Font("Consolas", Font.PLAIN, 20));
        filter.setMaximumSize(new Dimension(589, 40));
        searchPanel.add(filter);

        // Ingredient filter, Find button, Sorting ComboBox, and Sort button
        JPanel topRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ingredientFilter = new JTextField(20);
        ingredientFilter.setFont(new Font("Consolas", Font.PLAIN, 20));
        topRowPanel.add(ingredientFilter);

        JButton findIngredientButton = new JButton("Find");
        topRowPanel.add(findIngredientButton);

        sorting = new JComboBox<>();
        sorting.addItem("Select ");
        sorting.addItem("Preparation Time (Decreasing)");
        sorting.addItem("Preparation Time (Increasing)");
        sorting.addItem("Total Cost (Descending)");
        sorting.addItem("Total Cost (Increasing)");
        topRowPanel.add(sorting);

        sortButton = new JButton("Sort");
        topRowPanel.add(sortButton);

        searchPanel.add(topRowPanel);

       // Suggest filter and suggest button
        JPanel suggestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        suggestFilter = new JTextField(40);
        suggestFilter.setFont(new Font("Consolas", Font.PLAIN, 20));
        suggestPanel.add(suggestFilter);

        suggestButton = new JButton("Suggest");
        suggestPanel.add(suggestButton);
        searchPanel.add(suggestPanel);


        // Filter listener
        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterCards();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterCards();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterCards();
            }
        });


        // Sort button action listener
        sortButton.addActionListener(e -> {
            String selectedSortOption = (String) sorting.getSelectedItem();

            String query = "SELECT r.recipeID, r.recipeName, r.preparationTime, "
                    + "(SELECT SUM(rel.materialQuantity * ing.unitPrice) "
                    + "FROM relations rel JOIN ingredients ing ON rel.ingredientID = ing.ingredientID "
                    + "WHERE rel.recipeID = r.recipeID) AS totalCost "
                    + "FROM recipes r";

            if (selectedSortOption == null || selectedSortOption.equals("Select")) {
                return;
            }

            switch (selectedSortOption) {
                case "Preparation Time (Decreasing)":
                    query += " ORDER BY r.preparationTime DESC";
                    break;
                case "Preparation Time (Increasing)":
                    query += " ORDER BY r.preparationTime ASC";
                    break;
                case "Total Cost (Descending)":
                    query += " ORDER BY totalCost DESC";
                    break;
                case "Total Cost (Increasing)":
                    query += " ORDER BY totalCost ASC";
                    break;
                default:
                    return;
            }

            recipeCardPanel.removeAll();
            allCards.clear();
            loadDataFromDatabase(query);
            recipeCardPanel.revalidate();
            recipeCardPanel.repaint();
        });

        //This Part when you write ingredient show a recipes
        findIngredientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ingredientInput = ingredientFilter.getText();
                String[] ingredientArray = ingredientInput.split(",");
                List<String> ingredientList = new ArrayList<>();

                for (String ingredient : ingredientArray) {
                    ingredientList.add(ingredient.trim());
                }

                suggestRecipes(ingredientList);
            }
        });

        //suggest malzeme miktar ve ismine göre
        suggestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filterText = suggestFilter.getText();
                if (filterText.isEmpty()) {
                    return;
                }


                String[] ingredientCriteria = filterText.split(";");
                List<IngredientRequirement> requirements = new ArrayList<>();

                for (String criterion : ingredientCriteria) {
                    String[] parts = criterion.trim().split(",");
                    if (parts.length == 2) {
                        try {
                            String ingredient = parts[0].trim();
                            double quantity = Double.parseDouble(parts[1].trim().replaceAll("[^0-9.]", ""));
                            requirements.add(new IngredientRequirement(ingredient, quantity));
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid quantity format: " + parts[1]);
                        }
                    }
                }

                StringBuilder missingIngredientsReport = new StringBuilder("Eksik Malzemeler:\n");

                for (JPanel recipeCard : allCards) {
                    String recipeName = ((JLabel) ((JPanel) recipeCard.getComponent(0)).getComponent(0)).getText().replace("Name: ", "");
                    boolean hasAllIngredients = true;
                    boolean hasSufficientQuantity = true;

                    StringBuilder missingIngredients = new StringBuilder(); // To store missing ingredients info

                    try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/cookBook", "postgres", "merve2302")) {
                        for (IngredientRequirement requirement : requirements) {
                            String query = "SELECT r.materialQuantity FROM ingredients i " +
                                    "JOIN relations r ON i.ingredientID = r.ingredientID " +
                                    "JOIN recipes rc ON r.recipeID = rc.recipeID " +
                                    "WHERE rc.recipeName = ? AND i.ingredientName = ?";

                            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                                stmt.setString(1, recipeName);
                                stmt.setString(2, requirement.name);
                                ResultSet rs = stmt.executeQuery();
                                if (rs.next()) {
                                    double availableQuantity = rs.getFloat("materialQuantity");
                                    if (availableQuantity < requirement.quantity) {
                                        hasSufficientQuantity = false; // Not enough quantity
                                        double missingQuantity = requirement.quantity - availableQuantity;
                                        missingIngredients.append(requirement.name)
                                                .append(" (Gerekli: ")
                                                .append(requirement.quantity)
                                                .append(", Mevcut: ")
                                                .append(availableQuantity)
                                                .append(")\n");
                                    }
                                } else {
                                    hasAllIngredients = false; // Ingredient missing
                                    // Eğer malzeme tarifte yoksa, bunu da ekle
                                    missingIngredients.append(requirement.name)
                                            .append(" (Tarifte yok, Gerekli: ")
                                            .append(requirement.quantity)
                                            .append(")\n");
                                }
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace(); // Print error details
                    }

                    // Update recipe card component colors and log missing ingredients
                    JLabel recipeLabel = (JLabel) ((JPanel) recipeCard.getComponent(0)).getComponent(0);
                    if (hasAllIngredients && hasSufficientQuantity) {
                        recipeLabel.setForeground(Color.GREEN); // All ingredients are sufficient
                    } else {
                        recipeLabel.setForeground(Color.RED); // Missing or insufficient ingredients
                        // If there are missing ingredients, add to report
                        if (missingIngredients.length() > 0) {
                            missingIngredientsReport.append("Tarif: ").append(recipeName).append("\n")
                                    .append(missingIngredients).append("\n");
                        }
                    }
                }

                // Show missing ingredients report
                if (missingIngredientsReport.length() > "Eksik Malzemeler:\n".length()) {
                    JOptionPane.showMessageDialog(LoginWindow.this, missingIngredientsReport.toString(),
                            "Eksik Malzeme Raporu", JOptionPane.INFORMATION_MESSAGE);
                }

                recipeCardPanel.revalidate();
                recipeCardPanel.repaint();
            }
        });

        // All Cards Lists
        allCards = new ArrayList<>();
        loadDataFromDatabase();
        setVisible(true);
    }

    // malzeme ismine göre olan tarifleri getiriyor

    public void suggestRecipes(List<String> userIngredients) {

        StringBuilder query = new StringBuilder();
        query.append("SELECT r.recipeID, r.recipeName, ")
                .append("SUM(CASE WHEN ir.ingredientName IN (");

        for (int i = 0; i < userIngredients.size(); i++) {
            query.append("'").append(userIngredients.get(i)).append("'");
            if (i < userIngredients.size() - 1) {
                query.append(", ");
            }
        }

        query.append(") THEN 1 ELSE 0 END) AS matched_ingredients, ")
                .append("COUNT(i.ingredientID) AS total_ingredients, ")
                .append("(SUM(CASE WHEN ir.ingredientName IN (")
                .append(String.join(", ", userIngredients.stream()
                        .map(ing -> "'" + ing + "'")
                        .toArray(String[]::new)))
                .append(") THEN 1 ELSE 0 END)::float / NULLIF(COUNT(i.ingredientID), 0) * 100) AS match_percentage ")
                .append("FROM recipes r ")
                .append("JOIN relations rel ON r.recipeID = rel.recipeID ")
                .append("JOIN ingredients i ON rel.ingredientID = i.ingredientID ")
                .append("LEFT JOIN ingredients ir ON ir.ingredientID = i.ingredientID ")
                .append("GROUP BY r.recipeID ")
                .append("ORDER BY match_percentage DESC;");

        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/cookBook", "postgres", "merve2302");
             PreparedStatement stmt = conn.prepareStatement(query.toString());
             ResultSet rs = stmt.executeQuery()) {

            recipeCardPanel.removeAll();
            allCards.clear();

            while (rs.next()) {
                int recipeID = rs.getInt("recipeID");
                String recipeName = rs.getString("recipeName");
                int matchedIngredients = rs.getInt("matched_ingredients");
                float matchPercentage = rs.getFloat("match_percentage");

                JPanel recipeCard = createRecipeCardWithDetails(recipeName, matchedIngredients, matchPercentage, recipeID);
                allCards.add(recipeCard);
                recipeCardPanel.add(recipeCard);
                recipeCardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            recipeCardPanel.revalidate();
            recipeCardPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createRecipeCardWithDetails(String recipeName, int matchedIngredients, float matchPercentage, int recipeId) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        card.setBackground(new Color(255, 255, 255));
        card.setPreferredSize(new Dimension(600, 150));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(255, 255, 255));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Name: " + recipeName);
        JLabel ingredientsLabel = new JLabel("Matched Ingredients: " + matchedIngredients);
        JLabel percentageLabel = new JLabel("Percentage: " + String.format("%.2f", matchPercentage) + "%");

        nameLabel.setFont(new Font("Serif", Font.BOLD, 16));
        ingredientsLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        percentageLabel.setFont(new Font("Serif", Font.PLAIN, 14));

        infoPanel.add(nameLabel);
        infoPanel.add(ingredientsLabel);
        infoPanel.add(percentageLabel);
        card.add(infoPanel, BorderLayout.CENTER);

        return card;
    }




    private void loadDataFromDatabase(String query) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String url = "jdbc:postgresql://localhost:5432/cookBook";
            String user = "postgres";
            String password = "merve2302";

            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String recipeName = resultSet.getString("recipeName");
                int preparationTime = resultSet.getInt("preparationTime");
                int recipeId = resultSet.getInt("recipeID");

                double price = calculatePrice(recipeId, connection);
                JPanel recipeCard = createRecipeCard(recipeName,preparationTime,  price,  recipeId);

                allCards.add(recipeCard);
                recipeCardPanel.add(recipeCard);
                recipeCardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadDataFromDatabase() {
        loadDataFromDatabase("SELECT recipeID, recipeName, preparationTime FROM recipes");
    }

    private double calculatePrice(int recipeId, Connection connection) throws SQLException {
        double totalPrice = 0.0;
        String queryIngredients = "SELECT * FROM relations r JOIN ingredients i ON r.ingredientID = i.ingredientID WHERE r.recipeID = " + recipeId;
        try (Statement ingredientStatement = connection.createStatement();
             ResultSet ingredientsResultSet = ingredientStatement.executeQuery(queryIngredients)) {

            while (ingredientsResultSet.next()) {
                double materialQuantity = ingredientsResultSet.getDouble("materialQuantity");
                double unitPrice = ingredientsResultSet.getDouble("unitPrice");
                totalPrice += materialQuantity * unitPrice;
            }
        }
        return totalPrice;
    }

    private JPanel createRecipeCard(String recipeName, int preparationTime, double price, int recipeId){
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        card.setBackground(new Color(255, 255, 255));
        card.setPreferredSize(new Dimension(600, 150));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(255, 255, 255));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Name: " + recipeName);
        JLabel preparationLabel = new JLabel("Preparation Time: " + preparationTime + " mins");
        JLabel priceLabel = new JLabel("Price: $" + price);

        nameLabel.setFont(new Font("Serif", Font.BOLD, 16));
        preparationLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        priceLabel.setFont(new Font("Serif", Font.PLAIN, 14));

        infoPanel.add(nameLabel);
        infoPanel.add(preparationLabel);
        infoPanel.add(priceLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");

        updateButton.setBackground(new Color(244, 162, 97));
        deleteButton.setBackground(new Color(231, 111, 81));

        updateButton.addActionListener(e -> updateRecipe(recipeId));
        deleteButton.addActionListener(e -> deleteRecipe(recipeId));

        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showIngredientsPanel(recipeId,recipeName);
            }
        });

        return card;
    }

    private void showIngredientsPanel(int recipeId, String recipeName) {
        JFrame ingredientsFrame = new JFrame("Ingredients and Instructions for Recipe: " + recipeName);
        ingredientsFrame.setSize(500, 300);
        ingredientsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ingredientsFrame.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel recipeNamePanel = new JPanel();
        recipeNamePanel.setLayout(new BorderLayout());
        recipeNamePanel.setBorder(BorderFactory.createTitledBorder("Recipe Name"));
        JLabel recipeNameLabel = new JLabel(recipeName, SwingConstants.CENTER);
        recipeNameLabel.setFont(new Font("Serif", Font.BOLD, 18));
        recipeNamePanel.add(recipeNameLabel, BorderLayout.CENTER);
        mainPanel.add(recipeNamePanel);

        // Ingredients Panel
        JPanel ingredientsPanel = new JPanel();
        ingredientsPanel.setLayout(new BoxLayout(ingredientsPanel, BoxLayout.Y_AXIS));
        ingredientsPanel.setBorder(BorderFactory.createTitledBorder("Ingredients"));

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String url = "jdbc:postgresql://localhost:5432/cookBook";
            String user = "postgres";
            String password = "merve2302";

            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            String queryIngredients = "SELECT i.ingredientName, r.materialQuantity, i.unitPrice FROM relations r " +
                    "JOIN ingredients i ON r.ingredientID = i.ingredientID WHERE r.recipeID = " + recipeId;

            resultSet = statement.executeQuery(queryIngredients);

            while (resultSet.next()) {
                String ingredientName = resultSet.getString("ingredientName");
                double quantity = resultSet.getDouble("materialQuantity");
                double unitPrice = resultSet.getDouble("unitPrice");

                JLabel ingredientLabel = new JLabel(ingredientName + ": " + quantity + " units, $" + unitPrice + " each");
                ingredientLabel.setFont(new Font("Serif", Font.PLAIN, 14));
                ingredientsPanel.add(ingredientLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        mainPanel.add(ingredientsPanel);

        // Instructions Panel
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BorderLayout());
        instructionsPanel.setBorder(BorderFactory.createTitledBorder("Instructions"));

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/cookBook", "postgres", "merve2302");
            statement = connection.createStatement();
            String queryInstructions = "SELECT instructions FROM recipes WHERE recipeID = " + recipeId;
            ResultSet resultSetInstructions = statement.executeQuery(queryInstructions);

            if (resultSetInstructions.next()) {
                String instructions = resultSetInstructions.getString("instructions");

                JTextArea instructionsArea = new JTextArea(instructions);
                instructionsArea.setLineWrap(true);
                instructionsArea.setWrapStyleWord(true);
                instructionsArea.setEditable(false);
                instructionsArea.setFont(new Font("Serif", Font.PLAIN, 14));

                JScrollPane instructionsScrollPane = new JScrollPane(instructionsArea);
                instructionsScrollPane.setPreferredSize(new Dimension(450, 100));

                instructionsPanel.add(instructionsScrollPane, BorderLayout.CENTER);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        mainPanel.add(instructionsPanel);

        ingredientsFrame.add(new JScrollPane(mainPanel));
        ingredientsFrame.setVisible(true);
    }


    private void filterCards() {
        String searchText = filter.getText().toLowerCase();
        recipeCardPanel.removeAll();

        for (JPanel card : allCards) {
            JLabel nameLabel = (JLabel) ((JPanel) card.getComponent(0)).getComponent(0);
            String recipeName = nameLabel.getText().toLowerCase();

            if (recipeName.contains(searchText)) {
                recipeCardPanel.add(card);
                recipeCardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        recipeCardPanel.revalidate();
        recipeCardPanel.repaint();
    }
    //ikililik olmasın diye de remove ve repaint kullanıldı!!
    private void updateRecipe(int recipeId) {
        new Update(recipeId ,() ->{
            recipeCardPanel.removeAll();
            loadDataFromDatabase();
            recipeCardPanel.revalidate();
            recipeCardPanel.repaint();
        }); // loadRecipes metodunu Runnable olarak geçir
    }

    private void deleteRecipe(int recipeId) {
        // Silme işlemi tamamlandıktan sonra veriyi güncelleme ve eski kartları kaldırma
        new Delete(recipeId, () -> {
            recipeCardPanel.removeAll();       // Eski kartları kaldırır
            loadDataFromDatabase("SELECT * FROM recipes");  // Yeni veriyi tekrar yükler
            recipeCardPanel.revalidate();       // Arayüzü yeniden düzenler
            recipeCardPanel.repaint();          // Görsel olarak günceller
        });
    }



    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addRecipe) {

            recipeCardPanel.removeAll();  // Eski tarif kartlarını kaldır

            // Yeni tarif ekleme penceresini aç
            Add addWindow = new Add(() -> {
                loadDataFromDatabase();  // Veritabanından yeni verileri yükle
                recipeCardPanel.revalidate();  // Arayüzü yeniden düzenle
                recipeCardPanel.repaint();      // Görsel olarak güncelle
            });

            addWindow.setLocationRelativeTo(null);
            addWindow.setVisible(true);
        }
    }



    public static void main(String[] args) {
        new LoginWindow(true);
    }
}