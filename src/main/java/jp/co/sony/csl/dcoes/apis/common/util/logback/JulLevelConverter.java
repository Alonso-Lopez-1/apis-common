package jp.co.sony.csl.dcoes.apis.common.util.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;


public class JulLevelConverter extends ClassicConverter {

	@Override public String convert(ILoggingEvent event) {
		Level level = event.getLevel();
		if (level == null) return "INFO";
		switch (level.toInt()) {
		case Level.ERROR_INT: return "SEVERE";
		case Level.WARN_INT:  return "WARNING";
		case Level.INFO_INT:  return "INFO";
		case Level.DEBUG_INT: return "FINE";
		case Level.TRACE_INT: return "FINEST";
		default:              return "INFO";
		}
	}

}
