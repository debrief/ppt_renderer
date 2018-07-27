import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class ParsePresentation {
	public String[] retrieveDimensions(String unpack_path) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(unpack_path + "/ppt/presentation.xml"));
			
			Document soup = Jsoup.parse(new String(encoded), "", Parser.xmlParser());
			Element slide_size_tag = soup.select("p|sldSz").get(0);
			
			return new String[] {
					slide_size_tag.attr("cx"),
					slide_size_tag.attr("cy")
			};
		} catch (IOException e) {
			System.out.println("Corrupted xml file " + unpack_path);
			System.exit(1);
		}
		// It will never be reached.
		return null;
	}
}
