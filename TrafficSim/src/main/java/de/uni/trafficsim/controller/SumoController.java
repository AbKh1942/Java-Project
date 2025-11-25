package de.uni.trafficsim.controller;

import de.uni.trafficsim.model.RoadNetwork;
import de.uni.trafficsim.view.DashboardPanel;
import de.uni.trafficsim.view.SimulationFrame;
import de.uni.trafficsim.view.VisualizationPanel;
import org.eclipse.sumo.libtraci.*;

import javax.swing.*;
import java.io.IOException;
import java.util.Random;

public class SumoController implements Runnable {
    private final String sumoConfigPath;
    private final VisualizationPanel view;
    private final DashboardPanel dashboard; // Reference to Dashboard
    private final RoadNetwork roadNetwork;
    private final JLabel timeLabel; // Reference to UI label

    private volatile boolean running = false;
    private volatile boolean paused = false;
    private volatile boolean stepRequested = false; // Flag for single step

    // Random generator for mock data
    private final Random random = new Random();

    public SumoController(String configPath, VisualizationPanel view, DashboardPanel dashboard, JLabel timeLabel) {
        this.sumoConfigPath = configPath;
        this.view = view;
        this.dashboard = dashboard;
        this.timeLabel = timeLabel;
        this.roadNetwork = new RoadNetwork();
    }

    public void start() {
        if (running) return;
        running = true;
        paused = false;
        new Thread(this).start();
    }

    public void stop() {
        running = false;
        paused = false;
        stepRequested = false;
        // Clean close is handled in the run loop
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void stepOnce() {
        // Only allow manual stepping if we are paused
        if (paused) {
            stepRequested = true;
        }
    }

    @Override
    public void run() {
        Process sumoProcess = null;
        try {
            // ---------------------------------------------------------
            // 1. Start SUMO Process (Headless Mode)
            // ---------------------------------------------------------
            System.out.println("Launching SUMO...");
            String[] cmd = {
                    "sumo",
                    "-c", sumoConfigPath,
                    "--step-length", "0.1" // 100ms per step
            };
            sumoProcess = Runtime.getRuntime().exec(cmd);

            // Give SUMO a moment to start up and open the port
            Thread.sleep(2000);

            // ---------------------------------------------------------
            // 2. Connect via TraCI
            // ---------------------------------------------------------
            System.out.println("Connecting to TraCI");
            /// If you're using Windows/Linux, you should use line 69, in other cases - use line 68 with your path.
            System.load("/Users/alexandrbahno/sumo/bin/liblibtracijni.jnilib");
//            Simulation.preloadLibraries();

            Simulation.start(new StringVector(cmd));

            // ---------------------------------------------------------
            // 3. Initialization (Static Data)
            // ---------------------------------------------------------
            // We fetch the road network ONCE because it doesn't change.
            roadNetwork.loadFromSumo();
            view.setRoadNetwork(roadNetwork);

            // ---------------------------------------------------------
            // 4. Simulation Loop (Dynamic Data)
            // ---------------------------------------------------------
            while (running) {
                // Execute step if:
                // 1. Not paused (running normally)
                // 2. OR Paused but a manual step was requested
                if (!paused || stepRequested) {
                    Simulation.step();
                    // 1. Fetch Time
                    double currentTime = Simulation.getTime();

                    // 2. Update UI Label (Must be done on EDT)
                    SwingUtilities.invokeLater(() ->
                            timeLabel.setText(String.format("Time: %.1f s", currentTime))
                    );

                    // 3. Fetch Data
                    SimulationFrame frame = new SimulationFrame();

                    StringVector vehIds = Vehicle.getIDList();
                    for (String vid : vehIds) {
                        frame.vehiclePositions.put(vid, Vehicle.getPosition(vid));
                        frame.vehicleAngles.put(vid, Vehicle.getAngle(vid));
                    }

                    StringVector tlsIds = TrafficLight.getIDList();
                    for (String tid : tlsIds) {
                        frame.tlsStates.put(tid, TrafficLight.getRedYellowGreenState(tid));
                        StringVector junctions = TrafficLight.getControlledJunctions(tid);
                        if (!junctions.isEmpty()) {
                            frame.tlsPositions.put(tid, Junction.getPosition(junctions.get(0)));
                        }
                    }

                    // 3. Update Dashboard (MOCK DATA)
                    // We generate plausible numbers to demonstrate the UI
                    int mockTotalVehicles = vehIds.size() + random.nextInt(5); // Mix real count with noise
                    double mockAvgSpeed = 10.0 + random.nextDouble() * 15.0; // Random speed 10-25 m/s
                    int mockStopped = random.nextInt(mockTotalVehicles / 2 + 1);
                    double mockCo2 = mockTotalVehicles * (2.5 + random.nextDouble());

                    SwingUtilities.invokeLater(() ->
                            dashboard.updateStats(mockTotalVehicles, mockAvgSpeed, mockStopped, mockCo2)
                    );

                    view.updateFrame(frame);

                    // Reset single step flag immediately after processing
                    stepRequested = false;
                }

                // D. Rate Limiting (approx 30 FPS drawing)
                Thread.sleep(33);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error communicating with SUMO: " + e.getMessage());
        } finally {
            try {
                Simulation.close();
            } catch (Exception e) { /* Ignore close errors */ }

            if (sumoProcess != null) {
                sumoProcess.destroy();
            }
            System.out.println("Simulation stopped.");
        }
    }
}
