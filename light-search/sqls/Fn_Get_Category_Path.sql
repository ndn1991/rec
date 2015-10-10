raiserror('Không cần chạy đâu sói ạ', 100, -1) with log
USE [Adayroi_CategoryManagement]
GO

/****** Object:  UserDefinedFunction [dbo].[ProductSolr_GetCatPath]    Script Date: 5/18/2015 3:17:35 PM ******/
--SET ANSI_NULLS ON
--GO

--SET QUOTED_IDENTIFIER ON
--GO

---- =============================================
---- Author:		<Author,,Name>
---- Create date: <Create Date, ,>
---- Description:	<Description, ,>
---- =============================================
--IF EXISTS (SELECT * FROM sysobjects WHERE id = object_id(N'[dbo].[ProductSolr_GetCatPath]') AND xtype IN (N'FN', N'IF', N'TF'))
--    DROP FUNCTION [dbo].[ProductSolr_GetCatPath]
--GO
--CREATE FUNCTION [dbo].[ProductSolr_GetCatPath](@catId int)
--RETURNS varchar(200)
--AS
--BEGIN
--	declare @parentId int
--	declare @path varchar(200)

--	set @path = '' + cast(@catId as varchar)
--	select @parentId = ParentCategoryId from Category where id = @catId
--	while @parentId <> 0 and @parentId IS NOT NULL
--	begin
--		set @path = @path + ',' + cast(@parentId as varchar)
--		select @parentId = ParentCategoryId from Category where id = @parentId
--	end
--	if @parentId IS NULL set @path = NULL
--	else set @path = @path + ',0'
--	RETURN @path
--END

--GO

