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

import com.adr.bigdata.indexing.db.sql.beans.BrandBean;

/**
 *
 * @author ndn
 */
public class BrandMapper implements ResultSetMapper<BrandBean> {

    @Override
    public BrandBean map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        BrandBean bean = new BrandBean();
        bean.setId(rs.getInt("BrandId"));
        bean.setImage(rs.getString("BrandImage"));
        bean.setName(rs.getString("BrandName"));
        return bean;
    }

}
