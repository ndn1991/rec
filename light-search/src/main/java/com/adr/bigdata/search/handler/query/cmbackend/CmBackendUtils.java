/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.query.cmbackend;

import com.adr.bigdata.search.handler.query.common.Schema;
import com.google.api.client.util.Strings;
import com.google.common.base.Joiner;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

/**
 *
 * @author Tong Hoang Anh
 */
public class CmBackendUtils {

    static SimpleDateFormat searchTransformDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final DateFormat DevilFruit = new SimpleDateFormat("MMM d, yyyy hh:mm:ss a");
    private static final String ALL_EXCEPT = "-511";
    private static final String APPROVED = "price_status:1 AND vat_status:1";
    private static final String NOT_APPROVED = "-(price_status:1 AND vat_status:1)";

    public static String generateQuery(String keyword) {
        if (!"*".equals(keyword)) {
            keyword = QueryParser.escape(keyword); 
//            keyword = StringUtils.nomalizationStringSearch(keyword);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("product_item_name_cm: ").append(keyword).append(" + ")
                .append("merchant_product_item_sku_cm:").append(keyword.replaceAll("\\s+", "_"));
        sb.append(" + barcode:").append(keyword);

        return sb.toString();
    }

    public static void setSort(ModifiableSolrParams param, CmBackendQuery query) {
        if (!Strings.isNullOrEmpty(query.getSort())) {
            param.set("sort", query.getSort());
        }
    }

    public static void setFilter(ModifiableSolrParams param, CmBackendQuery query) {
        List<String> filter = new LinkedList<>();
        getFilter(Schema.CATEGORY_PATH, query.getCategoryIds(), filter);
        getFilter(Schema.BRAND_ID, query.getBrandIds(), filter);
        getDateFilter(query.getDateFromTo(), filter);
        getFilter(Schema.WAREHOUSE_ID, query.getWarehouseIds(), filter);
        getFilter(Schema.PRODUCT_ITEM_ID_WAREHOUSE_ID, query.getWarehouseProductItemMappingId(), filter);
        getFilter(Schema.MERCHANT_ID, query.getMerchantIds(), filter);
        getFilter(Schema.PRODUCT_ITEM_ID, query.getProductItemIds(), filter);
        getFilterVisible(query.getVisible(), filter);
        getFilter(Schema.PRODUCT_ITEM_STATUS, query.getProductItemStatuses(), filter); 
        getFilter(Schema.MERCHANT_PRODUCT_ITEM_STATUS, query.getMerchantProductItemStatuses(), filter); 
        getFilter(Schema.PRODUCT_ITEM_TYPE, query.getProductItemTypes(), filter);
        getFilterApproved(query.getApproved(), filter);
        getFilterSafetyStock(query.getSafetyStock(), filter);
        getFilterForIsOriginal(query.getIsOriginal(), filter);
        if(!Strings.isNullOrEmpty(query.getInStock())){
        	filter.add(query.getInStock());
        }
        if (!filter.isEmpty()) {
            String[] filterArr = new String[filter.size()];
            filter.toArray(filterArr);
            param.set(CommonParams.FQ, filterArr);
        }
    }
    
    public static void getFilterForIsOriginal(String isOriginal, List<String> filter){
    	if("true".equalsIgnoreCase(isOriginal)){
    		filter.add(Schema.WEIGHT + ":0");
    	}
    }
    
    private static void getFilterSafetyStock(String safetyStock, List<String> filter){
    	if(!Strings.isNullOrEmpty(safetyStock)){
    		if("0".equals(safetyStock.trim())){
    			filter.add(Schema.QUANTITY + ":[ * TO 0]");
    		}else{
    			filter.add(Schema.QUANTITY + ":[1 TO " + safetyStock + "]");
    		}
    	}
    }
    
    private static void getFilterApproved(String approved, List<String> filter){
    	if(Strings.isNullOrEmpty(approved)){
    		return;
    	}
    	
    	if("true".equalsIgnoreCase(approved.trim())){
    		filter.add(APPROVED);
    	}else if("false".equalsIgnoreCase(approved.trim())){
    		filter.add(NOT_APPROVED);
    	}
    }

    static void getFilter(String field, String[] filtered, List<String> filter) {
        if (filtered == null || filtered.length == 0) {
            return;
        }
        List<String> filteredList = new LinkedList<>();
        for (String str : filtered) {
            filteredList.add(field + ":" + str);
        }
        filter.add(Joiner.on(" OR ").join(filteredList));
    }

    private static String changeDateFormat(String dateFromTo) {
        try {
            String[] splitted = dateFromTo.split(" TO ");
            if (splitted.length != 2) {
                throw new IllegalStateException("Bad date format");
            }
            if (dateFromTo.contains("*")) {
                return dateFromTo;
            }
            Date firstDate = searchTransformDf.parse(splitted[0].replace("Z", ".00Z").substring(1));
            Date secondDate = searchTransformDf.parse(splitted[1].substring(0, splitted[1].length() - 1).replace("Z", ".00Z"));
            return "[" + firstDate.getTime() + " TO " + secondDate.getTime() + "]";
        } catch (IllegalStateException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDateFrom(long time) {
        return DevilFruit.format(new Date(time));
    }

    private static void getDateFilter(String dateFromTo, List<String> filter) {
        if (Strings.isNullOrEmpty(dateFromTo)) {
            return;
        }
        filter.add(Schema.CREATE_TIME + ":" + changeDateFormat(dateFromTo));
    }

    private static void getFilterVisible(String visible, List<String> filter) {
        if (Strings.isNullOrEmpty(visible)) {
            return;
        }
        if (ALL_EXCEPT.equals(visible)) {
            filter.add("-" + Schema.ON_SITE + ":511");
            return;
        }
        if("-256".equals(visible)){
        	filter.add("-" +Schema.VISIBLE + ":256");
        	return;
        }
        int v = 0;
        try {
            v = Integer.valueOf(visible);
        } catch (Exception e) {
        }
        filter.add(Joiner.on(" AND ").join(getListBits(v)));
    }
    
    private static List<String> getListBits(int n) {
        List<String> result = new ArrayList<>();
        int i = 0;
        int a = 1 << i;
        while (a <= n) {
            if ((n & a) == a) {
                result.add(Schema.VISIBLE + ":" + a);
            }
            i++;
            a = 1 << i;
        }
        return result;
    }
}
