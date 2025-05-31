package it.trenical.client.gui;

import it.trenical.client.command.*;
import it.trenical.common.grpc.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class FiltraTratteGUI extends JFrame {

    private MenuPrincipale menuPrincipale;
    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField partenzaField = new JTextField(10);
    private JTextField arrivoField = new JTextField(10);
    private JTextField dataField = new JTextField(10);
    private JComboBox<String> tipoTrenoBox = new JComboBox<>(new String[]{"", "Alta Velocità", "Regionale"});
    private JComboBox<String> classeBox = new JComboBox<>(new String[]{"", "1", "2"});

    public FiltraTratteGUI(MenuPrincipale menu) {
        this.menuPrincipale = menu;
        setTitle("Filtra Tratte");
        setSize(950, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Filtra Tratte"));

        inputPanel.add(new JLabel("Partenza:"));
        inputPanel.add(new JLabel("Arrivo:"));
        inputPanel.add(new JLabel("Data (yyyy-mm-gg):"));
        inputPanel.add(new JLabel("Tipo Treno:"));
        inputPanel.add(new JLabel("Classe:"));

        inputPanel.add(partenzaField);
        inputPanel.add(arrivoField);
        inputPanel.add(dataField);
        inputPanel.add(tipoTrenoBox);
        inputPanel.add(classeBox);

        JButton filtraButton = new JButton("Filtra");
        filtraButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eseguiFiltro();
            }
        });

        tableModel = new DefaultTableModel(new Object[]{
                "ID Tratta", "Partenza", "Arrivo", "Data", "Orario Partenza", "Orario Arrivo", "Tipo", "Classe", "Posti", "Binario"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panelFiltra = new JPanel(new BorderLayout());
        panelFiltra.add(inputPanel, BorderLayout.NORTH);
        panelFiltra.add(filtraButton, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(panelFiltra, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (menuPrincipale != null) {
                    menuPrincipale.setVisible(true);
                }
            }
        });
    }

    private void eseguiFiltro() {
        String partenza = partenzaField.getText().trim();
        String arrivo = arrivoField.getText().trim();
        String data = dataField.getText().trim();

        if (partenza.isEmpty() || arrivo.isEmpty() || data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Partenza, Arrivo e Data sono obbligatori.");
            return;
        }

        String tipoTreno = (String) tipoTrenoBox.getSelectedItem();
        if (tipoTreno == null) tipoTreno = "";

        String classe = (String) classeBox.getSelectedItem();
        if (classe == null) classe = "";

        FiltraTratteCommand comando = new FiltraTratteCommand(partenza, arrivo, data, tipoTreno, classe);
        RispostaDTO risposta = CommandInvoker.getInstance().esegui(comando);

        tableModel.setRowCount(0);

        if (!risposta.getEsito()) {
            JOptionPane.showMessageDialog(this,
                    risposta.getMessaggio(),
                    "Nessuna Tratta Trovata",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (TrattaDTO t : risposta.getTratteList()) {
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getStazionePartenza(),
                    t.getStazioneArrivo(),
                    t.getData(),
                    t.getOrarioPartenza(),
                    t.getOrarioArrivo(),
                    t.getTipoTreno(),
                    t.getClasseServizio(),
                    t.getPostiDisponibili(),
                    t.getBinario()
            });
        }
    }
}