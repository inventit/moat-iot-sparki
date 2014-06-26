/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.android.example3;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * 
 * @author dbaba@yourinventit.com
 * 
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	/**
	 * The name of the database file
	 */
	private static final String DATABASE_NAME = "moatexample3.db";

	/**
	 * The version of the database schema
	 */
	private static final int DATABASE_VERSION = 1;

	/**
	 * {@link RuntimeExceptionDao} for {@link Sparki}
	 */
	private RuntimeExceptionDao<Sparki, String> spakiDao = null;

	/**
	 * @param context
	 */
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * @return the sparkiDao
	 */
	@SuppressWarnings("unchecked")
	public RuntimeExceptionDao<Sparki, String> getSparkiDao() {
		if (this.spakiDao == null) {
			// Workaround for the issue: http://stackoverflow.com/a/9590042
			@SuppressWarnings("rawtypes")
			final RuntimeExceptionDao dao = getRuntimeExceptionDao(Sparki.class);
			this.spakiDao = (RuntimeExceptionDao<Sparki, String>) dao;
		}
		return spakiDao;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase,
	 *      com.j256.ormlite.support.ConnectionSource)
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Sparki.class);

		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
	 *      com.j256.ormlite.support.ConnectionSource, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, Sparki.class, true);
			onCreate(db, connectionSource);

		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#close()
	 */
	@Override
	public void close() {
		super.close();
		this.spakiDao = null;
	}

}
