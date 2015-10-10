package com.adr.bigdata.search.handler.db.sql.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.search.handler.HazelcastClientAdapter;
import com.adr.bigdata.search.handler.utils.CacheFields;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;
import com.hazelcast.core.IMap;

public class CategoryTreeModel extends AbstractModel {
	public static final int DISABLE_CATEGORY_ID = -1;

	public Map<Integer, CategoryTreeVO> getFullTree() {
		IMap<Integer, CategoryBean> id2Cat = null;
		id2Cat = HazelcastClientAdapter.getMap(CacheFields.CATEGORY);
		Map ret = new HashMap<Integer, CategoryTreeVO>();
		Set<Integer> keySets = id2Cat.keySet();
		for (Integer catId : keySets) {
			try {
				ret.put(catId, getCategoryTree(catId));
			} catch (Exception e) {
				continue;
			}
		}
		return ret;
	}

	public CategoryTreeVO getCategoryTree(int catId) {

		if (catId == DISABLE_CATEGORY_ID) {
			return null;
		}

		IMap<Integer, CategoryBean> id2Cat = null;
		IMap<Integer, Set<Integer>> id2CatChildIds = null;
		try {
			id2Cat = HazelcastClientAdapter.getMap(CacheFields.CATEGORY);
			id2CatChildIds = HazelcastClientAdapter.getMap(CacheFields.CATEGORY_PARENT);
		} catch (Exception e) {
			getLogger().error("fail to get category map from cache", e);
		}

		/* Get Ancestor and this, remove fist element */
		CategoryBean thisCategoryBean = null;
		List<CategoryBean> ancestorBeans = null;
		if (id2Cat != null) {
			thisCategoryBean = id2Cat.get(catId);
			getLogger().debug("get thisCategoryBean from cache: " + thisCategoryBean);
		}
		if (thisCategoryBean == null) {
			//			getLogger().debug("can not get thisCategoryBean from cache: " + thisCategoryBean);
			//			try (CategoryDAO categoryDAO = getDbAdapter().openDAO(CategoryDAO.class)) {
			//				/* Get from DB */
			//				getLogger().debug("hit to db to get ancestorBeans");
			//				ancestorBeans = categoryDAO.getAncestor(catId);
			//				thisCategoryBean = ancestorBeans.get(0);
			//				ancestorBeans.remove(0);
			//				getLogger().debug("thisCategoryBean geted from db: " + thisCategoryBean);
			//				getLogger().debug("ancestorBeans geted from db: " + ancestorBeans);
			//			}
			return null;
		} else {
			List<Integer> ancestorIds = thisCategoryBean.getPath();
			ancestorIds.remove(0);
			ancestorBeans = reOrder(id2Cat.getAll(new HashSet<Integer>(ancestorIds)), ancestorIds);
		}
		if (thisCategoryBean.isLeaf()) {
			getLogger().debug("thisCategoryBean is leaf");
			thisCategoryBean = ancestorBeans.get(0);
			getLogger().debug("new thisCategoryBean is: " + thisCategoryBean);
			ancestorBeans.remove(0);
		}
		/* End : Get Ancestor and this */

		/* Check ancestor active */
		if (!checkCategoryActive(ancestorBeans, thisCategoryBean)) {
			getLogger().debug("category is not active");
			return null;
		}
		/* End: Check ancestor active */

		// if size > 0, get its children and its sibling and concat to
		// return; if size == 0 return it and its children; if size == 0
		// mean error -> retturn null
		if (ancestorBeans.size() > 0) { // size > 0
			try {
				CategoryBean parentBean = ancestorBeans.get(0);

				List<CategoryBean> siblingBeans = getSiblingBeans(id2Cat, id2CatChildIds, thisCategoryBean.getId(),
						parentBean);

				List<CategoryBean> childrenBeans = getChildBeans(id2Cat, id2CatChildIds, thisCategoryBean.getId());

				/* Fill this category */
				List<CategoryTreeVO> childCategoryTreeVOs = getChildCategoryVOsFromBeans(childrenBeans);
				CategoryTreeVO thisCategoryTreeVO = getThisCategoryVOFromBean(thisCategoryBean, childCategoryTreeVOs);

				/* Fill parent Category */
				List<CategoryTreeVO> siblingCategoryTreeVOs = getSiblingCategoryVOsFromBeans(siblingBeans,
						thisCategoryTreeVO);
				CategoryTreeVO parentCategoryTreeVO = getThisCategoryVOFromBean(parentBean, siblingCategoryTreeVOs);

				/* Fill other */
				ancestorBeans.remove(0);
				CategoryTreeVO prevVO = parentCategoryTreeVO;
				for (int i = 0; i < ancestorBeans.size(); i++) {
					CategoryTreeVO curVO = getThisCategoryVOFromBean(ancestorBeans.get(i), Arrays.asList(prevVO));
					prevVO = curVO;
				}

				return prevVO;
			} catch (Exception ex) {
				getLogger().error("error reading categoryTree of normal cat...{}....{}", ex.getMessage(),
						ex.getStackTrace());
				return null;
			}
		} else { // size = 0
			try {
				List<CategoryBean> childrenBeans = getChildBeans(id2Cat, id2CatChildIds, catId);

				List<CategoryTreeVO> childCategoryTreeVOs = getChildCategoryVOsFromBeans(childrenBeans);
				CategoryTreeVO thisCategoryTreeVO = getThisCategoryVOFromBean(thisCategoryBean, childCategoryTreeVOs);
				return thisCategoryTreeVO;
			} catch (Exception ex) {
				getLogger().error("error reading categoryTree of leaf cat...{}....{}", ex.getMessage(),
						ex.getStackTrace());
				return null;
			}
		}
	}

