package com.ndn.tools

import scala.collection.mutable

/**
 * Created by ndn on 3/9/2015.
 */
object SimpleArgumentParser {
  def parse(args: Array[String]) = {
    val map = new mutable.HashMap[String, String]()
    for (v <- args) {
      val arr = v.split("=", 2)
      if (arr.size > 1) {
        map += arr(0) -> arr(1)
      }
    }
    map.toMap
  }
}
