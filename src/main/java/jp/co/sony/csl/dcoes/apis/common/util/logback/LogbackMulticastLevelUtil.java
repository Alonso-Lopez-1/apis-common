package jp.co.sony.csl.dcoes.apis.common.util.logback;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;


public final class LogbackMulticastLevelUtil {

	private LogbackMulticastLevelUtil() { }

	/**
	 * Checks whether a MULTICAST appender is available in the Logback configuration.
	 * @return true if a MULTICAST appender exists and is a MulticastAppender instance, false otherwise
	 * マルチキャストアペンダが利用可能かどうかをチェックする.
	 * @return MULTICAST アペンダが存在し MulticastAppender インスタンスの場合は true、そうでない場合は false
	 */
	public static boolean isMulticastAppenderAvailable() {
		Logger root = rootLogger_();
		if (root == null) {
			return false;
		}

		Appender<ILoggingEvent> appender = root.getAppender("MULTICAST");
		return appender instanceof MulticastAppender;
	}

	public static void setMulticastAppenderLevel(String levelName) {
		Logger root = rootLogger_();
		if (root == null) {
			throw new IllegalStateException("Logback root logger unavailable");
		}

		Appender<ILoggingEvent> appender = findMulticastAppender_(root);
		if (appender == null) {
			throw new IllegalStateException("MULTICAST appender not found");
		}

		if (!(appender instanceof MulticastAppender)) {
			throw new IllegalStateException("MULTICAST appender type mismatch");
		}

		MulticastAppender multicastAppender = (MulticastAppender) appender;

		if (levelName == null || levelName.trim().isEmpty()) {
			multicastAppender.restoreStartupThreshold();
			return;
		}

		Level level = levelFromName_(levelName);
		if (level == null) {
			throw new IllegalArgumentException("Invalid multicast log level: " + levelName);
		}

		multicastAppender.setLevelThreshold(level);
	}

	private static Logger rootLogger_() {
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		if (!(loggerFactory instanceof LoggerContext)) {
			return null;
		}

		LoggerContext context = (LoggerContext) loggerFactory;
		return context.getLogger(Logger.ROOT_LOGGER_NAME);
	}

	private static Appender<ILoggingEvent> findMulticastAppender_(Logger logger) {
		return logger.getAppender("MULTICAST");
	}

	private static Level levelFromName_(String levelName) {
		return Level.toLevel(levelName.trim(), null);
	}
}
