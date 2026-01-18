package de.uni.trafficsim;


import de.uni.trafficsim.view.MainFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Launches the Application
 * <p>
 * This class boots the Swing user interface and wires it to a SUMO
 * configuration file so the simulation can be started.
 */
public class App 
{
    public static Logger logger = LogManager.getLogger(App.class);

    /**
     * Launches the application.
     * <p>
     * Initializes the UI and opens the main window. The SUMO configuration
     * file path is selected inside the setup method.
     *
     * @param args command-line arguments (not used)
     */
    public static void main( String[] args )
    {
        setupUI();
    }

    private static void setupUI() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Test");
        MainFrame frame = MainFrame.getInstance();
        frame.run();
    }
}
