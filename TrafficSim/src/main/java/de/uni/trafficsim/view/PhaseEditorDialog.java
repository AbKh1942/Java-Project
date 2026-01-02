package de.uni.trafficsim.view;

import de.uni.trafficsim.controller.SumoController;
import de.uni.trafficsim.model.TrafficLight.TrafficLightPhase;
import org.eclipse.sumo.libtraci.TraCIPhase;
import org.eclipse.sumo.libtraci.TrafficLight;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PhaseEditorDialog extends JDialog {
    private final SumoController controller;
    private final String tlsId;
    private final int requiredLength;
    private final JPanel phasesContainer;

    public PhaseEditorDialog(Window owner, String tlsId, SumoController controller) {
        super(owner, "Edit Phases for " + tlsId, ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.tlsId = tlsId;

        // 1. Fetch Constraints
        // We do this immediately. If offline, it returns 0, meaning we accept first input as validity check.
        this.requiredLength = TrafficLight.getAllProgramLogics(tlsId).get(0).getPhases().get(0).getState().length();

        setSize(600, 450);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);

        // --- Header ---
        JPanel header = getJPanel(tlsId);
        add(header, BorderLayout.NORTH);

        // --- Phases Container (Scrollable Stack) ---
        phasesContainer = new JPanel();
        phasesContainer.setLayout(new BoxLayout(phasesContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(phasesContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);


        String currentPG = TrafficLight.getProgram(tlsId);
        List<TraCIPhase> phases = TrafficLight.getAllProgramLogics(tlsId).stream()
                .filter(program -> program.getProgramID().equals(currentPG))
                .toList()
                .get(0)
                .getPhases();

        for (TraCIPhase ph: phases) {
            addPhaseRow(ph.getState(), ph.getDuration());
        }

        // --- Footer (Buttons) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("+ Add Phase");
        JButton cancelBtn = new JButton("Cancel");
        JButton applyBtn = new JButton("Apply Program");

        addBtn.addActionListener(e -> addPhaseRow("", 10.0));
        cancelBtn.addActionListener(e -> dispose());
        applyBtn.addActionListener(e -> apply());

        // Left align 'Add' button
        JPanel footerContainer = new JPanel(new BorderLayout());
        JPanel leftFooter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftFooter.add(addBtn);

        footerContainer.add(leftFooter, BorderLayout.WEST);
        footerContainer.add(footer, BorderLayout.EAST);
        footer.add(cancelBtn);
        footer.add(applyBtn);

        add(footerContainer, BorderLayout.SOUTH);
    }

    private JPanel getJPanel(String tlsId) {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.setBackground(new Color(245, 245, 245));

        JLabel titleLbl = new JLabel("Traffic Light Logic Configuration");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));

        String reqText = (requiredLength > 0) ? String.valueOf(requiredLength) : "Auto-detect";
        JLabel infoLbl = new JLabel("<html><b>TLS ID:</b> " + tlsId + " &nbsp;|&nbsp; <b>Required State Length:</b> " + reqText + "</html>");

        header.add(titleLbl);
        header.add(infoLbl);
        return header;
    }

    private void addPhaseRow(String initialState, double initialDur) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lblState = new JLabel("State:");
        JTextField txtState = new JTextField(initialState, 20);
        txtState.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JLabel lblDur = new JLabel("Duration (s):");
        JSpinner spinDur = new JSpinner(new SpinnerNumberModel(initialDur, 1.0, 300.0, 1.0));

        JButton removeBtn = new JButton("X");
        removeBtn.setForeground(Color.RED);
        removeBtn.setMargin(new Insets(2, 5, 2, 5));
        removeBtn.addActionListener(e -> {
            phasesContainer.remove(row);
            phasesContainer.revalidate();
            phasesContainer.repaint();
        });

        // Validation tooltip
        if (requiredLength > 0) {
            txtState.setToolTipText("Must be exactly " + requiredLength + " characters.");
        }

        row.add(lblState);
        row.add(txtState);
        row.add(lblDur);
        row.add(spinDur);
        row.add(removeBtn);

        phasesContainer.add(row);
        phasesContainer.revalidate();
        phasesContainer.repaint();

        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane)phasesContainer.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void apply() {
        List<TrafficLightPhase> phases = new ArrayList<>();
        Component[] rows = phasesContainer.getComponents();

        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Program cannot be empty!");
            return;
        }

        for (int i = 0; i < rows.length; i++) {
            if (!(rows[i] instanceof JPanel)) continue;
            JPanel row = (JPanel) rows[i];

            JTextField txtState = (JTextField) row.getComponent(1); // Index 1 is state
            JSpinner spinDur = (JSpinner) row.getComponent(3);    // Index 3 is spinner

            String state = txtState.getText().trim();
            double dur = (Double) spinDur.getValue();

            if (state.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Row " + (i+1) + ": State cannot be empty.");
                return;
            }

            // Validation: Length Check
            if (state.length() != requiredLength) {
                JOptionPane.showMessageDialog(this,
                        "Row " + (i+1) + ": Invalid state length.\n" +
                                "Required: " + requiredLength + "\n" +
                                "Found: " + state.length() + " (" + state + ")");
                return;
            }

            phases.add(new TrafficLightPhase(state, dur));
        }

        controller.setCustomProgram(tlsId, phases);
        dispose();
    }
}
