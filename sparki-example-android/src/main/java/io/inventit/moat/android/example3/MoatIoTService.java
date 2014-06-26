/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.android.example3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.yourinventit.dmc.api.moat.ContextFactory;
import com.yourinventit.dmc.api.moat.DoneCallback;
import com.yourinventit.dmc.api.moat.Moat;
import com.yourinventit.dmc.api.moat.ModelMapper.SingletonOnMemory;
import com.yourinventit.dmc.api.moat.android.MoatAndroidFactory;
import com.yourinventit.dmc.api.moat.android.MoatInitResult;

/**
 * 
 * @author dbaba@yourinventit.com
 * 
 */
public class MoatIoTService extends Service {

	/**
	 * {@link Logger}
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MoatIoTService.class);

	/**
	 * {@link DatabaseHelper}
	 */
	private DatabaseHelper databaseHelper;

	/**
	 * {@link Moat}
	 */
	private Moat moat;

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// This service doesn't have any IBinder instance.
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		LOGGER.info("onCreate(): Preparing the token file and the enrollment info.");
		super.onCreate();

		// Just for code readability
		final Context context = this;

		// Creating a new DatabaseHelper
		databaseHelper = new DatabaseHelper(context);
		LOGGER.info("onCreate(): DatabaseHelper has been initialized.");

		byte[] token = null;
		try {
			// Loading a security token signed twice,
			// by ServiceSync Sandbox Server and your own.
			token = toByteArray(getAssets().open("moat/signed.bin"));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		// Initializing MOAT object asynchronously
		final MoatAndroidFactory factory = MoatAndroidFactory.getInstance();
		factory.initMoat(token, context).then(
				new DoneCallback<MoatInitResult, Throwable>() {
					/**
					 * This method will be invoked when the initialization is
					 * successfully terminated.
					 * 
					 * @see com.yourinventit.dmc.api.moat.DoneCallback#onSuccess(java.lang.Object)
					 */
					@Override
					public void onSuccess(MoatInitResult input) {
						LOGGER.info("onCreate(): OK! MOAT initialization is successful.");
						if (databaseHelper == null) {
							throw new IllegalStateException(
									"Inconsistent State. Re-start the app.");
						}
						final Moat moat = input.getMoat();
						// Holding the passed moat instance
						MoatIoTService.this.moat = moat;

						// AndroidContextFactory
						final ContextFactory contextFactory = SampleApplication
								.getContextFactory();

						// used for MOAT Java/Android
						// Registering Sparki model
						final SparkiModelMapper sparkiModelMapper = new SparkiModelMapper(
								databaseHelper.getSparkiDao(), databaseHelper
										.getConnectionSource());
						moat.registerModel(Sparki.class, sparkiModelMapper,
								contextFactory);
						Sparki sparki = sparkiModelMapper
								.findByUid(SampleApplication.SPARKI_UID);
						if (sparki == null) {
							sparkiModelMapper.add(SampleApplication.SPARKI_UID);
						}
						LOGGER.info("onCreate(): Sparki has been registered.");

						// used for MOAT PubSub
						moat.registerModel(SparkiAction.class,
								new SingletonOnMemory<SparkiAction>(
										new SparkiAction()));
						moat.registerModel(SparkiEvent.class,
								new SingletonOnMemory<SparkiEvent>(
										new SparkiEvent()));
						LOGGER.info("onCreate(): SparkiAction and SparkiEvnet have been registered.");

						SampleApplication.setMoat(moat);
						SampleApplication.setUrnPrefix(input.getUrnPrefix());
						SampleApplication
								.setSparkiModelMapper(sparkiModelMapper);

						LOGGER.info("onCreate(): OK. I'm ready.");
						Looper.prepare();
						new Handler(getMainLooper()).post(new Runnable() {
							public void run() {
								Toast.makeText(getApplicationContext(),
										"OK. Successfully connected to GW.",
										Toast.LENGTH_LONG).show();
							}
						});
					}

					/**
					 * This method will be invoked when unexpected exception
					 * occurs during initialization process.
					 * 
					 * @see com.yourinventit.dmc.api.moat.DoneCallback#onFailure(java.lang.Object)
					 */
					@Override
					public void onFailure(final Throwable throwable) {
						LOGGER.error("onCreate(): ERROR!!!!!!!!!!.", throwable);
						Looper.prepare();
						new Handler(getMainLooper()).post(new Runnable() {
							public void run() {
								Toast.makeText(
										getApplicationContext(),
										"Exception Occured. Failed to initMoat!:"
												+ throwable.getMessage(),
										Toast.LENGTH_LONG).show();
							}
						});
					}

				});
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		LOGGER.info("onDestroy(): Terminating this instance.");
		super.onDestroy();

		// Shutdown the databaseHelper
		databaseHelper.close();
		databaseHelper = null;

		final MoatAndroidFactory factory = MoatAndroidFactory.getInstance();
		if (factory.isValid(moat)) {
			// Remove unused model descriptors
			moat.removeModel(Sparki.class);
		}
		factory.destroyMoat(moat);
		moat = null;
		LOGGER.info("onDestroy(): Done!");
	}

	/**
	 * Reads an {@link InputStream} and returns as bytes.
	 * 
	 * @param inputStream
	 * @return
	 */
	static byte[] toByteArray(InputStream inputStream) {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		int len = 0;
		try {
			while ((len = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, len);
			}
			return outputStream.toByteArray();
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/**
	 * Returns the MOT URN
	 * 
	 * @param jobServiceId
	 * @param version
	 * @return
	 */
	static String getMoatUrn(String urnPrefix, String jobServiceId,
			String version) {
		return urnPrefix + jobServiceId + ":" + version;
	}
}
