import model.*;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PlotGpx {

	public static void main(String[] args) {
		PlotGpx plotGpx = new PlotGpx();
		Options arguments = new Options();
		arguments.addOption("donor", true, "Path to donor pptx file");
		arguments.addOption("tracks_path", true, "Path to gpx tracks file");

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine commandLine = parser.parse(arguments, args);

			if ( !commandLine.hasOption("donor") || !commandLine.hasOption("tracks_path") ) {
				plotGpx.printHelp(arguments);
			}

			String donor = commandLine.getOptionValue("donor");
			String tracks_path = commandLine.getOptionValue("tracks_path");

			byte[] encoded = Files.readAllBytes(Paths.get(tracks_path));
			String trackXml = new String(encoded);

			TrackData trackData = TrackParser.getInstance().parse(trackXml);
			
			PlotTracks plotter = new PlotTracks();
			
			plotter.export(trackData, donor);
		} catch (ParseException e) {
			plotGpx.printHelp(arguments);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printHelp(Options arguments) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Script to plot gpx data on pptx", arguments);
		System.exit(1);
	}
}
