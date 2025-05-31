package it.trenical.client.gui;

import it.trenical.client.command.CommandInvoker;
import it.trenical.client.command.VisualizzaPromozioniCommand;
import it.trenical.common.grpc.PromozioneDTO;
import it.trenical.common.grpc.RispostaDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class VisualizzaPromozioniGUI extends JFrame {

    private final MenuPrincipale menu;

    public VisualizzaPromozioniGUI(MenuPrincipale menuPrincipale) {
        this.menu = menuPrincipale;

        setTitle("Promozioni Attive");
        setSize(650, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel titolo = new JLabel("Tutte le promozioni disponibili", SwingConstants.CENTER);
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titolo.setForeground(new Color(30, 60, 130));
        add(titolo, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new Object[]{
                "Descrizione", "Sconto", "Destinatari"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabella = new JTable(model);
        tabella.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabella.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(tabella);
        add(scrollPane, BorderLayout.CENTER);

        JButton chiudi = new JButton("Chiudi");
        chiudi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chiudi.setBackground(new Color(178, 34, 34));
        chiudi.setForeground(Color.WHITE);
        chiudi.setFocusPainted(false);
        chiudi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chiudi.addActionListener(e -> {
            dispose();
            menu.setVisible(true);
        });
        add(chiudi, BorderLayout.SOUTH);

        caricaPromozioni(model);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                menu.setVisible(true);
            }
        });

        setVisible(true);
    }

    private void caricaPromozioni(DefaultTableModel model) {
        RispostaDTO risposta = CommandInvoker.getInstance()
                .esegui(new VisualizzaPromozioniCommand());

        model.setRowCount(0);

        if (risposta.getEsito() && risposta.getPromozioniCount() > 0) {
            for (int i = 0; i < risposta.getPromozioniCount(); i++) {
                PromozioneDTO promo = risposta.getPromozioni(i);
                String descrizione = promo.getDescrizione();
                String sconto = String.format("%.0f%%", promo.getSconto());
                String destinatari = promo.getSoloFedelta() ? "Solo FedeltàTreno" : "Tutti i Clienti";

                model.addRow(new Object[]{descrizione, sconto, destinatari});
            }
        } else {
            model.addRow(new Object[]{"Nessuna promozione attiva", "", ""});
        }
    }
}