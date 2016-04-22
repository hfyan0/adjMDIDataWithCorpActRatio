import org.nirvana._
import org.joda.time.{Period, DateTime, LocalDate, Duration, Days}
import java.io._

object AdjMDIWithCorpActRatio {

  def LoadCorpActRatio(filepath: String): Map[String, List[(DateTime, Double)]] = {

    val lines = scala.io.Source.fromFile(filepath).getLines.toList

    val lsTup = lines.map(l => {
      val csv = l.split(",").toList
      (Config.dfmt.parseDateTime(csv(0)).withTime(23, 59, 59, 0), csv(1), csv(2).toDouble)
    })

    lsTup.groupBy(_._2).map { case (sym, lstup) => { (sym, lstup.map(x => (x._1, x._3)).sortBy(_._1.getMillis)) } }.toMap

  }

  def applyCorpActAdjRatio(mdis: MDIStruct, adjmap: Map[String, List[(DateTime, Double)]]): MDIStruct = {

    def multiplyWithRndg(a: Double, b: Double): Double = {
      val scale = 100000.0
      Math.round(a * b * scale) / scale
    }

    val newmdis =
      if (adjmap.contains(mdis.symbol)) {
        val lsadjdtratio = adjmap.get(mdis.symbol).get
        val lsCurOrLater = lsadjdtratio.filter(_._1.getMillis >= mdis.dt.getMillis)
        val ratioToApply = if (!lsCurOrLater.isEmpty) lsCurOrLater.head._2 else lsadjdtratio.last._2

        val newbidpv = mdis.bidpv.map(tup => if (Math.abs(tup._1 - SUtil.ATU_INVALID_PRICE) > SUtil.EPSILON) (multiplyWithRndg(tup._1, ratioToApply), multiplyWithRndg(tup._2, 1.0 / ratioToApply).toLong) else (SUtil.ATU_INVALID_PRICE, 0L))
        val newaskpv = mdis.askpv.map(tup => if (Math.abs(tup._1 - SUtil.ATU_INVALID_PRICE) > SUtil.EPSILON) (multiplyWithRndg(tup._1, ratioToApply), multiplyWithRndg(tup._2, 1.0 / ratioToApply).toLong) else (SUtil.ATU_INVALID_PRICE, 0L))

        mdis.
          copy(tradeprice = multiplyWithRndg(mdis.tradeprice, ratioToApply)).
          copy(tradevolume = multiplyWithRndg(mdis.tradevolume, 1.0 / ratioToApply).toLong).
          copy(bidpv = newbidpv).
          copy(askpv = newaskpv)
      }
      else {
        mdis
      }

    newmdis

  }

  def main(args: Array[String]) =
    {
      if (args.length == 0) {
        println("USAGE   java -jar ... [properties file]")
        System.exit(1)
      }

      Config.readPropFile(args(0))

      //--------------------------------------------------
      // load corp_action_adj
      //--------------------------------------------------
      val mapCorpActAdjRatio = LoadCorpActRatio(Config.corpActionRatioFile)
      // println(mapCorpActAdjRatio)

      //--------------------------------------------------
      val lsMDIDataFiles = SUtil.getFilesInDir(Config.mdi_data_folder)
      println(Config.mdi_data_folder)

      lsMDIDataFiles.foreach(mdifile => {
        val lsdatalines = scala.io.Source.fromFile(mdifile).getLines.toList
        val lsmdis = lsdatalines.map(DataFmtAdaptors.parseCashMDI(_)).filter(_ != None).map(_.get)
        val lsOutStr = lsmdis.map(x => applyCorpActAdjRatio(x, mapCorpActAdjRatio)).map(_.toString)

        println(mdifile)

        val pw = new PrintWriter(new File(Config.output_folder + "/" + mdifile.getName))
        lsOutStr.foreach(x => { pw.write(x); pw.write('\n') })
        pw.close

      })
      //--------------------------------------------------

    }
}
