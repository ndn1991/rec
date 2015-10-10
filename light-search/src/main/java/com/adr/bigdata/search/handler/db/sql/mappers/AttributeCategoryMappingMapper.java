package com.adr.bigdata.search.handler.db.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.nhb.common.Loggable;

public class AttributeCategoryMappingMapper implements ResultSetMapper<AttributeCategoryMappingBean>, Loggable {

	private static final JSONParser parser = new JSONParser(1);

	@Override
	public AttributeCategoryMappingBean map(int i, ResultSet rs, StatementContext arg2) throws SQLException {
		AttributeCategoryMappingBean bean = new AttributeCategoryMappingBean();
		bean.setAttributeId(rs.getInt("AttributeId"));
		bean.setCategoryId(rs.getInt("CategoryId"));
		String sFilterSpan = rs.getString("FilterSpan");
		if (sFilterSpan != null && !sFilterSpan.trim().isEmpty()) {
			Object oFilterSpan;
			try {
				oFilterSpan = parser.parse(sFilterSpan);
				if (oFilterSpan instanceof JSONArray) {
					JSONArray jarrFilterSpans = (JSONArray) oFilterSpan;
					List<long[]> filterSpan = new ArrayList<long[]>();
					for (Object o : jarrFilterSpans) {
						if (o instanceof JSONObject) {
							JSONObject jsonObject = (JSONObject) o;
							if (jsonObject.containsKey("From") && jsonObject.containsKey("To")) {
								filterSpan
										.add(new long[] { (Long) jsonObject.get("From"), (Long) jsonObject.get("To") });
							} else {
								getLogger().warn("FilterSpan column has invalid format");
							}
						}
					}
					bean.setFilterSpan(filterSpan);
				}
			} catch (ParseException e) {
				getLogger().error("", e);
			}

		}
		bean.setBaseUnitId(rs.getInt("BaseUnitId"));
		bean.setAttributeType(rs.getInt("AttributeType"));
		bean.setUnitName(rs.getString("UnitName"));
		bean.setAttributeName(rs.getString("AttributeName"));
		return bean;
	}
}
