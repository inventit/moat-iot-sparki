/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.osgi.example3;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yourinventit.dmc.api.moat.Moat;
import com.yourinventit.dmc.api.moat.ModelMapper.SingletonOnMemory;
import com.yourinventit.dmc.api.moat.pubsub.PayloadCodec;
import com.yourinventit.dmc.api.moat.pubsub.PubSubCallback;
import com.yourinventit.dmc.api.moat.pubsub.PubSubClient;
import com.yourinventit.dmc.api.moat.pubsub.PubSubQoS;

/**
 * 
 * @author dbaba@yourinventit.com
 * 
 */
public class Activator implements BundleActivator {

	/**
	 * {@link Logger}
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Activator.class);

	// TODO Replaces the following values with your credentials and environment.
	public static final String COMM_PORT = "/dev/tty.usbmodem1422";
	public static final String APP_ID = "your-app-id";
	public static final String DOMAIN_ID = "your-domain-id";

	// Modify the following constants if needed.
	public static final int BAUDRATE = 9600;
	public static final String PACKAGE_ID = "sparki-example";

	private final PubSubCallback<SparkiAction> sparkiActionCallback = new PubSubCallback<SparkiAction>() {
		@Override
		public void onAction(SparkiAction modelObject) {
			LOGGER.info("[Arrived] {}",
					ToStringBuilder.reflectionToString(modelObject));
		}
	};

	private final SerialPortEventListener listener = new SerialPortEventListener() {
		private final byte[] data = new byte[1024];

		@Override
		public void serialEvent(SerialPortEvent event) {
			try {
				while (inputStream.available() > 0) {
					final int len = inputStream.read(data);
					for (int i = 0; i < len; i++) {
						if (isDelimiter(data[i])) {
							if (isInEmpty()) {
								continue;
							}
							try {
								final byte[] payload = getIn().toByteArray();
								resetIn();
								if (getPubSubClient().connected()) {
									final SparkiEvent sparkiEvent = new SparkiEvent();
									final String message = new String(payload);
									LOGGER.info("[serialEvent] message => {}",
											message);
									if (payload[0] == '{') {
										// in case of JSON format
										sparkiEvent.setBinary(payload);
									} else {
										// CSV format
										// 0 ... RangeInCentimeter
										// 1 ... LightCenter
										// 2 ... LightLeft
										// 3 ... LightRight
										final String[] params = message
												.split(",");
										sparkiEvent.setUid(UUID.randomUUID()
												.toString());
										sparkiEvent.setTimestamp(System
												.currentTimeMillis());
										int p = 0;
										if (StringUtils.isNotEmpty(params[p])) {
											sparkiEvent
													.setRangeInCentimeter(Double
															.valueOf(
																	params[p++]
																			.trim())
															.intValue());
										}
										if (StringUtils.isNotEmpty(params[p])) {
											sparkiEvent
													.setLightCenter(Double
															.valueOf(
																	params[p++]
																			.trim())
															.intValue());
										}
										if (StringUtils.isNotEmpty(params[p])) {
											sparkiEvent
													.setLightLeft(Double
															.valueOf(
																	params[p++]
																			.trim())
															.intValue());
										}
										if (StringUtils.isNotEmpty(params[p])) {
											sparkiEvent
													.setLightRight(Double
															.valueOf(
																	params[p++]
																			.trim())
															.intValue());
										}
									}

									getPubSubClient().publish(sparkiEvent,
											false);
								}

							} catch (RuntimeException e) {
								LOGGER.error("error", e);
							}

						} else {
							getIn().write(data[i]);
						}
					}
				}
			} catch (IOException e) {
				LOGGER.error("I/O error", e);
			}
		}
	};

	private PubSubClient<Class<?>> pubSubClient;

	private SerialPort serialPort;

	private InputStream inputStream;

	private ServiceReference<Moat> sysMoatReference;

	private Moat moat;

	/**
	 * The incoming data buffer
	 */
	private ByteArrayOutputStream in;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		try {
			doStart(bundleContext);
		} catch (Exception exception) {
			LOGGER.error(
					"## ## ## ## ## Unfortunately, this bundle failed to start. Now stopping this bundle...",
					exception);
			stop(bundleContext);
			throw exception;
		}
	}

	private void initCommPort(String port, int baudrate) {
		try {
			final CommPortIdentifier identifier = CommPortIdentifier
					.getPortIdentifier(port);
			serialPort = (SerialPort) identifier.open(
					Activator.class.getName(), 60000);
			serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, 1,
					SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

			inputStream = serialPort.getInputStream();

			serialPort.addEventListener(listener);
			serialPort.notifyOnDataAvailable(true);

		} catch (NoSuchPortException e) {
			throw new IllegalArgumentException(e);
		} catch (PortInUseException e) {
			throw new IllegalStateException(e);
		} catch (UnsupportedCommOperationException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (TooManyListenersException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void initPubSubClient(BundleContext bundleContext) {
		final Map<String, Object> additionalProperties = new HashMap<String, Object>();
		additionalProperties.put("urn:inventit:dmc:app:application-id", APP_ID);
		additionalProperties.put("urn:inventit:dmc:app:package-id", PACKAGE_ID);
		additionalProperties.put("urn:inventit:dmc:domain-id", DOMAIN_ID);
		additionalProperties.put("urn:inventit:dmc:pubsub:trace", "true");

		sysMoatReference = bundleContext.getServiceReference(Moat.class);
		// Gets a system global Moat instance.
		final Moat sysMoat = bundleContext.getService(sysMoatReference);
		// Creates a new Moat instance associated with this application Id.
		moat = sysMoat.newInstance(Moat.class, additionalProperties);
		moat.registerModel(SparkiAction.class,
				new SingletonOnMemory<SparkiAction>(new SparkiAction()));
		moat.registerModel(SparkiEvent.class,
				new SingletonOnMemory<SparkiEvent>(new SparkiEvent()));

		final PubSubClient<Class<?>> client = moat.newInstance(
				PubSubClient.class, additionalProperties);
		if (client.connected() == false) {
			client.begin(PayloadCodec.RAW, PayloadCodec.RAW);
			LOGGER.info("Starting subscription for {}",
					SparkiAction.class.getName());
			client.subscribe(SparkiAction.class, PubSubQoS.FIRE_AND_FORGET,
					sparkiActionCallback);
			LOGGER.info("Successfully subscribed");
		}
		this.pubSubClient = client;
	}

	PubSubClient<Class<?>> getPubSubClient() {
		synchronized (this) {
			while (pubSubClient == null) {
				try {
					wait(1000);
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
		}
		return pubSubClient;
	}

	/**
	 * Returns the serial input buffer.
	 * 
	 * @return the in
	 */
	ByteArrayOutputStream getIn() {
		if (in == null) {
			in = new ByteArrayOutputStream();
		}
		return in;
	}

	/**
	 * Resets the serial input buffer.
	 */
	void resetIn() {
		in = null;
	}

	/**
	 * Whether or not the serial input buffer is empty.
	 * 
	 * @return
	 */
	boolean isInEmpty() {
		return in == null || in.size() == 0;
	}

	/**
	 * Whether or not the data is a message delimiter.
	 * 
	 * @param c
	 * @return
	 */
	static boolean isDelimiter(int c) {
		return c == '\n' || c == '\r';
	}

	/**
	 * 
	 * @param bundleContext
	 * @throws Exception
	 */
	protected void doStart(BundleContext bundleContext) throws Exception {
		initPubSubClient(bundleContext);
		initCommPort(COMM_PORT, BAUDRATE);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		LOGGER.debug(">>> Activator#stop() begins.");
		if (serialPort != null) {
			serialPort.close();
		}
		if (this.pubSubClient != null) {
			this.pubSubClient.end();
			this.pubSubClient = null;
		}
		if (moat != null) {
			moat.removeModel(SparkiAction.class);
			moat.removeModel(SparkiEvent.class);
		}
		if (sysMoatReference != null) {
			bundleContext.ungetService(sysMoatReference);
		}
		LOGGER.debug(">>> Activator#stop() finished.");
		System.out.println(">>> Activator#stop() finished.");
	}
}
