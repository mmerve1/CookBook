import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Update extends JFrame implements ActionListener {
    private JTextField recipeNameField, prepTimeField, categoryField;
    private JTextArea instructionsArea;
    private int recipeId; // Store the recipe ID  
    private Runnable onUpdate; // Runnable for update callback

    public Update(int recipeId, Runnable onUpdate) {
        this.recipeId = recipeId; // Assign the recipe ID
        this.onUpdate = onUpdate; // Assign the Runnable
        initializeUI();
        loadRecipeData();
    }

    private void initializeUI() {
        setTitle("Update Recipe");
        setSize(600, 600);
        setLayout(null);

        JLabel nameLabel = new JLabel("Recipe Name:");
        nameLabel.setBounds(50, 30, 100, 30);
        add(nameLabel);

        recipeNameField = new JTextField();
        recipeNameField.setBounds(150, 30, 200, 30);
        add(recipeNameField);

        // Category Label and Field
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setBounds(50, 80, 100, 30);
        add(categoryLabel);

        categoryField = new JTextField();
        categoryField.setBounds(150, 80, 200, 30);
        add(categoryField);

        // Preparation Time Label and Field
        JLabel timeLabel = new JLabel("Preparation Time:");
        timeLabel.setBounds(50, 130, 100, 30);
        add(timeLabel);

        prepTimeField = new JTextField();
        prepTimeField.setBounds(150, 130, 200, 30);
        add(prepTimeField);

        // Instructions Label and Area
        JLabel instructionsLabel = new JLabel("Instructions:");
        instructionsLabel.setBounds(50, 180, 100, 30);
        add(instructionsLabel);

        instructionsArea = new JTextArea();
        instructionsArea.setBounds(150, 180, 200, 100);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        add(instructionsArea);

        JButton updateButton = new JButton("Update Recipe");
        updateButton.setBounds(150, 500, 150, 30);
        updateButton.addActionListener(this);
        add(updateButton);

        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void loadRecipeData() {
        String url = "jdbc:postgresql://localhost:5432/cookBook";
        String user = "postgres";
        String password= "merve2302";

        String query = "SELECT recipeName, category, preparationTime, instructions FROM recipes WHERE recipeID = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, recipeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                recipeNameField.setText(rs.getString("recipeName"));
                categoryField.setText(rs.getString("category"));
                prepTimeField.setText(Integer.toString(rs.getInt("preparationTime")));
                instructionsArea.setText(rs.getString("instructions"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading recipe data: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UpdateRecipes();
    }

    private void UpdateRecipes() {
        String url = "jdbc:postgresql://localhost:5432/cookBook";
        String user = "postgres";
        String password = "merve2302";

        String recipeQuery = "UPDATE recipes SET recipeName = ?, category = ?, preparationTime = ?, instructions = ? WHERE recipeID = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(recipeQuery)) {

            pstmt.setString(1, recipeNameField.getText());
            pstmt.setString(2, categoryField.getText());

            // Convert prep time to integer
            try {
                int prepTime = Integer.parseInt(prepTimeField.getText());
                pstmt.setInt(3, prepTime);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Preparation time must be a number!");
                return; // Exit the method if the input is invalid
            }

            pstmt.setString(4, instructionsArea.getText());
            pstmt.setInt(5, recipeId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Recipe updated successfully!");
                if (onUpdate != null) {
                    onUpdate.run(); // Call the runnable to update the main UI
                }
            } else {
                JOptionPane.showMessageDialog(this, "No recipe found with that ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating recipe: " + e.getMessage());
        }
    }
}
