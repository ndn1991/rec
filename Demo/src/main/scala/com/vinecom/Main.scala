object Main {
  def main(args: Array[String]) {
    val x = "select O.OrderOwner as user_id, O.OrderId as order_id, P.ProductId as product_id, count(I.ProductItemId) as num_product\nfrom Adayroi_Oms.dbo.Orders as O\n\tjoin Adayroi_Oms.dbo.OrderItem as I on O.OrderId=I.OrderId\n\tjoin Adayroi_CategoryManagement.dbo.ProductItem as P on I.ProductItemId=P.Id\nwhere O.Allstatus&524288 = 524288 and O.CreatedDate>'%s' and I.CreatedDate>'%s'\ngroup by O.OrderOwner, O.OrderId, P.ProductId"
    println(x)
  }
}