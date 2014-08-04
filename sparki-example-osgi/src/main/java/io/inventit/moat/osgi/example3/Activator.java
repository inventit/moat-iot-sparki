/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.osgi.example3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	public static final String PACKAGE_ID = "simple-example";

	private final PubSubCallback<VibrationDevice> callback = new PubSubCallback<VibrationDevice>() {
		@Override
		public void onAction(VibrationDevice modelObject) {
			LOGGER.info("[Arrived] {}",
					ToStringBuilder.reflectionToString(modelObject));
		}
	};

	private PubSubClient<Class<?>> pubSubClient;

	private ServiceReference<Moat> sysMoatReference;

	private Moat moat;

	private final ScheduledExecutorService executorService = Executors
			.newSingleThreadScheduledExecutor();

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

	@SuppressWarnings("unchecked")
	private void initPubSubClient(BundleContext bundleContext) {
		final Map<String, Object> additionalProperties = new HashMap<String, Object>();
		additionalProperties.put("urn:inventit:dmc:app:application-id", APP_ID);
		additionalProperties.put("urn:inventit:dmc:app:package-id", PACKAGE_ID);
		additionalProperties.put("urn:inventit:dmc:domain-id", DOMAIN_ID);

		sysMoatReference = bundleContext.getServiceReference(Moat.class);
		// Gets a system global Moat instance.
		final Moat sysMoat = bundleContext.getService(sysMoatReference);
		// Creates a new Moat instance associated with this application Id.
		moat = sysMoat.newInstance(Moat.class, additionalProperties);
		moat.registerModel(ShakeEvent.class, new SingletonOnMemory<ShakeEvent>(
				new ShakeEvent()));
		moat.registerModel(VibrationDevice.class,
				new SingletonOnMemory<VibrationDevice>(new VibrationDevice()));

		final PubSubClient<Class<?>> client = moat.newInstance(
				PubSubClient.class, additionalProperties);
		if (client.connected() == false) {
			client.begin(PayloadCodec.RAW, PayloadCodec.RAW);
			LOGGER.info("Starting subscription for {}",
					VibrationDevice.class.getName());
			client.subscribe(VibrationDevice.class, PubSubQoS.FIRE_AND_FORGET,
					callback);
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
	protected void initScheduledExecutorService(BundleContext bundleContext)
			throws Exception {
		this.executorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				// TODO publish ShakeEvent
				// TODO publish ShakeEvent
				// TODO publish ShakeEvent
			}
		}, 1000, 5000, TimeUnit.SECONDS);
	}

	/**
	 * 
	 * @param bundleContext
	 * @throws Exception
	 */
	protected void doStart(BundleContext bundleContext) throws Exception {
		initPubSubClient(bundleContext);
		initScheduledExecutorService(bundleContext);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		LOGGER.debug(">>> Activator#stop() begins.");
		if (this.pubSubClient != null) {
			this.pubSubClient.end();
			this.pubSubClient = null;
		}
		if (moat != null) {
			moat.removeModel(ShakeEvent.class);
			moat.removeModel(VibrationDevice.class);
		}
		if (sysMoatReference != null) {
			bundleContext.ungetService(sysMoatReference);
		}
		LOGGER.debug(">>> Activator#stop() finished.");
		System.out.println(">>> Activator#stop() finished.");
	}
}
