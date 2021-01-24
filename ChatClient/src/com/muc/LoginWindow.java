package com.muc;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginWindow extends JFrame {
    private final ChatClient client;
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");
    JButton signupButton = new JButton("Signup");

    public LoginWindow() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        super("Login");

        this.client = new ChatClient("localhost", 8818);
        client.connect();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginField.setFont(new Font("Arial",Font.PLAIN,35));
        passwordField.setFont(new Font("Arial",Font.PLAIN,35));
        loginButton.setFont(new Font("Arial",Font.PLAIN,35));
        signupButton.setFont(new Font("Arial",Font.PLAIN,35));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(loginButton);
        p.add(signupButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });

        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    doSignup();
                } catch (NoSuchAlgorithmException | NoSuchPaddingException noSuchAlgorithmException) {
                    noSuchAlgorithmException.printStackTrace();
                }
            }
        });

        getContentPane().add(p, BorderLayout.CENTER);

        pack();

        setVisible(true);
    }

    private void doSignup() throws NoSuchAlgorithmException, NoSuchPaddingException {
        setVisible(false);
        this.dispose();
        SignupWindow signupWindow = new SignupWindow();
        JFrame frame = new JFrame("Signup");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 1000);

        frame.getContentPane().add(signupWindow, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void doLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        try {
            if (client.login(login, password)) {
                // bring up the user list window
                UserListPane userListPane = new UserListPane(client);
                JFrame frame = new JFrame("  "+login + "- User List");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(600, 1000);

                frame.getContentPane().add(userListPane, BorderLayout.CENTER);
                frame.setVisible(true);

                setVisible(false);
            } else {
                // show error message
                JOptionPane.showMessageDialog(this, "Invalid login/password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
        LoginWindow loginWin = new LoginWindow();
        loginWin.setVisible(true);
    }
}
