package com.adr.bigdata.search.handler.query;

public class SortQuery {
	public static final String DEFAULT = "score desc, product_id asc";

	public static final String BUY_ASC = "count_sell asc";
	public static final String BUY_DESC = "count_sell desc";
	public static final String NEW_ASC = "create_time asc";
	public static final String NEW_DESC = "create_time desc";
	public static final String VIEW_ASC = "count_view asc";
	public static final String VIEW_DESC = "count_view desc";
	public static final String PRICE_ASC = "if(is_not_apply_commision,product(if(and(and(and(max(sub(ms(NOW), start_time_discount), 0), max(sub(finish_time_discount,ms(NOW)), 0)), is_promotion),is_promotion_mapping), promotion_price, sell_price),sub(1,div(commision_fee,100))),if(and(and(and(max(sub(ms(NOW), start_time_discount), 0), max(sub(finish_time_discount,ms(NOW)), 0)), is_promotion),is_promotion_mapping), promotion_price, sell_price)) asc";
	public static final String PRICE_DESC = "if(is_not_apply_commision,product(if(and(and(and(max(sub(ms(NOW), start_time_discount), 0), max(sub(finish_time_discount,ms(NOW)), 0)), is_promotion),is_promotion_mapping), promotion_price, sell_price),sub(1,div(commision_fee,100))),if(and(and(and(max(sub(ms(NOW), start_time_discount), 0), max(sub(finish_time_discount,ms(NOW)), 0)), is_promotion),is_promotion_mapping), promotion_price, sell_price)) desc";
	public static final String QUANTITY_ASC = "quantity asc";
	public static final String QUANTITY_DESC = "quantity desc";
	//quangvh add
	public static final String VIEW_TOTAL_ASC = "viewed_total asc";
	public static final String VIEW_TOTAL_DESC = "viewed_total desc";
	public static final String VIEW_YEAR_ASC = "viewed_year asc";
	public static final String VIEW_YEAR_DESC = "viewed_year desc";
	public static final String VIEW_MONTH_ASC = "viewed_month asc";
	public static final String VIEW_MONTH_DESC = "viewed_month desc";
	public static final String VIEW_WEEK_ASC = "viewed_week asc";
	public static final String VIEW_WEEK_DESC = "viewed_week desc";
	public static final String VIEW_DAY_ASC = "viewed_day asc";
	public static final String VIEW_DAY_DESC = "viewed_day desc";
}
