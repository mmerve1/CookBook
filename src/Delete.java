import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Delete extends JFrame implements ActionListener {

    private int recipeID;
    private Runnable onRecipeDeletedCallback;

    // Constructor
    public Delete(int recipeID, Runnable onRecipeDeletedCallback) {
        this.recipeID = recipeID;
        this.onRecipeDeletedCallback = onRecipeDeletedCallback;

        setTitle("Delete Recipe");
        setSize(300, 150);
        setLayout(new BorderLayout());

        JLabel confirmationLabel = new JLabel("Are you sure you want to delete this recipe?", SwingConstants.CENTER);
        confirmationLabel.setFont(new Font("Serif", Font.PLAIN, 14));
        add(confirmationLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton deleteButton = new JButton("Delete");
        JButton cancelButton = new JButton("Cancel");

        deleteButton.setBackground(new Color(255, 15, 15));
        cancelButton.setBackground(new Color(56, 125, 218));

        deleteButton.addActionListener(this);
        cancelButton.addActionListener(this);

        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Delete")) {
            deleteRecipe();
        } else if (e.getActionCommand().equals("Cancel")) {
            dispose();
        }
    }

    private void deleteRecipe() {
        String url = "jdbc:postgresql://localhost:5432/cookBook";
        String user = "postgres";
        String password = "merve2302";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {

            String deleteRelationsQuery = "DELETE FROM relations WHERE recipeID = ?";
            try (PreparedStatement deleteRelationsStmt = connection.prepareStatement(deleteRelationsQuery)) {
                deleteRelationsStmt.setInt(1, recipeID);
                deleteRelationsStmt.executeUpdate();  // Remove related entries
            }


            String deleteRecipeQuery = "DELETE FROM recipes WHERE recipeID = ?";
            try (PreparedStatement deleteRecipeStmt = connection.prepareStatement(deleteRecipeQuery)) {
                deleteRecipeStmt.setInt(1, recipeID);
                int rowsAffected = deleteRecipeStmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Recipe deleted successfully!");
                    if (onRecipeDeletedCallback != null) {
                        onRecipeDeletedCallback.run();
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Recipe not found!");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting recipe!");
        }
    }
}
