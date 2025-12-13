package de.uni.trafficsim.view;

import de.uni.trafficsim.controller.SumoController;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.awt.Desktop;
import java.util.HashMap;
import java.util.Map;

public class MainFrame {

    private static MainFrame instance;

    private final JFrame frame;
    private final DashboardPanel dashboard;
    private final VisualizationPanel panel;
    private final JLabel timeLabel;
    private final SumoController controller;
    private final JToolBar toolbar;
    private final JButton startBtn;
    private final JButton pauseBtn;
    private final JButton stepBtn; // Added Step Button
    private final JButton stopBtn;
    private final JButton addVehicleBtn;
    private final JButton zoomInBtn;
    private final JButton zoomOutBtn;
    private final JButton stressTestBtn; // stress Test button
    private final JButton helpBtn; // help button


    public static MainFrame getInstance(String sumoConfig) {
        if (instance == null) {
            instance = new MainFrame(sumoConfig);
        }
        return instance;
    }

    private MainFrame(String sumoConfig) {
        frame = new JFrame("SUMO Custom Controller");
        panel = new VisualizationPanel();
        dashboard = new DashboardPanel();
        timeLabel = new JLabel("Time: 0.0 s");
        controller = new SumoController(sumoConfig, panel, dashboard, timeLabel);
        panel.setController(controller);
        toolbar = new JToolBar();
        startBtn = new JButton("Start SUMO");
        pauseBtn = new JButton("Pause");
        stepBtn = new JButton("Step");
        stopBtn = new JButton("Stop");
        addVehicleBtn = new JButton("Add Car");
        zoomInBtn = new JButton(" + ");
        zoomOutBtn = new JButton(" - ");
        stressTestBtn = new JButton("Stress Test"); //stress test button
        helpBtn = new JButton("Help");

    }

    public void run() {
        SwingUtilities.invokeLater(() -> {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);

            setupSimulationButtons();
            setupZoomButtons();
            setupTimeLabel();
            setupToolbar();

            frame.add(toolbar, BorderLayout.NORTH);
            frame.add(panel, BorderLayout.CENTER);
            frame.add(dashboard, BorderLayout.EAST);

            frame.setVisible(true);
        });
    }

    private void setupSimulationButtons() {
        // Initial states
        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        stepBtn.setEnabled(false);
        addVehicleBtn.setEnabled(false);
        stressTestBtn.setEnabled(false); //new

        startBtn.addActionListener(e -> {
            controller.start();
            startBtn.setEnabled(false);
            pauseBtn.setEnabled(true);
            stepBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            addVehicleBtn.setEnabled(true);
            stressTestBtn.setEnabled(true); //new
            pauseBtn.setText("Pause"); // Reset text
        });

        pauseBtn.addActionListener(e -> {
            boolean isPaused = controller.isPaused();
            boolean newState = !isPaused;
            controller.setPaused(!isPaused);
            pauseBtn.setText(isPaused ? "Pause" : "Resume");
            stepBtn.setEnabled(newState);

        });

        stopBtn.addActionListener(e -> {
            controller.stop();
            startBtn.setEnabled(true);
            pauseBtn.setEnabled(false);
            stepBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            addVehicleBtn.setEnabled(false);
            stressTestBtn.setEnabled(false);
            pauseBtn.setText("Pause");
            timeLabel.setText("Time: 0.0 s"); // Reset time
        });

        stepBtn.addActionListener(e -> {
            controller.stepOnce();
        });

        addVehicleBtn.addActionListener(e -> {
            System.out.println("Inject vehicle requested.");
        });

        stressTestBtn.addActionListener(e -> {
            System.out.println("Stress test clicked");
            controller.runStressTest();
        });

        helpBtn.addActionListener(e -> {
            openGuide();
        });
    }

    private void setupZoomButtons() {
        zoomInBtn.setFont(new Font("Arial", Font.BOLD, 16));
        zoomInBtn.addActionListener(e -> panel.zoomIn());
        zoomOutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        zoomOutBtn.addActionListener(e -> panel.zoomOut());
    }

    private void setupTimeLabel() {
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    }

    private void setupToolbar() {
        toolbar.setFloatable(false);
        toolbar.add(startBtn);
        toolbar.add(pauseBtn);
        toolbar.add(stepBtn);
        toolbar.add(stopBtn);
        toolbar.add(addVehicleBtn);
        toolbar.add(stressTestBtn); //stress Test button added to toolbar in SUMO

        toolbar.addSeparator(); // Separator for Time
        toolbar.add(timeLabel);

        toolbar.addSeparator();
        toolbar.add(new JLabel("Zoom: "));
        toolbar.add(zoomOutBtn);
        toolbar.add(zoomInBtn);

        toolbar.addSeparator();
        toolbar.add(helpBtn); // help button added to toolbar in SUMO
    }

    private void openGuide() {
        try {
            // load file from resources
            InputStream is = getClass().getClassLoader().getResourceAsStream("help.pdf"); // or help.txt

            if (is == null) {
                JOptionPane.showMessageDialog(frame,
                        "Help file not found in resources.",
                        "Help", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // copy to temp file so Desktop can open it
            File tempFile = File.createTempFile("help_", ".pdf"); // ".txt" if needed
            tempFile.deleteOnExit();

            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Desktop.getDesktop().open(tempFile);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Could not open help file:\n" + ex.getMessage(),
                    "Help", JOptionPane.ERROR_MESSAGE);
        }
    }
}