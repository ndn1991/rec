package com.vinecom.common.data;

/**
 * Created by ndn on 7/21/2015.
 */
public class CommonDataTest {
    public static void main(String[] args) {
        String jsonString = "[10,[{\"productItemId\":215403,\"productId\":173331,\"categoryId\":5,\"categoryName\":\"Áo sơ mi nữ\",\"categoryStatus\":1,\"categoryPath\":[5,4,3,2,1],\"brandId\":12352,\"brandName\":\"365 Auto\",\"brandStatus\":1,\"productItemStatus\":3,\"productItemType\":1,\"productItemPolicy\":1,\"productItemName\":\"sku829282828abc\",\"barcode\":\"2adf0f0d-9566-4dc0-89ad-52aebdf8d48f\",\"image\":\"xxx\",\"freshFoodType\":0,\"weight\":0.0,\"updateTime\":1437466758320,\"createTime\":\"2015-07-21 15:18:23\",\"solrFeProductAttribute\":[{\"attributeId\":136,\"attributeName\":\"Xuất xứ\",\"attributeValueId\":1,\"attributeValue\":\"Việt Nam\",\"attributeStatus\":1},{\"attributeId\":143,\"attributeName\":\"Màu sắc\",\"attributeValueId\":12,\"attributeValue\":\"Xanh lam\",\"attributeStatus\":1},{\"attributeId\":233,\"attributeName\":\"Chất liệu vải\",\"attributeValueId\":1080,\"attributeValue\":\"Cotton, Polyester\",\"attributeStatus\":1},{\"attributeId\":248,\"attributeName\":\"Size\",\"attributeValueId\":9880,\"attributeValue\":\"M\",\"attributeStatus\":1},{\"attributeId\":298,\"attributeName\":\"Mã màu sắc\",\"attributeValueId\":9985,\"attributeValue\":\"530\",\"attributeStatus\":1}],\"warehouseProductItemMapping\":[{\"warehouseProductItemMappingId\":1585636,\"merchantId\":11975,\"merchantStatus\":0,\"merchantName\":\"Nguyễn Đức\",\"warehouseId\":1473,\"warehouseStatus\":1,\"provinceId\":4,\"merchantSKU\":\"sku829282828\",\"originalPrice\":0.0000,\"sellPrice\":100000.0000,\"quantity\":-5,\"safetyStock\":15,\"merchantProductItemStatus\":3,\"priceStatus\":1,\"vatStatus\":1,\"isVisible\":271}]}]]";

        CommonObject obj = new CommonObject("test", 123);
        CommonArray arr = new CommonArray(1, obj);
        CommonObject obj1 = new CommonObject("test_test", arr);

        String out = JsonObjectCommonTools.fromJson(jsonString).toJsonString();
        System.out.println(out);
        System.out.println(jsonString);
        System.out.println(out.equalsIgnoreCase(jsonString));
        System.out.println(obj1.toJsonString());
    }
}
