package de.uni.trafficsim.view;

import de.uni.trafficsim.controller.SumoController;

import javax.swing.*;
import java.awt.*;

public class MainFrame {

    private static MainFrame instance;

    private final JFrame frame;
    private final VisualizationPanel panel;
    private final SumoController controller;
    private final JToolBar toolbar;
    private final JButton startBtn;
    private final JButton pauseBtn;
    private final JButton stopBtn;
    private final JButton zoomInBtn;
    private final JButton zoomOutBtn;

    public static MainFrame getInstance(String sumoConfig) {
        if (instance == null) {
            instance = new MainFrame(sumoConfig);
        }
        return instance;
    }

    private MainFrame(String sumoConfig) {
        frame = new JFrame("SUMO Custom Controller");
        panel = new VisualizationPanel();
        controller = new SumoController(sumoConfig, panel);
        toolbar = new JToolBar();
        startBtn = new JButton("Start SUMO");
        pauseBtn = new JButton("Pause");
        stopBtn = new JButton("Stop");
        zoomInBtn = new JButton(" + ");
        zoomOutBtn = new JButton(" - ");
    }

    public void run() {
        SwingUtilities.invokeLater(() -> {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            setupSimulationButtons();
            setupZoomButtons();
            setupToolbar();

            frame.add(toolbar, BorderLayout.NORTH);
            frame.add(panel, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }

    private void setupSimulationButtons() {
        // Initial states
        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> {
            controller.start();
            startBtn.setEnabled(false);
            pauseBtn.setEnabled(true);
            stopBtn.setEnabled(true);
            pauseBtn.setText("Pause"); // Reset text
        });

        pauseBtn.addActionListener(e -> {
            boolean isPaused = controller.isPaused();
            controller.setPaused(!isPaused);
            pauseBtn.setText(isPaused ? "Pause" : "Resume");
        });

        stopBtn.addActionListener(e -> {
            controller.stop();
            startBtn.setEnabled(true);
            pauseBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            pauseBtn.setText("Pause");
        });
    }

    private void setupZoomButtons() {
        zoomInBtn.setFont(new Font("Arial", Font.BOLD, 16));
        zoomInBtn.addActionListener(e -> panel.zoomIn());
        zoomOutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        zoomOutBtn.addActionListener(e -> panel.zoomOut());
    }

    private void setupToolbar() {
        toolbar.setFloatable(false);
        toolbar.add(startBtn);
        toolbar.add(pauseBtn);
        toolbar.add(stopBtn);
        toolbar.addSeparator();
        toolbar.add(new JLabel("Zoom: "));
        toolbar.add(zoomOutBtn);
        toolbar.add(zoomInBtn);
    }
}
