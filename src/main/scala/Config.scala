import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import org.joda.time.{DateTime, LocalTime}
import org.joda.time.format.DateTimeFormat

object Config {

  val dfmt = DateTimeFormat.forPattern("yyyy-MM-dd")

  def readPropFile(propFileName: String) {

    try {

      val prop = new Properties()
      prop.load(new FileInputStream(propFileName))

      mdi_data_folder = prop.getProperty("mdi_data_folder")
      output_folder = prop.getProperty("output_folder")
      corpActionRatioFile = prop.getProperty("corpActionRatioFile")

      println(mdi_data_folder)
      println(output_folder)
      println(corpActionRatioFile)
    }
    catch {
      case e: Exception =>
        {
          e.printStackTrace()
          sys.exit(1)
        }
    }
  }

  //--------------------------------------------------
  // data
  //--------------------------------------------------
  var mdi_data_folder = ""
  var output_folder = ""
  var corpActionRatioFile = ""

}
