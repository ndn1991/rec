package assets.consts

/**
 * Created by ndn on 4/8/2015.
 */
object Const {
  val apiHosts = "http://localhost:9000"
  val apiGetRoot = s"$apiHosts/root"
  val apiGetChildren = s"$apiHosts/scs?root="
  val apiAncestor = s"$apiHosts/ancestor?cat="
  val apiGetProducts = s"$apiHosts/ps?ids="
  val apiGetProduct = s"$apiHosts/p?p="
  val apiGetShortProducts = s"$apiHosts/sps?cat=%d&index=%d"
}
