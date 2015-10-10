USE [Adayroi_Dichvu]
GO

/****** Object:  StoredProcedure [dbo].[Get_All_Deal]    Script Date: 5/29/2015 11:13:33 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		<NoiND,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Get_All_Deal]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[Get_All_Deal]
GO
CREATE PROCEDURE [dbo].[Get_All_Deal]
AS
BEGIN
	SET NOCOUNT ON;
	SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
	WITH DE AS (
		SELECT DD.DID, CAST(DEST.isDomestic AS varchar) AS isDomestic, CAST(DD.DesId AS varchar) AS DesId, DEST.DesName,
			CAST(DEST.Visible AS varchar) as DestVisible, CAST(DD.Visible AS varchar) as DestMappingVisible
		FROM 
			Deal_Destination AS DD
			INNER JOIN Destination AS DEST on DEST.DesID=DD.DesID
	),
	GroupedDestination AS (
		SELECT 
			T.DID, 
			STUFF((SELECT ',' + S.isDomestic FROM DE AS S WHERE S.DID=T.DID FOR XML PATH('')), 1, 1, '') AS isDomestics,
			STUFF((SELECT ',' + S.DesId FROM DE AS S WHERE S.DID=T.DID FOR XML PATH('')), 1, 1, '') AS DesIds,
			STUFF((SELECT (S.DesId + '_' + S.DestVisible + '_' + S.DestMappingVisible + '_' + S.DesName) AS DesFacet FROM DE AS S WHERE S.DID=T.DID FOR XML PATH('')), 1, 1, '<') AS DesFacets
		FROM DE AS T
		GROUP BY T.DID
	),
	City AS (
		select DID, STUFF((SELECT ',' + cast(S.CityID as varchar) FROM DealCity S WHERE S.DID=T.DID FOR XML PATH('')), 1, 1, '') AS CityIds
		from DealCity T 
		group by T.DID
	)

	SELECT 
		D.DID,
		D.CateID,
		D.DID_View,
		D.DealName,
		D.PriceOrigin,
		D.PriceSales,
		D.DiscountPercent,
		(D.QuantitySold + D.QuantityVirtual) as QuantitySold,		
		GroupedDestination.isDomestics,
		GroupedDestination.DesIds,
		GroupedDestination.DesFacets,
		D.CreateDate,
		D.DealStatusId,
		D.Visible,
		City.CityIds,
		CASE WHEN D.StartDate IS NULL THEN '1970-01-01 07:00:00' ELSE D.StartDate END AS StartDate,
		CASE WHEN D.EndDate IS NULL THEN '1970-01-01 07:00:00' ELSE D.EndDate END AS EndDate
	FROM
		Deal as D
		LEFT JOIN GroupedDestination ON GroupedDestination.DID=D.DID
		LEFT JOIN City on City.DID=D.DID
	SET NOCOUNT OFF;
END
GO