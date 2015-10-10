--USE [Adayroi_CategoryManagement]
--GO

--/****** Object:  StoredProcedure [dbo].[Product_Solr_Updated_Product]    Script Date: 5/29/2015 2:39:20 PM ******/
--SET ANSI_NULLS ON
--GO

--SET QUOTED_IDENTIFIER ON
--GO

---- =============================================
---- Author:		<NoiND,,Name>
---- Create date: <Create Date,,>
---- Description:	<Description,,>
---- =============================================
--IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Product_Solr_Updated_Product]') AND type in (N'P', N'PC'))
--	DROP PROCEDURE [dbo].[Product_Solr_Updated_Product]
--GO
--CREATE PROCEDURE [dbo].[Product_Solr_Updated_Product] @LastUpdatedTime datetime
--AS
--BEGIN
--	SET NOCOUNT ON;
--	SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
--	SET @LastUpdatedTime = DATEADD(HOUR, 7, @LastUpdatedTime);
--	with 
--	PO as (
--		select 
--			PPM.WarehouseProductItemMappingId, PRO.PercentDiscount, PRO.StartDate, PRO.FinishDate, PRO.PromotionStatus, PPM.PromotionProductItemStatus, PPM.RemainQuantity
--		from 
--			Promotion_ProductItem_Mapping as PPM 
--			inner join Promotions as PRO on PPM.PromotionId=PRO.Id
--		where PRO.FinishDate > getdate()
--	),
--	DATA_P as (
--		select 
--			WH_PI.Id as ProductItemWarehouseId,
--			P.Barcode as Barcode,
--			WH.Id as WarehouseId,
--			P.Id as ProductId,
--			C.Id as CategoryId,
--			P_I.Id as ProductItemId,
--			WH_PI.SellPrice as SellPrice,
--			--WH_PI.OriginalPrice as OriginalPrice,
--			(WH_PI.Quantity - WH_PI.SafetyStock) as Quantity,
--			BR.BrandName as BrandName,
--			BR.Id as BrandId,
--			case when P_I.ProductId <> '' then P_I.ProductItemName else P.ProductName end as 'ProductItemName',
--			P_I.CreatedDate as CreateTime, -- error
--			WH_PI.MerchantId as MerchantId,
--			MC.MerchantName as MerchantName,
--			WH_PI.MerchantSKU as MerchantProductItemSKU,
--			WH.WarehouseStatus as WarehouseStatus,
--			MC.MerchantStatus as MerchantStatus,
--			P_I.ProductItemStatus as ProductItemStatus,
--			C.CategoryStatus as CategoryStatus,
--			BR.BrandStatus as BrandStatus,
--			WH_PI.MerchantProductItemStatus as MerchantProductItemStatus,
--			case when WH_PI.MerchantProductItemStatus=1 and PO.WarehouseProductItemMappingId is not null and PO.PromotionProductItemStatus=1 and PO.RemainQuantity>0 then 1 else 0 end as IsPromotionMapping,
--			case when PO.WarehouseProductItemMappingId is not null and PO.PromotionStatus=2 then 1 else 0 end as IsPromotion,
--			PO.PercentDiscount as DiscountPercent,
--			PO.StartDate as StartDateDiscount,
--			PO.FinishDate as FinishDateDiscount,
--			--WH_PI.SafetyStock as SafetyStock,
--			--case when WH_PI.Quantity > WH_PI.SafetyStock then 'true' else 'false' end as IsSafetyStock,
--			case when (P_I.ProductItemType=2 or P_I.ProductItemType=4) then cast(WH.ProvinceId as varchar) + ',0' else '4,8,0' end as CityIds,
--			P_I.Weight as Weight,
--			--0 as freshFoodType,
--			P_I.ProductItemType as ProductItemType,
--			P_I.Images as Images,
--			WH_PI.IsVisible,
--			--cast(P_I.Id as varchar) + '_' + cast(MC.Id as varchar) as ProductItemMerchantId,
--			row_number() over (partition by WH_PI.Id order by PO.WarehouseProductItemMappingId) as num
--		from 
--			(select Id, BrandId, CategoryId, Barcode, ProductName from Product) as P
--			inner join (select Id, ProductId, ProductItemName, ProductItemStatus, Weight, ProductItemType, Images, CreatedDate from ProductItem) as P_I on P_I.ProductId=P.Id
--			inner join (
--				select 
--					Id, ProductItemId, MerchantId, WarehouseId, SellPrice, OriginalPrice, (Quantity - Isnull(SafetyStock,0)) as Quantity,
--					MerchantProductItemStatus, MerchantSku, SafetyStock, CreatedDate, IsVisible
--				from 
--					Warehouse_ProductItem_Mapping
--				where UpdatedDate > @LastUpdatedTime
--			) as WH_PI on WH_PI.ProductItemId=P_I.Id
--			inner join (select Id, MerchantName, MerchantStatus from MerchantProfile) as MC on MC.Id=WH_PI.MerchantId
--			inner join (select Id, WarehouseStatus, ProvinceId from Warehouse) as WH on WH.Id=WH_PI.WarehouseId
--			inner join (select Id, BrandName, BrandStatus from Brand) as BR on BR.Id=P.BrandId
--			inner join (select Id, CategoryStatus from Category) as C on C.Id=P.CategoryId
--			left join PO on PO.WarehouseProductItemMappingId=WH_PI.Id
--	)
--	select 
--		DATA_P.ProductItemWarehouseId as ProductItemWarehouseId,
--		DATA_P.ProductId as ProductId,
--		DATA_P.ProductItemId as ProductItemId,
--		DATA_P.BrandId as BrandId,
--		DATA_P.BrandName as BrandName,
--		DATA_P.CategoryId as CategoryId,
--		DATA_P.WarehouseId as WarehouseId,
--		DATA_P.MerchantId as MerchantId,
--		DATA_P.MerchantName as MerchantName,
--		DATA_P.MerchantProductItemSKU as MerchantProductItemSKU,
--		DATA_P.DiscountPercent as DiscountPercent,
--		DATA_P.StartDateDiscount as StartDateDiscount,
--		DATA_P.FinishDateDiscount as FinishDateDiscount,
--		DATA_P.Barcode as Barcode,
--		--1 as CountSell,
--		--1 as CountView,
--		--DATA_P.OriginalPrice as OriginalPrice,
--		DATA_P.SellPrice as SellPrice,
--		DATA_P.ProductItemName as ProductItemName,
--		DATA_P.CreateTime as CreateTime,
--		--1 as IsHot,
--		--1 as IsNew,
--		DATA_P.IsPromotion as IsPromotion,
--		DATA_P.IsPromotionMapping as IsPromotionMapping,
--		DATA_P.Quantity as Quantity,
--		DATA_P.CategoryStatus as CategoryStatus, 
--		DATA_P.BrandStatus as BrandStatus,
--		DATA_P.MerchantStatus as MerchantStatus,
--		DATA_P.WarehouseStatus as WarehouseStatus, 
--		DATA_P.ProductItemStatus as ProductItemStatus,
--		DATA_P.MerchantProductItemStatus as MerchantProductItemStatus,
--		--DATA_P.SafetyStock as SafetyStock,
--		--DATA_P.IsSafetyStock as IsSafetyStock,
--		DATA_P.CityIds as CityIds,
--		--DATA_P.FreshFoodType as FreshFoodType,
--		DATA_P.Weight as 'Weight',
--		DATA_P.ProductItemType,
--		DATA_P.Images as Images,
--		DATA_P.IsVisible as IsVisible,
--		DATA_P.IsVisible as OnSite
--		--DATA_P.ProductItemMerchantId as ProductItemMerchantId
--	from DATA_P
--	where num=1

--    SET NOCOUNT OFF;
--END

--GO

