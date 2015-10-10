-- Lấy toàn bộ thông tin từ bảng Warehouse_ProductItem_Mapping, trong tương lai có thể sử dụng được cả ProvinceId, IsFreshFoodType
select Id, ProductItemId, MerchantId, WarehouseId, SellPrice, (Quantity - Isnull(SafetyStock,0)) as Quantity, MerchantProductItemStatus, MerchantSku, IsVisible, IsVisible as OnSite, VatStatus, PriceStatus from Warehouse_ProductItem_Mapping

-- Lấy count cho ProductItem
select ProductItemId, case when LastUpdated >= convert(date, getdate()) then ISNULL(Yesterday, 0) else ISNULL(ToDay, 0) end as ViewCount from Adayroi_Tracking.dbo.ProductViews

-- Lấy toàn bộ các khuyến mại
select PPM.WarehouseProductItemMappingId, ISNULL(PRO.PercentDiscount, 0) as PercentDiscount, PRO.StartDate, PRO.FinishDate, PRO.PromotionStatus, (case when PPM.PromotionProductItemStatus=1 and PPM.RemainQuantity>0 then 1 else 0 end) as PromotionProductItemStatus
from Promotion_ProductItem_Mapping as PPM inner join Promotions as PRO on PPM.PromotionId=PRO.Id where PRO.FinishDate > getdate() and PRO.PromotionStatus=2 and PPM.PromotionProductItemStatus=1 and PPM.RemainQuantity>0

-- Lấy toàn bộ các Merchant
select Id, MerchantName, MerchantStatus from MerchantProfile

-- Lấy toàn bộ các Warehouse
select WPM.Id, WarehouseStatus, case when (P_I.ProductItemType=2 or P_I.ProductItemType=4) then cast(W.ProvinceId as varchar) + ',0' else '4,8,0' end as CityIds
from Warehouse W inner join Warehouse_ProductItem_Mapping WPM on WPM.WarehouseId=W.Id inner join ProductItem as P_I on P_I.Id=WPM.ProductItemId

-- Lấy toàn bộ các ProductItem
select Id, ProductId, ProductItemName, ProductItemStatus, Weight, ProductItemType, Images, CreatedDate from ProductItem

-- Lấy toàn bộ các Attribute
[Product_Solr_Get_All_Attribute]

-- Lấy toàn bộ các Product
select Id, BrandId, CategoryId, Barcode from Product

-- Lấy toàn bộ các Brand
select Id, BrandName, BrandStatus from Brand

-- Lấy toàn bộ các Category
[Product_Solr_Get_All_Cat_Path]