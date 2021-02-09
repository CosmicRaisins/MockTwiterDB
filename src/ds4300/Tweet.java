package ds4300;

public class Tweet {

  private String tweetID;
  private String userID;
  private String datetime;
  private String content;

  /**
   *
   * @param tweetID
   * @param userID
   * @param datetime
   * @param content
   */
  public Tweet(String tweetID, String userID, String datetime, String content) {
    this.tweetID = tweetID;
    this.userID = userID;
    this.datetime = datetime;
    this.content = content;
  }

  @Override
  public String toString() {
    return String.join(", ", this.tweetID, this.userID, this.datetime.toString(), this.content);
  }

  public String getTweetID() {
    return tweetID;
  }

  public String getUserID() {
    return userID;
  }

  public String getDatetime() {
    return datetime;
  }

  public String getContent() {
    return content;
  }
}
