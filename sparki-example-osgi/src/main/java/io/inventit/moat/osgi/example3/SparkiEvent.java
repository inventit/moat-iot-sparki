/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.osgi.example3;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.yourinventit.dmc.api.moat.Model;

/**
 * Represents Sparki's event (upstream model).
 * 
 * @author dbaba@yourinventit.com
 * 
 */
@Model(binaryPayloadField = "binary")
@DatabaseTable
public class SparkiEvent {

	/**
	 * The unique identifier
	 */
	@DatabaseField(id = true)
	private String uid;

	@DatabaseField
	private int rangeInCentimeter = Integer.MIN_VALUE;

	@DatabaseField
	private int lightLeft = Integer.MIN_VALUE;

	@DatabaseField
	private int lightCenter = Integer.MIN_VALUE;

	@DatabaseField
	private int lightRight = Integer.MIN_VALUE;

	/**
	 * Timestamp in milliseconds
	 */
	@DatabaseField
	private long timestamp;

	private byte[] binary;

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
	 * @return the rangeInCentimeter
	 */
	public int getRangeInCentimeter() {
		return rangeInCentimeter;
	}

	/**
	 * @param rangeInCentimeter
	 *            the rangeInCentimeter to set
	 */
	public void setRangeInCentimeter(int rangeInCentimeter) {
		this.rangeInCentimeter = rangeInCentimeter;
	}

	/**
	 * @return the lightLeft
	 */
	public int getLightLeft() {
		return lightLeft;
	}

	/**
	 * @param lightLeft
	 *            the lightLeft to set
	 */
	public void setLightLeft(int lightLeft) {
		this.lightLeft = lightLeft;
	}

	/**
	 * @return the lightCenter
	 */
	public int getLightCenter() {
		return lightCenter;
	}

	/**
	 * @param lightCenter
	 *            the lightCenter to set
	 */
	public void setLightCenter(int lightCenter) {
		this.lightCenter = lightCenter;
	}

	/**
	 * @return the lightRight
	 */
	public int getLightRight() {
		return lightRight;
	}

	/**
	 * @param lightRight
	 *            the lightRight to set
	 */
	public void setLightRight(int lightRight) {
		this.lightRight = lightRight;
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
	 * @return the binary
	 */
	public byte[] getBinary() {
		if (this.binary == null) {
			return null;
		}
		return this.binary.clone();
	}

	/**
	 * @param binary
	 *            the binary to set
	 */
	public void setBinary(byte[] binary) {
		this.binary = binary;
	}
}
