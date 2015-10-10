package com.vinecom.common.scala.tool.topK

/**
 * Lớp chỉ chứa k phần tử lớn nhất của tập các phần tử được lần lượt đưa vào
 * Created by ndn on 3/17/2015.
 */
class ArrayTopK[A](k: Int) extends TopK[A] {
	val arr = new Array[(A, Double)](k)
	private val size1 = k - 1
	private var iMin = 0
	private var vMin = Double.PositiveInfinity

	override def put(v: (A, Double)): TopK[A] = {
		if (length < k) {
			arr(length) = v
			if (v._2 < vMin) {
				vMin = v._2
				iMin = length
			}
			length += 1
		} else if (v._2 > vMin) {
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

	/**
	 * @return Trả về mảng với kích thước thật và được sắp xếp theo thứ tự điểm giảm dần
	 */
	override def sorted = arr.take(length).sortBy(_._2)(Ordering[Double].reverse)

	def iterator: Iterator[(A, Double)] = {
		return this.arr.take(length).iterator
	}

}
