package de.uni.trafficsim.view;

import de.uni.trafficsim.statistics.StatsSnapshot;
import de.uni.trafficsim.statistics.EdgeSnapshot;

import javax.swing.*;
import java.awt.*;

/**
 * Sidebar panel that displays live simulation statistics.
 * <p>
 * Shows aggregated metrics and exposes a callback for exporting CSV data.
 */
 public class DashboardPanel extends JPanel {
    // UI Elements
    private final JLabel totalVehiclesLabel;
    private final JLabel avgSpeedLabel;
    private final JLabel stoppedVehiclesLabel;
    private final JLabel co2Label;
    private final JLabel fuelConsumptionLabel;
    private final JLabel visibleVehiclesLabel;
    private final JLabel arrivedLabel;
    private final JLabel avgDensityLabel;
    private final JLabel avgOccupancyLabel;
    private Runnable onExportCsv; //Callback for Export Button

    /**
     * Constructor
     * Creates the statistics dashboard UI.
     * <p>
     * Initializes labels, layout, and the export button.
     */
    public DashboardPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(220, 0));
        setBackground(new Color(45, 45, 48)); // Dark grey background
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        add(createHeader());
        add(Box.createVerticalStrut(20)); // Spacer

        // Stats
        totalVehiclesLabel = createStatLabel("Vehicles: 0");
        avgSpeedLabel = createStatLabel("Avg Speed: 0.0 m/s");
        stoppedVehiclesLabel = createStatLabel("Stopped: 0");
        co2Label = createStatLabel("CO2: 0.0 kg");
        fuelConsumptionLabel = createStatLabel("Fuel Con.: 0.0 kg");
        visibleVehiclesLabel = createStatLabel("Visible: 0");
        arrivedLabel = createStatLabel("Arrived: 0");
        avgDensityLabel = createStatLabel("Avg Density: - veh/km");
        avgOccupancyLabel = createStatLabel("Avg Occup.:  - %");

        //adding labels for statistics
        add(totalVehiclesLabel);
        add(Box.createVerticalStrut(15));
        add(avgSpeedLabel);
        add(Box.createVerticalStrut(15));
        add(stoppedVehiclesLabel);
        add(Box.createVerticalStrut(15));
        add(co2Label);
        add(Box.createVerticalStrut(15));
        add(fuelConsumptionLabel);
        add(Box.createVerticalStrut(15));
        add(visibleVehiclesLabel);
        add(Box.createVerticalStrut(15));
        add(arrivedLabel);
        add(Box.createVerticalStrut(15));
        add(avgDensityLabel);
        add(Box.createVerticalStrut(15));
        add(avgOccupancyLabel);

        //Export CSV Button
        add(Box.createVerticalStrut(20));

        JButton exportBtn = new JButton("Export CSV");
        exportBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportBtn.addActionListener(e -> {
            if (onExportCsv != null) {
                onExportCsv.run();
            }
        });
        add(exportBtn);

        add(Box.createVerticalGlue()); // Push content to top
    }

    // method for creating a header
    private JLabel createHeader() {
        JLabel lbl = new JLabel("Statistics");
        lbl.setForeground(new Color(79, 195, 247)); // Light Blue
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // method for creating a stat label
    private JLabel createStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 14));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Updates all displayed statistics.
     *
     * @param snap current statistics snapshot
     * @param visibleVehicles number of vehicles currently visible in the view
     */
    public void updateStats(StatsSnapshot snap, int visibleVehicles) {
        totalVehiclesLabel.setText("Vehicles: " + snap.totalVehicles());
        avgSpeedLabel.setText(String.format("Avg %.1f", Double.valueOf(snap.globalAvgSpeedMs())));
        stoppedVehiclesLabel.setText("Stopped:  " + snap.stoppedVehicles());
        co2Label.setText(String.format("CO2:      %.1f kg", Double.valueOf(snap.totalCo2Kg())));
        fuelConsumptionLabel.setText(String.format("Fuel Con.: %.1f L", Double.valueOf(snap.totalFuelL())));
        arrivedLabel.setText("Arrived:  " + snap.arrivedVehiclesTotal());
        visibleVehiclesLabel.setText("Visible:  " + visibleVehicles);

        //values from Edge Snapshots
        //average density
        double avgDensity = snap.edges().values().stream()          //gets all EdgeSnapshot Objects from Map and turns it into datastream
                .mapToDouble(EdgeSnapshot::densityPerKm)            //takes density as double
                .filter(v -> v >= 0)
                .average()                                          //calculates average of all densities
                .orElse(0.0);                                 //if no valid values

        //average Occupancy
        double avgOccupancy = snap.edges().values().stream()
                .mapToDouble(EdgeSnapshot::occupancyPercent)
                .filter(v -> v >= 0)
                .average()
                .orElse(0.0);

        avgDensityLabel.setText(String.format("Avg Density: %.1f veh/km", Double.valueOf(avgDensity)));
        avgOccupancyLabel.setText(String.format("Avg Occup.:  %.1f %%", Double.valueOf(avgOccupancy)));
    }

    /**
     * Sets the callback invoked when the Export CSV button is pressed.
     *
     * @param onExportCsv callback to run on export
     */
    public void setOnExportCsv(Runnable onExportCsv) {
        this.onExportCsv = onExportCsv;
    }
}
