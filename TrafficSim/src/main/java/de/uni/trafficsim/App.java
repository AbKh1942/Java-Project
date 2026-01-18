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
        // POINT THIS TO YOUR .sumocfg FILE
        String sumoConfig = "/Users/johngrosch/sumo_cfg/sim.sumocfg";
//        String sumoConfig = "/Users/alexandrbahno/sumo/2025-11-11-15-39-28/osm.sumocfg";
//        String sumoConfig = "/Users/alexandrbahno/Downloads/sumo-scenarios-main/bologna/acosta/run.sumocfg";
        MainFrame frame = MainFrame.getInstance(sumoConfig);
        frame.run();
    }
}
