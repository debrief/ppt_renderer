

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import model.NarrativeEntry;
import model.Track;
import model.TrackData;
import model.TrackPoint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrackParser {
	private static final TrackParser instance = new TrackParser();

	private TrackParser() {

	}

	public static TrackParser getInstance() {
		return instance;
	}

	/**
	 * It parses the given xml and returns a Track instance of it
	 *
	 * @param xml Track File as String
	 * @return TrackData instance
	 */
	public TrackData parse(String xml) {
		TrackData trackData = new TrackData();

		Document soup = Jsoup.parse(xml, "", Parser.xmlParser());
		parseBasicInfo(trackData, soup);
		parseNarratives(trackData, soup);
		parseTracks(trackData, soup);

		return trackData;
	}

	/**
	 * Given a Soup Document and a Track, it retrieves the the narratives
	 * @param trackData TrackData instance where we are going to insert the info
	 * @param soup Soup file
	 */
	private void parseTracks(TrackData trackData, Document soup) {
		Elements tracks = soup.select("trk");
		for ( Element track : tracks ) {
			Track currentTrack = new Track();
			currentTrack.setName(track.selectFirst("name").text());
			currentTrack.setColor(track.selectFirst("color").text());

			for ( Element coordinate : track.select("trkpt") ) {
				TrackPoint point = new TrackPoint();
				point.setLongitude(Float.parseFloat(coordinate.attr("lon")));
				point.setLatitude(Float.parseFloat(coordinate.attr("lat")));

				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
				LocalDateTime dateTime = LocalDateTime.from(dateTimeFormatter.parse(coordinate.selectFirst("time").text()));
				point.setTime(dateTime);

				point.setCourse(Float.parseFloat(coordinate.selectFirst("course").text()));
				point.setElevation(Float.parseFloat(coordinate.selectFirst("ele").text()));
				point.setSpeed(Float.parseFloat(coordinate.selectFirst("speed").text()));

				currentTrack.getSegments().add(point);
			}
			trackData.getTracks().add(currentTrack);
		}
	}

	/**
	 * Given a Soup Document and a Track, it retrieves the the narratives
	 * @param trackData TrackData instance where we are going to insert the info
	 * @param soup Soup file
	 */
	private void parseNarratives(TrackData trackData, Document soup) {
		Elements narrativeEntries = soup.select("NarrativeEntries");

		if (!narrativeEntries.isEmpty()) {
			Elements entries = narrativeEntries.select("Entry");

			for (Element entry : entries) {
				NarrativeEntry entryInstance = new NarrativeEntry();
				entryInstance.setText(entry.attr("Text"));
				entryInstance.setDate(entry.attr("dateStr"));
				entryInstance.setElapsed(entry.attr("elapsed"));
				trackData.getNarrativeEntries().add(entryInstance);
			}
		}
	}

	/**
	 * Given a Soup Document and a Track, it retrieves the the dimensions, interval and name
	 * @param trackData TrackData instance where we are going to insert the info
	 * @param soup Soup file
	 */
	private void parseBasicInfo(TrackData trackData, Document soup) {
		// We get the dimensions
		Element dimensions = soup.selectFirst("dimensions");
		trackData.setWidth(Integer.parseInt(dimensions.attr("width")));
		trackData.setHeight(Integer.parseInt(dimensions.attr("height")));

		// We get the intervals
		Elements interval = soup.select("interval");
		if (interval.isEmpty()) {
			trackData.setIntervals(100);
		} else {
			trackData.setIntervals(Integer.parseInt(interval.get(0).attr("millis")));
		}

		Elements name = soup.select("name");
		if (!name.isEmpty()) {
			trackData.setName(name.get(0).text());
		}
	}
}
