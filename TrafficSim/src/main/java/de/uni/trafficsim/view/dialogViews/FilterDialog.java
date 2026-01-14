package de.uni.trafficsim.view.dialogViews;

import de.uni.trafficsim.controller.SumoController;
import de.uni.trafficsim.model.VehicleFilter;

import javax.swing.*;
import java.awt.*;

public class FilterDialog extends JDialog {
    private final SumoController controller;
    private final JCheckBox enableCheck;
    private final JSpinner minSpeedSpin;
    private final JSpinner maxSpeedSpin;
    private final JCheckBox colorFilterCheck;
    private final JButton colorBtn;
    private Color selectedColor = Color.YELLOW;
    private final JCheckBox stoppedCheck;

    public FilterDialog(Frame owner, SumoController controller) {
        super(owner, "Filter / Group Vehicles", false);
        this.controller = controller;
        setSize(320, 270);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        VehicleFilter current = controller.getFilter();

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        enableCheck = new JCheckBox("Enable Filter", current.enabled);

        // Speed
        JPanel speedPanel = new JPanel(new GridLayout(1, 2));
        minSpeedSpin = new JSpinner(new SpinnerNumberModel(current.minSpeed, 0.0, 100.0, 1.0));
        maxSpeedSpin = new JSpinner(new SpinnerNumberModel(current.maxSpeed, 0.0, 300.0, 1.0));
        speedPanel.add(new JLabel("Min:"));
        speedPanel.add(minSpeedSpin);
        speedPanel.add(new JLabel("Max:"));
        speedPanel.add(maxSpeedSpin);

        // Color Filter UI
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        colorFilterCheck = new JCheckBox("Filter by Color: ");
        colorFilterCheck.setSelected(current.filterColor != null);
        if (current.filterColor != null) selectedColor = current.filterColor;

        colorBtn = new JButton();
        colorBtn.setBackground(current.filterColor);
        setupColorButton();
        colorPanel.add(colorFilterCheck);
        colorPanel.add(colorBtn);

        // Stopped
        stoppedCheck = new JCheckBox("Show only Stopped", current.showStoppedOnly);

        setupForm(form, speedPanel, colorPanel);
    }

    private void setupColorButton() {
        colorBtn.setOpaque(true);
        colorBtn.setPreferredSize(new Dimension(20, 20));
        colorBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Select Filter Color", selectedColor);
            if (c != null) {
                selectedColor = c;
                colorBtn.setBackground(c);
                colorFilterCheck.setSelected(true);
            }
        });
    }

    private void setupForm(JPanel form, JPanel speedPanel, JPanel colorPanel) {
        form.add(enableCheck);
        form.add(new JSeparator());
        form.add(new JLabel("Speed Range (m/s):"));
        form.add(speedPanel);
        form.add(colorPanel);
        form.add(stoppedCheck);

        add(form, BorderLayout.CENTER);

        JButton applyBtn = new JButton("Apply Filter");
        applyBtn.addActionListener(e -> apply());
        add(applyBtn, BorderLayout.SOUTH);
    }

    private void apply() {
        VehicleFilter f = new VehicleFilter();
        f.enabled = enableCheck.isSelected();
        f.minSpeed = (Double) minSpeedSpin.getValue();
        f.maxSpeed = (Double) maxSpeedSpin.getValue();
        if (colorFilterCheck.isSelected()) {
            f.filterColor = selectedColor;
        } else {
            f.filterColor = null;
        }
        f.showStoppedOnly = stoppedCheck.isSelected();
        controller.setFilter(f);
        this.dispose();
    }
}
