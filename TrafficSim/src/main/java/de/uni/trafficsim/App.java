package de.uni.trafficsim;

import de.uni.trafficsim.view.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        setupUI();
    }

    private static void setupUI() {
        // POINT THIS TO YOUR .sumocfg FILE
//        String sumoConfig = "/Users/alexandrbahno/Desktop/Study/Java/final_project/TrafficSim/src/main/resources/sim.sumocfg";
//        String sumoConfig = "/Users/alexandrbahno/sumo/2025-11-11-15-39-28/osm.sumocfg";
        String sumoConfig = "/Users/alexandrbahno/Downloads/sumo-scenarios-main/bologna/acosta/run.sumocfg";
        MainFrame frame = MainFrame.getInstance(sumoConfig);
        frame.run();
    }
}
