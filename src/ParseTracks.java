import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

@Deprecated
public class ParseTracks
{
  /**
   * Retrieves the interval from the xml track file
   * 
   * @param trackPath
   *          Path of the track file
   * @return Interval or 100 if it doesn't exist.
   */
  public int getInterval(final String trackPath)
  {
    try
    {
      final byte[] encoded = Files.readAllBytes(Paths.get(trackPath));

      final Document soup = Jsoup.parse(new String(encoded), "", Parser
          .xmlParser());

      final Elements interval = soup.select("interval");
      if (interval.isEmpty())
      {
        return 100;
      }
      else
      {
        return Integer.parseInt(interval.get(0).attr("millis"));
      }
    }
    catch (final IOException e)
    {
      // XML file looks corrupted, but lets return the default value
      return 100;
    }
  }

  /**
   * It extracts the narratives from the xml track file.
   * 
   * @param trackPath
   *          Path of the track file
   * @return HashMap with the narrative structure.
   */
  public ArrayList<HashMap<String, Object>> getNarratives(
      final String trackPath)
  {
    try
    {
      final byte[] encoded = Files.readAllBytes(Paths.get(trackPath));

      final Document soup = Jsoup.parse(new String(encoded), "", Parser
          .xmlParser());

      final Elements narrativeEntries = soup.select("NarrativeEntries");

      if (!narrativeEntries.isEmpty())
      {
        final Elements entries = narrativeEntries.select("Entry");
        final ArrayList<HashMap<String, Object>> narratives = new ArrayList<>();

        for (final Element entry : entries)
        {
          final HashMap<String, Object> narrative = new HashMap<>();
          narrative.put("Text", entry.attr("Text"));
          narrative.put("dateStr", entry.attr("dateStr"));
          narrative.put("elapsed", entry.attr("elapsed"));
          narratives.add(narrative);
        }
        return narratives;
      }
      else
      {
        return new ArrayList<>();
      }
    }
    catch (final IOException e)
    {
      System.out.println("Corrupted xml file " + trackPath);
      System.exit(1);
    }
    // It will never be reached.
    return null;
  }