	private List<CategoryBean> getChildBeans(IMap<Integer, CategoryBean> id2Cat,
			IMap<Integer, Set<Integer>> id2CatChildIds, int catId) throws Exception {

		List<CategoryBean> childrenBeans = null;
		if (id2CatChildIds == null) {
			//			try (CategoryDAO categoryDAO = getDbAdapter().openDAO(CategoryDAO.class)) {
			//				getLogger().debug("get childrenBeans from db");
			//				childrenBeans = categoryDAO.getChildren(catId);
			//				getLogger().debug("childrenBeans geted from db: " + childrenBeans);
			//			}
			return Collections.emptyList();
		} else {
			Set<Integer> childIds = id2CatChildIds.get(catId);
			childrenBeans = reOrder(id2Cat.getAll(childIds));
		}

		if (childrenBeans == null) {
			childrenBeans = Collections.emptyList();
		}
		return childrenBeans;
	}

	private List<CategoryBean> getSiblingBeans(IMap<Integer, CategoryBean> id2Cat,
			IMap<Integer, Set<Integer>> id2CatChildIds, int catId, CategoryBean parentBean) throws Exception {

		List<CategoryBean> siblingBeans = null;
		if (id2CatChildIds == null) {
			//			try (CategoryDAO categoryDAO = getDbAdapter().openDAO(CategoryDAO.class)) {
			//				getLogger().debug("get siblingBeans from db");
			//				siblingBeans = categoryDAO.getSibling(catId);
			//				getLogger().debug("siblingBeans geted from db: " + siblingBeans);
			//			}
			return Collections.emptyList();
		} else {
			Set<Integer> siblingIds = id2CatChildIds.get(parentBean.getId());
			siblingBeans = reOrder(id2Cat.getAll(siblingIds));
		}

		if (siblingBeans == null) {
			siblingBeans = Collections.emptyList();
		}
		return siblingBeans;
	}

	private boolean checkCategoryActive(Collection<CategoryBean> ancestorBeans, CategoryBean thisCategoryBean) {
		boolean ancestorActive = true;
		for (CategoryBean ancestorBean : ancestorBeans) {
			if (ancestorBean.getStatus() != 1) {
				ancestorActive = false;
				break;
			}
		}
		if (thisCategoryBean.getStatus() != 1) {
			ancestorActive = false;
		}
		return ancestorActive;
	}

	private List<CategoryTreeVO> getChildCategoryVOsFromBeans(Collection<CategoryBean> categoryBeans) {
		List<CategoryTreeVO> categoryTreeVOs = new ArrayList<>();
		for (CategoryBean categoryBean : categoryBeans) {
			CategoryTreeVO categoryTreeVO = new CategoryTreeVO();
			categoryTreeVO.setCategoryId(categoryBean.getId());
			categoryTreeVO.setCategoryName(categoryBean.getName());
			categoryTreeVO.setCategoryParentId(categoryBean.getParentId());

			categoryTreeVO.setIsLeaf(categoryBean.isLeaf());

			categoryTreeVO.setListCategorySub(Collections.emptyList());
			categoryTreeVOs.add(categoryTreeVO);
		}
		return categoryTreeVOs;
	}

	private List<CategoryTreeVO> getSiblingCategoryVOsFromBeans(Collection<CategoryBean> categoryBeans,
			CategoryTreeVO thisCategoryTreeVO) {
		List<CategoryTreeVO> categoryTreeVOs = new ArrayList<>();
		for (CategoryBean categoryBean : categoryBeans) {
			if (categoryBean.getId() == thisCategoryTreeVO.getCategoryId()) {
				categoryTreeVOs.add(thisCategoryTreeVO);
			} else {
				CategoryTreeVO categoryTreeVO = new CategoryTreeVO();
				categoryTreeVO.setCategoryId(categoryBean.getId());
				categoryTreeVO.setCategoryName(categoryBean.getName());
				categoryTreeVO.setCategoryParentId(categoryBean.getParentId());
				categoryTreeVO.setIsLeaf(categoryBean.isLeaf());
				categoryTreeVO.setListCategorySub(Collections.emptyList());
				categoryTreeVOs.add(categoryTreeVO);
			}
		}
		return categoryTreeVOs;
	}

	private CategoryTreeVO getThisCategoryVOFromBean(CategoryBean categoryBean,
			List<CategoryTreeVO> childCategoryTreeVOs) {
		CategoryTreeVO categoryTreeVO = new CategoryTreeVO();
		categoryTreeVO.setCategoryId(categoryBean.getId());
		categoryTreeVO.setCategoryName(categoryBean.getName());
		categoryTreeVO.setCategoryParentId(categoryBean.getParentId());
		categoryTreeVO.setIsLeaf(categoryBean.isLeaf());
		categoryTreeVO.setListCategorySub(childCategoryTreeVOs);
		return categoryTreeVO;
	}

	private List<CategoryBean> reOrder(Map<Integer, CategoryBean> categoryBeans, List<Integer> order) {
		List<CategoryBean> result = new ArrayList<>();
		for (int id : order) {
			result.add(categoryBeans.get(id));
		}
		return result;
	}

	private List<CategoryBean> reOrder(Map<Integer, CategoryBean> categoryBeans) {
		List<CategoryBean> result = new ArrayList<CategoryBean>();
		for (CategoryBean bean : categoryBeans.values()) {
			if (bean.getStatus() == 1) {
				result.add(bean);
			}
		}
		return result;
	}
}
