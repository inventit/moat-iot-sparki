/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.android.example3;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents an action set (downstream model) to Sparki.
 * 
 * @author dbaba@yourinventit.com
 * 
 */
@DatabaseTable
public class SparkiAction {

	/**
	 * The unique identifier
	 */
	@DatabaseField(id = true)
	private String uid;

	/**
	 * Control signal
	 */
	@DatabaseField
	private String control;

	/**
	 * The Red parameter of RGB LED
	 */
	@DatabaseField
	private byte ledRed;

	/**
	 * The Green parameter of RGB LED
	 */
	@DatabaseField
	private byte ledGreen;

	/**
	 * The Blue parameter of RGB LED
	 */
	@DatabaseField
	private byte ledBlue;

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
	 * @return the control
	 */
	public String getControl() {
		return control;
	}

	/**
	 * @param control
	 *            the control to set
	 */
	public void setControl(String control) {
		this.control = control;
	}

	/**
	 * @return the ledRed
	 */
	public byte getLedRed() {
		return ledRed;
	}

	/**
	 * @param ledRed
	 *            the ledRed to set
	 */
	public void setLedRed(byte ledRed) {
		this.ledRed = ledRed;
	}

	/**
	 * @return the ledGreen
	 */
	public byte getLedGreen() {
		return ledGreen;
	}

	/**
	 * @param ledGreen
	 *            the ledGreen to set
	 */
	public void setLedGreen(byte ledGreen) {
		this.ledGreen = ledGreen;
	}

	/**
	 * @return the ledBlue
	 */
	public byte getLedBlue() {
		return ledBlue;
	}

	/**
	 * @param ledBlue
	 *            the ledBlue to set
	 */
	public void setLedBlue(byte ledBlue) {
		this.ledBlue = ledBlue;
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

}
