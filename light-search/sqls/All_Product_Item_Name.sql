--USE [Adayroi_CategoryManagement]
--GO

--/****** Object:  StoredProcedure [dbo].[Product_Solr_Suggesstion]    Script Date: 5/18/2015 3:16:13 PM ******/
--SET ANSI_NULLS ON
--GO

--SET QUOTED_IDENTIFIER ON
--GO

---- =============================================
---- Author:		<Author,,Name>
---- Create date: <Create Date,,>
---- Description:	<Description,,>
---- =============================================
--IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Product_Solr_Suggesstion]') AND type in (N'P', N'PC'))
--DROP PROCEDURE [dbo].[Product_Solr_Suggesstion]
--GO
--CREATE PROCEDURE [dbo].[Product_Solr_Suggesstion]
--AS
--BEGIN
--	SET NOCOUNT ON;
--	with PRI as (
--		select ProductItem.Id, ProductItemName
--		from ProductItem
--			inner join Product on Product.Id=ProductItem.ProductId
--			inner join Brand on Brand.Id=Product.BrandId
--		where ProductItemStatus=1 
--			and ProductItemName<>''
--			and dbo.ProductSolr_GetCatPathStatus(CategoryId)=1
--			and BrandStatus=1
--	),
--	WPM as (
--		select ProductItemId, row_number() over (partition by ProductItemId order by Warehouse_ProductItem_Mapping.Id desc) as num
--		from Warehouse_ProductItem_Mapping
--			inner join Warehouse on Warehouse.Id=Warehouse_ProductItem_Mapping.WarehouseId
--			inner join MerchantProfile on MerchantProfile.Id=Warehouse_ProductItem_Mapping.MerchantId
--		where MerchantProductItemStatus=1 
--			and Quantity>SafetyStock 
--			and SellPrice>0 
--			and WarehouseStatus=1 
--			and MerchantStatus=1
--	)

--	select ProductItemName from (
--		select 
--			ProductItemName, row_number() over (partition by ProductItemName order by Id asc) as num
--		from 
--			PRI inner join (select * from WPM where num=1) as WH_PI on PRI.Id=WH_PI.ProductItemId
--	) as tmp
--	where num=1
--	SET NOCOUNT OFF;
--END

--GO

