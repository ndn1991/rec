/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.db.sql.daos;

import java.util.List;
import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.adr.bigdata.search.handler.db.sql.mappers.BrandMapper;
import com.nhb.common.db.sql.daos.AbstractDAO;

/**
 *
 * @author ndn
 */
@UseStringTemplate3StatementLocator
public abstract class BrandDAO extends AbstractDAO {
    
    @SqlQuery("select Id as BrandId, BrandName, BrandLogos as BrandImage from Brand where Id=:brandId")
    @Mapper(BrandMapper.class)
    public abstract BrandBean getBrand(@Bind("brandId") int brandId);
    
    @SqlQuery("select Id as BrandId, BrandName, BrandLogos as BrandImage from Brand where Id in (<brandIds>)")
    @Mapper(BrandMapper.class)
    public abstract List<BrandBean> getBrands(@BindIn("brandIds") Set<Integer> brandIds);
}
