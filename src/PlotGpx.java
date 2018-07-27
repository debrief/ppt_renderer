import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import model.TrackData;
import net.lingala.zip4j.exception.ZipException;

public class PlotGpx
{

  public static void main(final String[] args)
  {
    final PlotGpx plotGpx = new PlotGpx();
    final Options arguments = new Options();
    arguments.addOption("donor", true, "Path to donor pptx file");
    arguments.addOption("tracks_path", true, "Path to gpx tracks file");

    try
    {
      final CommandLineParser parser = new DefaultParser();
      final CommandLine commandLine = parser.parse(arguments, args);

      if (!commandLine.hasOption("donor") || !commandLine.hasOption(
          "tracks_path"))
      {
        plotGpx.printHelp(arguments);
      }

      final String donor = commandLine.getOptionValue("donor");
      final String tracks_path = commandLine.getOptionValue("tracks_path");

      final byte[] encoded = Files.readAllBytes(Paths.get(tracks_path));
      final String trackXml = new String(encoded);

      final TrackData trackData = TrackParser.getInstance().parse(trackXml);

      final PlotTracks plotter = new PlotTracks();

      plotter.export(trackData, donor);
    }
    catch (final ParseException e)
    {
      plotGpx.printHelp(arguments);
    }
    catch (final ZipException e)
    {
      e.printStackTrace();
    }
    catch (final IOException e)
    {
      e.printStackTrace();
    }
  }

  private void printHelp(final Options arguments)
  {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Script to plot gpx data on pptx", arguments);
    System.exit(1);
  }
}
