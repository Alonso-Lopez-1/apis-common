package jp.co.sony.csl.dcoes.apis.common.util.logback;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import io.vertx.core.json.JsonObject;
import jp.co.sony.csl.dcoes.apis.common.util.vertx.VertxConfig;


public class ApisLogPrefixConverterTest {

	@After public void tearDown() {
		// VertxConfig.config is a process-wide singleton; reset so tests don't leak into each other.
		VertxConfig.config.setJsonObject(null);
	}

	private String convert_() {
		// The converter ignores the event, so null is fine here.
		return new ApisLogPrefixConverter().convert(null);
	}

	@Test public void mainWithUnitId() {
		VertxConfig.config.setJsonObject(new JsonObject().put("programId", "apis-main").put("unitId", "E001"));
		assertEquals("[[[apis-main:E001]]] ", convert_());
	}

	@Test public void mainWithEmptyUnitId() {
		VertxConfig.config.setJsonObject(new JsonObject().put("programId", "apis-main").put("unitId", ""));
		assertEquals("[[[apis-main]]] ", convert_());
	}

	@Test public void toolsWithoutUnitId() {
		VertxConfig.config.setJsonObject(new JsonObject().put("programId", "apis-ccc"));
		assertEquals("[[[apis-ccc]]] ", convert_());
	}

	@Test public void emptyBeforeConfigParsed() {
		VertxConfig.config.setJsonObject(null);
		assertEquals("[[[]]] ", convert_());
	}

}
