USE [Adayroi_CategoryManagement]
GO

/****** Object:  StoredProcedure [dbo].[Product_Solr_Get_All_Cat_Path]    Script Date: 5/18/2015 3:11:15 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Product_Solr_Get_All_Cat_Path]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[Product_Solr_Get_All_Cat_Path]
GO
CREATE PROCEDURE [dbo].[Product_Solr_Get_All_Cat_Path]
AS
BEGIN
	SET NOCOUNT ON;
	WITH cte (Id, Name, ParentCategoryId, Path, Status, UpdatedDate, Tree)
	AS (SELECT
	  c.id,
	  c.CategoryName,
	  c.ParentCategoryId,
	  CAST(id AS varchar(200)) pids,
	  case when CategoryStatus=1 then 1 else 0 end as CategoryStatus,
	  UpdatedDate,
	  cast(CategoryName as nvarchar(1000)) as Tree
	FROM dbo.Category c
	WHERE ParentCategoryId = 0
	UNION ALL
	SELECT
	  n.id,
	  n.CategoryName,
	  n.ParentCategoryId,
	  CAST(CAST(n.id AS varchar(200)) + ',' + (CAST(cte.Path AS varchar(200))) AS varchar(200)) pids,
	  (case when n.CategoryStatus=1 then 1 else 0 end) & cte.Status as CategoryStatus,
	  n.UpdatedDate,
	  cast((n.CategoryName + '>>' + cte.Tree) as nvarchar(1000)) Tree
	FROM dbo.Category n
	JOIN cte
	  ON n.ParentCategoryId = cte.id)
	SELECT Id, Name, ParentCategoryId, Path + ',0' as Path, Status, UpdatedDate, Tree,
		case when exists(select top 1 1 from Category as tmp where tmp.ParentCategoryId=cte.Id and tmp.CategoryStatus=1) then 'false' else 'true' end as IsLeaf
	FROM cte
	SET NOCOUNT OFF;
END

GO

