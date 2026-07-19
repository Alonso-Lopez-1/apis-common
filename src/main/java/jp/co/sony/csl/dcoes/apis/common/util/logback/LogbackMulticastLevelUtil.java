package jp.co.sony.csl.dcoes.apis.common.util.logback;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;


public final class LogbackMulticastLevelUtil {

	public enum SetMulticastLevelResult {
		UPDATED,
		RESTORED,
		ROOT_LOGGER_UNAVAILABLE,
		APPENDER_NOT_FOUND,
		APPENDER_TYPE_MISMATCH,
		INVALID_LEVEL
	}

	private LogbackMulticastLevelUtil() { }

	/**
	 * Checks whether a MULTICAST appender is available in the Logback configuration.
	 * @return true if a MULTICAST appender exists and is a MulticastAppender instance, false otherwise
	 * マルチキャストアペンダが利用可能かどうかをチェックする.
	 * @return MULTICAST アペンダが存在し MulticastAppender インスタンスの場合は true、そうでない場合は false
	 */
	public static boolean isMulticastAppenderAvailable() {
		Object loggerFactory = LoggerFactory.getILoggerFactory();
		
		if (!(loggerFactory instanceof ch.qos.logback.classic.LoggerContext)) {
			return false;
		}
		
		ch.qos.logback.classic.LoggerContext context =
			(ch.qos.logback.classic.LoggerContext) loggerFactory;
		Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
		
		if (root == null) {
			return false;
		}
		
		Appender<ILoggingEvent> appender = root.getAppender("MULTICAST");
		return appender instanceof MulticastAppender;
	}

	public static SetMulticastLevelResult setMulticastAppenderLevel(String LevelName) {
		Logger root = (Logger) LoggerFactory.getILoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME);
		if (root == null) {
			return SetMulticastLevelResult.ROOT_LOGGER_UNAVAILABLE;
		}
		Appender<ILoggingEvent> appender = findMulticastAppender_(root);
		if (appender == null) {
			return SetMulticastLevelResult.APPENDER_NOT_FOUND;
		}
		if (!(appender instanceof MulticastAppender)) {
			return SetMulticastLevelResult.APPENDER_TYPE_MISMATCH;
		}
		MulticastAppender multicastAppender = (MulticastAppender) appender;
		if (LevelName == null || LevelName.trim().isEmpty()) {
			multicastAppender.restoreStartupThreshold();
			return SetMulticastLevelResult.RESTORED;
		}
		Level level = levelFromJulName_(LevelName);
		if (level == null) {
			return SetMulticastLevelResult.INVALID_LEVEL;
		}
		multicastAppender.setLevelThreshold(level);
		return SetMulticastLevelResult.UPDATED;
	}

	private static Appender<ILoggingEvent> findMulticastAppender_(Logger logger) {
		Appender<ILoggingEvent> appender = logger.getAppender("MULTICAST");
		if (appender != null) {
			return appender;
		}
		return null;
	}

	private static Level levelFromJulName_(String LevelName) {
		String trimmed = LevelName.trim();
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
