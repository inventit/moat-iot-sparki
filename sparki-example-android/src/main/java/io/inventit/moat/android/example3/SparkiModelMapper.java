/*
 * Copyright (C) 2014 InventIt Inc.
 * 
 * See https://github.com/inventit/moat-iot-sparki
 */
package io.inventit.moat.android.example3;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.yourinventit.dmc.api.moat.ModelMapper;

/**
 * 
 * @author dbaba@yourinventit.com
 * 
 */
public class SparkiModelMapper implements ModelMapper<Sparki> {

	/**
	 * {@link RuntimeExceptionDao} for {@link Sparki}
	 */
	private final RuntimeExceptionDao<Sparki, String> dao;

	/**
	 * {@link ConnectionSource} for handling transactions
	 */
	private final ConnectionSource connectionSource;

	/**
	 * 
	 * @param dao
	 * @param connectionSource
	 */
	public SparkiModelMapper(RuntimeExceptionDao<Sparki, String> dao,
			ConnectionSource connectionSource) {
		this.dao = dao;
		this.connectionSource = connectionSource;
	}

	/**
	 * @return the dao
	 */
	protected RuntimeExceptionDao<Sparki, String> getDao() {
		return dao;
	}

	/**
	 * @return the connectionSource
	 */
	protected ConnectionSource getConnectionSource() {
		return connectionSource;
	}

	/**
	 * 
	 * @param callable
	 * @return
	 */
	protected <T> T doInTransaction(Callable<T> callable) {
		try {
			return TransactionManager.callInTransaction(getConnectionSource(),
					callable);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 
	 * @param entity
	 */
	public void save(final Sparki entity) {
		doInTransaction(new Callable<Void>() {
			public Void call() throws Exception {
				if (StringUtils.isEmpty(entity.getUid())) {
					final String uid = UUID.randomUUID().toString();
					entity.setUid(uid);
				}
				getDao().create(entity);
				return null;
			}
		});
	}

	/**
	 * Finds and removes all records
	 */
	public List<Sparki> findAndRemoveAll() {
		return doInTransaction(new Callable<List<Sparki>>() {
			public List<Sparki> call() throws Exception {
				final List<Sparki> result = getDao().queryForAll();
				getDao().deleteBuilder().delete();
				return result;
			}
		});
	}

	/**
	 * Finds and removes all records. {@link Sparki} array is returned.
	 */
	public Sparki[] findAndRemoveAllAsArray() {
		final List<Sparki> result = findAndRemoveAll();
		if (result.isEmpty()) {
			return new Sparki[0];
		} else {
			return result.toArray(new Sparki[result.size()]);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.yourinventit.dmc.api.moat.ModelMapper#update(java.lang.Object)
	 */
	public Sparki update(final Sparki entity) {
		return doInTransaction(new Callable<Sparki>() {
			public Sparki call() throws Exception {
				getDao().update(entity);
				return entity;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.yourinventit.dmc.api.moat.ModelMapper#updateFields(java.lang.Object,
	 *      java.lang.String[])
	 */
	public void updateFields(Sparki entity, String[] updateFields) {
		// You can write more efficient code here...
		update(entity);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.yourinventit.dmc.api.moat.ModelMapper#remove(java.lang.String)
	 */
	public void remove(final String uid) {
		doInTransaction(new Callable<Void>() {
			public Void call() throws Exception {
				getDao().deleteById(uid);
				return null;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.yourinventit.dmc.api.moat.ModelMapper#findByUid(java.lang.String)
	 */
	public Sparki findByUid(String uid) {
		return getDao().queryForId(uid);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.yourinventit.dmc.api.moat.ModelMapper#add(java.lang.String)
	 */
	public Sparki add(final String uid) {
		return doInTransaction(new Callable<Sparki>() {
			public Sparki call() throws Exception {
				final Sparki event = new Sparki();
				event.setUid(uid);
				getDao().create(event);
				return event;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.yourinventit.dmc.api.moat.ModelMapper#findAllUids()
	 */
	public List<String> findAllUids() {
		final QueryBuilder<Sparki, String> queryBuilder = getDao()
				.queryBuilder();
		try {
			final GenericRawResults<String[]> results = getDao().queryRaw(
					queryBuilder.selectRaw("`uid`").prepareStatementString());
			final List<String> list = new ArrayList<String>();
			for (Iterator<String[]> iterator = results.iterator(); iterator
					.hasNext();) {
				list.add(iterator.next()[0]);
			}
			return list;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.yourinventit.dmc.api.moat.ModelMapper#count()
	 */
	public long count() {
		return getDao().countOf();
	}

}
