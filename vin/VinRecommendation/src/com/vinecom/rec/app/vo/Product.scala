package com.vinecom.rec.app.vo

/**
 * @author ndn
 */
case class ShortProduct(id: Int, name: String, image: String)

case class Product(id: Int, name: String, image: String, fullCate: Array[Cate])

case class Cate(id: Int, text: String, children: Array[Cate] = null)

case class RootCate(id: Int, text: String, children: Array[RootCate] = null)