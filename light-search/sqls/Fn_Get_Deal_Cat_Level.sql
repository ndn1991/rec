USE [Adayroi_Dichvu]
GO

/****** Object:  UserDefinedFunction [dbo].[LevelCat]    Script Date: 6/4/2015 2:00:05 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date, ,>
-- Description:	<Description, ,>
-- =============================================
IF EXISTS (SELECT * FROM sysobjects WHERE id = object_id(N'[dbo].[LevelCat]') AND xtype IN (N'FN', N'IF', N'TF'))
    DROP FUNCTION [dbo].[LevelCat]
GO
CREATE FUNCTION [dbo].[LevelCat](@Cat varchar(MAX))
RETURNS int
AS
BEGIN
	if (len(@Cat) > 0)
	begin
		declare @Result int = 0
		declare @FirstIndexPoint int = charindex('.', @Cat)
		while (@FirstIndexPoint > 0)
		begin
			set @Result = @Result + 1
			set @FirstIndexPoint = charindex('.', @Cat, @FirstIndexPoint + 1)
		end
		return @Result + 1
	end
	return 0
END

GO

