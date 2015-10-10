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

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.search.handler.db.sql.mappers.AttributeCategoryMappingMapper;
import com.adr.bigdata.search.handler.db.sql.mappers.CategoryMapper;
import com.adr.bigdata.search.handler.db.sql.mappers.CategoryTreeMapper;
import com.nhb.common.db.sql.daos.AbstractDAO;

/**
 *
 * @author ndn
 */
@UseStringTemplate3StatementLocator
public abstract class CategoryDAO extends AbstractDAO {

	@SqlQuery("select Id as CategoryId, ParentCategoryId as CategoryParentId, CategoryName from Category where Id in(<catIds>)")
	@Mapper(CategoryMapper.class)
	public abstract List<CategoryBean> getCategories(@BindIn("catIds") Set<Integer> catIds);

	@SqlQuery("with HierarchyCTE (ID, ParentCategoryId, CategoryName, CategoryStatus) as\n"
			+ "	(select id, ParentCategoryId, CategoryName, CategoryStatus\n" + "	from dbo.Category\n"
			+ "	where id = :id\n" + "	union all\n"
			+ "	select Category.id, Category.ParentCategoryId, Category.CategoryName, Category.CategoryStatus\n"
			+ "	from dbo.Category\n" + "	inner join hierarchycte\n"
			+ "		on Category.Id = hierarchycte.ParentCategoryId)\n" + "	select * from hierarchycte")
	@Mapper(CategoryTreeMapper.class)
	public abstract List<CategoryBean> getAncestor(@Bind("id") int id);

	@SqlQuery("select Id, ParentCategoryId, CategoryName, CategoryStatus from Category where ParentCategoryId=:id and CategoryStatus=1")
	@Mapper(CategoryTreeMapper.class)
	public abstract List<CategoryBean> getChildren(@Bind("id") int id);

	@SqlQuery("select Id, ParentCategoryId, CategoryName, CategoryStatus from Category where ParentCategoryId=(select ParentCategoryId from Category where Id=:id) and CategoryStatus=1")
	@Mapper(CategoryTreeMapper.class)
	public abstract List<CategoryBean> getSibling(@Bind("id") int id);

	@SqlQuery("with CA as (\n"
			+ " select ACM.AttributeId, ACM.CategoryId, ACM.FilterSpan, ACM.BaseUnitId, ACM.AttributeType, M.UnitName, A.AttributeName, \n"
			+ " row_number() over (partition by ACM.AttributeId, ACM.CategoryId order by ACM.BaseUnitId) as num\n"
			+ " from Attribute_Category_Mapping as ACM\n" 
			+ "  left join MeasureUnit as M on ACM.BaseUnitId=M.Id\n"
			+ "  inner join Attribute as A on ACM.AttributeId=A.Id\n" 
			+ " where isFilter=1\n" 
			+ "),\n"
			+ "HierarchyCTE (ID, ParentID) as (\n" 
			+ " select id, ParentCategoryId\n" + " from dbo.Category\n"
			+ " where id = :catId \n" 
			+ " union all\n" 
			+ " select Category.id, Category.ParentCategoryId\n"
			+ " from dbo.Category\n" 
			+ " inner join hierarchycte\n" 
			+ "  on Category.ID = hierarchycte.ParentID\n"
			+ ")\n"
			+ "select AttributeId, CategoryId, FilterSpan, BaseUnitId, AttributeType, UnitName, AttributeName\n"
			+ "from HierarchyCTE \n"
			+ "inner join (select * from CA where num=1) as C_A on C_A.CategoryId=HierarchyCTE.Id")
	@Mapper(AttributeCategoryMappingMapper.class)
	public abstract List<AttributeCategoryMappingBean> getAttributeCategoryMappings(@Bind("catId") int catId);
}
