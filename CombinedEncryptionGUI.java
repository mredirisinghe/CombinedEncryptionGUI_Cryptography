import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class CombinedEncryptionGUI extends JFrame implements ActionListener {

    private JComboBox<String> encryptionMethodComboBox;
    private JTextField inputTextTextField;
    private JTextField caesarKeyTextField;
    private JTextArea outputTextArea;
    private JButton encryptButton;
    private JButton decryptButton;
    private SecretKey aesKey;
    private static final String AES_ALGORITHM = "AES";

    public CombinedEncryptionGUI() {
        // Set up the JFrame
        setTitle("Combined Encryption Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null); // Center the window

        // Create GUI components
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        JLabel methodLabel = new JLabel("Encryption Method:");
        String[] methods = {"Caesar Cipher", "AES"};
        encryptionMethodComboBox = new JComboBox<>(methods);
        JLabel inputLabel = new JLabel("Input Text:");
        inputTextTextField = new JTextField(20);
        JLabel caesarKeyLabel = new JLabel("Caesar Key:");
        caesarKeyTextField = new JTextField(5);
        caesarKeyTextField.setEnabled(false); // Disable initially for AES

        inputPanel.add(methodLabel);
        inputPanel.add(encryptionMethodComboBox);
        inputPanel.add(inputLabel);
        inputPanel.add(inputTextTextField);
        inputPanel.add(caesarKeyLabel);
        inputPanel.add(caesarKeyTextField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        encryptButton.addActionListener(this);
        decryptButton.addActionListener(this);
        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);

        outputTextArea = new JTextArea(10, 40);
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);

        // Set layout and add components
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(outputScrollPane, BorderLayout.SOUTH);

        // Generate AES key
        try {
            aesKey = generateAESKey();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating AES key: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            // Consider disabling AES if key generation fails
            encryptionMethodComboBox.removeItem("AES");
        }

        // Add action listener to combo box
        encryptionMethodComboBox.addActionListener(this);

        setVisible(true); // Make the JFrame visible
    }

    // Caesar Cipher methods
    public static String caesarEncrypt(String plaintext, int key) {
        StringBuilder ciphertext = new StringBuilder();
        for (char character : plaintext.toCharArray()) {
            if (Character.isLetter(character)) {
                char base = Character.isUpperCase(character) ? 'A' : 'a';
                ciphertext.append((char) (((character - base + key) % 26) + base));
            } else {
                ciphertext.append(character);
            }
        }
        return ciphertext.toString();
    }

    public static String caesarDecrypt(String ciphertext, int key) {
        return caesarEncrypt(ciphertext, 26 - (key % 26));
    }

    // AES methods
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    public String aesEncrypt(String plaintext, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] ciphertextBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(ciphertextBytes);
    }

    public String aesDecrypt(String ciphertext, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plaintextBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(plaintextBytes, StandardCharsets.UTF_8);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == encryptionMethodComboBox) {
            // Enable/disable Caesar key field based on selected method
            String selectedMethod = (String) encryptionMethodComboBox.getSelectedItem();
            caesarKeyTextField.setEnabled(selectedMethod.equals("Caesar Cipher"));
        } else if (event.getSource() == encryptButton || event.getSource() == decryptButton) {
            String selectedMethod = (String) encryptionMethodComboBox.getSelectedItem();
            String inputText = inputTextTextField.getText();
            outputTextArea.setText(""); // Clear previous output

            if (inputText.isEmpty()) {
                outputTextArea.append("Please enter text to process.\n");
                return;
            }

            try {
                if (selectedMethod.equals("Caesar Cipher")) {
                    String keyText = caesarKeyTextField.getText();
                    if (keyText.isEmpty()) {
                        outputTextArea.append("Please enter a Caesar Cipher key.\n");
                        return;
                    }
                    int caesarKey = Integer.parseInt(keyText);
                    if (event.getSource() == encryptButton) {
                        String encryptedText = caesarEncrypt(inputText, caesarKey);
                        outputTextArea.append("Encrypted Text (Caesar):\n" + encryptedText + "\n");
                    } else {
                        String decryptedText = caesarDecrypt(inputText, caesarKey);
                        outputTextArea.append("Decrypted Text (Caesar):\n" + decryptedText + "\n");
                    }
                } else if (selectedMethod.equals("AES")) {
                    if (event.getSource() == encryptButton) {
                        String encryptedText = aesEncrypt(inputText, aesKey);
                        outputTextArea.append("Encrypted Text (AES):\n" + encryptedText + "\n");
                        outputTextArea.append("AES Key (Base64):\n" + Base64.getEncoder().encodeToString(aesKey.getEncoded()) + "\n");
                    } else {
                        String decryptedText = aesDecrypt(inputText, aesKey);
                        outputTextArea.append("Decrypted Text (AES):\n" + decryptedText + "\n");
                    }
                }
            } catch (NumberFormatException e) {
                outputTextArea.append("Invalid Caesar key. Please enter an integer.\n");
            } catch (Exception e) {
                outputTextArea.append("Error during " + selectedMethod + " operation:\n" + e.getMessage() + "\n");
                e.printStackTrace(); // For debugging
            }
        }
    }

    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure GUI updates are done on the EDT
        SwingUtilities.invokeLater(() -> new CombinedEncryptionGUI());
    }
}

