package mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.Assertions;

import entities.Outage;

class OutageMapperTest {

	private final Path resourceDirectory = Paths.get("src", "test", "resources");
	private final OutageMapper mapper = new OutageMapper(null);

	@Test
	void test_happyPath_parseRawJSON2Outages() {

		Path filePath = resourceDirectory.resolve("test.json");

		String content = "";

		try {
			content = Files.readString(filePath);
			// System.out.println(content);
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Outage> list = mapper.parseRawJSON2Outages(content);
		assertEquals(24, list.size());
		assertEquals(7, list.get(0).customersAffected());
		assertEquals(13, list.get(0).start().getHour());

	}

	@Test
	void test_EmptyContent_parseRawJSON2Outages() {
		List<Outage> list = mapper.parseRawJSON2Outages("");
		assertEquals(0, list.size());

	}

	@Test
	void test_NullContent_parseRawJSON2Outages() {
		List<Outage> list = mapper.parseRawJSON2Outages(null);
		assertEquals(0, list.size());

	}

	@Test
	void test_BadContent_parseRawJSON2Outages() {
		List<Outage> list = mapper.parseRawJSON2Outages("abc");
		assertEquals(0, list.size());

	}

	@Test
	void mockTest() {

		System.out.println("start");
		// mock creation
		List mockedList = mock(List.class);
		// or even simpler with Mockito 4.10.0+
		// List mockedList = mock();

		// using mock object - it does not throw any "unexpected interaction" exception
		mockedList.add("one");
		mockedList.clear();

		// selective, explicit, highly readable verification
		verify(mockedList).add("one");
		verify(mockedList).clear();
		when(mockedList.size()).thenReturn(5);
		assertEquals(mockedList.size(), 5);
		System.out.println("end");

	}

	@Test
	void testParseOutages2JSONAndBack() {

		LocalDateTime start = LocalDateTime.of(2026, 12, 30, 13, 14, 15);
		Outage o1 = new Outage(1, start, -71.5, 46.5);

		Outage o2 = new Outage(22, start, -71.6, 46.6);
		Outage o3 = new Outage(333, start, -71.7, 46.7);
		List<Outage> list = Arrays.asList(o1, o2, o3);

		String json = mapper.parseOutages2JSON(list);
		List<Outage> importedList = mapper.parseJSONOutages2Outages(json);

		assertEquals(3, importedList.size());
		assertEquals(1, importedList.get(0).customersAffected());
		assertEquals(13, importedList.get(1).start().getHour());
		assertEquals(-71.6, importedList.get(1).longitude());
		assertEquals(46.7, importedList.get(2).latitude());
	}

}