  /**
   * It parses the track data file and return it as a map.
   * 
   * @param trackPath
   *          Path of the track file
   * @return Map with the information of the track
   */
  public HashMap<String, Object> getTrackData(final String trackPath)
  {
    try
    {
      final byte[] encoded = Files.readAllBytes(Paths.get(trackPath));

      final Document soup = Jsoup.parse(new String(encoded), "", Parser
          .xmlParser());
      final Element dimensions = soup.select("dimensions").get(0);
      final String dimensionWidth = dimensions.attr("width");
      final String dimensionHeight = dimensions.attr("height");

      final Elements tracks = soup.select("trk");

      final ArrayList<HashMap<String, Object>> trackData = new ArrayList<>();
      for (final Element track : tracks)
      {
        final HashMap<String, Object> trackDetails = new HashMap<>();
        trackDetails.put("name", track.select("name").get(0).text());
        trackDetails.put("color", track.select("color").get(0).text());

        final ArrayList<HashMap<String, Object>> coordinates =
            new ArrayList<>();
        trackDetails.put("coordinates", coordinates);

        for (final Element coordinate : track.select("trkpt"))
        {
          final HashMap<String, Object> coordinateDetails = new HashMap<>();
          final ArrayList<String> coor_set = new ArrayList<>();
          coor_set.add(coordinate.attr("lon"));
          coor_set.add(coordinate.attr("lat"));
          coordinateDetails.put("coor_set", coor_set);

          final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
              .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
          final LocalDateTime dateTime = LocalDateTime.from(dateTimeFormatter
              .parse(coordinate.select("time").get(0).text()));
          coordinateDetails.put("time", dateTime);

          coordinates.add(coordinateDetails);
        }

        trackData.add(trackDetails);
      }

      final HashMap<String, Object> answer = new HashMap<>();
      answer.put("trackData", trackData);
      answer.put("dimensionWidth", dimensionWidth);
      answer.put("dimensionHeight", dimensionHeight);
      return answer;

    }
    catch (final IOException e)
    {
      System.out.println("Corrupted xml file " + trackPath);
      System.exit(1);
    }
    // It will never be reached.
    return null;
  }
}
/*
 * ''' Example of trackData returned - [{'color': u'java.awt.Color[r=0,g=100,b=189]', 'name':
 * u'COLLINGWOOD', 'coordinates': [{'coor_set': ('435.0', '340.0'), 'time':
 * u'1995-12-12T10:25:31Z'}, {'coor_set': ('423.0', '343.0'), 'time': u'1995-12-12T10:26:31Z'},
 * {'coor_set': ('410.0', '346.0'), 'time': u'1995-12-12T10:27:31Z'}, {'coor_set': ('397.0',
 * '349.0'), 'time': u'1995-12-12T10:28:31Z'}, {'coor_set': ('385.0', '353.0'), 'time':
 * u'1995-12-12T10:29:31Z'}, {'coor_set': ('372.0', '355.0'), 'time': u'1995-12-12T10:30:31Z'},
 * {'coor_set': ('360.0', '352.0'), 'time': u'1995-12-12T10:31:31Z'}, {'coor_set': ('352.0',
 * '342.0'), 'time': u'1995-12-12T10:32:31Z'}, {'coor_set': ('347.0', '328.0'), 'time':
 * u'1995-12-12T10:33:31Z'}, {'coor_set': ('345.0', '312.0'), 'time': u'1995-12-12T10:34:31Z'},
 * {'coor_set': ('341.0', '296.0'), 'time': u'1995-12-12T10:35:31Z'}, {'coor_set': ('337.0',
 * '281.0'), 'time': u'1995-12-12T10:36:31Z'}, {'coor_set': ('328.0', '271.0'), 'time':
 * u'1995-12-12T10:37:31Z'}, {'coor_set': ('318.0', '268.0'), 'time': u'1995-12-12T10:38:31Z'},
 * {'coor_set': ('308.0', '273.0'), 'time': u'1995-12-12T10:39:31Z'}, {'coor_set': ('302.0',
 * '284.0'), 'time': u'1995-12-12T10:40:31Z'}, {'coor_set': ('298.0', '298.0'), 'time':
 * u'1995-12-12T10:41:31Z'}, {'coor_set': ('294.0', '310.0'), 'time': u'1995-12-12T10:42:31Z'},
 * {'coor_set': ('285.0', '316.0'), 'time': u'1995-12-12T10:43:31Z'}, {'coor_set': ('274.0',
 * '315.0'), 'time': u'1995-12-12T10:44:31Z'}, {'coor_set': ('266.0', '305.0'), 'time':
 * u'1995-12-12T10:45:31Z'}, {'coor_set': ('265.0', '289.0'), 'time': u'1995-12-12T10:46:31Z'},
 * {'coor_set': ('273.0', '276.0'), 'time': u'1995-12-12T10:47:31Z'}, {'coor_set': ('286.0',
 * '273.0'), 'time': u'1995-12-12T10:48:31Z'}, {'coor_set': ('296.0', '283.0'), 'time':
 * u'1995-12-12T10:49:31Z'}, {'coor_set': ('298.0', '300.0'), 'time': u'1995-12-12T10:50:31Z'},
 * {'coor_set': ('298.0', '322.0'), 'time': u'1995-12-12T10:51:31Z'}, {'coor_set': ('308.0',
 * '333.0'), 'time': u'1995-12-12T10:52:31Z'}, {'coor_set': ('316.0', '322.0'), 'time':
 * u'1995-12-12T10:53:31Z'}, {'coor_set': ('314.0', '301.0'), 'time': u'1995-12-12T10:54:31Z'},
 * {'coor_set': ('301.0', '293.0'), 'time': u'1995-12-12T10:55:31Z'}, {'coor_set': ('283.0',
 * '299.0'), 'time': u'1995-12-12T10:56:31Z'}, {'coor_set': ('278.0', '319.0'), 'time':
 * u'1995-12-12T10:57:31Z'}, {'coor_set': ('291.0', '331.0'), 'time': u'1995-12-12T10:58:31Z'},
 * {'coor_set': ('312.0', '330.0'), 'time': u'1995-12-12T10:59:31Z'}, {'coor_set': ('329.0',
 * '337.0'), 'time': u'1995-12-12T11:00:31Z'}, {'coor_set': ('328.0', '354.0'), 'time':
 * u'1995-12-12T11:01:31Z'}, {'coor_set': ('312.0', '360.0'), 'time': u'1995-12-12T11:02:31Z'},
 * {'coor_set': ('292.0', '363.0'), 'time': u'1995-12-12T11:03:31Z'}, {'coor_set': ('287.0',
 * '378.0'), 'time': u'1995-12-12T11:04:31Z'}, {'coor_set': ('297.0', '383.0'), 'time':
 * u'1995-12-12T11:05:31Z'}, {'coor_set': ('314.0', '379.0'), 'time': u'1995-12-12T11:06:31Z'},
 * {'coor_set': ('326.0', '392.0'), 'time': u'1995-12-12T11:07:31Z'}, {'coor_set': ('321.0',
 * '412.0'), 'time': u'1995-12-12T11:08:31Z'}, {'coor_set': ('304.0', '420.0'), 'time':
 * u'1995-12-12T11:09:31Z'}, {'coor_set': ('294.0', '437.0'), 'time': u'1995-12-12T11:10:31Z'},
 * {'coor_set': ('302.0', '451.0'), 'time': u'1995-12-12T11:11:31Z'}, {'coor_set': ('313.0',
 * '441.0'), 'time': u'1995-12-12T11:12:31Z'}, {'coor_set': ('312.0', '420.0'), 'time':
 * u'1995-12-12T11:13:31Z'}, {'coor_set': ('318.0', '402.0'), 'time': u'1995-12-12T11:14:31Z'},
 * {'coor_set': ('332.0', '385.0'), 'time': u'1995-12-12T11:15:31Z'}, {'coor_set': ('347.0',
 * '381.0'), 'time': u'1995-12-12T11:16:31Z'}, {'coor_set': ('354.0', '393.0'), 'time':
 * u'1995-12-12T11:17:31Z'}, {'coor_set': ('349.0', '407.0'), 'time': u'1995-12-12T11:18:31Z'},
 * {'coor_set': ('338.0', '420.0'), 'time': u'1995-12-12T11:19:31Z'}, {'coor_set': ('326.0',
 * '434.0'), 'time': u'1995-12-12T11:20:31Z'}, {'coor_set': ('313.0', '449.0'), 'time':
 * u'1995-12-12T11:21:31Z'}, {'coor_set': ('302.0', '462.0'), 'time': u'1995-12-12T11:22:31Z'},
 * {'coor_set': ('292.0', '474.0'), 'time': u'1995-12-12T11:23:31Z'}, {'coor_set': ('282.0',
 * '483.0'), 'time': u'1995-12-12T11:24:31Z'}, {'coor_set': ('271.0', '489.0'), 'time':
 * u'1995-12-12T11:25:31Z'}, {'coor_set': ('261.0', '494.0'), 'time': u'1995-12-12T11:26:31Z'},
 * {'coor_set': ('252.0', '499.0'), 'time': u'1995-12-12T11:27:31Z'}, {'coor_set': ('242.0',
 * '503.0'), 'time': u'1995-12-12T11:28:31Z'}, {'coor_set': ('233.0', '508.0'), 'time':
 * u'1995-12-12T11:29:31Z'}, {'coor_set': ('224.0', '512.0'), 'time': u'1995-12-12T11:30:31Z'},
 * {'coor_set': ('215.0', '517.0'), 'time': u'1995-12-12T11:31:31Z'}, {'coor_set': ('206.0',
 * '521.0'), 'time': u'1995-12-12T11:32:31Z'}, {'coor_set': ('197.0', '526.0'), 'time':
 * u'1995-12-12T11:33:31Z'}, {'coor_set': ('188.0', '530.0'), 'time': u'1995-12-12T11:34:31Z'},
 * {'coor_set': ('180.0', '535.0'), 'time': u'1995-12-12T11:35:31Z'}, {'coor_set': ('171.0',
 * '539.0'), 'time': u'1995-12-12T11:36:31Z'}, {'coor_set': ('162.0', '543.0'), 'time':
 * u'1995-12-12T11:37:31Z'}]}, {'color': u'java.awt.Color[r=224,g=28,b=62]', 'name': u'NELSON',
 * 'coordinates': [{'coor_set': ('289.0', '202.0'), 'time': u'1995-12-12T10:25:31Z'}, {'coor_set':
 * ('285.0', '172.0'), 'time': u'1995-12-12T10:26:31Z'}, {'coor_set': ('281.0', '141.0'), 'time':
 * u'1995-12-12T10:27:31Z'}, {'coor_set': ('277.0', '110.0'), 'time': u'1995-12-12T10:28:31Z'},
 * {'coor_set': ('271.0', '81.0'), 'time': u'1995-12-12T10:29:31Z'}, {'coor_set': ('254.0', '75.0'),
 * 'time': u'1995-12-12T10:30:31Z'}, {'coor_set': ('243.0', '93.0'), 'time':
 * u'1995-12-12T10:31:31Z'}, {'coor_set': ('234.0', '116.0'), 'time': u'1995-12-12T10:32:31Z'},
 * {'coor_set': ('224.0', '140.0'), 'time': u'1995-12-12T10:33:31Z'}, {'coor_set': ('213.0',
 * '165.0'), 'time': u'1995-12-12T10:34:31Z'}, {'coor_set': ('210.0', '194.0'), 'time':
 * u'1995-12-12T10:35:31Z'}, {'coor_set': ('221.0', '216.0'), 'time': u'1995-12-12T10:36:31Z'},
 * {'coor_set': ('237.0', '228.0'), 'time': u'1995-12-12T10:37:31Z'}, {'coor_set': ('250.0',
 * '239.0'), 'time': u'1995-12-12T10:38:31Z'}, {'coor_set': ('261.0', '247.0'), 'time':
 * u'1995-12-12T10:39:31Z'}, {'coor_set': ('268.0', '260.0'), 'time': u'1995-12-12T10:40:31Z'},
 * {'coor_set': ('274.0', '273.0'), 'time': u'1995-12-12T10:41:31Z'}, {'coor_set': ('280.0',
 * '286.0'), 'time': u'1995-12-12T10:42:31Z'}, {'coor_set': ('285.0', '299.0'), 'time':
 * u'1995-12-12T10:43:31Z'}, {'coor_set': ('283.0', '312.0'), 'time': u'1995-12-12T10:44:31Z'},
 * {'coor_set': ('275.0', '317.0'), 'time': u'1995-12-12T10:45:31Z'}, {'coor_set': ('268.0',
 * '311.0'), 'time': u'1995-12-12T10:46:31Z'}, {'coor_set': ('266.0', '300.0'), 'time':
 * u'1995-12-12T10:47:31Z'}, {'coor_set': ('269.0', '288.0'), 'time': u'1995-12-12T10:48:31Z'},
 * {'coor_set': ('275.0', '278.0'), 'time': u'1995-12-12T10:49:31Z'}, {'coor_set': ('284.0',
 * '278.0'), 'time': u'1995-12-12T10:50:31Z'}, {'coor_set': ('291.0', '287.0'), 'time':
 * u'1995-12-12T10:51:31Z'}, {'coor_set': ('293.0', '300.0'), 'time': u'1995-12-12T10:52:31Z'},
 * {'coor_set': ('295.0', '313.0'), 'time': u'1995-12-12T10:53:31Z'}, {'coor_set': ('303.0',
 * '322.0'), 'time': u'1995-12-12T10:54:31Z'}, {'coor_set': ('313.0', '317.0'), 'time':
 * u'1995-12-12T10:55:31Z'}, {'coor_set': ('314.0', '300.0'), 'time': u'1995-12-12T10:56:31Z'},
 * {'coor_set': ('303.0', '291.0'), 'time': u'1995-12-12T10:57:31Z'}, {'coor_set': ('290.0',
 * '303.0'), 'time': u'1995-12-12T10:58:31Z'}, {'coor_set': ('292.0', '321.0'), 'time':
 * u'1995-12-12T10:59:31Z'}, {'coor_set': ('302.0', '326.0'), 'time': u'1995-12-12T11:00:31Z'},
 * {'coor_set': ('311.0', '331.0'), 'time': u'1995-12-12T11:01:31Z'}, {'coor_set': ('313.0',
 * '342.0'), 'time': u'1995-12-12T11:02:31Z'}, {'coor_set': ('304.0', '352.0'), 'time':
 * u'1995-12-12T11:03:31Z'}, {'coor_set': ('293.0', '355.0'), 'time': u'1995-12-12T11:04:31Z'},
 * {'coor_set': ('285.0', '367.0'), 'time': u'1995-12-12T11:05:31Z'}, {'coor_set': ('290.0',
 * '377.0'), 'time': u'1995-12-12T11:06:31Z'}, {'coor_set': ('297.0', '381.0'), 'time':
 * u'1995-12-12T11:07:31Z'}, {'coor_set': ('306.0', '387.0'), 'time': u'1995-12-12T11:08:31Z'},
 * {'coor_set': ('309.0', '398.0'), 'time': u'1995-12-12T11:09:31Z'}, {'coor_set': ('305.0',
 * '408.0'), 'time': u'1995-12-12T11:10:31Z'}, {'coor_set': ('298.0', '417.0'), 'time':
 * u'1995-12-12T11:11:31Z'}, {'coor_set': ('296.0', '428.0'), 'time': u'1995-12-12T11:12:31Z'},
 * {'coor_set': ('303.0', '437.0'), 'time': u'1995-12-12T11:13:31Z'}, {'coor_set': ('313.0',
 * '428.0'), 'time': u'1995-12-12T11:14:31Z'}, {'coor_set': ('309.0', '417.0'), 'time':
 * u'1995-12-12T11:15:31Z'}, {'coor_set': ('303.0', '410.0'), 'time': u'1995-12-12T11:16:31Z'},
 * {'coor_set': ('294.0', '406.0'), 'time': u'1995-12-12T11:17:31Z'}, {'coor_set': ('282.0',
 * '409.0'), 'time': u'1995-12-12T11:18:31Z'}, {'coor_set': ('273.0', '424.0'), 'time':
 * u'1995-12-12T11:19:31Z'}, {'coor_set': ('276.0', '444.0'), 'time': u'1995-12-12T11:20:31Z'},
 * {'coor_set': ('285.0', '451.0'), 'time': u'1995-12-12T11:21:31Z'}, {'coor_set': ('295.0',
 * '451.0'), 'time': u'1995-12-12T11:22:31Z'}, {'coor_set': ('304.0', '450.0'), 'time':
 * u'1995-12-12T11:23:31Z'}, {'coor_set': ('316.0', '446.0'), 'time': u'1995-12-12T11:24:31Z'},
 * {'coor_set': ('325.0', '428.0'), 'time': u'1995-12-12T11:25:31Z'}, {'coor_set': ('320.0',
 * '408.0'), 'time': u'1995-12-12T11:26:31Z'}, {'coor_set': ('304.0', '398.0'), 'time':
 * u'1995-12-12T11:27:31Z'}, {'coor_set': ('286.0', '389.0'), 'time': u'1995-12-12T11:28:31Z'},
 * {'coor_set': ('269.0', '383.0'), 'time': u'1995-12-12T11:29:31Z'}, {'coor_set': ('256.0',
 * '390.0'), 'time': u'1995-12-12T11:30:31Z'}, {'coor_set': ('245.0', '396.0'), 'time':
 * u'1995-12-12T11:31:31Z'}, {'coor_set': ('235.0', '401.0'), 'time': u'1995-12-12T11:32:31Z'},
 * {'coor_set': ('224.0', '406.0'), 'time': u'1995-12-12T11:33:31Z'}, {'coor_set': ('210.0',
 * '413.0'), 'time': u'1995-12-12T11:34:31Z'}, {'coor_set': ('195.0', '421.0'), 'time':
 * u'1995-12-12T11:35:31Z'}, {'coor_set': ('177.0', '430.0'), 'time': u'1995-12-12T11:36:31Z'},
 * {'coor_set': ('158.0', '440.0'), 'time': u'1995-12-12T11:37:31Z'}]}] '''
 */