package it.trenical.server.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminMainGUI extends JFrame {

    public AdminMainGUI() {
        setTitle("Pannello Amministrativo TreniCal");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 1, 10, 10));

        JButton tratteBtn = new JButton("Gestione Tratte");
        JButton promoBtn = new JButton("Gestione Promozioni");

        tratteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AdminTratteGUI().setVisible(true);
            }
        });

        promoBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AdminPromozioniGUI().setVisible(true);
            }
        });

        add(tratteBtn);
        add(promoBtn);
    }
}