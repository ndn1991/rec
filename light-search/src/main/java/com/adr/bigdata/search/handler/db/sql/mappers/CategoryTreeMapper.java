/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.db.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;

/**
 *
 * @author ndn
 */
public class CategoryTreeMapper implements ResultSetMapper<CategoryBean> {

    @Override
    public CategoryBean map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        CategoryBean bean = new CategoryBean();
        bean.setId(rs.getInt("Id"));
        bean.setParentId(rs.getInt("ParentCategoryId"));
        bean.setName(rs.getString("CategoryName"));
        bean.setStatus(rs.getInt("CategoryStatus"));
        return bean;
    }

}
