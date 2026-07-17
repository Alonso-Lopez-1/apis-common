package jp.co.sony.csl.dcoes.apis.common.util.logback;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assume;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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

	@Test public void endToEndMulticastIfSupported() throws Exception {
		LoggerContext lc = new LoggerContext();
		PatternLayoutEncoder encoder = buildEncoder_(lc);

		MulticastAppender appender = new MulticastAppender();
		appender.setContext(lc);
		appender.setName("MULTICAST");
		appender.setGroupAddress(GROUP);
		appender.setPort(PORT);
		appender.setEncoder(encoder);
		appender.start();
		Assume.assumeTrue("no multicast-capable network interface; skipping live send", appender.isStarted());

		NetworkInterface ni = firstMulticastInterface_();
		Assume.assumeNotNull(ni);
		MulticastSocket receiver = new MulticastSocket(PORT);
		try {
			receiver.joinGroup(new InetSocketAddress(InetAddress.getByName(GROUP), PORT), ni);
			receiver.setSoTimeout(2000);

			VertxConfig.config.setJsonObject(new JsonObject().put("programId", "apis-main").put("unitId", "E001"));
			Logger logger = lc.getLogger("jp.co.sony.csl.dcoes.apis.main.app.Helo");
			ILoggingEvent event = new LoggingEvent("fqcn", logger, Level.INFO, "hello-multicast", null, null);
			appender.doAppend(event);

			byte[] buf = new byte[8192];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				receiver.receive(packet);
			} catch (SocketTimeoutException e) {
				Assume.assumeNoException("multicast loopback not delivered on this host; skipping", e);
			}
			String line = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).replaceFirst("\\R$", "");
			assertTrue("received: " + line, line.startsWith("[[[apis-main:E001]]] "));
			assertTrue("received: " + line, line.contains("hello-multicast"));
		} finally {
			receiver.close();
			appender.stop();
		}
	}

	private PatternLayoutEncoder buildEncoder_(LoggerContext lc) {
		Map<String, String> rules = new HashMap<>();
		rules.put("apisPrefix", ApisLogPrefixConverter.class.getName());
		rules.put("jullevel", JulLevelConverter.class.getName());
		lc.putObject(CoreConstants.PATTERN_RULE_REGISTRY, rules);

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(lc);
		encoder.setPattern("%apisPrefix[%thread] %d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %jullevel [%logger]  %msg%n");
		encoder.start();
		return encoder;
	}

	private NetworkInterface firstMulticastInterface_() throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface ni = interfaces.nextElement();
			try {
				if (ni.isUp() && !ni.isLoopback() && ni.supportsMulticast()) return ni;
			} catch (SocketException e) {
				// skip
			}
		}
		return null;
	}

}
