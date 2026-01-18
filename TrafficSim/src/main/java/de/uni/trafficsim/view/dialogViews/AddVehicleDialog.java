package de.uni.trafficsim.view.dialogViews;

import de.uni.trafficsim.App;
import de.uni.trafficsim.controller.SumoController;
import de.uni.trafficsim.model.VehicleWrapper;
import org.eclipse.sumo.libtraci.TraCIColor;
import org.eclipse.sumo.libtraci.VehicleType;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * Dialog for injecting a new vehicle into the simulation.
 * <p>
 * Allows selecting a route and vehicle type, or creating a custom type,
 * and scheduling the injection through the controller.
 */
public class AddVehicleDialog extends JDialog {
    private final SumoController controller;
    private JComboBox<String> routeCombo;
    private JComboBox<String> typeCombo;
    private JPanel newTypePanel;
    private JTextField newTypeIdField;
    private JSpinner lengthSpinner;
    private JSpinner speedSpinner;
    private JButton colorButton;
    private Color selectedColor = Color.CYAN;

    /**
     * Constructor.
     * Creates the add-vehicle dialog.
     *
     * @param owner parent window for modality and positioning
     * @param controller controller used to schedule vehicle injection
     */
    public AddVehicleDialog(Frame owner, SumoController controller) {
        super(owner, "Inject New Vehicle", true);
        this.controller = controller;
        setSize(400, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- 1. Route Selector ---
        setupRouteSelector(form, gbc);

        // --- 2. Type Selector ---
        setupTypeSelector(form, gbc);

        // --- 3. Color Picker ---
        setupColorPicker(form, gbc);

        // --- 4. New Type Fields (Hidden by default) ---
        setupNewTypeFields(form, gbc);

        // Set up listeners
        setupListeners();

        // --- Footer Buttons ---
        setUpFooter();
    }

    // Build "Select Route" label + dropdown.
    private void setupRouteSelector(JPanel form, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Select Route:"), gbc);

        gbc.gridx = 1;
        routeCombo = new JComboBox<>(controller.getAvailableRoutes().toArray(new String[0]));
        form.add(routeCombo, gbc);
    }

    // Build "Vehicle Type" label + dropdown.
    private void setupTypeSelector(JPanel form, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Vehicle Type:"), gbc);

        gbc.gridx = 1;
        typeCombo = new JComboBox<>();
        List<String> types = controller.getAvailableTypes();
        for(String t : types) {
            typeCombo.addItem(t);
        }
        typeCombo.addItem("--- Create New Type ---");
        form.add(typeCombo, gbc);
    }

    // Add color chooser button and set initial color.
    private void setupColorPicker(JPanel form, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Vehicle Color:"), gbc);

        gbc.gridx = 1;
        colorButton = new JButton("Choose Color");
        colorButton.setBackground(selectedColor);
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false);

        form.add(colorButton, gbc);
    }

    // Build panel for custom type fields.
    private void setupNewTypeFields(JPanel form, GridBagConstraints gbc) {
        newTypePanel = new JPanel(new GridLayout(3, 2, 5, 5));
        newTypePanel.setBorder(BorderFactory.createTitledBorder("New Type Attributes"));

        newTypeIdField = new JTextField("CustomType");
        lengthSpinner = new JSpinner(new SpinnerNumberModel(4.5, 2.0, 20.0, 0.5));
        speedSpinner = new JSpinner(new SpinnerNumberModel(30.0, 5.0, 100.0, 5.0));

        newTypePanel.add(new JLabel("Type ID:"));
        newTypePanel.add(newTypeIdField);
        newTypePanel.add(new JLabel("Length (m):"));
        newTypePanel.add(lengthSpinner);
        newTypePanel.add(new JLabel("Max Speed (m/s):"));
        newTypePanel.add(speedSpinner);

        newTypePanel.setVisible(false);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        form.add(newTypePanel, gbc);

        add(form, BorderLayout.CENTER);
    }

    // Color picker and type dropdown logic
    private void setupListeners() {
        colorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Pick Color", selectedColor);
            if (c != null) {
                selectedColor = c;
                colorButton.setBackground(c);
            }
        });

        // --- Logic to Toggle New Type Panel ---
        typeCombo.addActionListener(e -> {
            boolean createNew = "--- Create New Type ---".equals(typeCombo.getSelectedItem());
            newTypePanel.setVisible(createNew);
            revalidate();
            repaint();
        });
    }

    // Add "add Vehicle" and "Cancel" Button (footer).
    private void setUpFooter() {
        JPanel footer = new JPanel();
        JButton addBtn = new JButton("Add Vehicle");
        JButton cancelBtn = new JButton("Cancel");

        addBtn.addActionListener(e -> injectVehicle());
        cancelBtn.addActionListener(e -> dispose());

        footer.add(cancelBtn);
        footer.add(addBtn);
        add(footer, BorderLayout.SOUTH);
    }

    // Validates input, builds parameters, and schedules the actual injection task.
    private void injectVehicle() {
        String routeId = (String) routeCombo.getSelectedItem();
        String typeSelection = (String) typeCombo.getSelectedItem();
        boolean isNewType = "--- Create New Type ---".equals(typeSelection);

        if (routeId == null) {
            JOptionPane.showMessageDialog(this, "No Route Selected!");
            return;
        }

        // Generate a random vehicle ID
        String vehId = "veh_" + System.currentTimeMillis();

        // Prepare Data for the Lambda
        final String finalTypeId = isNewType ? newTypeIdField.getText().trim() : typeSelection;
        final double length = (Double) lengthSpinner.getValue();
        final double speed = (Double) speedSpinner.getValue();
        final Color c = selectedColor;

        if (isNewType && finalTypeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Type ID.");
            return;
        }

        // Schedule the Injection Task
        controller.scheduleTask(() -> {
            addCarToSimulation(isNewType, finalTypeId, vehId, routeId, length, speed, c);
        });

        dispose();
    }

    // runs inside Simulation Thread, calls SUMO to add vehicle to simulation.
    private void addCarToSimulation(
            boolean isNewType,
            String finalTypeId,
            String vehId,
            String routeId,
            double length,
            double speed,
            Color c
    ) {
        try {
            if (isNewType) {
                // Try to copy from the first available type or default
                String baseType = controller.getAvailableTypes().isEmpty() ? "DEFAULT_VEHTYPE" : controller.getAvailableTypes().get(0);
                // Create new type by copying base
                VehicleType.copy(baseType, finalTypeId);

                VehicleType.setLength(finalTypeId, length);
                VehicleType.setMaxSpeed(finalTypeId, speed);
                VehicleType.setColor(finalTypeId, new TraCIColor(c.getRed(), c.getGreen(), c.getBlue(), 255));

                App.logger.info("Created new type: {}", finalTypeId);
            }

            VehicleWrapper vehicleWrapper = new VehicleWrapper(vehId, routeId, c);
            controller.getSimulationFrame().vehicleManager.addVehicleToSimulation(vehicleWrapper, finalTypeId);
        } catch (Exception ex) {
            App.logger.error("Failed to inject vehicle: {}", ex.getMessage());
        }
    }
}
