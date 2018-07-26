import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

	/**
	 * Not implemented
	 * @param soup
	 */
	private void cleanSoup(Document soup) {
		// TODO Not implemented, because it wasn't needed.

	}

	private void writeSoup(String slide_path, Document soup) {
		try {
			soup.outputSettings().indentAmount(0).prettyPrint(false);
			FileWriter fileWriter = new FileWriter(slide_path);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			cleanSoup(soup);
			printWriter.println(soup);
			printWriter.close();
		} catch (IOException e) {
			System.out.println("Unable to write the slide file");
			System.exit(1);
		}
	}

	/**
	 * Insert the narratives in the track file to the soup document
	 * @param spTreeobj
	 * @param intervalDuration
	 * @param trackData
	 * @param time_tag
	 * @param time_anim_tag_first
	 * @param anim_insertion_tag_upper
	 * @param time_anim_tag_big
	 * @param time_anim_tag_big_insertion
	 * @param narrativeEntries
	 * @param narrative_tag
	 */
	private void createTimeNarrativeShapes(Element spTreeobj, int intervalDuration, ArrayList<?> trackData, Element time_tag, Element time_anim_tag_first, Element anim_insertion_tag_upper, Element time_anim_tag_big, Element time_anim_tag_big_insertion, ArrayList<HashMap<String, Object>> narrativeEntries, Element narrative_tag) {
		// Create parent animation object for all time box animations
		ArrayList<Element> time_shape_objs = new ArrayList<>();
		int coord_num = 0;
		int time_delay = intervalDuration;
		int current_time_id = Integer.parseInt(time_tag.selectFirst("p|cNvPr").attr("id"));
		System.out.println("Last Time Id::::: " + current_time_id);
		// we will get the timestamps from the first track

		HashMap<String, Object> firstItem = (HashMap<String, Object>) trackData.get(0);
		ArrayList<HashMap<String, Object>> coordinates = (ArrayList<HashMap<String, Object>>) firstItem.get("coordinates");
		for ( Object coordinateObj : coordinates ){
			HashMap<String, Object> coordinate = (HashMap<String, Object>) coordinateObj;
			LocalDateTime timestamp = (LocalDateTime) coordinate.get("time");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy MMM ddHHmm");
			String timestampString = timestamp.format(formatter);
			Element temp_time_tag = time_tag.clone();
			temp_time_tag.selectFirst("p|cNvPr").attr("id", current_time_id + "");
			temp_time_tag.selectFirst("p|txBody").selectFirst("a|p").selectFirst("a|r").selectFirst("a|t").text(timestampString);
			time_shape_objs.add(temp_time_tag);

			// handle animation objs for time
			if ( coord_num == 0 ){
				Element temp_time_anim = time_anim_tag_first.clone();
				temp_time_anim.selectFirst("p|spTgt").attr("spid", current_time_id + "");
				temp_time_anim.selectFirst("p|cond").attr("delay", "0");
				temp_time_anim.selectFirst("p|cTn").attr("nodeType", "withEffect");
				anim_insertion_tag_upper.insertChildren(anim_insertion_tag_upper.childNodeSize(), temp_time_anim);
			}else{
				Element temp_time_anim = time_anim_tag_big.clone();
				temp_time_anim.selectFirst("p|spTgt").attr("spid", current_time_id + "");
				temp_time_anim.selectFirst("p|cond").attr("delay", time_delay + "");
				time_delay += intervalDuration;
				temp_time_anim.selectFirst("p|cTn").attr("nodeType", "afterEffect");
				temp_time_anim.selectFirst("p|par").child(0).selectFirst("p|par").selectFirst("p|cond").attr("delay", intervalDuration + "");
				time_anim_tag_big_insertion.insertChildren(time_anim_tag_big_insertion.childNodeSize(), temp_time_anim);
			}

			if ( coord_num == 0 ){
				current_time_id = 300;
			}
			current_time_id++;
			coord_num++;
		}

		for ( Element timeshape : time_shape_objs ){
			spTreeobj.insertChildren(spTreeobj.childNodeSize(), timeshape);
		}

		// Adding narratives -
		ArrayList<Element> narrative_objects = new ArrayList<>();
		time_delay = 0;
		int current_narrative_id = Integer.parseInt(narrative_tag.selectFirst("p|cNvPr").attr("id"));
		System.out.println("Last Narrative Id::::: " + current_narrative_id);

		// Blank narrative box
		Element blank_narrative = narrative_tag.clone();
		blank_narrative.selectFirst("p|cNvPr").attr("id", current_narrative_id + "");
		blank_narrative.selectFirst("p|txBody").selectFirst("a|p").selectFirst("a|r").selectFirst("a|t").text("");
		narrative_objects.add(blank_narrative);
		current_narrative_id = 400;
		int num_narrative = 0;
		for ( HashMap<String, Object> narrative : narrativeEntries ){
			time_delay += Integer.parseInt((String) narrative.get("elapsed")) - time_delay;
			String time_str = (String) narrative.get("dateStr");
			Element temp_narrative_tag = narrative_tag.clone();
			temp_narrative_tag.selectFirst("p|cNvPr").attr("id", current_narrative_id + "");
			temp_narrative_tag.selectFirst("p|txBody").selectFirst("a|p").selectFirst("a|r").selectFirst("a|t").text(time_str + " " + narrative.get("Text"));
			narrative_objects.add(temp_narrative_tag);
			if ( num_narrative == 0 ){
				Element temp_narrative_anim = time_anim_tag_first.clone();
				temp_narrative_anim.selectFirst("p|spTgt").attr("spid", current_narrative_id + "");
				temp_narrative_anim.selectFirst("p|cond").attr("delay", time_delay + "");
				temp_narrative_anim.selectFirst("p|cTn").attr("nodeType", "withEffect");
				anim_insertion_tag_upper.insertChildren(anim_insertion_tag_upper.childNodeSize(), temp_narrative_anim);
			}else{
				Element temp_narrative_anim = time_anim_tag_big.clone();
				temp_narrative_anim.selectFirst("p|spTgt").attr("spid", current_narrative_id + "");
				temp_narrative_anim.selectFirst("p|cond").attr("delay", time_delay + "");
				temp_narrative_anim.selectFirst("p|cTn").attr("nodeType", "afterEffect");
				temp_narrative_anim.selectFirst("p|par").selectFirst("p|cond").attr("delay", intervalDuration + "");
				time_anim_tag_big_insertion.insertChildren(time_anim_tag_big_insertion.childNodeSize(), temp_narrative_anim);
			}

			current_narrative_id++;
			num_narrative++;
		}

		for ( Element narrative : narrative_objects ){
			spTreeobj.insertChildren(spTreeobj.childNodeSize(), narrative);
		}
	}

	private void addAnimationObjects(ArrayList<ArrayList<Element>> all_animation_objs, Element anim_tag_upper, Element anim_insertion_tag_upper) {
		int track_num = 1;
		for ( ArrayList<Element> track_anim_objs : all_animation_objs ){
			Element anim_tag_upper_temp = anim_tag_upper.clone();
			anim_tag_upper_temp.tagName("p:seq");
			Element parent_temp = anim_tag_upper_temp.selectFirst("p|animMotion").parent();
			anim_tag_upper_temp.selectFirst("p|animMotion").remove();
			for ( Element anim : track_anim_objs ){
				parent_temp.insertChildren(parent_temp.childNodeSize(), anim);
			}

			anim_tag_upper_temp.selectFirst("p|cTn").removeAttr("accel");
			anim_tag_upper_temp.selectFirst("p|cTn").removeAttr("decel");
			anim_tag_upper_temp.selectFirst("p|cTn").attr("id", track_num + "");
			track_num++;
			anim_insertion_tag_upper.insertChildren(anim_insertion_tag_upper.childNodeSize(), anim_tag_upper_temp);
		}
	}

	private void addShapeMarkerObjects(Element spTreeobj, ArrayList<Element> shape_objs, ArrayList<Element> arrow_objs) {
		for ( Element shape : shape_objs ){
			spTreeobj.insertChildren(spTreeobj.childNodeSize(), shape);
		}
		for ( Element arrow : arrow_objs){
			spTreeobj.insertChildren(spTreeobj.childNodeSize(), arrow);
		}
	}

	/**
	 * Extract the color from the data track map
	 * @param track data track map
	 * @return Hexadecimal color in the format %02X%02X%02X
	 */
	private String getColorinHex(HashMap<String, Object> track) {
		String colors = track.get("color").toString();
		colors = colors.substring(colors.indexOf("[") + 1, colors.length() - 1);
		String[] temp = colors.split(",");
		int r = Integer.parseInt(temp[0].split("=")[1]);
		int g = Integer.parseInt(temp[1].split("=")[1]);
		int b = Integer.parseInt(temp[2].split("=")[1]);

		String hex_value = String.format("%02X%02X%02X", r, g, b).toLowerCase();
		return hex_value;
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
	 * Extract the coordinates of the arrow tag.
	 * @param temp_arrow_tag Arrow tag
	 * @return An array with two integers, (x,y) arrow pointers.
	 */
	private int[] getArrowPointerCoordinates(Element temp_arrow_tag) {
		Elements gds = temp_arrow_tag.select("p|spPr").select("a|prstGeom").select("a|avLst").select("a|gd");
		int arrow_pointer_x = Integer.parseInt(gds.get(0).attr("fmla").substring(4));
		int arrow_pointer_y = Integer.parseInt(gds.get(1).attr("fmla").substring(4));
		return new int[]{ arrow_pointer_x, arrow_pointer_y };
	}

	/**
	 * Get arrow shape off and ext coordinates
	 * @param temp_arrow_tag Arrow tag
	 * @return An array with four integers, (offX, offY, extX, extY) arrow pointers.
	 */
	private float[] arrowCoordinates(Element temp_arrow_tag) {
		float arrow_off_x = Float.parseFloat(temp_arrow_tag.selectFirst("a|off").attr("x"));
		float arrow_off_y = Float.parseFloat(temp_arrow_tag.selectFirst("a|off").attr("y"));
		float arrow_ext_cx = Float.parseFloat(temp_arrow_tag.selectFirst("a|ext").attr("cx"));
		float arrow_ext_cy = Float.parseFloat(temp_arrow_tag.selectFirst("a|ext").attr("cy"));

		return new float[]{
				arrow_off_x, arrow_off_y, arrow_ext_cx, arrow_ext_cy
		};
	}

	/**
	 * Scaling the coordinates.
	 * @param x
	 * @param y
	 * @param dimensionWidth
	 * @param dimensionHeight
	 * @param rectX
	 * @param rectY
	 * @param rectWidth
	 * @param rectHeight
	 * @param invertY
	 * @return Scaled coordinates
	 */
	private float[] coordinateTransformation(float x, float y, float dimensionWidth, float dimensionHeight, float rectX, float rectY, float rectWidth, float rectHeight, int invertY) {
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

	/**
	 * Scaling the coordinates as integer.
	 * @param x
	 * @param y
	 * @param dimensionWidth
	 * @param dimensionHeight
	 * @param rectX
	 * @param rectY
	 * @param rectWidth
	 * @param rectHeight
	 * @param invertY
	 * @return
	 */
	private int[] coordinateTransformation(int x, int y, int dimensionWidth, int dimensionHeight, int rectX, int rectY, int rectWidth, int rectHeight, int invertY) {
		x = rectX + x * ( rectWidth / dimensionWidth );
		if ( invertY == 1 ){
			y = y - dimensionHeight;
			// floor was needed because Java rounds to the nearest integer, instead of flooring.
			y = rectY + y * ( (int)Math.floor((float)rectHeight / -dimensionHeight));
		}else{
			y = rectY + y * (rectHeight / dimensionHeight);
		}
		return new int[]{
				x , y
		};
	}

	private float[] coordinateTransformation(float x, float y, float dimensionWidth, float dimensionHeight, float rectX, float rectY, float rectWidth, float rectHeight) {
		return coordinateTransformation(x, y, dimensionWidth, dimensionHeight, rectX, rectY, rectWidth, rectHeight, 1);
	}

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
	 * It creates the new pptx file adding the track data previously parsed.
	 * @param GPXData Track Data
	 * @param narrativeEntries Narratives from the XML Track File
	 * @param intervalDuration Interval from the XML Track File
	 * @param slide_path First slide of the pptx file
	 * @param temp_unpack_path Working directory
	 */
	private void createPptxFromTrackData(HashMap<String, Object> GPXData,
										 ArrayList<HashMap<String, Object>> narrativeEntries, int intervalDuration, String slide_path,
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

			int trackCount = 0;
			int current_shape_id = Integer.parseInt(shape_tag.selectFirst("p|cNvPr").attr("id"));
			int current_arrow_id = Integer.parseInt(arrow_tag.selectFirst("p|cNvPr").attr("id"));
			System.out.println("Last Shape Id::::: " + current_shape_id);
			System.out.println("Last Arrow Id::::: " + current_arrow_id);

			ArrayList<Integer> shape_ids = new ArrayList<>();
			ArrayList<Integer> arrow_ids = new ArrayList<>();
			ArrayList<Element> shape_objs = new ArrayList<>();
			ArrayList<Element> arrow_objs = new ArrayList<>();
			ArrayList<ArrayList<Element>> all_animation_objs = new ArrayList<>();

			for ( Object trackObject : trackData ){
				HashMap<String, Object> track = (HashMap<String, Object>)trackObject;

				Element temp_arrow_tag = arrow_tag.clone();
				Element temp_shape_tag = shape_tag.clone();

				// getting coordinates arrow pointer
				int [] arrow_pointer_temp = getArrowPointerCoordinates(temp_arrow_tag);
				int arrow_pointer_x = arrow_pointer_temp[0];
				int arrow_pointer_y = arrow_pointer_temp[1];

				// Get arrow shape off and ext
				float[] arrowCoordinatesTemp = arrowCoordinates(temp_arrow_tag);
				float arrow_off_x = arrowCoordinatesTemp[0];
				float arrow_off_y = arrowCoordinatesTemp[1];
				float arrow_ext_cx = arrowCoordinatesTemp[2];
				float arrow_ext_cy = arrowCoordinatesTemp[3];

				// Get middle point of arrow
				float arrow_center_x = (arrow_off_x+arrow_ext_cx/2);
				float arrow_center_y = (arrow_off_y+arrow_ext_cy/2);

				// TailX and TailY contains the offset(relative distance from the centre and not the absolute)
				float TailX = arrow_ext_cx * (float)( (float)arrow_pointer_x / 100000.0 );
				float TailY = arrow_ext_cy * (float)( (float)arrow_pointer_y / 100000.0 );

				float[] tempCoordinates = coordinateTransformation(TailX, TailY, Float.parseFloat(slide_dimen_x), Float.parseFloat(slide_dimen_y), 0, 0, 1, 1, 0 );
				TailX = tempCoordinates[0];
				TailY = tempCoordinates[1];

				// Scaling centre coordinates of call out values to 0...1
				tempCoordinates = coordinateTransformation(arrow_center_x, arrow_center_y, Float.parseFloat(slide_dimen_x), Float.parseFloat(slide_dimen_y), 0, 0, 1, 1, 0);
				float arrow_center_x_small = tempCoordinates[0];
				float arrow_center_y_small = tempCoordinates[1];

				// Adding text to arrow shape -
				String trackName = track.get("name").toString();

				// trimming the trackname -
				trackName = trackName.substring(0, 4);
				temp_arrow_tag.selectFirst("p|txBody").selectFirst("a|p").selectFirst("a|r").selectFirst("a|t").text(trackName);

				shape_ids.add(current_shape_id);
				arrow_ids.add(current_arrow_id);

				// Assign ids to arrow shape and path shape
				temp_arrow_tag.selectFirst("p|cNvPr").attr("id", current_arrow_id + "");
				temp_shape_tag.selectFirst("p|cNvPr").attr("id", current_shape_id + "");

				// Get Shape offsets and exts
				int temp_shape_x = Integer.parseInt(temp_shape_tag.selectFirst("a|off").attr("x"));
				int temp_shape_y = Integer.parseInt(temp_shape_tag.selectFirst("a|off").attr("y"));

				String animation_path = "";
				Element path_tag = temp_shape_tag.selectFirst("a|path");
				for ( Element child : path_tag.children() ){
					child.remove();
				}

				// Adding coordinates (ArrayList< HashMap<String, Object> >)
				ArrayList< ? > coordinates_detail = (ArrayList<?>) track.get("coordinates");
				ArrayList< ArrayList<String> > coordinates = new ArrayList<>();

				for ( Object coordinate_detail_object : coordinates_detail ){
					HashMap<String, Object> coordinate_detail = (HashMap<String, Object>) coordinate_detail_object;
					coordinates.add((ArrayList<String>) coordinate_detail.get("coor_set"));
				}

				int num_coordinate = 0;

				// multiple anim per tracks
				int coord_count = 1;

				String first_x = coordinates.get(0).get(0), first_y = coordinates.get(0).get(1);
				tempCoordinates = coordinateTransformation(Float.parseFloat(first_x), Float.parseFloat(first_y), (float)dimensionWidth, (float)dimensionHeight, animX, animY, animCX, animCY, 1);
				float prev_anim_x = tempCoordinates[0], prev_anim_y = tempCoordinates[1];
				prev_anim_x = prev_anim_x - TailX - arrow_center_x_small;
				prev_anim_y = prev_anim_y - TailY - arrow_center_y_small;

				ArrayList<Element> track_anim_objs = new ArrayList<>();

				for ( ArrayList<String> coordinate : coordinates ){
					String x = coordinate.get(0), y = coordinate.get(1);

					Element temp_anim_tag = anim_tag.clone();
					tempCoordinates = coordinateTransformation(Float.parseFloat(x), Float.parseFloat(y), (float)dimensionWidth, (float)dimensionHeight, animX, animY, animCX, animCY, 1);
					float anim_x = tempCoordinates[0], anim_y = tempCoordinates[1];
					anim_x = anim_x - TailX - arrow_center_x_small;
					anim_y = anim_y - TailY - arrow_center_y_small;

					animation_path = "M " + prev_anim_x + " " + prev_anim_y + " L " + anim_x + " " + anim_y;
					prev_anim_x = anim_x;
					prev_anim_y = anim_y;

					temp_anim_tag.attr("path", animation_path);
					temp_anim_tag.selectFirst("p|spTgt").attr("spid", current_arrow_id + "");
					temp_anim_tag.selectFirst("p|cTn").attr("id", Integer.parseInt(temp_anim_tag.selectFirst("p|cTn").attr("id")) + trackCount + coord_count + "");
					temp_anim_tag.selectFirst("p|cTn").attr("dur", intervalDuration + "");
					track_anim_objs.add(temp_anim_tag);
					coord_count++;

					int x_int = Math.round(Float.parseFloat(x));
					int y_int = Math.round(Float.parseFloat(y));

					int[] tempCoordinatesInt = coordinateTransformation( x_int, y_int, dimensionWidth, dimensionHeight, mapX, mapY, mapCX, mapCY, 1);
					x_int = tempCoordinatesInt[0]; y_int = tempCoordinatesInt[1];

					// remove the offsets for the track object
					x_int = x_int - temp_shape_x;
					y_int = y_int - temp_shape_y;

					x = x_int + "";
					y = y_int + "";

					Element coordinate_soup = Jsoup.parse("<a:pt x='" + x + "' y='" + y + "'/>", "", Parser.xmlParser());
					if ( num_coordinate == 0 ){
						coordinate_soup.tagName("a:moveTo");
					}else{
						coordinate_soup.tagName("a:lnTo");
					}
					path_tag.insertChildren(path_tag.childNodeSize(), coordinate_soup);
					num_coordinate++;
				}

				all_animation_objs.add(track_anim_objs);
				// Adding color to the track
				String colorHexValue = getColorinHex(track).toUpperCase();
				temp_shape_tag.selectFirst("a|srgbClr").attr("val", colorHexValue);

				// changing arrow to rect callout -
				temp_arrow_tag.selectFirst("a|prstGeom").attr("prst", "wedgeRectCallout");

				// Adding border color to marker
				temp_arrow_tag.selectFirst("p|spPr").selectFirst("a|ln").selectFirst("a|solidFill").selectFirst("a|srgbClr").attr("val", colorHexValue);

				// We will add the shape and arrow objects in arrays for now
				shape_objs.add(temp_shape_tag);
				arrow_objs.add(temp_arrow_tag);

				if ( trackCount == 0 ){
					current_shape_id = 500;
					current_arrow_id = 600;
				}
				current_shape_id++;
				current_arrow_id++;
				trackCount++;
			}

			// Adding all shape and arrow objects
			Element spTreeobj = soup.selectFirst("p|spTree");
			addShapeMarkerObjects(spTreeobj, shape_objs, arrow_objs);
			addAnimationObjects(all_animation_objs, anim_tag_upper, anim_insertion_tag_upper);
			createTimeNarrativeShapes(spTreeobj, intervalDuration, trackData, time_tag, time_anim_tag_first, anim_insertion_tag_upper, time_anim_tag_big, time_anim_tag_big_insertion, narrativeEntries, narrative_tag);
			writeSoup(slide_path, soup);
			new PackFunction().packFunction(null, temp_unpack_path);
		} catch (IOException e) {
			System.out.println("Corrupted xml file " + slide_path);
			System.exit(1);
		}
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
