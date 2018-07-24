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
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

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


	/**
	 * Returns the integer from the strings.
	 * @param mapDetails Map from the slide
	 * @return x, y, cx, cy
	 */
	private int[] getMapDimesions(HashMap<String, String> mapDetails) {
		return new int[]{
				Integer.parseInt(mapDetails.get("x")),
				Integer.parseInt(mapDetails.get("y")),
				Integer.parseInt(mapDetails.get("cx")),
				Integer.parseInt(mapDetails.get("cy"))
		};
	}

	/**
	 * Remove all the remaining items inside the spTree tag
	 * TODO Not tested.
	 * @param soup Soup Document
	 */
	private void cleanSpTree(Document soup){
		for (Element treeElement : soup.select("p|spTree")){
			for ( Element children : treeElement.children() ){
				children.remove();
			}
		}
	}

	/**
	 * We return the track, marker, time and narrative, removing it
	 * from the soup document
	 * @param soup soup document
	 * @return Array containing the track, marker, time and narrative.
	 */
	private Element[] getShapes(Document soup){
		Element shape_tag = null, arrow_tag = null, time_tag = null, narrative_tag = null;

		// retrieve the sample arrow and path tag
		Elements all_shape_tags = soup.select("p|sp");
		for ( Element shape : all_shape_tags ){
			String name = shape.select("p|cNvPr").get(0).attr("name");
			if ( "track".equals(name) ){
				shape_tag = shape;
			}else if ( "marker".equals(name) ){
				arrow_tag = shape;
			}else if ( "time".equals(name) ){
				time_tag = shape;
			}else if ( "narrative".equals(name) ){
				narrative_tag = shape;
			}
		}

		shape_tag.remove();
		arrow_tag.remove();
		time_tag.remove();
		narrative_tag.remove();
		return new Element[]{
				shape_tag, arrow_tag, time_tag, narrative_tag
		};
	}

	/**
	 * Reassignment of the ID to the XML.
	 * TODO Not tested.
	 *
	 * This method was not implemented because jsoup
	 * parses the original xml properly.
	 *
	 * Saul Hidalgo
	 * @param soup
	 */
	private void fixCreationId(Document soup) {
		Element creationIdsoup = soup.selectFirst("p14|creationId");
		//...
	}

	/**
	 * Extract and remove the time animation objects from the XML document
	 * @param soup XML Document
	 * @param time_tag Time tag previously removed from the XML document
	 * @return
	 */
	private Element[] findTimeAnimationObjects(Document soup, Element time_tag) {
		String time_id_original = time_tag.select("p|cNvPr").get(0).attr("id");
		Elements spTgts = soup.select("p|spTgt");
		Element time_anim_tag_big = null;
		Element time_anim_tag_first = null;
		for ( Element spTgt : spTgts ){
			if ( time_id_original.equals(spTgt.attr("spid")) ){
				time_anim_tag_first = spTgt.parent().parent().parent().parent().parent().parent();
				time_anim_tag_big = time_anim_tag_first.parent().parent().parent();
				break;
			}
		}

		Element time_anim_tag_big_insertion = time_anim_tag_big.parent();
		time_anim_tag_big.remove();
		return new Element[]{
				time_anim_tag_first, time_anim_tag_big, time_anim_tag_big_insertion
		};
	}

	/**
	 * Return the animation motion tags from the xml document after removing it
	 * @param soup XML Document.
	 * @return Animation Motion tags
	 */
	private Element[] findAnimationTagObjects(Document soup) {
		Element anim_tag = soup.selectFirst("p|animMotion");
		Element anim_tag_upper = anim_tag.parent().parent().parent();
		Element anim_insertion_tag_upper = anim_tag_upper.parent();
		anim_tag_upper.remove();
		return new Element[]{
				anim_tag, anim_tag_upper, anim_insertion_tag_upper
		};
	}
	
	/**
	 * It creates the new pptx file adding the track data previously parsed.
	 * @param GPXData Track Data
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

			// Get Map shape details
			FindMap findMap = new FindMap();
			HashMap<String, String> mapDetails = findMap.getMapDetails(temp_unpack_path);
			int[] dimensionsTemp = getMapDimesions(mapDetails);
			int mapX = dimensionsTemp[0], mapY = dimensionsTemp[1], mapCX = dimensionsTemp[2], mapCY = dimensionsTemp[3];

			// Calculating TL and BR
			float[] tl_tmp = coordinateTransformation((float)mapX, (float)mapY, Float.parseFloat(slide_dimen_x), Float.parseFloat(slide_dimen_y), 0, 0, 1, 1, 0);
			float TLx = tl_tmp[0], TLy = tl_tmp[1];
			tl_tmp = coordinateTransformation((float)(mapX + mapCX), (float)(mapY + mapCY), Float.parseFloat(slide_dimen_x), Float.parseFloat(slide_dimen_y), 0, 0, 1, 1, 0);
			float BRx = tl_tmp[0], BRy = tl_tmp[1];

			// Calculating rectangle representated as animated target values
			float animX = TLx;
			float animY = TLy;
			float animCX = BRx - TLx;
			float animCY = BRy - TLy;

			// getting shape tags
			Element[] shapes_temp = getShapes(soup);
			Element shape_tag = shapes_temp[0], arrow_tag = shapes_temp[1], time_tag = shapes_temp[2], narrative_tag = shapes_temp[3];

			// Remove all the remaining shapes.
			// cleanSpTree(soup);
			// Find time_animation objs -
			Element[] timeAnimTemp = findTimeAnimationObjects(soup, time_tag);
			Element time_anim_tag_first = timeAnimTemp[0];
			Element time_anim_tag_big = timeAnimTemp[1];
			Element time_anim_tag_big_insertion = timeAnimTemp[2];

			timeAnimTemp = findAnimationTagObjects(soup);
			Element anim_tag = timeAnimTemp[0];
			Element anim_tag_upper = timeAnimTemp[1];
			Element anim_insertion_tag_upper = timeAnimTemp[2];


		} catch (IOException e) {
			System.out.println("Corrupted xml file " + slide_path);
			System.exit(1);
		}
	}

	private float[] coordinateTransformation(float x, float y, float dimensionWidth, float dimensionHeight, int rectX, int rectY, int rectWidth, int rectHeight, int invertY) {
		x = rectX + x * ( rectWidth / dimensionWidth );
		if ( invertY == 1 ){
			y = y - dimensionHeight;
			y = rectY + y * ( rectHeight / (-dimensionHeight) );
		}else{
			y = rectY + y * (rectHeight / dimensionHeight);
		}
		return new float[]{
				x , y
		};
	}

	private float[] coordinateTransformation(float x, float y, float dimensionWidth, float dimensionHeight, int rectX, int rectY, int rectWidth, int rectHeight) {
		return coordinateTransformation(x, y, dimensionWidth, dimensionHeight, rectX, rectY, rectWidth, rectHeight, 1);
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
