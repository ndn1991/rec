package com.vinecom.recommend.tools.topK

/**
 * Created by ndn on 7/20/2015.
 */
trait TopK[A] extends Serializable{
  def put(v: (A, Double)): TopK[A]
  def put(other: TopK[A]): TopK[A]
  def real: Array[(A, Double)]

  def size: Int
  def get(index: Int): (A, Double)
}
