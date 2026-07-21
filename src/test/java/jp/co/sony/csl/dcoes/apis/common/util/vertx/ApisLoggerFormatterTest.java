package jp.co.sony.csl.dcoes.apis.common.util.vertx;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import jp.co.sony.csl.dcoes.apis.common.util.logback.ApisLogPrefixConverter;
import jp.co.sony.csl.dcoes.apis.common.util.logback.JulLevelConverter;

// Reconstruct the legacy output traits using the new Logback converters.
@RunWith(VertxUnitRunner.class)
public class ApisLoggerFormatterTest {

	private final LoggerContext lc = new LoggerContext();

	private String render_(String loggerName, String message) {
		Logger logger = lc.getLogger(loggerName);
		ILoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, message, null, null);
		String prefix = new ApisLogPrefixConverter().convert(event);
		String level = new JulLevelConverter().convert(event);
		// Keep validating the same output traits as legacy formatter: prefix + level + logger + message.
		return prefix + "1970-01-01T00:00:00.000+00:00 " + level + " " + event.getLoggerName() + " : " + event.getFormattedMessage();
	}

	public ApisLoggerFormatterTest() {
		super();
	}

	@Test public void nullProgramId(TestContext context) {
		VertxConfig.config.setJsonObject(null);
		String result = render_("testLogger", "test message");
		// Format: [[[programId]]] timestamp LEVEL loggerName : message
		context.assertTrue(result.startsWith("[[[]]]"), "Should start with [[[]]]");
		context.assertTrue(result.contains("INFO"), "Should contain INFO level");
		context.assertTrue(result.contains("testLogger"), "Should contain logger name");
		context.assertTrue(result.contains("test message"), "Should contain message");
	}

	@Test public void emptyProgramId(TestContext context) {
		VertxConfig.config.setJsonObject(new JsonObject("{\"programId\":\"\"}"));
		String result = render_("testLogger", "test message");
		// Format: [[[programId]]] timestamp LEVEL loggerName : message
		context.assertTrue(result.startsWith("[[[]]]"), "Should start with [[[]]]");
		context.assertTrue(result.contains("INFO"), "Should contain INFO level");
		context.assertTrue(result.contains("testLogger"), "Should contain logger name");
		context.assertTrue(result.contains("test message"), "Should contain message");
	}

	@Test public void normalProgramId(TestContext context) {
		VertxConfig.config.setJsonObject(new JsonObject("{\"programId\":\"apis-log\"}"));
		String result = render_("testLogger", "test message");
		// Format: [[[programId]]] timestamp LEVEL loggerName : message
		context.assertTrue(result.startsWith("[[[apis-log]]]"), "Should start with [[[apis-log]]]");
		context.assertTrue(result.contains("INFO"), "Should contain INFO level");
		context.assertTrue(result.contains("testLogger"), "Should contain logger name");
		context.assertTrue(result.contains("test message"), "Should contain message");
	}

}
