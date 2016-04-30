import org.nirvana._
import org.scalatest.junit.AssertionsForJUnit
import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import org.junit.Before
import org.joda.time.{Period, DateTime, LocalDate, Duration, Days}

class AdjWithCorpActRatioTest extends AssertionsForJUnit {

    val dt1 = new DateTime(2016, 1, 4, 23, 59, 59)
    val dt2 = new DateTime(2014, 1, 4, 23, 59, 59)

    val bidpv: List[(Double,Long)] = List((234.0, 0), (235.0, 1), (236.0, 2), (237.0, 3), (238.0, 4))
    val askpv: List[(Double,Long)] = List((1234.0, 0), (1235.0, 1), (1236.0, 2), (1237.0, 3), (1238.0, 4))

  @Test def testApplyCorpActAdjRatio1() {

    val mdis = MDIStruct(
      new DateTime(2016, 1, 3, 12, 45, 0),
      "DUMMY",
      80.2,
      1000,
      bidpv,
      askpv
    )

    var adjmap = Map[String, List[(DateTime, Double)]]()
    adjmap += "DUMMY" -> List((dt2, 0.2),(dt1, 1.2))

    val mdis1 = AdjWithCorpActRatio.applyCorpActAdjRatio(mdis, adjmap)
    assertEquals(mdis1.tradeprice, 80.2*1.2, 0.01)
    assertEquals(mdis1.tradevolume, (1000.0/1.2).toLong, 0.01)
    assertEquals(mdis1.bidpv(2)._1, 236.0*1.2, 0.01)
    assertEquals(mdis1.bidpv(2)._2, (2.0/1.2).toLong, 0.01)
    assertEquals(mdis1.askpv(4)._1, 1238.0*1.2, 0.01)
    assertEquals(mdis1.askpv(4)._2, (4.0/1.2).toLong, 0.01)
  }

  @Test def testApplyCorpActAdjRatio2() {

    val mdis = MDIStruct(
      new DateTime(2016, 1, 6, 12, 45, 0),
      "DUMMY",
      80.2,
      1000,
      bidpv,
      askpv
    )

    var adjmap = Map[String, List[(DateTime, Double)]]()
    adjmap += "DUMMY" -> List((dt2, 0.2),(dt1, 1.2))

    val mdis1 = AdjWithCorpActRatio.applyCorpActAdjRatio(mdis, adjmap)
    assertEquals(mdis1.tradeprice, 80.2*1.2, 0.01)
    assertEquals(mdis1.tradevolume, (1000.0/1.2).toLong, 0.01)
    assertEquals(mdis1.bidpv(2)._1, 236.0*1.2, 0.01)
    assertEquals(mdis1.bidpv(2)._2, (2.0/1.2).toLong, 0.01)
    assertEquals(mdis1.askpv(4)._1, 1238.0*1.2, 0.01)
    assertEquals(mdis1.askpv(4)._2, (4.0/1.2).toLong, 0.01)
  }

  @Test def testApplyCorpActAdjRatio3() {

    val mdis = MDIStruct(
      new DateTime(2011, 1, 6, 12, 45, 0),
      "DUMMY",
      80.2,
      1000,
      bidpv,
      askpv
    )

    var adjmap = Map[String, List[(DateTime, Double)]]()
    adjmap += "DUMMY" -> List((dt2, 0.2),(dt1, 1.2))

    val mdis1 = AdjWithCorpActRatio.applyCorpActAdjRatio(mdis, adjmap)
    assertEquals(mdis1.tradeprice, 80.2*0.2, 0.01)
    assertEquals(mdis1.tradevolume, (1000.0/0.2).toLong, 0.01)
    assertEquals(mdis1.bidpv(2)._1, 236.0*0.2, 0.01)
    assertEquals(mdis1.bidpv(2)._2, (2.0/0.2).toLong, 0.01)
    assertEquals(mdis1.askpv(4)._1, 1238.0*0.2, 0.01)
    assertEquals(mdis1.askpv(4)._2, (4.0/0.2).toLong, 0.01)
  }



}
