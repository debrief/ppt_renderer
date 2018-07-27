import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FindMap {
	
	/**
	 * Slides_path contains path to all the slide.xml files
	 * @param slides_base_path slide's path
	 * @return list of the slides` paths
	 */
	private static ArrayList<String> getSlides(String slides_base_path) {
		ArrayList<String> slides_path = new ArrayList<>();
		for ( File slide : new File(slides_base_path).listFiles() ) {
			if ( slide.getName().endsWith(".xml") ) {
				slides_path.add(slides_base_path + "/" + slide.getName());
			}
		}
		return slides_path;
	}
	
	/**
	 * Now we need to check for every slide if it contains a rectangle named map
	 * @param slides_path slides` path
	 * @return shape of the rectangles (Map details)
	 */
	private static HashMap<String, String> checkForMap(ArrayList<String> slides_path) {
		HashMap<String, String> mapDetails = new HashMap<>();
		
		for (String slidePath : slides_path) {
			int flag = 0;
			
			try {
				byte[] encoded = Files.readAllBytes(Paths.get(slidePath));
				Document soup = Jsoup.parse(new String(encoded), "", Parser.xmlParser());

				Elements shapes = soup.select("p|sp");
				String cnvpr = "p|cNvPr";
				
				for (Element shape : shapes) {
					HashMap<String, String> shapeDetails = new HashMap<>();
					shapeDetails.put("name", shape.select(cnvpr).attr("name"));
					if ( "map".equals(shapeDetails.get("name")) ) {
						shapeDetails.put("x", shape.select("a|off").get(0).attr("x"));
						shapeDetails.put("y", shape.select("a|off").get(0).attr("y"));
						shapeDetails.put("cx", shape.select("a|ext").get(0).attr("cx"));
						shapeDetails.put("cy", shape.select("a|ext").get(0).attr("cy"));
						mapDetails = shapeDetails;
						System.out.println("mapDetails - " + Arrays.toString(mapDetails.entrySet().toArray()));
						flag = 1;
						break;
					}
				}

				if ( flag == 1 ){
					break;
				}
			} catch (IOException e) {
				System.out.println("Corrupted xml file " + slidePath);
				System.exit(1);
			}
		}
		
		return mapDetails;
	}
	
	public static HashMap<String, String> getMapDetails(String unpack_path) {
		String slides_base_path = unpack_path + "/ppt/slides";
		ArrayList<String> slides_path = getSlides(slides_base_path);
		HashMap<String, String> mapDetail = checkForMap(slides_path);
		return mapDetail;
	}
}
