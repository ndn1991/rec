raiserror('Không cần chạy đâu sói ạ', 100, -1) with log
USE [Adayroi_CategoryManagement]
GO

--/****** Object:  UserDefinedFunction [dbo].[ProductSolr_GetCatPathStatus]    Script Date: 5/18/2015 3:18:06 PM ******/
--SET ANSI_NULLS ON
--GO

--SET QUOTED_IDENTIFIER ON
--GO

---- =============================================
---- Author:		<Author,,Name>
---- Create date: <Create Date, ,>
---- Description:	<Description, ,>
---- =============================================
--IF EXISTS (SELECT * FROM sysobjects WHERE id = object_id(N'[dbo].[ProductSolr_GetCatPathStatus]') AND xtype IN (N'FN', N'IF', N'TF'))
--    DROP FUNCTION [dbo].[ProductSolr_GetCatPathStatus]
--GO
--CREATE FUNCTION [dbo].[ProductSolr_GetCatPathStatus] (@catId int)
--RETURNS int
--AS
--BEGIN
--	declare @parentId int
--	declare @stt int

--	select @parentId = ParentCategoryId, @stt = CategoryStatus from Category where id = @catId
--	while @parentId <> 0 and @parentId IS NOT NULL and @stt = 1
--	begin
--		select @parentId = ParentCategoryId, @stt = CategoryStatus from Category where id = @parentId
--	end
--	if @parentId IS NULL set @stt = 0
	
--	RETURN @stt
--END

--GO

