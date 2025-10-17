package com.healthsense.ui;

import com.healthsense.model.User;
import com.healthsense.service.AuthService;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class SignupPanel extends JPanel {
    private final AuthService authService;
    private final Runnable onSuccess;
    private final Runnable onBack;

    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public SignupPanel(AuthService authService, Runnable onSuccess, Runnable onBack) {
        this.authService = authService;
        this.onSuccess = onSuccess;
        this.onBack = onBack;
        setLayout(new GridBagLayout());

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(660, 500));

        // Increase text field sizes
        Font inputFont = nameField.getFont().deriveFont(30f);
        nameField.setFont(inputFont);
        nameField.setColumns(100);
        emailField.setFont(inputFont);
        emailField.setColumns(100);
        passwordField.setFont(inputFont);
        passwordField.setColumns(100);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel title = new JLabel("Create your account", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 50f));
        card.add(title, c);

        c.gridwidth = 1;
        c.gridy++; c.gridx = 0; card.add(new JLabel("Name"), c);
        c.gridx = 1; card.add(nameField, c);

        c.gridy++; c.gridx = 0; card.add(new JLabel("Email"), c);
        c.gridx = 1; card.add(emailField, c);

        c.gridy++; c.gridx = 0; card.add(new JLabel("Password"), c);
        c.gridx = 1; card.add(passwordField, c);

        c.gridy++; c.gridx = 0; c.gridwidth = 2;
        JButton submit = new JButton("Sign up");
        card.add(submit, c);

        c.gridy++; c.gridx = 0; c.gridwidth = 2;
        JButton back = new JButton("Back");
        card.add(back, c);

        submit.addActionListener(e -> doSignup());
        back.addActionListener(e -> onBack.run());

        add(card);
    }

    private void doSignup() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }
        try {
            User created = authService.signUp(name, email, password);
            if (created == null) {
                JOptionPane.showMessageDialog(this, "Email already registered");
                return;
            }
            onSuccess.run();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}


