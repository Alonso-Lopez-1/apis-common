package jp.co.sony.csl.dcoes.apis.common.util.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import jp.co.sony.csl.dcoes.apis.common.util.vertx.VertxConfig;


public class ApisLogPrefixConverter extends ClassicConverter {

	@Override public String convert(ILoggingEvent event) {
		String programId = VertxConfig.programId();
		if (programId == null) programId = "";
		String unitId = VertxConfig.config.getString("unitId");
		if (unitId == null || unitId.isEmpty()) {
			return "[[[" + programId + "]]] ";
		}
		return "[[[" + programId + ":" + unitId + "]]] ";
	}

}
