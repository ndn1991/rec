USE [Adayroi_Dichvu]
GO

/****** Object:  UserDefinedFunction [dbo].[StatusCat]    Script Date: 6/4/2015 2:00:37 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date, ,>
-- Description:	<Description, ,>
-- =============================================
IF EXISTS (SELECT * FROM sysobjects WHERE id = object_id(N'[dbo].[StatusCat]') AND xtype IN (N'FN', N'IF', N'TF'))
    DROP FUNCTION [dbo].[StatusCat]
GO
CREATE FUNCTION [dbo].[StatusCat](@catId int)
RETURNS tinyint
AS
BEGIN
	declare @parentId int
	declare @stt tinyint
	declare @count int = 0;
	select @parentId = CateParentId, @stt = Visible from Category where CateID = @catId
	while @parentId <> @catId and @parentId IS NOT NULL and @stt = 1 and @count < 10
	begin
		set @catId = @parentId
		select @parentId = CateParentId, @stt = Visible from Category where CateID = @catId
		set @count = @count + 1
	end

	if @parentId IS NULL or @count >= 10 set @stt = 0	
	return @stt
END

GO

