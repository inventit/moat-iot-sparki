/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.android.example3;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.yourinventit.dmc.api.moat.Command;

/**
 * Represents Sparki, an Arduino based robot (generic object model).
 * 
 * @author dbaba@yourinventit.com
 * 
 */
/**
 * 
 * @author dbaba@yourinventit.com
 * 
 */
@DatabaseTable
public class Sparki {

	/**
	 * {@link Logger}
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Sparki.class);

	/**
	 * The unique identifier
	 */
	@DatabaseField(id = true)
	private String uid;

	/**
	 * LCD Text
	 */
	@DatabaseField
	private String lcdText;

	/**
	 * Event sampling rate in mHZ, up to 10,000 mHz. Event sampling is disabled
	 * when 0 mHz is given.
	 */
	private int samplingRateInMillihertz;

	/**
	 * Timestamp in milliseconds
	 */
	@DatabaseField
	private long timestamp;

	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * @return the lcdText
	 */
	public String getLcdText() {
		return lcdText;
	}

	/**
	 * @param lcdText
	 *            the lcdText to set
	 */
	public void setLcdText(String lcdText) {
		this.lcdText = lcdText;
	}

	/**
	 * @return the samplingRateInMillihertz
	 */
	public int getSamplingRateInMillihertz() {
		return samplingRateInMillihertz;
	}

	/**
	 * @param samplingRateInMillihertz
	 *            the samplingRateInMillihertz to set
	 */
	public void setSamplingRateInMillihertz(int samplingRateInMillihertz) {
		if (samplingRateInMillihertz > 10000) {
			this.samplingRateInMillihertz = 10000;
		} else if (samplingRateInMillihertz < 0) {
			this.samplingRateInMillihertz = 0;
		} else {
			this.samplingRateInMillihertz = samplingRateInMillihertz;
		}
	}

	/**
	 * Returns the sampling interval in seconds, T = 1,000/fmHz
	 * (samplingRateInMillihertz)
	 * 
	 * @return T
	 */
	public int getSamplingIntervalInSeconds() {
		return 1000 / getSamplingRateInMillihertz();
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Sending showing text on LCD command to Arduino.
	 * 
	 * Format: <code>LCD:{AlphaNumeric+Symbols}\n</code>
	 * 
	 * @param context
	 */
	@Command
	public void showTextOnLcd(Map<String, Object> context) {
		final String paramText = (String) context.get("data");
		setLcdText(paramText);
		LOGGER.debug("[{}:showTextOnLcd] => [{}]", uid, paramText);
		final UsbSerialDevice usbSerialDevice = (UsbSerialDevice) context
				.get(UsbSerialDevice.class.getName());
		final String payload = "LCD:" + getLcdText() + "\n";
		final SerialInputOutputManager serialInputOutputManager = usbSerialDevice
				.getSerialInputOutputManager();
		serialInputOutputManager.writeAsync(payload.getBytes());
		final SparkiModelMapper mapper = (SparkiModelMapper) context
				.get(SparkiModelMapper.class.getName());
		mapper.update(this);
		((SampleApplication) context.get(SampleApplication.class.getName()))
				.appendText("[SRV]=>" + payload);
	}

	@Command
	public void sampleData(Map<String, Object> context) {
		final String onOrOff = (String) context.get("data");
		if ("on".equalsIgnoreCase(onOrOff)) {
			// TODO starting data sampling
		} else {
			// TODO stopping data sampling
		}
	}

	@Command
	public void updateFirmware(Map<String, Object> context) {
		// final String url = (String) context.get("data");
		// TODO download
	}

}
