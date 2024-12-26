import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class Add extends JFrame implements ActionListener {

    private JTextField recipeNameField, prepTimeField, categoryField;
    private JTextArea instructionsArea;
    private JButton addButton, cancelButton, addIngredientButton, createIngredient;
    private JComboBox<String> ingredientComboBox;
    private JTextField quantityField;
    private DefaultListModel<String> ingredientListModel;
    private JList<String> ingredientList;
    private ArrayList<IngredientQuantity> selectedIngredients;
    private Runnable onRecipeAddedCallback;

    public Add(Runnable onRecipeAddedCallback) {
        this.onRecipeAddedCallback = onRecipeAddedCallback;
        setTitle("Add New Recipe");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        selectedIngredients = new ArrayList<>();

        // Bileşenleri oluştur
        JLabel nameLabel = new JLabel("Recipe Name:");
        recipeNameField = new JTextField();

        JLabel categoryLabel = new JLabel("Category:");
        categoryField = new JTextField();

        JLabel timeLabel = new JLabel("Preparation Time:");
        prepTimeField = new JTextField();

        JLabel instructionsLabel = new JLabel("Instructions:");
        instructionsArea = new JTextArea(5, 20);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        JScrollPane instructionsScrollPane = new JScrollPane(instructionsArea);

        JLabel ingredientLabel = new JLabel("Ingredient:");
        ingredientComboBox = new JComboBox<>(getIngredientsFromDatabase());

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityField = new JTextField();

        addIngredientButton = new JButton("Add Ingredient");
        addIngredientButton.setBackground(new Color(59, 89, 182));
        addIngredientButton.addActionListener(e -> addIngredient());

        ingredientListModel = new DefaultListModel<>();
        ingredientList = new JList<>(ingredientListModel);
        JScrollPane ingredientScrollPane = new JScrollPane(ingredientList);

        addButton = new JButton("Add Recipe");
        addButton.setBackground(new Color(46, 204, 113)); // Green button
        addButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(231, 76, 60)); // Red button
        cancelButton.addActionListener(e -> dispose());

        createIngredient = new JButton("Create Ingredient");
        createIngredient.setBackground(new Color(52, 152, 219));
        createIngredient.addActionListener(this);

        // Layout oluştur (GroupLayout kullanımı)
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(nameLabel)
                                        .addComponent(categoryLabel)
                                        .addComponent(timeLabel)
                                        .addComponent(instructionsLabel)
                                        .addComponent(ingredientLabel)
                                        .addComponent(quantityLabel))
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(recipeNameField)
                                        .addComponent(categoryField)
                                        .addComponent(prepTimeField)
                                        .addComponent(instructionsScrollPane)
                                        .addComponent(ingredientComboBox)
                                        .addComponent(quantityField)))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(addIngredientButton)
                                .addComponent(createIngredient))
                        .addComponent(ingredientScrollPane)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(addButton)
                                .addComponent(cancelButton))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(nameLabel)
                                .addComponent(recipeNameField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(categoryLabel)
                                .addComponent(categoryField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(timeLabel)
                                .addComponent(prepTimeField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(instructionsLabel)
                                .addComponent(instructionsScrollPane))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(ingredientLabel)
                                .addComponent(ingredientComboBox))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(quantityLabel)
                                .addComponent(quantityField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(addIngredientButton)
                                .addComponent(createIngredient))
                        .addComponent(ingredientScrollPane)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(addButton)
                                .addComponent(cancelButton))
        );

        setVisible(true);
    }

    private String[] getIngredientsFromDatabase() {
        ArrayList<String> ingredients = new ArrayList<>();
        String url = "jdbc:postgresql://localhost:5432/cookBook";
        String user = "postgres";
        String password = "merve2302";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT ingredientName FROM ingredients")) {

            while (resultSet.next()) {
                ingredients.add(resultSet.getString("ingredientName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients.toArray(new String[0]);
    }

    private void addIngredient() {
        String ingredient = (String) ingredientComboBox.getSelectedItem();
        String quantity = quantityField.getText();
        if (ingredient != null && !quantity.isEmpty()) {
            ingredientListModel.addElement(ingredient + " - " + quantity);
            selectedIngredients.add(new IngredientQuantity(ingredient, Float.parseFloat(quantity)));
        } else {
            JOptionPane.showMessageDialog(this, "Please select an ingredient and enter a quantity.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            String recipeName = recipeNameField.getText();
            String category = categoryField.getText();
            String prepTimeStr = prepTimeField.getText();
            String instructions = instructionsArea.getText();

            if (recipeName.isEmpty() || category.isEmpty() || prepTimeStr.isEmpty() || instructions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!");
                return;
            }

            try {
                int prepTime = Integer.parseInt(prepTimeStr);
                addRecipeToDatabase(recipeName, category, prepTime, instructions);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Preparation time must be a number!");
            }
        }

        if (e.getSource() == createIngredient) {
            new CreateIngredients(onRecipeAddedCallback);
        }
    }

    private void addRecipeToDatabase(String name, String category, int prepTime, String instructions) {
        String url = "jdbc:postgresql://localhost:5432/cookBook";
        String user = "postgres";
        String password = "merve2302";

        String recipeQuery = "INSERT INTO recipes (recipeName, category, preparationTime, instructions) VALUES (?, ?, ?, ?)";
        String ingredientIDQuery = "SELECT ingredientID FROM ingredients WHERE ingredientName = ?";
        String relationQuery = "INSERT INTO relations (recipeID, ingredientID, materialQuantity) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (PreparedStatement recipeStmt = connection.prepareStatement(recipeQuery, Statement.RETURN_GENERATED_KEYS)) {
                recipeStmt.setString(1, name);
                recipeStmt.setString(2, category);
                recipeStmt.setInt(3, prepTime);
                recipeStmt.setString(4, instructions);
                recipeStmt.executeUpdate();

                ResultSet keys = recipeStmt.getGeneratedKeys();
                if (keys.next()) {
                    int recipeId = keys.getInt(1);

                    for (IngredientQuantity ingredientQuantity : selectedIngredients) {
                        try (PreparedStatement ingredientStmt = connection.prepareStatement(ingredientIDQuery)) {
                            ingredientStmt.setString(1, ingredientQuantity.ingredientName);
                            ResultSet ingredientResult = ingredientStmt.executeQuery();
                            if (ingredientResult.next()) {
                                int ingredientId = ingredientResult.getInt("ingredientID");

                                try (PreparedStatement relationStmt = connection.prepareStatement(relationQuery)) {
                                    relationStmt.setInt(1, recipeId);
                                    relationStmt.setInt(2, ingredientId);
                                    relationStmt.setFloat(3, ingredientQuantity.quantity);
                                    relationStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }

            }

            JOptionPane.showMessageDialog(this, "Recipe added successfully!");
            if (onRecipeAddedCallback != null) {
                onRecipeAddedCallback.run();
            }
            dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding recipe!");
        }
    }

    private static class IngredientQuantity {
        String ingredientName;
        float quantity;

        IngredientQuantity(String ingredientName, float quantity) {
            this.ingredientName = ingredientName;
            this.quantity = quantity;
        }
    }
}