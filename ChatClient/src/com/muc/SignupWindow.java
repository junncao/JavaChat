package com.muc;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class SignupWindow extends JFrame {
    private final ChatClient client;
    JTextField usernameField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JPasswordField repasswordField = new JPasswordField();
    JButton signupButton = new JButton("Login");

    public SignupWindow() throws NoSuchPaddingException, NoSuchAlgorithmException {
        super("Signup");

        this.client = new ChatClient("localhost", 8818);
        client.connect();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 35));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 35));
        repasswordField.setFont(new Font("Arial", Font.PLAIN, 35));
        signupButton.setFont(new Font("Arial", Font.PLAIN, 35));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(usernameField);
        p.add(passwordField);
        p.add(repasswordField);
        p.add(signupButton);

        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    doSignup();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        getContentPane().add(p, BorderLayout.CENTER);

        pack();

        setVisible(true);
    }

    private void doSignup() throws IOException {
        String login = usernameField.getText();
        String password = passwordField.getText();
        String repassword = repasswordField.getText();

        if (repassword.equals(password)) {
            // bring up the user list window
            client.signup(login,password);
            UserListPane userListPane = new UserListPane(client);
            JFrame frame = new JFrame("  "+ login + "- User List");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 1000);

            frame.getContentPane().add(userListPane, BorderLayout.CENTER);
            frame.setVisible(true);

            setVisible(false);
        } else {
            // show error message
            JOptionPane.showMessageDialog(this, "Invalid password entry.");
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException {
        SignupWindow signupWin = new SignupWindow();
        signupWin.setVisible(true);
    }
}


