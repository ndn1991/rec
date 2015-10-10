package assets.dao

import java.io.{BufferedReader, InputStreamReader}

import assets.model.Cate
import play.api.Play
import play.api.Play.current

import scala.collection.mutable

/**
 * Created by ndn on 4/8/2015.
 */
object CategoryDAO {

  val (map1, map2) = loadCat()

  private def loadCat() = {
    val reader = new BufferedReader(new InputStreamReader(Play.application.classloader.getResourceAsStream("cat.dat")))
    var line: String = null
    val map1 = mutable.HashMap[Int, (Int, String, Int, Array[(Int, String)])]()
    val map2 = mutable.HashMap[Int, mutable.ArrayBuffer[(Int, String, Int, Array[(Int, String)])]]().withDefault(mutable.ArrayBuffer())
    while ((line = reader.readLine()) != null) {
      val es = line.split("\t")
      val cat = (es(0).toInt, es(1), es(2).toInt, es(3), es(4).toInt, es(6))
      if (cat._5 == 1) { //Active
        val path = cat._4.split(",").zip(cat._6.split(">>")).map(v => (v._1.toInt, v._2))
        map1 += cat._1 -> (cat._1, cat._2, cat._3, path)
        map2(cat._3) += ((cat._1, cat._2, cat._3, path))
      }
    }
    (map1, map2)
  }


  def getRootCate() = {
    val level1s = mutable.ArrayBuffer[Cate]()
    map2(0).map(_cat => {
      val children = mutable.ArrayBuffer[Cate]()
      map2(_cat._1).map(_cat2 => {
        children += Cate(_cat2._1, _cat2._2)
      })
      val catLevel1 = Cate(_cat._1, _cat._2, children.toArray)
      level1s += catLevel1
    })
    val root = Cate(0, "root", level1s.toArray)
    root
  }
}
