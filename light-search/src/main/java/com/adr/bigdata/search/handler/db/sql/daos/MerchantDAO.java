package com.adr.bigdata.search.handler.db.sql.daos;

import java.util.List;
import java.util.Set;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

import com.adr.bigdata.indexing.db.sql.beans.MerchantBean;
import com.adr.bigdata.search.handler.db.sql.mappers.MerchantMapper;
import com.nhb.common.db.sql.daos.AbstractDAO;

@UseStringTemplate3StatementLocator
public abstract class MerchantDAO extends AbstractDAO {

	@SqlQuery("select Id, MerchantName, MerchantLogo as MerchantImage, MerchantDescription as Info from MerchantProfile where id=:id")
	@Mapper(MerchantMapper.class)
	public abstract MerchantBean getMerchant(@Bind("id") int id);

	@SqlQuery("select Id, MerchantName, MerchantLogo as MerchantImage, MerchantDescription as Info from MerchantProfile where id in (<ids>)")
	@Mapper(MerchantMapper.class)
	public abstract List<MerchantBean> getMerchants(@Bind("ids") Set<Integer> merchantIds);
}
