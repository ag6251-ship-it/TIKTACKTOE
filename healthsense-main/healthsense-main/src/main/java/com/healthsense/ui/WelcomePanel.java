package com.healthsense.ui;

import java.awt.*;
import javax.swing.*;

public class WelcomePanel extends JPanel {
    public WelcomePanel(Runnable onLogin, Runnable onSignup) {
        setLayout(new GridBagLayout());

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(600, 400));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        int row = 0;
        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        JLabel title = new JLabel("Welcome to HEALTHSENSE", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 50f));
        card.add(title, c);

        row++; c.gridy = row;
        JLabel subtitle = new JLabel("Track your health metrics, food logs, and insights.", SwingConstants.CENTER);
        subtitle.setFont(subtitle.getFont().deriveFont(35f));
        card.add(subtitle, c);

        row++; c.gridy = row; c.gridwidth = 1;
        JButton login = new JButton("Log in");  
        JButton signup = new JButton("Sign up");
        Font buttonFont = login.getFont().deriveFont(35f);
        login.setFont(buttonFont);
        signup.setFont(buttonFont);
        c.gridx = 0; card.add(login, c);
        c.gridx = 1; card.add(signup, c);

        login.addActionListener(e -> onLogin.run());
        signup.addActionListener(e -> onSignup.run());

        add(card);
    }
}


