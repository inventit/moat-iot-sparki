/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.osgi.example3;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.HashMap;
import java.util.Map;

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

	public static final String APP_ID = "1943116b-508e-4ec8-b1b5-ad71f5895682";
	public static final String PACKAGE_ID = "sparki-example";
	public static final String DOMAIN_ID = "73ab9e38dda2dfa0b13ed84ba0ec4c50f5aa01a2";

	private final PubSubCallback<SparkiAction> sparkiActionCallback = new PubSubCallback<SparkiAction>() {
		@Override
		public void onAction(SparkiAction modelObject) {
			LOGGER.info("[Arrived] {}",
					ToStringBuilder.reflectionToString(modelObject));
		}
	};
	
	private final SerialPortEventListener listener = new SerialPortEventListener() {
		
		@Override
		public void serialEvent(SerialPortEvent event) {
			
		}
	};

	private PubSubClient<Class<?>> client;

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

	/**
	 * 
	 * @param bundleContext
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void doStart(BundleContext bundleContext) throws Exception {
		final Map<String, Object> additionalProperties = new HashMap<String, Object>();
		additionalProperties.put("urn:inventit:dmc:app:application-id", APP_ID);
		additionalProperties.put("urn:inventit:dmc:app:package-id", PACKAGE_ID);
		additionalProperties.put("urn:inventit:dmc:domain-id", PACKAGE_ID);
		final ServiceReference<Moat> moatRef = bundleContext
				.getServiceReference(Moat.class);
		final Moat moat = bundleContext.getService(moatRef);
		moat.registerModel(SparkiAction.class,
				new SingletonOnMemory<SparkiAction>(new SparkiAction()));
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
		this.client = client;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		LOGGER.debug(">>> Activator#stop() begins.");
		if (this.client != null) {
			this.client.end();
			this.client = null;
		}
		LOGGER.debug(">>> Activator#stop() finished.");
		System.out.println(">>> Activator#stop() finished.");
	}
}
