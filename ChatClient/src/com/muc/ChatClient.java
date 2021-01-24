package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    KeyPair keyPair;
    PublicKey pubKey;
    PrivateKey privateKey;

    public ChatClient(String serverName, int serverPort) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.serverName = serverName;
        this.serverPort = serverPort;
        keyPair = buildKeyPair();
        pubKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, byte[] msgBody) {
                System.out.println("You got a message from " + fromLogin + " ===>" + msgBody);
            }
        });

        if (!client.connect()) {
            System.err.println("Connect failed.");
        } else {
            System.out.println("Connect successful");

            if ((client.login("Rose","rose")) || (client.login("Jim","jim")) || (client.login("Smith","smith"))) {
                System.out.println("Login successful");

            } else {
                System.err.println("Login failed");
            }

            //client.logoff();
        }
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line:" + response);

        if ("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg) throws Exception {
        RSA rsa= new RSA();
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];
        byte[] msg= msgBody.getBytes();
        byte[] encrypted = encrypt(pubKey, msgBody);

        for(MessageListener listener : messageListeners) {
            listener.onMessage(login, encrypted);
        }
    }

    /*public static int[] buildKeyPair(){
        int d=34;
        int G=45;
        int Q[]= {d*G[0],d*G[1],d};
        return Q;
    }

    public static byte[] encrypt(int[] publicKey, String message) throws Exception {
        int m=45;
        int k=78;
        int q=63;
        char[] msg= message.toCharArray();
        char[] cipher= new char[msg.length];
        for (int i=0; i<msg.length; i++)
        {
            cipher[i]= (char) (Math.pow(m, publicKey)%q);
        }
        String ciph= cipher.toString();
        byte[] cip= ciph.getBytes();
        return cip;
    }
    public static byte[] decrypt(int privateKey, byte [] encrypted) throws Exception {
        int q=63;
        String enc= encrypted.toString();
        char[] e= enc.toCharArray();
        char[] M= new char[e.length];
        for (int i=0; i<e.length; i++)
        {
            M[i]= (char) (Math.pow(e[i], privateKey)%q);
        }
        String plaintext= M.toString();
        byte[] pt= plaintext.getBytes();
        return pt;
    }*/

    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message.getBytes());
    }
    public static byte[] decrypt(PrivateKey privateKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(encrypted);
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public void signup(String login, String password) throws IOException {
        String cmd = "signup" + " "+ login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
        startMessageReader();
    }
}

