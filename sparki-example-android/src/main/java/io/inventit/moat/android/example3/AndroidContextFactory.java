/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.android.example3;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.yourinventit.dmc.api.moat.ContextFactory;

/**
 * The {@link ContextFactory} providing Android's {@link Context} object.
 * 
 * @author dbaba@yourinventit.com
 * 
 */
public class AndroidContextFactory implements ContextFactory {

	/**
	 * {@link SampleApplication}
	 */
	private final SampleApplication sampleApplication;

	/**
	 * 
	 * @param sampleApplication
	 */
	public AndroidContextFactory(SampleApplication sampleApplication) {
		this.sampleApplication = sampleApplication;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.yourinventit.dmc.api.moat.ContextFactory#createExecutionContext(java.lang.Object,
	 *      java.lang.String)
	 */
	public <T> Map<String, Object> createExecutionContext(T model,
			String methodName) {
		final Map<String, Object> executionContext = new HashMap<String, Object>();
		executionContext.put(Context.class.getName(),
				sampleApplication.getApplicationContext());
		executionContext.put(UsbSerialDevice.class.getName(),
				sampleApplication.getUsbSerialDevice());
		executionContext.put(SampleApplication.class.getName(),
				sampleApplication);
		executionContext.put(SparkiModelMapper.class.getName(),
				SampleApplication.getSparkiModelMapper());
		return executionContext;
	}

}
