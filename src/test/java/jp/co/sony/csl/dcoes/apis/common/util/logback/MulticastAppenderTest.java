package jp.co.sony.csl.dcoes.apis.common.util.logback;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

import org.junit.After;
import org.junit.Assume;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreConstants;

import io.vertx.core.json.JsonObject;
import jp.co.sony.csl.dcoes.apis.common.util.vertx.VertxConfig;


public class MulticastAppenderTest {

	private static final String GROUP = "224.2.2.7";
	private static final int PORT = 18888;

	@After public void tearDown() {
		VertxConfig.config.setJsonObject(null);
	}

	@Test public void doesNotStartWithoutEncoder() {
		MulticastAppender appender = new MulticastAppender();
		appender.setContext(new LoggerContext());
		appender.setName("MULTICAST");
		appender.setGroupAddress(GROUP);
		appender.setPort(PORT);
		appender.start();
		assertFalse("appender must not start without an encoder", appender.isStarted());
	}

	@Test public void appendBeforeStartDoesNotThrow() {
		MulticastAppender appender = new MulticastAppender();
		appender.setContext(new LoggerContext());
		appender.setName("MULTICAST");
		LoggerContext lc = new LoggerContext();
		ILoggingEvent event = new LoggingEvent("fqcn", lc.getLogger("test"), Level.INFO, "hi", null, null);
		// Must be a no-op, not an exception, when the appender never started.
		appender.doAppend(event);
	}


}
