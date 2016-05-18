import org.nirvana._
import org.joda.time.{Period, DateTime, LocalDate, Duration, Days}
import java.io._

object AdjWithCorpActRatio {

  def multiplyWithRndg(a: Double, b: Double): Double = {
    val scale = 100000.0
    Math.round(a * b * scale) / scale
  }

  def LoadCorpActRatio(filepath: String): Map[String, List[(DateTime, Double)]] = {

    val lines = scala.io.Source.fromFile(filepath).getLines.toList

    val lsTup = lines.map(l => {
      val csv = l.split(",").toList
      (Config.dfmt.parseDateTime(csv(0)).withTime(23, 59, 59, 0), csv(1), csv(2).toDouble)
    })

    lsTup.groupBy(_._2).map { case (sym, lstup) => { (sym, lstup.map(x => (x._1, x._3)).sortBy(_._1.getMillis)) } }.toMap

  }

  def applyCorpActAdjRatio(mdis: MDIStruct, adjmap: Map[String, List[(DateTime, Double)]]): MDIStruct = {

    val newmdis =
      if (adjmap.contains(mdis.symbol)) {
        val lsadjdtratio = adjmap.get(mdis.symbol).get
        val lsCurOrLater = lsadjdtratio.filter(_._1.getMillis >= mdis.dt.getMillis)
        val ratioToApply = if (!lsCurOrLater.isEmpty) lsCurOrLater.head._2 else lsadjdtratio.last._2

        val newbidpv = mdis.bidpv.map(tup => if (Math.abs(tup._1 - SUtil.ATU_INVALID_PRICE) > SUtil.EPSILON) (multiplyWithRndg(tup._1, ratioToApply), multiplyWithRndg(tup._2, Math.ceil(1.0 / ratioToApply)).toLong) else (SUtil.ATU_INVALID_PRICE, 0L))
        val newaskpv = mdis.askpv.map(tup => if (Math.abs(tup._1 - SUtil.ATU_INVALID_PRICE) > SUtil.EPSILON) (multiplyWithRndg(tup._1, ratioToApply), multiplyWithRndg(tup._2, Math.ceil(1.0 / ratioToApply)).toLong) else (SUtil.ATU_INVALID_PRICE, 0L))

        mdis.
          copy(tradeprice = multiplyWithRndg(mdis.tradeprice, ratioToApply)).
          copy(tradevolume = multiplyWithRndg(mdis.tradevolume, Math.ceil(1.0 / ratioToApply)).toLong).
          copy(bidpv = newbidpv).
          copy(askpv = newaskpv)
      }
      else {
        mdis
      }

    newmdis

  }

  def applyCorpActAdjRatio(ohlc: OHLCBar, adjmap: Map[String, List[(DateTime, Double)]]): OHLCBar = {

    val newohlc =
      if (adjmap.contains(ohlc.symbol)) {
        val lsadjdtratio = adjmap.get(ohlc.symbol).get
        val lsCurOrLater = lsadjdtratio.filter(_._1.getMillis >= ohlc.dt.getMillis)
        //--------------------------------------------------
        // Bloomberg is doing backward adjustment, so by default we should assume the latest dates to have an adjustment ratio of 1.0
        //--------------------------------------------------
        val ratioToApply = if (!lsCurOrLater.isEmpty) lsCurOrLater.head._2 else 1.0

        val ohlcpb =
          if (Math.abs(ratioToApply - 1.0) > SUtil.SMALLNUM) {
            OHLCPriceBar(
              multiplyWithRndg(ohlc.priceBar.o, ratioToApply),
              multiplyWithRndg(ohlc.priceBar.h, ratioToApply),
              multiplyWithRndg(ohlc.priceBar.l, ratioToApply),
              multiplyWithRndg(ohlc.priceBar.c, ratioToApply),
              multiplyWithRndg(ohlc.priceBar.v, Math.ceil(1.0 / ratioToApply)).toLong
            )
          }
          else {
            ohlc.priceBar
          }

        ohlc.copy(priceBar = ohlcpb)
      }
      else {
        ohlc
      }

    newohlc

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
      val lsMDIDataFiles = SUtil.getFilesInDir(Config.data_folder)
      println(Config.data_folder)

      lsMDIDataFiles.foreach(infile => {
        val lsdatalines = scala.io.Source.fromFile(infile).getLines.toList

        val lsOutStr: List[String] =
          if (Config.data_fmt == "cashmdi") {
            val lssrcdata = lsdatalines.map(DataFmtAdaptors.parseCashMDI(_)).filter(_ != None).map(_.get)
            lssrcdata.map(x => applyCorpActAdjRatio(x, mapCorpActAdjRatio)).map(_.toString)
          }
          else if (Config.data_fmt == "cashohlcfeed") {
            val lssrcdata = lsdatalines.map(DataFmtAdaptors.parseCashOHLCFmt1(_, false)).filter(_ != None).map(_.get)
            lssrcdata.map(x => applyCorpActAdjRatio(x, mapCorpActAdjRatio)).map(_.toCashOHLCFeed("HKSE", 0))
          }
          else if (Config.data_fmt == "blmgohlc") {
            val lssrcdata = lsdatalines.map(DataFmtAdaptors.parseBlmgFmt1(_, true)).filter(_ != None).map(_.get)
            lssrcdata.map(x => applyCorpActAdjRatio(x, mapCorpActAdjRatio)).map(_.toCashOHLCFeed("HKSE", 0))
          }
          else List[String]()

        println(infile)

        val pw = new PrintWriter(new File(Config.output_folder + "/" + infile.getName))
        lsOutStr.foreach(x => { pw.write(x); pw.write('\n') })
        pw.close

      })
      //--------------------------------------------------

    }
}
