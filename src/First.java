import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class First extends JFrame implements ActionListener {
    private JButton nextButton;

    public First() {
        setTitle("Welcome");
        setSize(800, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(new Color(255, 243, 224));

        // Panel for images and subtitle
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(255, 243, 224));
        add(mainPanel, BorderLayout.CENTER); // Add main panel to the center

        // Title Label in the Center
        JLabel titleLabel = new JLabel("MERVE'S RECIPE BOOK", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 48));
        titleLabel.setForeground(new Color(139, 69, 19));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        // Add title label in the center of the main panel
        GridBagConstraints gbcTitle = new GridBagConstraints();
        gbcTitle.gridx = 0; // Column 0
        gbcTitle.gridy = 0; // Row 0
        gbcTitle.gridwidth = 2; // Span across both columns
        gbcTitle.anchor = GridBagConstraints.CENTER; // Center it
        mainPanel.add(titleLabel, gbcTitle);

        // Add left image (top left)
        JLabel leftImageLabel = new JLabel(new ImageIcon("food3.png"));
        leftImageLabel.setPreferredSize(new Dimension(350, 350)); // Left image size
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0; // Column 0
        gbc1.gridy = 1; // Row 1
        gbc1.anchor = GridBagConstraints.NORTHWEST; // Align to top left
        mainPanel.add(leftImageLabel, gbc1); // Add to left side

        // Add right image (bottom right)
        JLabel rightImageLabel = new JLabel(new ImageIcon("food2.png"));
        rightImageLabel.setPreferredSize(new Dimension(350, 350)); // Right image size
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1; // Column 1
        gbc2.gridy = 1; // Row 1
        gbc2.anchor = GridBagConstraints.SOUTHEAST; // Align to bottom right
        mainPanel.add(rightImageLabel, gbc2); // Add to right side

        // Subtitle Label in the Center
        JLabel subtitleLabel = new JLabel("Welcome to the Recipe Book!", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.ITALIC, 20));
        subtitleLabel.setForeground(new Color(102, 51, 0));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

        // Add subtitle label in the center
        GridBagConstraints gbcSubtitle = new GridBagConstraints();
        gbcSubtitle.gridx = 0; // Column 0
        gbcSubtitle.gridy = 2; // Row 2
        gbcSubtitle.gridwidth = 2; // Span across both columns
        gbcSubtitle.anchor = GridBagConstraints.CENTER; // Center it
        mainPanel.add(subtitleLabel, gbcSubtitle);

        // Next Button with customized style
        nextButton = new JButton("Next");
        nextButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        nextButton.setBackground(new Color(205, 133, 63));
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        nextButton.addActionListener(this);

        // Button Panel for padding
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 243, 224));
        buttonPanel.add(nextButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Center the window
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nextButton) {
            new LoginWindow(true); // Open LoginWindow with firstTime set to true
            setVisible(false); // Hide the splash screen instead of disposing
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(First::new);
    }
}
