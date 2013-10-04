import com.google.gdata.client.webmastertools.WebmasterToolsService
import com.google.gdata.data.webmastertools.{CrawlIssueEntry, CrawlIssuesFeed}
import java.io.File
import java.net.{URLEncoder, URL}
import java.sql.DriverManager
import scala.collection.JavaConversions._
import com.gu.conf.{Configuration, ConfigurationFactory}

object GrabErrorLog extends App {
  val FEEDS_PATH = "https://www.google.com/webmasters/tools/feeds/"
  val SITE = "http://www.theguardian.com/"
  val configuration: Configuration = ConfigurationFactory.getConfiguration("webmastertoolkit")


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
  val db = DriverManager.getConnection("jdbc:sqlite:output/webmastertoolkit.db")
  db.setAutoCommit(false)

  val statement = db.createStatement()
  statement.executeUpdate("create table errors (url text);")

  Range(1, 100).foreach {
    i =>
      val url = getCrawlIssuesPageUrl(SITE, i * 1000, 1000)
      val feed: Option[CrawlIssuesFeed] = Option(service.getFeed(url, classOf[CrawlIssuesFeed]))
      feed.foreach {
        x =>
          val entries = x.getEntries()
          entries.foreach {
            entry =>
              val value = entry.getUrl().getUrl().replace("'", "%27")

              val insert = s"""INSERT INTO errors(url) values ('$value');"""
              statement.executeUpdate(insert.toString)
          }
      }

  }
  statement.close()
  db.commit()
  db.close()
}