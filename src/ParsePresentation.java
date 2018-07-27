import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class ParsePresentation
{
  public String[] retrieveDimensions(final String unpack_path)
  {
    try
    {
      final byte[] encoded = Files.readAllBytes(Paths.get(unpack_path
          + "/ppt/presentation.xml"));

      final Document soup = Jsoup.parse(new String(encoded), "", Parser
          .xmlParser());
      final Element slide_size_tag = soup.select("p|sldSz").get(0);

      return new String[]
      {slide_size_tag.attr("cx"), slide_size_tag.attr("cy")};
    }
    catch (final IOException e)
    {
      System.out.println("Corrupted xml file " + unpack_path);
      System.exit(1);
    }
    // It will never be reached.
    return null;
  }
}
