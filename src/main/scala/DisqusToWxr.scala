import scala.xml._

object DisqusToWxr {
  def main(args: Array[String]): Unit = {
    if (args.length < 1 || args.length > 2) {
      println("Usage: DisqusToWxr disqus_xml_file [thread_id]")
    } else {
      val disqusXmlFile = args(0).trim
      val threadIdFilter = if (args.length >= 2) Some(args(1).trim) else None

      val disqusXml = XML.loadFile(disqusXmlFile)
      val wxrXml = this.wxr(disqusXml, threadIdFilter)
      XML.save(s"$disqusXmlFile.wxr.xml", wxrXml, "UTF-8")
    }
  }

  def wxr(disqus: Elem, threadIdFilter: Option[String] = None): Elem = {
    val threads = this.threads(disqus)
    val posts = this.posts(disqus)

    <rss version="2.0"
      xmlns:content="http://purl.org/rss/1.0/modules/content/"
      xmlns:dsq="http://www.disqus.com/"
      xmlns:dc="http://purl.org/dc/elements/1.1/"
      xmlns:wp="http://wordpress.org/export/1.0/">{
        for {
          thread <- threads
          if threadIdFilter.map(_ == thread.id).getOrElse(true)
        } yield {
          <item>
            <title>{ thread.title }</title>
            <link>{ thread.link }</link>
            <content:encoded>{ Unparsed(s"<![CDATA[${thread.content}]]>") }</content:encoded>
            <dsq:thread_identifier>{ thread.id }</dsq:thread_identifier>
            <wp:post_date_gmt>{ thread.date }</wp:post_date_gmt>
            <wp:comment_status>open</wp:comment_status>
            {
              for {
                post <- posts.filter(_.threadId == thread.id)
              } yield {
                <wp:comment>
                  <wp:comment_id>{ post.id }</wp:comment_id>
                  <wp:comment_author>{ post.author }</wp:comment_author>
                  <wp:comment_author_email>{ post.authorEmail }</wp:comment_author_email>
                  <!-- <wp:comment_author_url></wp:comment_author_url> -->
                  <wp:comment_author_IP>{ post.authorIp }</wp:comment_author_IP>
                  <wp:comment_date_gmt>{ post.date }</wp:comment_date_gmt>
                  <wp:comment_content>{ Unparsed(s"<![CDATA[${post.content}]]>") }</wp:comment_content>
                  <wp:comment_approved>1</wp:comment_approved>
                  <wp:comment_parent>0</wp:comment_parent>
                </wp:comment>
              }
            }
          </item>
        }
      }</rss>
  }

  protected def threads(disqus: Elem): Seq[Thread] =
    for {
      post <- disqus \ "thread"
      id = post.singleAttribute(DisqusUri, "id")
      link = (post \ "link").text
      title = (post \ "title").text
      content = (post \ "message").text
      date = (post \ "createdAt").text.replaceAll("T", " ").replaceAll("Z", "")
    } yield Thread(
      id = id,
      link = link,
      title = title,
      content = content,
      date = date)

  protected def posts(disqus: Elem): Seq[Post] =
    for {
      post <- disqus \ "post"
      id = post.singleAttribute(DisqusUri, "id")
      threadId = (post \ "thread").head.singleAttribute(DisqusUri, "id")
      author = (post \ "author" \ "name").text
      authorEmail = (post \ "author" \ "email").text
      authorIp = (post \ "ipAddress").text
      content = (post \ "message").text
      date = (post \ "createdAt").text.replaceAll("T", " ").replaceAll("Z", "")
    } yield Post(
      id = id,
      threadId = threadId,
      author = author,
      authorEmail = authorEmail,
      authorIp = authorIp,
      content = content,
      date = date)

  implicit class NodeOps(node: Node) {
    def singleAttribute(uri: String, key: String): String =
      node.attribute(uri, key).flatMap(_.headOption).map(_.text).getOrElse("")
  }

  case class Thread(id: String, link: String, title: String, content: String, date: String)

  /*
  <post dsq:id="1764461582">
      <id>4627219507490294554</id>
      <message>
          <![CDATA[<p>Oliver, <br>
          <br>this is a great description - very helpful and very detailed! How did you figure out the coordinate system issues? What a challenge!<br>
          <br>If you happen to have GPS tracks in either GPS or NMEA format, also take a look at Fodysseus as a slick alternative to Robogeo: very intuitive automated geotagging of photographs! <a href="http://www.fodysseus.com" rel="nofollow">http://www.fodysseus.com</a>
      </p>]]>
  </message>
  <createdAt>2006-11-08T10:56:00Z</createdAt>
  <isDeleted>false</isDeleted>
  <isSpam>false</isSpam>
  <author>
      <email>ole_seidel@blogger.disqus.net</email>
      <name>Ole Seidel</name>
      <isAnonymous>true</isAnonymous>
  </author>
  <ipAddress>255.255.255.255</ipAddress>
  <thread dsq:id="3373411701"/>
  </post>
   */

  case class Post(id: String,
                  threadId: String,
                  author: String,
                  authorEmail: String,
                  authorIp: String,
                  content: String,
                  date: String)

  protected val DisqusUri = "http://disqus.com/disqus-internals"
}

