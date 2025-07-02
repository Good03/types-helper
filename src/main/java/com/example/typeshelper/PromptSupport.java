package com.example.typeshelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PromptSupport {
    public static void setPrompt(String promptText, JTextField textField) {
        setPrompt(promptText, textField, Color.GRAY);
    }

    public static void setPrompt(String promptText, JTextField textField, Color promptColor) {
        Font originalFont = textField.getFont();
        Color originalForeground = textField.getForeground();

        textField.setForeground(promptColor);
        textField.setText(promptText);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(promptText)) {
                    textField.setText("");
                    textField.setForeground(originalForeground);
                    textField.setFont(originalFont);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(promptColor);
                    textField.setText(promptText);
                }
            }
        });
    }
}
