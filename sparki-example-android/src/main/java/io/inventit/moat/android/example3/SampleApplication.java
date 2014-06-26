/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.android.example3;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.hoho.android.usbserial.util.SerialInputOutputManager.Listener;
import com.yourinventit.dmc.api.moat.ContextFactory;
import com.yourinventit.dmc.api.moat.Moat;
import com.yourinventit.dmc.api.moat.android.MoatAndroidFactory;
import com.yourinventit.dmc.api.moat.pubsub.PayloadCodec;
import com.yourinventit.dmc.api.moat.pubsub.PubSubCallback;
import com.yourinventit.dmc.api.moat.pubsub.PubSubClient;
import com.yourinventit.dmc.api.moat.pubsub.PubSubQoS;

/**
 * 
 * @author dbaba@yourinventit.com
 * 
 */
public class SampleApplication extends Activity implements Listener,
		PubSubCallback<SparkiAction> {

	/**
	 * {@link Logger}
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SampleApplication.class);

	/**
	 * Threshold to detect if the button is kept pressed.
	 */
	static final long PRESS_THRESHOLD_MS = 1500;

	/**
	 * The uid value for the connected {@link Sparki} object.
	 */
	static final String SPARKI_UID = "734b7dc0-e0b0-11e3-8b68-0800200c9a66";

	/**
	 * {@link ContextFactory}
	 */
	private static ContextFactory contextFactory;

	/**
	 * {@link SparkiModelMapper}
	 */
	private static SparkiModelMapper sparkiModelMapper;

	/**
	 * {@link Moat}
	 */
	private static Moat moat;

	/**
	 * URN prefix
	 */
	private static String urnPrefix;

	/**
	 * {@link UsbSerialDevice}
	 */
	private UsbSerialDevice usbSerialDevice;

	/**
	 * The incoming data buffer
	 */
	private ByteArrayOutputStream in;

	/**
	 * {@link ScrollView}
	 */
	private ScrollView scrollView;

	/**
	 * {@link TextView}
	 */
	private TextView textView;

	private PubSubClient<Class<?>> client;

	/**
	 * (static)
	 * 
	 * @return the contextFactory
	 */
	static ContextFactory getContextFactory() {
		return contextFactory;
	}

	/**
	 * (static)
	 * 
	 * @param sparkiModelMapper
	 *            the sparkiModelMapper to set
	 */
	static void setSparkiModelMapper(SparkiModelMapper sparkiModelMapper) {
		SampleApplication.sparkiModelMapper = sparkiModelMapper;
	}

	/**
	 * (static)
	 * 
	 * @return the sparkiModelMapper
	 */
	static SparkiModelMapper getSparkiModelMapper() {
		return sparkiModelMapper;
	}

	/**
	 * (static)
	 * 
	 * @param moat
	 *            the moat to set
	 */
	static void setMoat(Moat moat) {
		SampleApplication.moat = moat;
	}

	/**
	 * (static)
	 * 
	 * @return the moat
	 */
	static Moat getMoat() {
		return moat;
	}

	/**
	 * (static)
	 * 
	 * @return whether or not the underlying client is activated
	 */
	static boolean isActivated() {
		if (moat == null) {
			return false;
		}
		return MoatAndroidFactory.getInstance().isActivated(getMoat());
	}

	/**
	 * (static)
	 * 
	 * @param urnPrefix
	 *            the urnPrefix to set
	 */
	static void setUrnPrefix(String urnPrefix) {
		SampleApplication.urnPrefix = urnPrefix;
	}

	/**
	 * (static)
	 * 
	 * @return the urnPrefix
	 */
	static String getUrnPrefix() {
		return urnPrefix;
	}

	/**
	 * @return the usbSerialDevice
	 */
	UsbSerialDevice getUsbSerialDevice() {
		return usbSerialDevice;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Widgets
		setContentView(R.layout.simple);
		scrollView = (ScrollView) findViewById(R.id.scroll_view);
		textView = (TextView) findViewById(R.id.scroll_content);

		// ContextFactory
		contextFactory = new AndroidContextFactory(this);

		// USB Serial Device
		usbSerialDevice = new UsbSerialDevice(
				(UsbManager) getSystemService(Context.USB_SERVICE), this);

		// Starting the MoatIoTService on this application starting up.
		startService(new Intent(this, MoatIoTService.class));
	}

	/**
	 * Returns the PubSubClient object
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected PubSubClient<Class<?>> getPubSubClient() {
		if (client == null) {
			client = getMoat().newInstance(PubSubClient.class, null);
		}
		if (client.connected() == false) {
			client.begin(PayloadCodec.RAW, PayloadCodec.RAW);
			LOGGER.info("Starting subscription for {}",
					SparkiAction.class.getName());
			client.subscribe(SparkiAction.class, PubSubQoS.FIRE_AND_FORGET,
					this);
			LOGGER.info("Successfully subscribed");
		}
		return client;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see android.app.Activity#onResume()
	 */
	protected void onResume() {
		super.onResume();
		if (usbSerialDevice.inquireUsbSerialDriver()) {
			final UsbDevice usbDevice = usbSerialDevice.getUsbSerialDriver()
					.getDevice();
			if (textView.getText() == null || textView.getText().length() == 0) {
				textView.setText("USB Serial Device is detected => { VID:"
						+ usbDevice.getVendorId() + ", PID:"
						+ usbDevice.getProductId() + "}\n");
			}
		} else {
			textView.setText("USB Serial Device is missing.");
		}
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	protected void onDestroy() {
		super.onDestroy();
		usbSerialDevice.closeUsbSerialDriver();
		stopService(new Intent(this, MoatIoTService.class));
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
	 * Whether or not the application is initialzied.
	 * 
	 * @return
	 */
	static boolean isNotInitialized() {
		return getSparkiModelMapper() == null;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see com.hoho.android.usbserial.util.SerialInputOutputManager.Listener#onNewData(byte[])
	 */
	@Override
	public void onNewData(byte[] data) {
		if (isActivated() == false) {
			return;
		}

		for (int i = 0; i < data.length; i++) {
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
						LOGGER.info("[onNewData] message => {}", message);
						if (payload[0] == '{') {
							// in case of JSON format
							sparkiEvent.setBinary(payload);
						} else {
							// CSV format
							// 0 ... RangeInCentimeter
							// 1 ... LightCenter
							// 2 ... LightLeft
							// 3 ... LightRight
							final String[] params = message.split(",");
							sparkiEvent.setUid(UUID.randomUUID().toString());
							sparkiEvent
									.setTimestamp(System.currentTimeMillis());
							int p = 0;
							if (StringUtils.isNotEmpty(params[p])) {
								sparkiEvent
										.setRangeInCentimeter(Double.valueOf(
												params[p++].trim()).intValue());
							}
							if (StringUtils.isNotEmpty(params[p])) {
								sparkiEvent.setLightCenter(Double.valueOf(
										params[p++].trim()).intValue());
							}
							if (StringUtils.isNotEmpty(params[p])) {
								sparkiEvent.setLightLeft(Double.valueOf(
										params[p++].trim()).intValue());
							}
							if (StringUtils.isNotEmpty(params[p])) {
								sparkiEvent.setLightRight(Double.valueOf(
										params[p++].trim()).intValue());
							}
						}

						getPubSubClient().publish(sparkiEvent, false);
						appendText("[PUB] => " + message + "\n");
					}

				} catch (RuntimeException e) {
					LOGGER.error("error", e);
				}

			} else {
				getIn().write(data[i]);
			}
		}
	}

	/**
	 * 
	 * @param line
	 */
	public void appendText(final String line) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView.append(line);
				scrollView.smoothScrollTo(0, textView.getBottom());
			}
		});
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see io.inventit.moat.android.example3.SerialInputOutputManager.Listener#onRunError(java.lang.Exception)
	 */
	@Override
	public void onRunError(Exception e) {
		LOGGER.warn("Exception detected. Restart SerialInputOutputManager.", e);
		usbSerialDevice.closeUsbSerialDriver();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ignored) {
		}
		usbSerialDevice.inquireUsbSerialDriver();
	}

	/**
	 * When the image is tapped.
	 * 
	 * @param view
	 */
	public void tapToEnd(View view) {
		stopService(new Intent(this, MoatIoTService.class));
		finish();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.yourinventit.dmc.api.moat.pubsub.PubSubCallback#onAction(java.lang.Object)
	 */
	@Override
	public void onAction(SparkiAction sparkiCommand) {
		try {
			if (StringUtils.isEmpty(sparkiCommand.getControl())) {
				appendText("[MSG] => No control command.");
			} else {
				final String commandString = sparkiCommand.getControl().trim()
						+ "\n"; // \n is a delimiter
				appendText("[MSG] control=>" + commandString);
				final UsbSerialDevice usbSerialDevice = getUsbSerialDevice();
				final SerialInputOutputManager manager = usbSerialDevice
						.getSerialInputOutputManager();
				if (manager == null) {
					LOGGER.info("SerialInputOutputManager is null...");
				} else {
					manager.writeAsync(commandString.getBytes());
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			appendText("[MSG] => Internal error.");
		}
	}
}
