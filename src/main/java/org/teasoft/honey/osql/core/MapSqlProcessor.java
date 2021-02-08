/*
 * Copyright 2016-2021 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.teasoft.bee.osql.BeeException;
import org.teasoft.bee.osql.MapSqlKey;
import org.teasoft.bee.osql.exception.BeeIllegalBusinessException;
import org.teasoft.bee.osql.MapSql;
import org.teasoft.honey.osql.name.NameUtil;
import org.teasoft.honey.util.ObjectUtils;
import org.teasoft.honey.util.StringUtils;

/**
 * @author Kingstar
 * @since  1.9
 */
public class MapSqlProcessor {
	
	public static String toSelectSqlByMap(MapSql mapSql) {
		
		MapSqlImpl suidMapImpl=(MapSqlImpl)mapSql;
		Map<MapSqlKey, String> sqlkeyMap = suidMapImpl.getSqlkeyMap();
		Map<String, Object> whereConditonMap = suidMapImpl.getWhereCondtionMap();
		
		String tableName=sqlkeyMap.get(MapSqlKey.Table);
		if(StringUtils.isBlank(tableName)) {
			throw new BeeException("The Map which key is SqlMapKey.Table must define!");
		}
		String selectColumns=sqlkeyMap.get(MapSqlKey.SelectColumns);
		
		//TODO 根据配置是否要对表名和列名进行命名转换.
		//是否有指定不需要转换的场景???
		
		String isTransfer=sqlkeyMap.get(MapSqlKey.IsNamingTransfer);
		if("true".equals(isTransfer)) {
			selectColumns=_toColumnName(selectColumns);
			OneTimeParameter.setAttribute("_SYS_Bee_DoNotCheckAnnotation", "tRue");//map sql do not check notation
			tableName=_toTableName(tableName);
		}
		
		StringBuffer sqlBuffer=new StringBuffer();
		
		sqlBuffer.append(K.select).append(K.space)
		.append(selectColumns).append(K.space)
		.append(K.from).append(K.space)
		.append(tableName)
		;
		
		//where
		List<PreparedValue> list = new ArrayList<>();
		where(whereConditonMap, list, sqlBuffer);
		
		//group by
		String groupByField=sqlkeyMap.get(MapSqlKey.GroupBy);
		if(StringUtils.isNotBlank(groupByField)) {
		   sqlBuffer.append(K.space)
		   .append(K.groupBy).append(K.space)
		   .append(groupByField);
		}
		//having
		String havingStr=sqlkeyMap.get(MapSqlKey.Having);
		if(StringUtils.isNotBlank(havingStr)) {
		   sqlBuffer.append(K.space)
		   .append(K.having).append(K.space)
		   .append(havingStr);
		}
		//order by
		String orderByStr=sqlkeyMap.get(MapSqlKey.OrderBy);
		if(StringUtils.isNotBlank(orderByStr)) {
			sqlBuffer.append(K.space)
			.append(K.orderBy).append(K.space)
			.append(orderByStr);
		}
		
		String sql = sqlBuffer.toString();
		setContext(sql, list, tableName);
		
		return sql;
	}
	
	public static String toDeleteSqlByMap(MapSql mapSql) {
		
		MapSqlImpl suidMapImpl=(MapSqlImpl)mapSql;
		Map<MapSqlKey, String> sqlkeyMap = suidMapImpl.getSqlkeyMap();
		Map<String, Object> whereConditonMap = suidMapImpl.getWhereCondtionMap();
		
		String tableName=sqlkeyMap.get(MapSqlKey.Table);
		if(StringUtils.isBlank(tableName)) {
			throw new BeeException("The Map which key is SqlMapKey.Table must define!");
		}
		
		String isTransfer=sqlkeyMap.get(MapSqlKey.IsNamingTransfer);
		if("true".equals(isTransfer)) {
			OneTimeParameter.setAttribute("_SYS_Bee_DoNotCheckAnnotation", "tRue");//map sql do not check notation
			tableName=_toTableName(tableName);
		}
		
		StringBuffer sqlBuffer=new StringBuffer();
		
		sqlBuffer.append(K.delete).append(K.space)
		.append(K.from).append(K.space)
		.append(tableName)
		;
		
		//where
		List<PreparedValue> list = new ArrayList<>();
		boolean firstWhere=where(whereConditonMap, list, sqlBuffer);
		
		String sql = sqlBuffer.toString();
		
		
		//不允许删整张表
		//只支持是否带where检测   v1.7.2 
		if (firstWhere) {
			boolean notDeleteWholeRecords = HoneyConfig.getHoneyConfig().isNotDeleteWholeRecords();
			if (notDeleteWholeRecords) {
				Logger.logSQL("In MapSuid, delete SQL: ", sql);
				throw new BeeIllegalBusinessException("BeeIllegalBusinessException: It is not allowed delete whole records in one table.");
			}
		}
		setContext(sql, list, tableName);
		
		return sql;
		
	}
	
	private static boolean where(Map<String, Object> whereConditonMap,List<PreparedValue> list,StringBuffer sqlBuffer) {
		boolean firstWhere=true;
		if(ObjectUtils.isNotEmpty(whereConditonMap)) {
			PreparedValue preparedValue = null;
			for (Map.Entry<String, Object> entry : whereConditonMap.entrySet()) {
				
				if (firstWhere) {
					sqlBuffer.append(K.space).append(K.where).append(K.space);  //where 
					firstWhere=false;
				} else {
					sqlBuffer.append(K.space).append(K.and).append(K.space);   //and
				}
				
				sqlBuffer.append(entry.getKey());
				sqlBuffer.append("=");
				sqlBuffer.append("?");
				
				preparedValue = new PreparedValue();
				preparedValue.setType(entry.getValue().getClass().getSimpleName());
				preparedValue.setValue(entry.getValue());
				list.add(preparedValue);
			}
		}
		return firstWhere;
	}
	
	private static void setContext(String sql,List<PreparedValue> list,String tableName){
		HoneyContext.setContext(sql, list, tableName);
	}
	
	private static String _toTableName(String tableName){
		return NameTranslateHandle.toTableName(tableName);
	}
	
	private static String _toColumnName(String fieldName) {
		return NameTranslateHandle.toColumnName(fieldName);
	}

}