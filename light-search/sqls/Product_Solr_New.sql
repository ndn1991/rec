USE [Adayroi_CategoryManagement]
GO
/****** Object:  StoredProcedure [dbo].[Product_Solr_New]    Script Date: 6/17/2015 9:49:16 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Product_Solr_New]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[Product_Solr_New]
GO
CREATE PROCEDURE [dbo].[Product_Solr_New]
AS
BEGIN
set nocount on;
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
with 
PO as (
	select * from (
		select *, ROW_NUMBER() over (partition by WarehouseProductItemMappingId order by PromotionProductItemStatus desc, PromotionStatus desc, StartDate asc) as num
		from (
			select PPM.WarehouseProductItemMappingId, PPM.PromotionPrice, PRO.StartDate, PRO.FinishDate,  
				case when PPM.PromotionProductItemStatus=1 and PPM.RemainQuantity>0 then 1 else 0 end as PromotionProductItemStatus,
				case when PRO.PromotionStatus=2 then 1 else 0 end as PromotionStatus
			from Promotion_ProductItem_Mapping as PPM inner join Promotions as PRO on PPM.PromotionId=PRO.Id
			where PRO.FinishDate > getdate()
		) as tmp1
	) as tmp2 
	where num=1
),
DATA_P as (
	select 
		WH_PI.Id as ProductItemWarehouseId,
		P.Barcode as Barcode,
		WH.Id as WarehouseId,
		P.Id as ProductId,
		P.CategoryId as CategoryId,
		P_I.Id as ProductItemId,
		WH_PI.SellPrice as SellPrice,
		WH_PI.Quantity as Quantity,
		BR.BrandName as BrandName,
		BR.Id as BrandId,
		P_I.ProductItemName,
		P_I.CreatedDate as CreateTime,
		WH_PI.MerchantId as MerchantId,
		MC.MerchantName as MerchantName,
		WH_PI.MerchantSKU as MerchantProductItemSKU,
		MC.MerchantStatus as MerchantStatus,
		P_I.ProductItemStatus as ProductItemStatus,
		BR.BrandStatus as BrandStatus,
		WH_PI.MerchantProductItemStatus as MerchantProductItemStatus,
		case when PO.WarehouseProductItemMappingId is not null and PO.PromotionProductItemStatus=1 then 1 else 0 end as IsPromotionMapping,
		case when PO.WarehouseProductItemMappingId is not null and PO.PromotionStatus=1 then 1 else 0 end as IsPromotion,
		PO.PromotionPrice,
		PO.StartDate as StartDateDiscount,
		PO.FinishDate as FinishDateDiscount,
		case when P_I.ProductItemType=2 or P_I.ProductItemType=4 or P_I.ProductItemPolicy<>1 then cast(WH.ProvinceId as varchar) + ',0' else '4,8,0' end as CityIds,
		P_I.Weight as Weight,
		P_I.ProductItemType as ProductItemType,
		P_I.Images as Images,
		WH_PI.IsVisible,
		WH_PI.IsVisible as OnSite,
		WH_PI.VatStatus,
		WH_PI.PriceStatus,
		BS.JsonScore as Score,
		BS.Score as BoostScore,
		BS.DistrictsJson as AdministrativeUnit,
		P_I.ProductItemPolicy,
		P_I.AttributeDetail as AttributeDetail,
		LPPIM.Priority as LandingPagePriority,
		LPPIM.LandingPageId,
		LPPIM.LandingPageGroupId,
		case when WH_PI.OriginalPrice>WH_PI.SellPrice then 'true' else 'false' end as PriceFlag
	from 
		(select Id, BrandId, CategoryId, Barcode, ProductName from Product) as P
		inner join (select Id, ProductId, ProductItemName, ProductItemStatus, Weight, ProductItemType, Images, CreatedDate, ProductItemPolicy, AttributeDetail from ProductItem) as P_I on P_I.ProductId=P.Id
		inner join (
			select 
				Id, ProductItemId, MerchantId, WarehouseId, SellPrice, OriginalPrice, (Quantity - Isnull(SafetyStock,0)) as Quantity,
				MerchantProductItemStatus, MerchantSku, SafetyStock, CreatedDate, IsVisible, VatStatus, PriceStatus
			from 
				Warehouse_ProductItem_Mapping
		) as WH_PI on WH_PI.ProductItemId=P_I.Id
		inner join (select Id, MerchantName, MerchantStatus from MerchantProfile) as MC on MC.Id=WH_PI.MerchantId
		inner join (select Id, ProvinceId from Warehouse) as WH on WH.Id=WH_PI.WarehouseId
		inner join (select Id, BrandName, BrandStatus from Brand) as BR on BR.Id=P.BrandId
		left join PO on PO.WarehouseProductItemMappingId=WH_PI.Id
		left join (select WarehouseProductMapId, Score, DistrictsJson, JsonScore from BootstScore) as BS on BS.WarehouseProductMapId=WH_PI.Id
		left join (select WarehouseProductItemId, Priority, LandingPageId, LandingPageGroupId from LandingPage_ProductItem_Mapping where StatusId=1) as LPPIM on LPPIM.WarehouseProductItemId=WH_PI.Id
)
select *
from DATA_P
set nocount off;
END