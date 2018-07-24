import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

public class PlotGpx {

	/**
	 * It parses the dimensions data
	 * @param GPXData
	 * @return Array with two elements, width and height
	 */
	private int[] getDimensionsFromGPXData(HashMap<String,Object> GPXData) {
		return new int[] {
				Integer.parseInt(GPXData.get("dimensionWidth").toString()),
				Integer.parseInt(GPXData.get("dimensionHeight").toString())
		};
	}
	


	private void fixCreationId(Document soup) {
		// TODO
		/**
		 * This method was not implemented because jsoup
		 * parses the original xml properly.
		 * 
		 * Saul Hidalgo
		 */
	}
	
	/**
	 * It creates the new pptx file adding the track data previously parsed.
	 * @param gPXData Track Data
	 * @param narrativeEntries Narratives from the XML Track File
	 * @param interval Interval from the XML Track File
	 * @param slide_path First slide of the pptx file
	 * @param temp_unpack_path Working directory
	 */
	private void createPptxFromTrackData(HashMap<String, Object> GPXData,
			ArrayList<HashMap<String, Object>> narrativeEntries, int interval, String slide_path,
			String temp_unpack_path) {
		
		ArrayList<?> trackData = (ArrayList<?>) GPXData.get("trackData");
		System.out.println("Number of tracks::: " + trackData.size());
		
		// Dimension data
		int[] dimensionsArray = getDimensionsFromGPXData(GPXData);
		int dimensionWidth = dimensionsArray[0];
		int dimensionHeight = dimensionsArray[1];
		
		// Get slide size from presentation.xml file
		String[] slideDimen = new ParsePresentation().parsePresentation(temp_unpack_path);
		String slide_dimen_x = slideDimen[0];
		String slide_dimen_y = slideDimen[1];
		
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(slide_path));
			Document soup = Jsoup.parse(new String(encoded), "", Parser.xmlParser());
			
			// Fix creation id tag
			fixCreationId(soup);
			
			FindMap findMap = new FindMap();
			findMap.getMapDetails(temp_unpack_path);
		} catch (IOException e) {
			System.out.println("Corrupted xml file " + slide_path);
			System.exit(1);
		}
	}

	/**
	 * Helper function declarations -
	 * @param donor donor file
	 * @param tracks_path tracks path file
	 */
	public String[] checkPathandInitialization(String donor, String tracks_path) {
		if ( Files.notExists(Paths.get(donor)) ) {
			System.out.println("donor file does not exist");
			System.exit(1);
		}
		if ( Files.notExists(Paths.get(donor)) ) {
			System.out.println("donor file does not exist");
			System.exit(1);
		}
		
		// We delete the directory where we are going to unpack the xml.
		String unpack_path = donor.split("\\.")[0];
		if ( Files.exists(Paths.get(unpack_path)) ) {
			try {
				FileUtils.deleteDirectory(new File(unpack_path));
			} catch (IOException e) {
				System.out.println("Impossible to remove the directory " + unpack_path);
			}
		}
		
		// Now we unpack the file.
		String[] temps = tracks_path.split("/");
		String ppt_name = temps[temps.length - 1];
		
		String temp_unpack_path = ppt_name.split("\\.")[0] + "_temp";
		new UnpackFunction().unpackFunction(donor, temp_unpack_path);
		
		String slide_path = temp_unpack_path + "/ppt/slides/slide1.xml";
		
		return new String[] {slide_path, temp_unpack_path };
	}
	
	public static void main(String[] args) {
		new PlotGpx().run(args);
	}
	
	public void run(String[] args) {
		// TODO: Configure the Parameter parser to match the original GNU-like format
		Options arguments = new Options();
		arguments.addOption("donor", true, "Path to donor pptx file");
		arguments.addOption("tracks_path", true, "Path to gpx tracks file");

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine commandLine = parser.parse(arguments, args);
			
			if ( !commandLine.hasOption("donor") || !commandLine.hasOption("tracks_path") ) {
				printHelp(arguments);
			}
			
			String donor = commandLine.getOptionValue("donor");
			String tracks_path = commandLine.getOptionValue("tracks_path");
			
			// check paths check
			String[] output = checkPathandInitialization(donor, tracks_path);
			
			String slide_path = output[0];
			String temp_unpack_path = output[1];
			
			ParseTracks parseTracksInstance = new ParseTracks();
			HashMap<String, Object> GPXData = parseTracksInstance.getTrackData(tracks_path);
			ArrayList<HashMap<String, Object>> narrativeEntries = parseTracksInstance.getNarratives(tracks_path);
			int interval = parseTracksInstance.getInterval(tracks_path);
			createPptxFromTrackData(GPXData, narrativeEntries, interval, slide_path, temp_unpack_path);
		} catch (ParseException e) {
			printHelp(arguments);
		}
	}

	private void printHelp(Options arguments) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Script to plot gpx data on pptx", arguments);
		System.exit(1);
	}
}
