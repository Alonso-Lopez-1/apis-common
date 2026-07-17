package jp.co.sony.csl.dcoes.apis.common.util.logback;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;


public class JulLevelConverterTest {

	private final LoggerContext lc = new LoggerContext();
	private final Logger logger = lc.getLogger("test");

	private String render_(Level level) {
		ILoggingEvent event = new LoggingEvent("fqcn", logger, level, "msg", null, null);
		return new JulLevelConverter().convert(event);
	}

	@Test public void mapsAllLevels() {
		assertEquals("SEVERE", render_(Level.ERROR));
		assertEquals("WARNING", render_(Level.WARN));
		assertEquals("INFO", render_(Level.INFO));
		assertEquals("FINE", render_(Level.DEBUG));
		assertEquals("FINEST", render_(Level.TRACE));
	}

	@Test public void renderedNamesAreParseableByJul() {
		// This is the property that matters downstream: MongoDbWriter calls Level.parse().
		for (Level level : new Level[] { Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE }) {
			String julName = render_(level);
			java.util.logging.Level parsed = java.util.logging.Level.parse(julName);
			assertEquals(julName, parsed.getName());
		}
	}

}
