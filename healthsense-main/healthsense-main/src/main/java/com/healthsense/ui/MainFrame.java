package com.healthsense.ui;

import com.healthsense.service.AuthService;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    private final AuthService authService = new AuthService();

    public MainFrame() {
        super("HEALTHSENSE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLayout(new BorderLayout());

        LoginPanel loginPanel = new LoginPanel(authService, this::onLoginSuccess, this::showWelcome);
        SignupPanel signupPanel = new SignupPanel(authService, this::onLoginSuccess, this::showWelcome);
        AppPanel appPanel = new AppPanel(authService, this::onSignOut);
        WelcomePanel welcomePanel = new WelcomePanel(this::showLogin, this::showSignup);

        container.add(welcomePanel, "welcome");
        container.add(loginPanel, "login");
        container.add(signupPanel, "signup");
        container.add(appPanel, "app");

        add(container, BorderLayout.CENTER);
        showWelcome();
    }

    private void onLoginSuccess() {
        ((AppPanel) ((JPanel) container).getComponent(3)).onActivated();
        showApp();
    }

    private void onSignOut() {
        showWelcome();
    }

    private void showLogin() {
        cardLayout.show(container, "login");
    }

    private void showSignup() {
        cardLayout.show(container, "signup");
    }

    private void showApp() {
        cardLayout.show(container, "app");
    }

    private void showWelcome() {
        cardLayout.show(container, "welcome");
    }
}


