package com.vinecom

/**
 * Lớp chỉ chứa k phần tử lớn nhất của tập các phần tử được lần lượt đưa vào
 * Created by ndn on 3/17/2015.
 */
class TopK[A](k: Int) {
  val arr = new Array[(A, Double)](k)
  private val size1 = k - 1
  private var iMin = 0
  private var vMin = Double.PositiveInfinity
  var size = 0

  def put(v: (A, Double)): TopK[A] = {
    if (size < k) {
      arr(size) = v
      if (v._2 < vMin) {
        vMin = v._2
        iMin = size
      }
      size += 1
    }
    else if (v._2 > vMin) {
      arr(iMin) = v
      iMin = 0
      vMin = arr(0)._2
      for (i <- 1 to size1)
        if (arr(i)._2 < vMin) {
          iMin = i
          vMin = arr(i)._2
        }
    }
    this
  }

  def put(other: TopK[A]): TopK[A] = {
    val size1 = other.size - 1
    for (i <- 0 to size1) {
      put(other.arr(i))
    }
    this
  }

  def real = arr.take(size).sortBy(_._2)(Ordering[Double].reverse)
}
