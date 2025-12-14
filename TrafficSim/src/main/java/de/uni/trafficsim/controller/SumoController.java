package de.uni.trafficsim.controller;

import de.uni.trafficsim.model.RoadNetwork;
import de.uni.trafficsim.model.TrafficLightWrapper;
import de.uni.trafficsim.model.VehicleWrapper;
import de.uni.trafficsim.view.DashboardPanel;
import de.uni.trafficsim.model.SimulationFrame;
import de.uni.trafficsim.view.VisualizationPanel;
import org.eclipse.sumo.libtraci.*;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class SumoController implements Runnable {
    private final String sumoConfigPath;
    private final VisualizationPanel view;
    private final DashboardPanel dashboard; // Reference to Dashboard
    private final RoadNetwork roadNetwork;
    private final JLabel timeLabel; // Reference to UI label

    private SimulationFrame simulationFrame;

    private volatile boolean running = false;
    private volatile boolean paused = false;
    private volatile boolean stepRequested = false; // Flag for single step

    //new for stress test
    private volatile boolean stressTestRequested = false; // Stress test requested
    private volatile int stressVehiclesLeft = 0; // amount of cars generated
    private int stressVehicleCounter = 0; // Counter for vehicle ID

    // Random generator for mock data
    private final Random random = new Random();

    // Generic Task Queue for interacting with SUMO from EDT
    private final Queue<Runnable> taskQueue = new LinkedList<>();

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

        //new stress test
        stressTestRequested = false;
        stressVehiclesLeft = 0;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    // Helpers for the Dialog
    public List<String> getAvailableRoutes() {
        return simulationFrame.availableRoutes;
    }
    public List<String> getAvailableTypes() {
        return simulationFrame.availableTypes;
    }

    public SimulationFrame getSimulationFrame() {
        return simulationFrame;
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

    // Called by the View when user clicks a specific light
    public void switchTrafficLight(TrafficLightWrapper tl) {
        scheduleTask(tl::changeState);
        System.out.println("Queued switch for TLS: " + tl.getId());
    }

    // Thread-safe method to schedule SUMO commands
    public synchronized void scheduleTask(Runnable task) {
        taskQueue.add(task);
    }

    //new function for stress Test
    public void runStressTest() {
        //System.out.println("Stress test triggert");
        stressVehiclesLeft = 100;
        stressTestRequested = true;
    }

    //new function for stress Test
    private String getAnyRouteId() {
        try {
            StringVector routes = Route.getIDList();
            if (routes != null && !routes.isEmpty()) {
                return routes.get(0);
            }
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public void run() {
        Process sumoProcess = null;
        try {
            // 1. Start SUMO Process (Headless Mode)
            System.out.println("Launching SUMO...");
            String[] cmd = {
                    "sumo",
                    "-c", sumoConfigPath,
                    "--step-length", "0.1" // 100ms per step
            };
            sumoProcess = Runtime.getRuntime().exec(cmd);

            // Give SUMO a moment to start up and open the port
            Thread.sleep(2000);

            // 2. Connect via TraCI
            System.out.println("Connecting to TraCI");

            /// If you're using Windows/Linux, you should use line 69, in other cases - use line 68 with your path.
            System.load("/Users/alexandrbahno/sumo/bin/liblibtracijni.jnilib");
//            Simulation.preloadLibraries();

            Simulation.start(new StringVector(cmd));

            // 3. Initialization (Static Data)
            // We fetch the road network ONCE because it doesn't change.
            roadNetwork.loadFromSumo();
            view.setRoadNetwork(roadNetwork);

            // 4. Simulation Loop (Dynamic Data)
            while (running) {
                // Execute step if:
                // 1. Not paused (running normally)
                // 2. OR Paused but a manual step was requested
                if (!paused || stepRequested) {
                    // --- Process Task Queue (Injections, Switches, etc.) ---
                    synchronized(this) {
                        while (!taskQueue.isEmpty()) {
                            try {
                                taskQueue.poll().run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // --- Handle Stress Test Request ---
                    if (stressTestRequested && stressVehiclesLeft > 0) {
                        try {
                            String routeId = getAnyRouteId();
                            if (routeId != null) {
                                String vehId = "stressVeh_" + stressVehicleCounter++;
                                Vehicle.add(vehId, routeId, "DEFAULT_VEHTYPE", "now", "best", "0", "0");
                                stressVehiclesLeft--;

                                if (stressVehiclesLeft == 0) {
                                    stressTestRequested = false;
                                }
                            } else {
                                stressTestRequested = false;
                            }
                        } catch (Exception e) {
                            stressTestRequested = false;
                        }
                    }

                    Simulation.step();
                    // 1. Fetch Time
                    fetchSimulationTime();

                    // 2. Fetch Data
                    simulationFrame = new SimulationFrame();

                    // Cache available routes and vehicle types for dropdowns
                    simulationFrame.availableRoutes.addAll(Route.getIDList());
                    simulationFrame.availableTypes.addAll(VehicleType.getIDList());

                    StringVector vehIds = Vehicle.getIDList();
                    fetchVehicles(simulationFrame, vehIds);

                    StringVector tlsIds = TrafficLight.getIDList();
                    fetchTrafficLights(simulationFrame, tlsIds);

                    // 3. Update Dashboard (MOCK DATA)
                    updateStatDashboard(vehIds);

                    // 4. Update our map
                    view.updateFrame(simulationFrame);

                    // Reset single step flag immediately after processing
                    stepRequested = false;
                }

                // Rate Limiting (approx 30 FPS drawing)
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

    private void fetchSimulationTime() {
        double currentTime = Simulation.getTime();

        // Update UI Label (Must be done on EDT)
        SwingUtilities.invokeLater(() ->
                timeLabel.setText(String.format("Time: %.1f s", currentTime))
        );
    }

    private void fetchVehicles(SimulationFrame frame, StringVector vehIds) {
        for (String vid : vehIds) {
            VehicleWrapper vehicle = new VehicleWrapper(
                    vid,
                    Vehicle.getPosition(vid),
                    Vehicle.getAngle(vid),
                    Vehicle.getSpeed(vid),
                    Vehicle.getLength(vid),
                    Vehicle.getRouteID(vid),
                    Vehicle.getColor(vid)
            );
            frame.vehicleManager.addVehicle(vehicle);
//            frame.vehiclePositions.put(vid, Vehicle.getPosition(vid));
//            frame.vehicleAngles.put(vid, Vehicle.getAngle(vid));
        }
    }

    private void fetchTrafficLights(SimulationFrame frame, StringVector tlsIds) {
        for (String tid : tlsIds) {
            String state = TrafficLight.getRedYellowGreenState(tid);
            List<TraCIPosition> positions = roadNetwork.getTlsStopLines().get(tid);
            // Zip the state string with the positions
            if (positions != null) {
                int count = Math.min(state.length(), positions.size());
                for (int k = 0; k < count; k++) {
                    frame.trafficLights.add(
                            new TrafficLightWrapper(tid, positions.get(k), state.charAt(k))
                    );
                }
            }
        }
    }

    private void updateStatDashboard(StringVector vehIds) {
        // We generate plausible numbers to demonstrate the UI
        int mockTotalVehicles = vehIds.size() + random.nextInt(5); // Mix real count with noise
        double mockAvgSpeed = 10.0 + random.nextDouble() * 15.0; // Random speed 10-25 m/s
        int mockStopped = random.nextInt(mockTotalVehicles / 2 + 1);
        double mockCo2 = mockTotalVehicles * (2.5 + random.nextDouble());

        SwingUtilities.invokeLater(() ->
                dashboard.updateStats(mockTotalVehicles, mockAvgSpeed, mockStopped, mockCo2)
        );
    }
}
