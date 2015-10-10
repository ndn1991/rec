USE [Adayroi_CategoryManagement]
GO

/****** Object:  StoredProcedure [dbo].[Product_Solr_Get_All_Merchant]    Script Date: 5/29/2015 11:02:13 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		<NoiNd,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Product_Solr_Get_All_Merchant]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[Product_Solr_Get_All_Merchant]
GO
CREATE PROCEDURE [dbo].[Product_Solr_Get_All_Merchant]
AS
BEGIN
	SET NOCOUNT ON;

	WITH GROUPED_MC_ADDRESSES AS (
		SELECT 
			MerchantId,
			STUFF((SELECT ',' + CAST(S.Id AS varchar) FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS MerchantAddressIds,
			STUFF((SELECT ',' + CAST(S.AddressId AS varchar) FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS AddressIds,
			STUFF((SELECT ',' + CAST(S.ProvinId AS varchar) FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS ProvinceIds,
			STUFF((SELECT ',' + CAST(S.DistrictId AS varchar) FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS DistrictIds,
			STUFF((SELECT ',' + CAST(S.VillageId AS varchar) FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS VillageIds,
			STUFF((SELECT ',' + CAST(S.RoadId AS varchar) FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS RoadIds,
			STUFF((SELECT ',' + CAST(S.AddressType AS varchar) FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS AddressTypes,
			STUFF((SELECT ',' + CAST(S.AddressStatus AS varchar) FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS AddressStatuses,
			STUFF((SELECT S.AddressDetail FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '<') AS AddressDetails,
			STUFF((SELECT S.FullAddress FROM MerchantAddress as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '<') AS FullAddresss
		FROM MerchantAddress AS T
		GROUP BY T.MerchantId
	),
	GROUPED_MC_DOCUMENTS AS (
		SELECT
			MerchantId,
			STUFF((SELECT ',' + CAST(S.Id AS varchar) FROM MerchantDocument as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS DocumentIds,
			STUFF((SELECT S.DocumentName FROM MerchantDocument as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '<') AS DocumentNames,
			STUFF((SELECT S.DocumentCode FROM MerchantDocument as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '<') AS DocumentCodes,
			STUFF((SELECT ',' + CAST(S.DocumentTypeId AS varchar) FROM MerchantDocument as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS DocumentTypeIds,
			--STUFF((SELECT S.DocumentStorePath FROM MerchantDocument as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '<') AS DocumentStorePaths,
			STUFF((SELECT S.CreatedDate FROM MerchantDocument as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '<') AS CreatedDates,
			STUFF((SELECT S.UpdatedDate FROM MerchantDocument as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '<') AS UpdatedDates
		FROM MerchantDocument AS T
		GROUP BY T.MerchantId
	),
	GROUPED_MC_CONTACT AS (
		SELECT
			MerchantId,
			STUFF((SELECT ',' + CAST(S.Id AS varchar) FROM MerchantContact as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS MerchantContactIds,
			STUFF((SELECT S.ContactName FROM MerchantContact as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '<') AS ContactNames,
			STUFF((SELECT ',' + CAST(S.ContactType AS varchar) FROM MerchantContact as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS ContactTypes,
			STUFF((SELECT S.Email FROM MerchantContact as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '<') AS Emails
			--STUFF((SELECT ',' + CAST(S. AS varchar) FROM MerchantContact as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS MerchantContactIds,
			--STUFF((SELECT ',' + CAST(S.Id AS varchar) FROM MerchantContact as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS MerchantContactIds,
			--STUFF((SELECT ',' + CAST(S.Id AS varchar) FROM MerchantContact as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS MerchantContactIds,
			--STUFF((SELECT ',' + CAST(S.Id AS varchar) FROM MerchantContact as S WHERE S.MerchantId=T.MerchantId FOR XML PATH('')), 1, 1, '') AS MerchantContactIds
		FROM MerchantContact AS T
		GROUP BY T.MerchantId
	)
	SELECT
		M.Id,
		M.MerchantCode,
		M.MerchantName,
		M.CompanyName,
		M.MerchantDescription,
		M.MerchantLogo,
		M.Phone,
		M.Fax,
		M.Email,
		M.Website,
		M.BankOwner,
		M.BankName,
		M.BankBranch,
		M.TaxCode,
		M.MerchantStatus,
		M.UpdatedDate,
		M.CreatedDate,
		M.UserId,
		M.CreatedUserId,
		M.UpdatedUserId,
		M.MerchantType,
		M.UnFulFilled,
		M.NoteAdmin,
		M.ReturnPolicy,
		M.BankNumber,
		M.GroupMember,
		GROUPED_MC_CONTACT.MerchantContactIds,
		GROUPED_MC_CONTACT.ContactNames,
		GROUPED_MC_CONTACT.ContactTypes,
		GROUPED_MC_CONTACT.Emails AS EmailContacts,
		GROUPED_MC_ADDRESSES.MerchantAddressIds,
		GROUPED_MC_ADDRESSES.AddressIds,
		GROUPED_MC_ADDRESSES.ProvinceIds,
		GROUPED_MC_ADDRESSES.DistrictIds,
		GROUPED_MC_ADDRESSES.VillageIds,
		GROUPED_MC_ADDRESSES.RoadIds,
		GROUPED_MC_ADDRESSES.AddressTypes,
		GROUPED_MC_ADDRESSES.AddressStatuses,
		GROUPED_MC_ADDRESSES.AddressDetails,
		GROUPED_MC_ADDRESSES.FullAddresss,
		GROUPED_MC_DOCUMENTS.DocumentIds,
		GROUPED_MC_DOCUMENTS.DocumentNames,
		GROUPED_MC_DOCUMENTS.DocumentCodes,
		GROUPED_MC_DOCUMENTS.DocumentTypeIds,
		GROUPED_MC_DOCUMENTS.CreatedDates AS DocumentCreatedDates,
		GROUPED_MC_DOCUMENTS.UpdatedDates AS DocumentUpdatedDates
	FROM 
		MerchantProfile AS M
		LEFT JOIN GROUPED_MC_CONTACT ON GROUPED_MC_CONTACT.MerchantId=M.Id
		LEFT JOIN GROUPED_MC_DOCUMENTS ON GROUPED_MC_DOCUMENTS.MerchantId=M.Id
		LEFT JOIN GROUPED_MC_ADDRESSES ON GROUPED_MC_ADDRESSES.MerchantId=M.Id

    SET NOCOUNT OFF;
END

GO

