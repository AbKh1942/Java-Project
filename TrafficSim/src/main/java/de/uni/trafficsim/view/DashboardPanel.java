package de.uni.trafficsim.view;

import javax.swing.*;
import java.awt.*;

/**
 A sidebar panel to display simulation statistics.
 **/
 public class DashboardPanel extends JPanel {
    private JLabel totalVehiclesLabel;
    private JLabel avgSpeedLabel;
    private JLabel stoppedVehiclesLabel;
    private JLabel co2Label;

    public DashboardPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(220, 0));
        setBackground(new Color(45, 45, 48)); // Dark grey background
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        add(createHeader("Statistics"));
        add(Box.createVerticalStrut(20)); // Spacer

        // Stats
        totalVehiclesLabel = createStatLabel("Vehicles: 0");
        avgSpeedLabel = createStatLabel("Avg Speed: 0.0 m/s");
        stoppedVehiclesLabel = createStatLabel("Stopped: 0");
        co2Label = createStatLabel("CO2: 0.0 mg/s");

        add(totalVehiclesLabel);
        add(Box.createVerticalStrut(15));
        add(avgSpeedLabel);
        add(Box.createVerticalStrut(15));
        add(stoppedVehiclesLabel);
        add(Box.createVerticalStrut(15));
        add(co2Label);

        add(Box.createVerticalGlue()); // Push content to top
    }

    private JLabel createHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(79, 195, 247)); // Light Blue
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel createStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 14));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    public void updateStats(int count, double speed, int stopped, double co2) {
        totalVehiclesLabel.setText("Vehicles: " + count);
        avgSpeedLabel.setText(String.format("Avg Speed: %.1f m/s", speed));
        stoppedVehiclesLabel.setText("Stopped:  " + stopped);
        co2Label.setText(String.format("CO2:      %.1f mg", co2));
    }
}
