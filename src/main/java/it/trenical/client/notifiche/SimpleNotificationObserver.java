package it.trenical.client.notifiche;

import it.trenical.common.grpc.PromozioneDTO;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SimpleNotificationObserver implements NotificationObserver {

    private final String email;
    private final Queue<String> codaNotifiche = new LinkedList<>();
    private boolean isShowing = false;
    private final Set<String> notificheMostrate = new HashSet<>();


    public SimpleNotificationObserver(String email) {
        this.email = email;
    }

    @Override
    public void aggiornaPromozione(PromozioneDTO promozione) {
        String messaggio = "NUOVA PROMOZIONE DISPONIBILE\n\n" +
                "Descrizione: " + promozione.getDescrizione() + "\n" +
                "Sconto: " + promozione.getSconto() + "%";

        aggiorna(messaggio); // riusa il sistema già in coda, evita duplicati e mostra popup custom
    }


    @Override
    public void aggiorna(String messaggio) {
        synchronized (codaNotifiche) {
            if (notificheMostrate.contains(messaggio)) return; // evita duplicati
            notificheMostrate.add(messaggio);
            codaNotifiche.add(messaggio);
            if (!isShowing) {
                mostraProssimaNotifica();
            }
        }
    }

    private void mostraProssimaNotifica() {
        synchronized (codaNotifiche) {
            String messaggio = codaNotifiche.poll();
            if (messaggio == null) {
                isShowing = false;
                return;
            }
            isShowing = true;

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Aggiornamento");
                frame.setSize(650, 450);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setAlwaysOnTop(true);

                JPanel content = new RoundedPanel();
                content.setLayout(new BorderLayout(15, 15));
                content.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
                content.setBackground(Color.decode("#fdfdfd"));

                JLabel title = new JLabel("Aggiornamento ricevuto");
                title.setFont(new Font("Segoe UI", Font.BOLD, 18));
                title.setForeground(new Color(30, 60, 130));
                title.setHorizontalAlignment(SwingConstants.CENTER);

                JTextPane body = new JTextPane();
                body.setContentType("text/html");
                body.setText(formattaHtml(messaggio));
                body.setEditable(false);
                body.setBackground(Color.decode("#fdfdfd"));
                body.setBorder(null);

                JScrollPane scroll = new JScrollPane(body);
                scroll.setBorder(null);
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

                JButton okButton = new JButton("OK");
                okButton.setFocusPainted(false);
                okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
                okButton.setForeground(Color.WHITE);
                okButton.setBackground(new Color(30, 130, 76));
                okButton.setPreferredSize(new Dimension(100, 35));
                okButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                okButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                okButton.addActionListener(e -> {
                    frame.dispose();
                    synchronized (codaNotifiche) {
                        isShowing = false;
                        mostraProssimaNotifica();
                    }
                });

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.setBackground(Color.decode("#fdfdfd"));
                buttonPanel.add(okButton);

                content.add(title, BorderLayout.NORTH);
                content.add(scroll, BorderLayout.CENTER);
                content.add(buttonPanel, BorderLayout.SOUTH);

                frame.setContentPane(content);
                frame.setVisible(true);
                frame.getRootPane().setDefaultButton(okButton);
            });
        }
    }

    private String formattaHtml(String testo) {
        String html = testo
                .replaceAll("(?i)binario", "<b>Binario</b>")
                .replaceAll("(?i)ritardo", "<b>Ritardo</b>")
                .replaceAll("(?i)cancellata", "<b>Cancellata</b>")
                .replaceAll("(?i)orario partenza", "<b>Orario Partenza</b>")
                .replaceAll("(?i)orario arrivo", "<b>Orario Arrivo</b>");
        return "<html><body style='font-family:Segoe UI; font-size:13px; text-align:justify; color:#333333;'>" + html.replace("\n", "<br>") + "</body></html>";
    }

    // Pannello con bordi stondati
    static class RoundedPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        }
    }
}