package com.fernandez.net;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame {

	private JTextField MessageTextField;
	private JTextArea  MessageHistory;

	private String ipAddress;
	private Socket serverSocket;
	private int socketNumber;

	private String defaultIPAddress = "47.197.62.146";
	private int defaultSocket = 27015;

	private ObjectOutputStream outputStream;
	private ObjectInputStream  inputStream;

	public Client() {
		super("Client Application");
		initComponents();
	}

	private void initComponents() {
		ipAddress = defaultIPAddress;
		socketNumber = defaultSocket;

		// Message box configuration.
		MessageTextField = new JTextField();
		MessageTextField.setEditable(false);
		MessageTextField.addActionListener(
			new ActionListener() {
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
			setupStreams();
			activeConversation();
		} catch (EOFException eofException) {
			showMessage("[CLIENT]: Client terminated connection...\n");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			shutdownClientConnection();
		}
	}

	private void establishServerConnection() throws IOException {
		showMessage("[CLIENT]: Attempting to establish server connection...\n");
		serverSocket = new Socket(InetAddress.getByName(ipAddress), socketNumber);
		showMessage("[CLIENT]: Connected to: " + serverSocket.getInetAddress());
	}

	private void setupStreams() throws IOException {
		outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
		outputStream.flush();

		inputStream = new ObjectInputStream(serverSocket.getInputStream());
	}

	private void activeConversation() throws IOException, EOFException {
		String message = "";
		enableUserTyping(true);

		do {
			try {
				//message = (String) inputStream.readObject();
				Object receivedData = inputStream.readObject();
				System.out.println("Class: " + receivedData.getClass());
				showMessage("[SERVER]: " + message + "\n");
			} catch (EOFException eofException) {
				showMessage("[CLIENT]: Server terminated the connection.");
			} catch (ClassNotFoundException classNotFoundException) {
				showMessage("[ERROR]: " + classNotFoundException.getLocalizedMessage());
				classNotFoundException.printStackTrace();
			}
		} while (!message.equalsIgnoreCase("EXIT"));
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

	private void sendMessage(final String message) {
		try {
			outputStream.writeObject("[CLIENT]: " + message + "\n");
			outputStream.flush();
			showMessage("[CLIENT]: " + message + "\n");
		} catch (IOException ioException) {
			MessageHistory.append("[ERROR]: " + ioException.getLocalizedMessage());
			ioException.printStackTrace();
		}
	}

	private void showMessage(final String message) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					MessageHistory.append(String.format("%s\n", message));
				}
			}
		);
	}

	private void enableUserTyping(boolean enable) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					MessageTextField.setEditable(enable);
				}
			}
		);
	}
}
