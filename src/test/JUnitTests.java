package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.debrief.DebriefException;
import com.debrief.PlotTracks;
import com.debrief.TrackParser;
import com.debrief.model.TrackData;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

class JUnitTests
{

  final private String[] donorFiles = new String[]
  {"donor.pptx", "designed.pptx"};
  final private String[] trackFiles = new String[]
  {"long_tracks.txt", "multi_tracks.txt", "scenario_long_range.txt",
      "scenario_short_range.txt", "speed_change.txt"};
  final private String trackFolder = "track_data";
  final private String resultsFolder = "expected_test_results";
  final private String slide1Path = "ppt" + File.separator + "slides"
      + File.separator + "slide1.xml";

  @Test
  void IntegrationTests() throws IOException, ZipException, DebriefException
  {
    for (String donor : donorFiles)
    {
      for (String track : trackFiles)
      {
        System.out.println("Testing donor file " + donor + " with track file "
            + trackFolder + "/" + track);

        final byte[] encoded = Files.readAllBytes(Paths.get(trackFolder
            + File.separator + track));
        final String trackXml = new String(encoded);

        final TrackData trackData = TrackParser.getInstance().parse(trackXml);

        final PlotTracks plotter = new PlotTracks();

        String pptxGenerated = plotter.export(trackData, donor);

        String cleanTrackName = track.substring(0, track.lastIndexOf('.'));
        String expectedPptx = resultsFolder + File.separator + donor.substring(
            0, donor.lastIndexOf('.')) + File.separator + cleanTrackName
            + File.separator + cleanTrackName + "_temp.pptx";

        final File generatedPptxTemporaryFolder = new File(pptxGenerated
            .substring(0, pptxGenerated.lastIndexOf('.')) + "generated");
        if (!generatedPptxTemporaryFolder.exists())
        {
          generatedPptxTemporaryFolder.mkdir();
        }
        final File expectedPptxTemporaryFolder = new File(expectedPptx
            .substring(0, expectedPptx.lastIndexOf('.')) + "expected");
        if (!expectedPptxTemporaryFolder.exists())
        {
          expectedPptxTemporaryFolder.mkdir();
        }

        final ZipFile generatedPptxZip = new ZipFile(pptxGenerated);
        generatedPptxZip.extractAll(generatedPptxTemporaryFolder
            .getAbsolutePath());
        final ZipFile expectedPptxZip = new ZipFile(expectedPptx);
        expectedPptxZip.extractAll(expectedPptxTemporaryFolder
            .getAbsolutePath());

        // We compare the file structure first

        Assert.assertTrue("Directories Structures are diferent",
            compareDirectoriesStructures(generatedPptxTemporaryFolder,
                expectedPptxTemporaryFolder));

        // Now we compare the slide1.xml structure
        String expectedXml = expectedPptxTemporaryFolder.getAbsolutePath()
            + File.separator + slide1Path;
        String generatedXml = generatedPptxTemporaryFolder.getAbsolutePath()
            + File.separator + slide1Path;

        Diff delta = DiffBuilder.compare(Input.fromFile(expectedXml)).withTest(
            Input.fromFile(generatedXml)).build();
        Iterator<Difference> iter = delta.getDifferences().iterator();
        int size = 0;
        while (iter.hasNext()) {
            String diff = iter.next().toString();
            System.out.println(diff);
            size++;
        }
        System.out.println(size);

        FileUtils.deleteDirectory(generatedPptxTemporaryFolder);
        FileUtils.deleteDirectory(expectedPptxTemporaryFolder);
        new File(pptxGenerated).delete();
        System.out.println("Success!!");
      }
    }
  }

  private boolean compareDirectoriesStructures(
      File generatedPptxTemporaryFolder, File expectedPptxTemporaryFolder)
  {
    HashSet<String> gen = new HashSet<>();
    for (File genFile : FileUtils.listFiles(generatedPptxTemporaryFolder, null,
        true))
    {
      gen.add(genFile.getAbsolutePath().substring(generatedPptxTemporaryFolder
          .getAbsolutePath().length()));
    }
    HashSet<String> exp = new HashSet<>();
    for (File expFile : FileUtils.listFiles(expectedPptxTemporaryFolder, null,
        true))
    {
      exp.add(expFile.getAbsolutePath().substring(expectedPptxTemporaryFolder
          .getAbsolutePath().length()));
    }

    return exp.containsAll(gen) && gen.containsAll(exp);
  }

}
