package it.trenical.client.gui;

import it.trenical.client.command.CommandInvoker;
import it.trenical.client.command.NotificaClientCommand;
import it.trenical.client.notifiche.SimpleNotificationObserver;
import it.trenical.common.grpc.*;
import it.trenical.server.notification.NotificationDispatcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MenuPrincipale extends JFrame {

    private ClienteDTO clienteAutenticato;
    private SimpleNotificationObserver observer;

    public MenuPrincipale() {
        chiediCredenziali();

        setTitle("TreniCal - Menu Principale");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel imgLabel = new JLabel(new ImageIcon("Immagine1.jpg"));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(imgLabel, BorderLayout.CENTER);

        JPanel testoPanel = new JPanel(new GridLayout(2, 1));
        JLabel titolo = new JLabel("TreniCal", SwingConstants.CENTER);
        titolo.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 48));
        titolo.setForeground(new Color(178, 34, 34));

        JLabel benvenuto = new JLabel("Benvenuto a bordo di TreniCal, " + clienteAutenticato.getNome() + "!", SwingConstants.CENTER);
        benvenuto.setFont(new Font("SansSerif", Font.PLAIN, 20));
        benvenuto.setForeground(Color.DARK_GRAY);

        testoPanel.add(titolo);
        testoPanel.add(benvenuto);
        testoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        topPanel.add(testoPanel, BorderLayout.SOUTH);

        topPanel.setPreferredSize(new Dimension(getWidth(), 300));
        add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        String[] comandi = {
                "Filtra Tratte", "Acquista Biglietto", "Modifica Biglietto",
                "Prenota Biglietto", "I miei Biglietti", "Promozioni", "Esci"
        };

        for (String cmd : comandi) {
            JButton btn = new JButton(cmd);
            btn.setFont(new Font("SansSerif", Font.BOLD, 18));
            btn.setBackground(new Color(178, 34, 34));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(178, 34, 34), 2, true));

            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(220, 20, 60));
                }

                public void mouseExited(MouseEvent e) {
                    btn.setBackground(new Color(178, 34, 34));
                }
            });

            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switch (cmd) {
                        case "Esci":
                            System.exit(0);
                            break;
                        case "Filtra Tratte":
                            apriFinestra(new FiltraTratteGUI(MenuPrincipale.this));
                            break;
                        case "Acquista Biglietto":
                            apriFinestra(new AcquistaBigliettoGUI(MenuPrincipale.this, clienteAutenticato));
                            break;
                        case "Modifica Biglietto":
                            ModificaBigliettoGUI.apri(MenuPrincipale.this, clienteAutenticato);
                            break;
                        case "Prenota Biglietto":
                            apriFinestra(new GestionePrenotazioniGUI(clienteAutenticato));
                            break;
                        case "I miei Biglietti":
                            apriFinestra(new BigliettiAcquistatiGUI(clienteAutenticato));
                            break;
                        case "Promozioni":
                            new VisualizzaPromozioniGUI(MenuPrincipale.this);
                            break;
                    }
                }
            });
            bottomPanel.add(btn);
        }

        add(bottomPanel, BorderLayout.CENTER);
        pack();

        javax.swing.Timer notificationTimer = new javax.swing.Timer(3000, e -> controllaNotifiche());
        notificationTimer.start();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void controllaNotifiche() {
        try {
            RichiestaDTO richiesta = RichiestaDTO.newBuilder()
                    .setTipo(TipoRichiesta.NOTIFICA_CLIENT)
                    .setCliente(clienteAutenticato)
                    .build();

            RispostaDTO risposta = CommandInvoker.getInstance().esegui(new ControllaNotificheCommand(richiesta));

            if (risposta.getEsito() && risposta.getNotificheCount() > 0) {
                for (int i = 0; i < risposta.getNotificheCount(); i++) {
                    observer.aggiorna(risposta.getNotifiche(i).getMessaggio());
                }
            }
        } catch (Exception ex) {
            System.err.println("Errore nelle notifiche: " + ex.getMessage());
        }
    }

    private static class ControllaNotificheCommand implements it.trenical.client.command.Command {
        private final RichiestaDTO richiesta;

        public ControllaNotificheCommand(RichiestaDTO richiesta) {
            this.richiesta = richiesta;
        }

        @Override
        public RichiestaDTO esegui() {
            return richiesta;
        }
    }

    private void apriFinestra(final JFrame finestra) {
        this.setVisible(false);
        finestra.setVisible(true);
        finestra.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                finestra.dispose();
                MenuPrincipale.this.setVisible(true);
            }
        });
    }

    private void chiediCredenziali() {
        JTextField nomeField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JComboBox<String> fedeltaBox = new JComboBox<>(new String[]{"No", "Sì"});
        JCheckBox promoBox = new JCheckBox("Ricevi notifiche promozioni");
        promoBox.setEnabled(false);

        fedeltaBox.addItemListener(e -> {
            boolean isFedelta = fedeltaBox.getSelectedItem().equals("Sì");
            promoBox.setEnabled(isFedelta);
            if (!isFedelta) promoBox.setSelected(false);
        });

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Nome e Cognome:"));
        panel.add(nomeField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Vuoi iscriverti a FedeltàTreno?"));
        panel.add(fedeltaBox);
        panel.add(new JLabel(""));
        panel.add(promoBox);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel, "Autenticazione", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String nome = nomeField.getText().trim();
                String email = emailField.getText().trim();
                boolean isFedelta = fedeltaBox.getSelectedItem().equals("Sì");
                boolean riceviPromo = promoBox.isSelected();

                if (nome.split("\\s+").length >= 2 && email.contains("@")) {
                    clienteAutenticato = ClienteDTO.newBuilder()
                            .setNome(nome)
                            .setEmail(email)
                            .setIsFedelta(isFedelta)
                            .setRiceviPromo(riceviPromo)
                            .build();

                    NotificaClientCommand notificaCmd = new NotificaClientCommand(clienteAutenticato);
                    CommandInvoker.getInstance().esegui(notificaCmd);

                    observer = new SimpleNotificationObserver(email);

                    NotificationDispatcher.getInstance().registra(email, observer);

                    if (isFedelta && riceviPromo) {
                        NotificationDispatcher.getInstance().registraPerPromozioni(email, observer);
                    }

                    break;
                } else {
                    JOptionPane.showMessageDialog(this, "Inserisci almeno nome e cognome validi e una email corretta.");
                }
            } else {
                System.exit(0);
            }
        }
    }
}