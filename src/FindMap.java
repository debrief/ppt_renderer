import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class FindMap {
	
	/**
	 * Slides_path contains path to all the slide.xml files
	 * @param slides_base_path slide's path
	 * @return list of the slides` paths
	 */
	private ArrayList<String> getSlides(String slides_base_path) {
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
	private HashMap<String, Object> checkForMap(ArrayList<String> slides_path) {
		HashMap<String, Object> mapDetails = new HashMap<>();
		
		for (String slidePath : slides_path) {
			int flag = 0;
			
			try {
				byte[] encoded = Files.readAllBytes(Paths.get(slidePath));
				Document soup = Jsoup.parse(new String(encoded), "", Parser.xmlParser());

				Elements shapes = soup.select("p|sp");
				String cnvpr = "p|cNvPr";
				
				// TODO Confirm if it is needed to try reparsing for lxml.
				/*
				if(not shapes):
		            soup = BeautifulSoup(open(slidePath, 'r').read(), 'lxml-xml')
		            # print "soup","\n\n\n", soup,"\n\n"
		            shapes = soup.find_all("p:sp")
		            cnvpr = "p:cNvPr"
				 */
				
				for (Element shape : shapes) {
					HashMap<String, String> shapeDetails = new HashMap<>();
					shapeDetails.put("name", shape.select(cnvpr).attr("name"));
					if ( "map".equals(shapeDetails.get("name")) ) {
						
					}
				}
				
			} catch (IOException e) {
				System.out.println("Corrupted xml file " + slidePath);
				System.exit(1);
			}
		}
		
		return mapDetails;
	}
	
	public void getMapDetails(String unpack_path) {
		String slides_base_path = unpack_path + "/ppt/slides";
		ArrayList<String> slides_path = getSlides(slides_base_path);
		
	}
}
