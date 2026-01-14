package de.uni.trafficsim.view;

import de.uni.trafficsim.controller.SumoController;
import de.uni.trafficsim.model.RoadNetwork;
import de.uni.trafficsim.model.SimulationFrame;
import de.uni.trafficsim.model.TrafficLight.TrafficLightWrapper;
import de.uni.trafficsim.model.VehicleFilter;
import de.uni.trafficsim.model.VehicleWrapper;
import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.TraCIPosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class VisualizationPanel extends JPanel implements WindowListener {
    private RoadNetwork roadNetwork;
    private SimulationFrame currentFrame;
    private SumoController controller; // Reference to controller for callbacks
    private VehicleFilter filter;

    // Viewport transforms
    private double scale = 2.0; // Zoom level
    private double offsetX = 50;
    private double offsetY = 600; // Offset to handle coordinate flip

    // Constructor
    public VisualizationPanel() {
        setBackground(Color.DARK_GRAY);
        // --- ADDED INTERACTION LOGIC ---
        MouseAdapter ma = new MouseAdapter() {
            private Point lastPoint;
            TrafficLightWrapper pendingTls = null;
            Timer longPressTimer;

            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                pendingTls = getTlsAt(e.getX(), e.getY());

                if (pendingTls != null) {
                    // Start Long Press Timer (500 ms)
                    if (longPressTimer != null && longPressTimer.isRunning()) longPressTimer.stop();

                    longPressTimer = new Timer(500, evt -> {
                        // Action for Long Press: Open Editor
                        if (pendingTls != null && controller != null) {
                            controller.openPhaseEditorFor(pendingTls.getId());
                            pendingTls = null; // Consume event
                        }
                    });
                    longPressTimer.setRepeats(false);
                    longPressTimer.start();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // If released before timer, it's a CLICK
                if (longPressTimer != null && longPressTimer.isRunning()) {
                    longPressTimer.stop();
                    if (pendingTls != null && controller != null) {
                        // Action for Short Click: Switch Light
                        controller.switchTrafficLight(pendingTls);
                    }
                }
                pendingTls = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint == null) return;

                // Cancel click/long-press if dragging
                if (pendingTls != null && e.getPoint().distance(lastPoint) > 5.0) {
                    if(longPressTimer!=null) longPressTimer.stop();
                    pendingTls = null;
                }

                // Calculate how much the mouse moved
                int dx = e.getX() - lastPoint.x;
                int dy = e.getY() - lastPoint.y;

                // Update offsets
                offsetX += dx;
                offsetY += dy;

                lastPoint = e.getPoint();
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Zoom towards mouse pointer
                double zoomFactor = (e.getWheelRotation() < 0) ? 1.1 : (1.0 / 1.1);
                applyZoom(zoomFactor, e.getX(), e.getY());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Handle Traffic Light Clicking
                if (currentFrame != null && controller != null) {
                    checkTlsClick(e.getX(), e.getY());
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);

        // --- KEYBOARD SHORTCUTS ---
        setupKeyBindings();
    }

    // Setters
    public void setController(SumoController c) {
        this.controller = c;
    }
    public void setFilter(VehicleFilter f) { this.filter = f; }

    public void setRoadNetwork(RoadNetwork net) {
        this.roadNetwork = net;
        repaint();
    }

    public void updateFrame(SimulationFrame frame) {
        this.currentFrame = frame;
        repaint();
    }

    private TrafficLightWrapper getTlsAt(int screenX, int screenY) {
        if (currentFrame == null) return null;

        // Convert Screen to World
        double worldX = (screenX - offsetX) / scale;
        double worldY = (offsetY - screenY) / scale;

        for (TrafficLightWrapper l : currentFrame.trafficLights) {
            double d = Math.sqrt(Math.pow(l.getPosition().getX() - worldX, 2) + Math.pow(l.getPosition().getY() - worldY, 2));
            if (d < 5.0) return l;
        }
        return null;
    }

    // Check whether click on the map was on a traffic light or not
    private void checkTlsClick(int screenX, int screenY) {
        TrafficLightWrapper tls = getTlsAt(screenX, screenY);

        if (tls != null) {
            System.out.println("Clicked TLS: " + tls.getId());
            controller.switchTrafficLight(tls);
        }
    }

    // Method for setting up keyboard shortcuts
    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // Zoom In keys: '+', '=', and Numpad Add
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "zoomIn"); // often same key
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "zoomIn");

        // Zoom Out keys: '-' and Numpad Subtract
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoomOut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "zoomOut");

        am.put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { zoomIn(); }
        });
        am.put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { zoomOut(); }
        });
    }

    // --- Public Zoom Methods for Buttons ---
    public void zoomIn() {
        // Button/Key zoom focuses on center of screen
        applyZoom(1.1, getWidth() / 2.0, getHeight() / 2.0);
    }

    public void zoomOut() {
        applyZoom(1.0 / 1.1, getWidth() / 2.0, getHeight() / 2.0);
    }

    // Core logic to zoom towards a specific point (focusX, focusY)
    private void applyZoom(double zoomFactor, double focusX, double focusY) {
        double oldScale = scale;
        scale *= zoomFactor;

        // Offset adjustment formula:
        // NewOffset = Focus - (Focus - OldOffset) * (NewScale / OldScale)
        offsetX = focusX - (focusX - offsetX) * (scale / oldScale);
        offsetY = focusY - (focusY - offsetY) * (scale / oldScale);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- 1. Coordinate System Setup ---
        AffineTransform oldTx = g2.getTransform();

        // Translate to offset
        g2.translate(offsetX, offsetY);
        // Scale (Zoom)
        g2.scale(scale, scale);
        // Flip Y-Axis (SUMO is y-up, Java is y-down)
        g2.scale(1, -1);

        // --- 2. Draw Roads (Static) ---
        if (roadNetwork != null) {
            g2.setColor(Color.LIGHT_GRAY);
            for (Shape s : roadNetwork.getLaneShapes().values()) {
                drawRoad(g2, s);
            }
        }

        // --- 3. Draw Dynamic Entities ---
        if (currentFrame != null) {
            // Draw Traffic Lights
            for (TrafficLightWrapper tl: currentFrame.trafficLights) {
                drawTrafficLight(g2, tl);
            }

            // Draw Vehicles
            for (VehicleWrapper veh : currentFrame.vehicleManager.getVehicles()) {
                if (controller.getFilter().matches(veh)) {
                    drawVehicle(g2, veh);
                }
            }
        }

        g2.setTransform(oldTx);

        // --- 4. Draw HUD ---
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.drawString("Controls: Drag to Pan | Scroll or +/- to Zoom", 10, 20);
        g2.drawString(String.format("Zoom: %.2fx", scale), 10, 35);
    }

    private void drawRoad(Graphics2D g2, Shape s) {
        g2.fill(s); // Fill lane
        g2.setColor(Color.lightGray);
    }

    private void drawVehicle(Graphics2D g2, VehicleWrapper vehicle) {
        AffineTransform tx = g2.getTransform();
        g2.translate(vehicle.getPosition().getX(), vehicle.getPosition().getY());
        // Rotate vehicle (SUMO 0 is North/Up, Java 0 is East/Right)
        // We flip Y axis previously, so we must adjust rotation carefully.
        // Standard mapping: Java Rotation = Math.toRadians(90 - SumoAngle)
        g2.rotate(Math.toRadians(90 - vehicle.getAngle()));

        g2.setColor(vehicle.getColor());

        double halfLen = vehicle.getLength() / 2.0;
        Path2D veh = new Path2D.Double();
        veh.moveTo(halfLen, 0); // Front center
        veh.lineTo(-halfLen, 1.0); // Back Left
        veh.lineTo(-halfLen, -1.0); // Back Right
        veh.closePath();

        g2.fill(veh);

        // Outline
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke((float)(0.2))); // Constant thin stroke relative to object
        g2.draw(veh);

        g2.setTransform(tx);
    }

    private void drawTrafficLight(Graphics2D g2, TrafficLightWrapper tl) {
        // Simple visualization: A circle at the junction center
        g2.setColor(tl.getColor());
        // Draw as a rotated bar section
        AffineTransform tx = g2.getTransform();
        g2.translate(tl.getPosition().getX(), tl.getPosition().getY());
        g2.rotate(Math.toRadians(tl.getAngle()));
        // Width 1.5 (spacing), Height 0.5 (thickness)
        Rectangle2D.Double rect = new Rectangle2D.Double(-0.75, -0.25, 1.5, 0.5);
        g2.fill(rect);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(0.1f));
        g2.draw(rect);
        g2.setTransform(tx);
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) {
        Simulation.close();
    }

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
}
