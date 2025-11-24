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
        String sumoConfig = "/Users/alexandrbahno/Desktop/Study/Java/trass-demo/src/main/resources/sim.sumocfg";
//        String sumoConfig = "/Users/alexandrbahno/sumo/tests/complex/tutorial/public_transport/data/run.sumocfg";
        MainFrame frame = MainFrame.getInstance(sumoConfig);
        frame.run();
    }
}
