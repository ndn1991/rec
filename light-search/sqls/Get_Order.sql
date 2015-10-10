select O.OrderOwner as user_id, O.OrderId as order_id, P.ProductId as product_id, count(I.ProductItemId) as num_product
from Adayroi_Oms.dbo.Orders as O
 join Adayroi_Oms.dbo.OrderItem as I on O.OrderId=I.OrderId
 join Adayroi_CategoryManagement.dbo.ProductItem as P on I.ProductItemId=P.Id
where O.ShippingAddressSearch not like '%test%' and O.ShippingAddressSearch not like '%Test%' and O.BuyerAddressSearch not like '%test%'
 and O.BuyerAddressSearch not like '%Test%'
 and O.BuyerAddressSearch not like '%auto%' and O.BuyerAddressSearch not like '%Auto%'
 and O.ShippingAddressSearch not like '%auto%' and O.ShippingAddressSearch not like '%Auto%'
 and P.ProductItemName not like '@Test' and P.ProductItemName not like 'Test_'
group by O.OrderOwner, O.OrderId, P.ProductId