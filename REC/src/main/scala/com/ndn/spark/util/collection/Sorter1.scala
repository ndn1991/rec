package com.ndn.spark.util.collection

import java.util.Comparator

import org.apache.spark.util.collection.TimSort


class Sorter1[K, Buffer](private val s: SortDataFormat1[K, Buffer]) {

  private val timSort = new TimSort(s)

  def sort(a: Buffer, lo: Int, hi: Int, c: Comparator[_ >: K]): Unit = {
    timSort.sort(a, lo, hi, c)
  }
}