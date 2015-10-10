USE [Adayroi_CategoryManagement]
GO

/****** Object:  StoredProcedure [dbo].[Product_Solr_Get_All_Attribute]    Script Date: 5/29/2015 10:53:52 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		<NoiNd>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Product_Solr_Get_All_Attribute]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[Product_Solr_Get_All_Attribute]
GO
CREATE PROCEDURE [dbo].[Product_Solr_Get_All_Attribute]
AS
BEGIN
	SET NOCOUNT ON;
	SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
    select ProductItemId, AttributeId, Value--, AttributeValueId, AttributeStatus, MeasureId, MeasureName, AttributeName
	from (
		select PAM.ProductItemId, PAM.AttributeId, AV.Value,-- PAM.AttributeValueId, M.Id as MeasureId, M.UnitName as MeasureName,
			--case when AttributeStatus=1 and AttributeValueStatus=1 then 'true' else 'false' end as AttributeStatus,
			--A.AttributeName,
			row_number() over (partition by PAM.ProductItemId, PAM.AttributeId order by AV.Value desc) as num
		from 
			Product_Attribute_Mapping as PAM
			inner join Attribute as A on A.Id=PAM.AttributeId
			inner join AttributeValue as AV on AV.Id=PAM.AttributeValueId
			--left join MeasureUnit as M on PAM.UnitId=M.Id
		where A.AttributeStatus=1 and AV.AttributeValueStatus=1
	) as Temp
	where num=1

	SET NOCOUNT OFF;
END

GO

