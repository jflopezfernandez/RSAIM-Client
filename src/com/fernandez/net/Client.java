package com.fernandez.net;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame{
    private JTextField MessageTextField;
    private JTextArea MessageHistory;

    private String ipAddress;
    private Socket serverSocket;
    private int socketNumber;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private String defaultIPAddress = "47.197.62.146";
    private int defaultSocket = 27015;


    public Client() {
        super("Client Application");
        initComponents();
    }

    private void initComponents() {
        ipAddress = defaultIPAddress;
        socketNumber = defaultSocket;

        MessageTextField = new JTextField();
        MessageTextField.setEditable(false);
        MessageTextField.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        sendMessage(event.getActionCommand());
                        MessageTextField.setText("");
                    }
                }
        );
        add(MessageTextField, BorderLayout.NORTH);

        MessageHistory = new JTextArea();
        add(new JScrollPane(MessageHistory), BorderLayout.CENTER);

        setSize(800, 600);
        setVisible(true);
    }
    public void run() {
        try {
            establishServerConnection();
            setUpStreams();
            activeConversation();
        } catch (EOFException ioException) {
            ioException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            shutdownClientConnection();
        }
    }

    private void shutdownClientConnection() {
        showMessage("Closing client connection...\n");
        enableUserTyping(false);

        try {
            outputStream.close();
            inputStream.close();
            serverSocket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void activeConversation() throws IOException {
        enableUserTyping(true);
        String message = "";
        do {
            try {
                message = (String) inputStream.readObject();
                showMessage("[SERVER]: " + message);
            } catch (ClassNotFoundException classNotFoundException) {
                showMessage("[ERROR]: " + classNotFoundException.getLocalizedMessage());
                classNotFoundException.printStackTrace();
            } catch (EOFException eofException) {
                showMessage("[Client]: Server terminated the connection...");
            }
        } while(!message.equalsIgnoreCase("EXIT"));
    }

    private void enableUserTyping(boolean b) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        MessageTextField.setEditable(b);
                    }
                }
        );
    }

    private void setUpStreams() throws IOException {
        outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
        outputStream.flush();

        inputStream = new ObjectInputStream(serverSocket.getInputStream());

        showMessage("Streams have been set up");
    }

    private void establishServerConnection() throws IOException {
        showMessage("Attempting to establish server connection");
        serverSocket = new Socket(InetAddress.getByName(ipAddress), socketNumber);
        showMessage("Connected to " + serverSocket.getInetAddress());
    }

    private void showMessage(final String message) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        MessageHistory.append(message + "\n");
                    }
                }
        );
    }

    private void sendMessage(final String message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
            showMessage("[CLIENT]: " + message + "\n");
        } catch (IOException ioException) {
            MessageHistory.append("[ERROR]: " + ioException.getLocalizedMessage());
            ioException.printStackTrace();
        }
    }
}
