/*
 * Copyright 2013-2018 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.honey.osql.core;

import java.util.List;

import org.bee.osql.ObjSQLException;
import org.bee.osql.ObjToSQL;
import org.bee.osql.SQL;
import org.bee.osql.Suid;

/**
 * 通过对象来操作数据库，并返回结果
 * @author Kingstar
 * Create on 2013-6-30 下午10:19:27
 * @since  1.0
 */
public class ObjSQL implements Suid {
	

	private SQL sQL;// = BeeFactory.getHoneyFactory().getSQL();
	private ObjToSQL objToSQL = BeeFactory.getHoneyFactory().getObjToSQL();

	public ObjSQL() {}

	public SQL getsQL() {
		if(sQL==null) sQL = BeeFactory.getHoneyFactory().getSQL();
		return sQL;
	}

	public void setsQL(SQL sQL) {
		this.sQL = sQL;
	}

	@Override
	public <T> List<T> select(T entity) {

		if (entity == null) return null;

		List<T> list = null;
		String sql = objToSQL.toSelectSQL(entity);
		Logger.logSQL("select SQL: ", sql);
		list = sQL.select(sql, entity); // 返回值用到泛型
		return list;
	}

	@Override
	public <T> int insert(T entity) {

		if (entity == null) return 0;

		String sql = objToSQL.toInsertSQL(entity);
		int insertNum = -3;
		Logger.logSQL("insert SQL: ", sql);
		insertNum = sQL.modify(sql);
		return insertNum;
	}

	@Override
	public int delete(Object entity) {

		if (entity == null) return 0;

		String sql = objToSQL.toDeleteSQL(entity);
		int deleteNum = -3;
		Logger.logSQL("delete SQL: ", sql);
		deleteNum = sQL.modify(sql);
		return deleteNum;
	}

	@Override
	public <T> int update(T entity) {
		// 当id为null时抛出异常  在转sql时抛出

		if (entity == null) return 0;

		String sql = "";
		int updateNum = 0;
		try {
			sql = objToSQL.toUpdateSQL(entity);
			Logger.logSQL("update SQL: ", sql);
			updateNum = sQL.modify(sql);
		} catch (ObjSQLException e) {
			// TODO: handle exception
			System.err.println(e.getMessage());
		}

		return updateNum;
	}
}
