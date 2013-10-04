import com.google.gdata.client.webmastertools.WebmasterToolsService
import com.google.gdata.data.webmastertools.{CrawlIssueEntry, CrawlIssuesFeed}
import java.net.{URLEncoder, URL}
import java.sql.DriverManager
import scala.collection.JavaConversions._
import com.gu.conf.{Configuration, ConfigurationFactory}
import com.github.nscala_time.time.Imports._

object GrabErrorLog extends App {
  val FEEDS_PATH = "https://www.google.com/webmasters/tools/feeds/"
  val SITE = "http://www.theguardian.com/"
  val configuration: Configuration = ConfigurationFactory.getConfiguration("webmastertoolkit")

  def escapeSingleQuotes(input: String) = input.replace("'", "%27")

  def getCrawlIssuesPageUrl(site: String, start: Int, count: Int): URL = {
    val crawlIssuesUrl: String = FEEDS_PATH + URLEncoder.encode(site, "UTF-8") + "/crawlissues/" + "?start-index=" + start + "&max-results=" + count
    return new URL(crawlIssuesUrl)
  }

  Console.println("Going to try to get the logs")

  val username= configuration.getStringProperty("gdata.username").getOrElse(throw new RuntimeException("Google Webmastertoolkit Username not specified"))
  val password= configuration.getStringProperty("gdata.password").getOrElse(throw new RuntimeException("Google Webmastertoolkit password not specified"))

  val service = new WebmasterToolsService("Guardian-404-Log-Fetcher")
  service.setUserCredentials(username, password)

  Class.forName("org.sqlite.JDBC")
  val now = DateTimeFormat.forPattern("yyyyMMdd-HHmm").print(DateTime.now)
  val db = DriverManager.getConnection(s"jdbc:sqlite:output/webmastertoolkit-$now.db")
  db.setAutoCommit(false)

  val statement = db.createStatement()
  statement.executeUpdate("""create table errors (url text,
                                                | crawlType text,
                                                | error text,
                                                | linkedFrom text,
                                                | dateDetected text);""".stripMargin
  )


  Range(1, 100).foreach {
    i =>
      val url = getCrawlIssuesPageUrl(SITE, i * 1000, 1000)
      val feed: Option[CrawlIssuesFeed] = Option(service.getFeed(url, classOf[CrawlIssuesFeed]))
      feed.foreach {
        x =>
          val entries = x.getEntries()
          entries.foreach {
            entry =>
              val url = escapeSingleQuotes(entry.getUrl().getUrl())
              val crawlType = entry.getCrawlType.getCrawlType
              val error = entry.getDetail.getDetail
              val linkedFrom = entry.getLinkedFroms.map(_.getLinkedFromUrl()).map(escapeSingleQuotes(_)).mkString("; ")
              val dateDetected = entry.getDateDetected.getDateDetected

              val insert = s"""INSERT INTO errors(url, crawlType, error, linkedFrom, dateDetected) values ('$url', '$crawlType', '$error', '$linkedFrom', '$dateDetected');"""
              println(insert)
              statement.executeUpdate(insert)
          }
      }

  }
  statement.close()
  db.commit()
  db.close()
}