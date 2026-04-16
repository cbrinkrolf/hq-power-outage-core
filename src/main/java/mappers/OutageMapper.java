package mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import entities.Outage;

public class OutageMapper {

	private Logger logger = null;

	public OutageMapper(Logger logger) {
		this.logger = logger;
	}

	public List<Outage> parseRawJSON2Outages(String rawJSON) {
		List<Outage> result = new ArrayList<>();
		if (rawJSON == null || rawJSON.isBlank() || rawJSON.isEmpty()) {
			return result;
		}

		String[] pannes = rawJSON.split("\"pannes\":");

		if (pannes.length < 2) {
			return result;
		}

		List<String> matches = this.getMatches(pannes[1].trim());

		for (String match : matches) {
			String[] elements = prepareAndSplitMatch(match);
			if (elements.length < 10) {
				return new ArrayList<>();
			}
			Outage o = this.parseJSON2Outage(elements[0], elements[1], elements[4], elements[4]);
			if (o != null) {
				result.add(o);
			}
		}
		return result;
	}

	private List<String> getMatches(String text) {
		Pattern pattern = Pattern.compile("\\[.?\\d+[^\\[]+\\[[^\\[]+\\]");
		Matcher matcher = pattern.matcher(text.strip());

		List<String> matches = new ArrayList<>();
		while (matcher.find()) {
			matches.add(matcher.group());
		}
		return matches;
	}

	private String[] prepareAndSplitMatch(String match) {
		match = match.replace("[", "");
		match = match.replace("]", "");
		match = match.replace("\"", "");
		return match.split(",");
	}

	private Outage parseJSON2Outage(String customersAffectedString, String startString, String longitudeString,
			String latitudeString) {

		int customersAffected = 0;

		try {
			customersAffected = Integer.parseInt(customersAffectedString.strip());
		} catch (NumberFormatException e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			return null;
		}

		String dateTimeStart = startString.strip();
		LocalDateTime start;
		try {
			start = this.parseDateTime(dateTimeStart);
		} catch (NumberFormatException e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			return null;
		}

		double longitude;
		double latitude;
		try {
			longitude = Double.parseDouble(longitudeString.strip());
			latitude = Double.parseDouble(latitudeString.strip());

			if (longitude > latitude) {
				logger.log(Level.SEVERE, "coordinates seems to be wrong, longitude is greater than latitude");
				return null;
			}

		} catch (NumberFormatException e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			return null;
		}

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		// System.out.println(customersAffected);
		// System.out.println(start.format(dtf));
		// System.out.println(longitude);
		// System.out.println(latitude);

		return new Outage(customersAffected, start, longitude, latitude);
	}

	private LocalDateTime parseDateTime(String dateTime) throws NumberFormatException {
		dateTime = dateTime.replaceAll("\\D", "");

		int yyyy = Integer.parseInt(dateTime.substring(0, 4));
		int MM = Integer.parseInt(dateTime.substring(4, 6));
		int dd = Integer.parseInt(dateTime.substring(6, 8));
		int hh = Integer.parseInt(dateTime.substring(8, 10));
		int mm = Integer.parseInt(dateTime.substring(10, 12));
		int ss = Integer.parseInt(dateTime.substring(12, 14));

		return LocalDate.of(yyyy, MM, dd).atTime(hh, mm, ss);
	}

	public String parseOutages2JSON(List<Outage> outages) {

		StringBuilder builder = new StringBuilder();
		builder.append("{ \"outages\": [");
		int i = 0;
		for (Outage outage : outages) {

			this.appendOutage(outage, builder);
			i++;
			if (outages.size() > i) {
				builder.append(", ");
			}
		}

		builder.append("] }");
		return builder.toString();

	}

	private void appendOutage(Outage outage, StringBuilder builder) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		builder.append("[");
		builder.append("\"" + outage.customersAffected() + "\",");
		builder.append("\"" + outage.start().format(dtf) + "\",");
		builder.append("[\"" + outage.longitude() + "\",");
		builder.append("\"" + outage.latitude() + "\"]");
		builder.append("]");
	}

	public List<Outage> parseJSONOutages2Outages(String jsonOutages) {

		List<Outage> result = new ArrayList<>();

		List<String> matches = this.getMatches(jsonOutages);

		for (String match : matches) {
			String[] elements = prepareAndSplitMatch(match);

			if (elements.length < 4) {
				return new ArrayList<>();
			}
			Outage o = this.parseJSON2Outage(elements[0], elements[1], elements[2], elements[3]);
			if (o != null) {
				result.add(o);
			}
		}

		return result;
	}
}
