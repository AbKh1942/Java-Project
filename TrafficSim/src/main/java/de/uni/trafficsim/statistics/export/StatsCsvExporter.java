package de.uni.trafficsim.statistics.export;

import de.uni.trafficsim.statistics.StatsSnapshot;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * Exports simulation statistics to CSV files.
 * <p>
 * Writes a global statistics history into a timestamped file
 * under the user's home directory.
 */
public class StatsCsvExporter {

    //time stamp for export file
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    //Export directory, builds path to TrafficSimExport folder in Users Home-directory
    private static Path exportDir() throws IOException {            //throws exception if something with directory goes wrong, i.e no access rights
        Path dir = Paths.get(System.getProperty("user.home"), "TrafficSimExports");
        Files.createDirectories(dir); // creates directory, if it doesnt already exist
        return dir;  //returns path where csv file is saved (export directory)
    }

    // --- Csv Export ---
    
    /**
     * Exports the global statistics history to a CSV file.
     * takes saved statistic history as input
     * <p>
     * Creates the export directory if needed and writes one row per snapshot.
     *
     * @param history list of statistics snapshots to export
     * @return path to the written CSV file
     * @throws IOException if the file cannot be written
     */
    public static Path exportGlobalCsv(List<StatsSnapshot> history) throws IOException {
        Path out = exportDir().resolve("stats_global_" + LocalDateTime.now().format(TS) + ".csv"); //creates file- / pathname
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8,                    //creates writer w for writing text into file out. (UTF-8 Format)
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {                     //if file already exists, overwrite

            //This is the text in the csv file
            w.write("simulationTimeSec;globalAvgSpeedMs;totalVehicles;stoppedVehicles;totalCo2Kg;totalFuelL;arrivedVehiclesTotal;avgDensityVehPerKm;avgOccupancyPercent;edgeCount"); //Header
            w.newLine();
            for (StatsSnapshot s : history) {                  //take the data from all Snapshots in history
                double time = s.simulationTimeSec();
                double avgSpeed = s.globalAvgSpeedMs();
                int edgeCount;

                if (s.edges() == null) {
                    edgeCount = 0;
                } else {
                    edgeCount = s.edges().size();
                }

                double avgDensity = 0.0;
                double avgOccupancy = 0.0;

                //calcuclating avergae density and Occupancy, because data in EdgeSnapshot is per Edge
                if (s.edges() != null && !s.edges().isEmpty()) {
                    avgDensity = s.edges().values().stream()
                            .mapToDouble(de.uni.trafficsim.statistics.EdgeSnapshot::densityPerKm)
                            .filter(v -> v >= 0)
                            .average()
                            .orElse(0.0);

                    avgOccupancy = s.edges().values().stream()
                            .mapToDouble(de.uni.trafficsim.statistics.EdgeSnapshot::occupancyPercent)
                            .filter(v -> v >= 0)
                            .average()
                            .orElse(0.0);
                }

                //write one snapshot entry
                String line = String.format(java.util.Locale.US,
                        "\"%.2f\";%.3f;%d;%d;%.6f;%.6f;%d;%.3f;%.3f;%d",
                        time,
                        avgSpeed,
                        s.totalVehicles(),
                        s.stoppedVehicles(),
                        s.totalCo2Kg(),
                        s.totalFuelL(),
                        s.arrivedVehiclesTotal(),
                        avgDensity,
                        avgOccupancy,
                        edgeCount
                );
                w.write(line);
                w.newLine();
            }
        }
        return out; //returns path to csv file
    }
}
