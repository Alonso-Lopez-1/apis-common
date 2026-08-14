package jp.co.sony.csl.dcoes.apis.common.util.logback;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;


public class MulticastAppender extends AppenderBase<ILoggingEvent> {

	private String groupAddress;
	private int port;
	private String networkInterface;
	private Encoder<ILoggingEvent> encoder;

	private InetAddress sendAddress;
	private MulticastSocket sock;

	private volatile Level threshold;
	private Level startupThreshold;

	public void setGroupAddress(String groupAddress) { this.groupAddress = groupAddress; }
	public void setPort(int port) { this.port = port; }
	public void setNetworkInterface(String networkInterface) { this.networkInterface = networkInterface; }
	public void setEncoder(Encoder<ILoggingEvent> encoder) { this.encoder = encoder; }
	public Encoder<ILoggingEvent> getEncoder() { return encoder; }

	/** Sets the runtime level threshold (events below it are dropped). {@code null} disables filtering. */
	public void setLevelThreshold(Level threshold) { this.threshold = threshold; }
	public void setLevelThresholdByName(String levelName) {
		if (levelName == null || levelName.trim().isEmpty()) {
			restoreStartupThreshold();
			return;
		}
		Level parsed = Level.toLevel(levelName.trim(), null);
		if (parsed == null) {
			throw new IllegalArgumentException("Invalid multicast log level: " + levelName);
		}
		this.threshold = parsed;
	}
	public Level getLevelThreshold() { return threshold; }
	/** Restores the threshold that was in effect when the appender started. */
	public void restoreStartupThreshold() { this.threshold = startupThreshold; }

	@Override public void start() {
		if (encoder == null) { addError("No encoder set for MulticastAppender [" + getName() + "]"); return; }
		if (groupAddress == null) { addError("No groupAddress set for MulticastAppender [" + getName() + "]"); return; }
		if (port <= 0) { addError("Invalid port [" + port + "] for MulticastAppender [" + getName() + "]"); return; }
		try {
			if (!encoder.isStarted()) encoder.start();
			sendAddress = InetAddress.getByName(groupAddress);
			sock = new MulticastSocket(port);
			NetworkInterface ni = resolveInterface_();
			if (ni == null) {
				addError("No suitable multicast network interface for MulticastAppender [" + getName() + "]");
				safeClose_();
				return;
			}
			sock.joinGroup(new InetSocketAddress(sendAddress, port), ni);
			sock.setTimeToLive(1);
		} catch (Exception e) {
			addError("Failed to initialize multicast socket for MulticastAppender [" + getName() + "]", e);
			safeClose_();
			return;
		}
		startupThreshold = threshold;
		super.start();
	}


	@Override protected void append(ILoggingEvent event) {
		if (!isStarted() || sock == null) return;
		Level t = threshold;
		if (t != null && event.getLevel().toInt() < t.toInt()) return;
		try {
			byte[] data = encoder.encode(event);
			if (data == null || data.length == 0) return;
			DatagramPacket packet = new DatagramPacket(data, data.length, sendAddress, port);
			sock.send(packet);
		} catch (Exception e) {
			addError("Failed to send multicast log packet from MulticastAppender [" + getName() + "]", e);
		}
	}


	@Override public void stop() {
		safeClose_();
		if (encoder != null && encoder.isStarted()) encoder.stop();
		super.stop();
	}

	private void safeClose_() {
		if (sock != null) {
			try { sock.close(); } catch (Exception ignore) { /* nop */ }
			sock = null;
		}
	}


	private NetworkInterface resolveInterface_() throws SocketException {
		if (networkInterface != null && !networkInterface.isEmpty()) {
			NetworkInterface ni = NetworkInterface.getByName(networkInterface);
			if (ni != null) return ni;
			addWarn("Configured networkInterface [" + networkInterface + "] not found; auto-detecting");
		}
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface ni = interfaces.nextElement();
			try {
				if (ni.isUp() && !ni.isLoopback() && ni.supportsMulticast()) return ni;
			} catch (SocketException e) {
				// Skip interfaces whose state cannot be determined
			}
		}
		return null;
	}

}
