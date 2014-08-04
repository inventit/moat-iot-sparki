/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-get-started
 */
package io.inventit.moat.osgi.example3;

import java.util.Map;

import com.yourinventit.dmc.api.moat.ResourceType;

/**
 * The class represents a device capable for vibration.
 * 
 * @author dbaba@yourinventit.com
 * 
 */
public class VibrationDevice {

	/**
	 * The image data resource
	 */
	@ResourceType
	private Map<String, String> image;

	/**
	 * @return the image
	 */
	public Map<String, String> getImage() {
		return image;
	}

	/**
	 * @param image
	 *            the image to set
	 */
	public void setImage(Map<String, String> image) {
		this.image = image;
	}
}
