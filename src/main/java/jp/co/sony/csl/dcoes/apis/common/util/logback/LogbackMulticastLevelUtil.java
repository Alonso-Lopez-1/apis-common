package jp.co.sony.csl.dcoes.apis.common.util.logback;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachable;


public final class LogbackMulticastLevelUtil {

	private LogbackMulticastLevelUtil() { }


	public static void setMulticastAppenderLevel(String julName) {
		Logger root = (Logger) LoggerFactory.getILoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME);
		if (root == null) {
			return;
		}
		Appender<ILoggingEvent> appender = findMulticastAppender_(root);
		if (appender == null) {
			return;
		}
		if (julName == null || julName.trim().isEmpty()) {
			if (appender instanceof MulticastAppender) {
				((MulticastAppender) appender).restoreStartupThreshold();
			}
			return;
		}
		Level level = levelFromJulName_(julName);
		if (level == null) {
			return;
		}
		if (appender instanceof MulticastAppender) {
			((MulticastAppender) appender).setLevelThreshold(level);
		}
	}

	private static Appender<ILoggingEvent> findMulticastAppender_(Logger logger) {
		Appender<ILoggingEvent> appender = logger.getAppender("MULTICAST");
		if (appender != null) {
			return appender;
		}
		return null;
	}

	private static Level levelFromJulName_(String julName) {
		String trimmed = julName.trim();
		if (trimmed.equalsIgnoreCase("SEVERE") || trimmed.equalsIgnoreCase("ERROR")) {
			return Level.ERROR;
		}
		if (trimmed.equalsIgnoreCase("WARNING") || trimmed.equalsIgnoreCase("WARN")) {
			return Level.WARN;
		}
		if (trimmed.equalsIgnoreCase("INFO")) {
			return Level.INFO;
		}
		if (trimmed.equalsIgnoreCase("FINE") || trimmed.equalsIgnoreCase("DEBUG")) {
			return Level.DEBUG;
		}
		if (trimmed.equalsIgnoreCase("FINEST") || trimmed.equalsIgnoreCase("TRACE")) {
			return Level.TRACE;
		}
		if (trimmed.equalsIgnoreCase("OFF")) {
			return Level.OFF;
		}
		return null;
	}
}
