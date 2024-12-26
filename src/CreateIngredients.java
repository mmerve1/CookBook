import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateIngredients extends JFrame implements ActionListener {

    private JTextField ingredientidField, ingrediennameField, totalquantityField, materialunitField, unitpriceField;
    private JButton addButton, cancelButton;
    private Runnable onIngredientAddedCallback;



    public CreateIngredients(Runnable onIngredientAddedCallback) {
        this.onIngredientAddedCallback = onIngredientAddedCallback;

        setTitle("Add New Ingredient");
        setSize(400, 400);
        setLayout(null);

        JLabel idLabel = new JLabel("Ingredient ID:");
        idLabel.setBounds(50, 30, 100, 30);
        add(idLabel);

        ingredientidField = new JTextField();
        ingredientidField.setBounds(150, 30, 200, 30);
        add(ingredientidField);

        JLabel nameLabel = new JLabel("Ingredient Name:");
        nameLabel.setBounds(50, 80, 100, 30);
        add(nameLabel);

        ingrediennameField = new JTextField();
        ingrediennameField.setBounds(150, 80, 200, 30);
        add(ingrediennameField);

        JLabel totalquantityLabel = new JLabel("Total Quantity:");
        totalquantityLabel.setBounds(50, 130, 100, 30);
        add(totalquantityLabel);

        totalquantityField = new JTextField();
        totalquantityField.setBounds(150, 130, 200, 30);
        add(totalquantityField);

        JLabel materialUnitLabel = new JLabel("Material Unit:");
        materialUnitLabel.setBounds(50, 180, 100, 30);
        add(materialUnitLabel);

        materialunitField = new JTextField();
        materialunitField.setBounds(150, 180, 200, 30);
        add(materialunitField);

        JLabel unitPriceLabel = new JLabel("Unit Price:");
        unitPriceLabel.setBounds(50, 230, 100, 30);
        add(unitPriceLabel);

        unitpriceField = new JTextField();
        unitpriceField.setBounds(150, 230, 200, 30);
        add(unitpriceField);

        addButton = new JButton("Add");
        addButton.setBounds(80, 300, 100, 30);
        addButton.addActionListener(this);
        add(addButton);

        cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 300, 100, 30);
        cancelButton.addActionListener(e -> dispose()); // Close window on cancel
        add(cancelButton);

        setVisible(true);
    }

    private void addIngredientToDatabase(int ingredientId, String ingredientName, String totalQuantity, String materialUnit, float unitPrice) {
        String url = "jdbc:postgresql://localhost:5432/cookBook";
        String user = "postgres";
        String password = "merve2302";

        String query = "INSERT INTO ingredients (ingredientid, ingredientname, totalquantity, materialunit, unitprice) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, ingredientId);
            preparedStatement.setString(2, ingredientName);
            preparedStatement.setString(3, totalQuantity);
            preparedStatement.setString(4, materialUnit);
            preparedStatement.setFloat(5, unitPrice);

            preparedStatement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Ingredient added successfully!");
            onIngredientAddedCallback.run();
            dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding ingredient!");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            try {
                int ingredientId = Integer.parseInt(ingredientidField.getText());
                String ingredientName = ingrediennameField.getText();
                float totalQuantity = Float.parseFloat(totalquantityField.getText());
                String total =totalQuantity+" gram";
                String materialUnit = materialunitField.getText();
                float unitPrice = Float.parseFloat(unitpriceField.getText());


                if (ingredientName.isEmpty() || materialUnit.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields!");
                    return;
                }

                addIngredientToDatabase(ingredientId, ingredientName, total, materialUnit, unitPrice);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values!");
            }
        }
    }
}
