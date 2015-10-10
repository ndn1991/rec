USE [Adayroi_Dichvu]
GO

/****** Object:  StoredProcedure [dbo].[Get_All_Cat]    Script Date: 6/4/2015 1:58:18 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Get_All_Cat]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[Get_All_Cat]
GO
CREATE PROCEDURE [dbo].[Get_All_Cat]
AS
BEGIN
	SET NOCOUNT ON;
	SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
	with CAT_3 as (
		select '.' + cast(CateID as varchar(1000)) + '.' as CateId, cast(CateID as nvarchar(1000)) + '_' + cast(Visible as nvarchar(1000)) + '_' + cast(Sort as nvarchar(1000)) + '_' + cast(CateName as nvarchar(1000)) as Cat3
		from Category
		where dbo.LevelCat(CatePath)=3
	),
	CAT_OVER_3 as (
		select CateID, CatePath + '.' AS CatePath, CateName, CatePath as OriCatePath, Sort
		from Category
		--where dbo.LevelCat(CatePath) > 2
	),
	CAT_2 AS (
		select '.' + cast(CateID as varchar(1000)) + '.' as CateId, CateID as Cate_2_Id, cast(CateName as nvarchar(1000)) as Cat2
		from Category
		where dbo.LevelCat(CatePath)=2
	),
	CAT_WITH_3 as (
		select CAT_OVER_3.CateID, Cat3, Cat2, dbo.StatusCat(CAT_OVER_3.CateID) as CatPathStatus, CateName, CAT_OVER_3.OriCatePath, CAT_OVER_3.Sort, CAT_2.Cate_2_Id as Cate2Id
		from CAT_OVER_3
			left join CAT_3 on CAT_OVER_3.CatePath like '%' + CAT_3.CateId + '%'
			left join CAT_2 on CAT_OVER_3.CatePath like '%' + CAT_2.CateId + '%'
	)
	select * from CAT_WITH_3

	SET NOCOUNT OFF;
END

GO

