package com.vinecom.common.scala.tool.topK

/**
 * Created by ndn on 7/20/2015.
 */
trait TopK[A] extends Serializable with Iterable[(A, Double)] {
	var length: Int = 0

	def put(other: TopK[A]): TopK[A] = {
		other.foreach(v => put(v))
		this
	}

	def sorted: Array[(A, Double)]
	def put(v: (A, Double)): TopK[A]
}
