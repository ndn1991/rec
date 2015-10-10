/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.utils;

/**
 *
 * @author quangvh
 */
public class Constant {
	public final static String FACET_LIMIT = "20";
	public final static Boolean IS_ACCENT = false;
	public final static String EDISMAX = "edismax";
	public final static String AUTO_PHRASE = "autophrasingParser";
	public final static String EDISMAX_QF = "product_item_name^50.0 brand_name barcode^1000000000 merchant_name^0.02 category_tree attribute_search tags^1000";
	public final static String EDISMAX_PF = "product_item_name^100.0 brand_name barcode^1000000000 merchant_name^0.02 category_tree attribute_search tags^1000";

	/**
	 * Đại ý là ta sẽ tìm trên các trường riêng rẽ do bên ra đề bài yêu cầu. Ngoài ra ta còn tìm theo cả các tổ hợp sau
	 * 1=(name, attribute), 2=(tag, attribute), 3=(name, brand), 4=(tag, brand)
	 */
	public final static String VIN_QUERY = "query_:{!dismax qf=barcode^100 pf=barcode^200 mm=100% v=$name} "
			+ "_query_:{!autophrasingParser qf=tags^10 pf=tags^25 mm=3<75% v=$name} "
			+ "_query_:{!autophrasingParser qf=product_item_name^10 pf=product_item_name^20 pf2=product_item_name^14 pf3=product_item_name^17 ps=3 mm=3<75% v=$name} "
			+ "_query_:{!dismax qf=brand_name^1 pf=brand_name^2 mm=3<75% v=$name} "
			+ "_query_:{!dismax qf=merchant_name^0.5 pf=merchant_name^1 mm=3<75% v=$name} "
			+ "_query_:{!dismax qf=category_tree^1 pf=category_tree^3 mm=2<75% v=$name} "

			+ "_query_:{!autophrasingParser qf=$qf_1 pf=$pf_1 mm=4<80% v=$name} "
			+ "_query_:{!autophrasingParser qf=$qf_2 pf=$pf_2 mm=4<80% v=$name} "

			+ "_query_:{!autophrasingParser qf=$qf_3 pf=$pf_3 mm=4<80% v=$name} "
			+ "_query_:{!autophrasingParser qf=$qf_4 pf=$pf_4 mm=4<80% v=$name}";
	
	public final static String VIN_QUERY_BQ = "query_:{!dismax qf=barcode^100 pf=barcode^200 mm=100% v=$name} "
			+ "_query_:{!autophrasingParser qf=tags^10 pf=tags^25 mm=3<75% v=$name} "
			+ "_query_:{!autophrasingParser qf=product_item_name^10 pf=product_item_name^20 pf2=product_item_name^14 pf3=product_item_name^17 ps=3 mm=3<75% @bq@ v=$name} "
			+ "_query_:{!dismax qf=brand_name^1 pf=brand_name^2 mm=3<75% v=$name} "
			+ "_query_:{!dismax qf=merchant_name^0.5 pf=merchant_name^1 mm=3<75% v=$name} "
			+ "_query_:{!dismax qf=category_tree^1 pf=category_tree^3 mm=2<75% v=$name} "

			+ "_query_:{!autophrasingParser qf=$qf_1 pf=$pf_1 mm=4<80% v=$name} "
			+ "_query_:{!autophrasingParser qf=$qf_2 pf=$pf_2 mm=4<80% v=$name} "

			+ "_query_:{!autophrasingParser qf=$qf_3 pf=$pf_3 mm=4<80% v=$name} "
			+ "_query_:{!autophrasingParser qf=$qf_4 pf=$pf_4 mm=4<80% v=$name}";

	public final static String QF_1_VALUE = "product_item_name^10 attribute_search^1";
	public final static String QF_2_VALUE = "tags^10 attribute_search^1";
	public final static String QF_3_VALUE = "product_item_name^10 brand_name^1";
	public final static String QF_4_VALUE = "tags^10 brand_name^1";
	
	public final static String PF_1_VALUE = "product_item_name^20 attribute_search^1";
	public final static String PF_2_VALUE = "tags^25 attribute_search^1";
	public final static String PF_3_VALUE = "product_item_name^20 brand_name^1";
	public final static String PF_4_VALUE = "tags^25 brand_name^1";
	
	public final static String QF_1 = "qf_1";
	public final static String QF_2 = "qf_2";
	public final static String QF_3 = "qf_3";
	public final static String QF_4 = "qf_4";
	
	public final static String PF_1 = "pf_1";
	public final static String PF_2 = "pf_2";
	public final static String PF_3 = "pf_3";
	public final static String PF_4 = "pf_4";
	public static final String KEYWORD = "name";

}