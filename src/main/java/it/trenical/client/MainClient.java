package it.trenical.client;

import it.trenical.client.gui.MenuPrincipale;

import javax.swing.*;

public class MainClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MenuPrincipale().setVisible(true);
            }
        });
    }
}