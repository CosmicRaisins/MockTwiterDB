package ds4300;

/**
 * a class tha represents a tweet in the mock twitter database.
 */
public class Tweet {

  private String tweetID;
  private String userID;
  private String datetime;
  private String content;

  /**
   * constructs a tweet
   * @param tweetID id for the tweet
   * @param userID id for the user who tweeted
   * @param datetime the date and time this tweet is sent
   * @param content the textual content of the tweet. cannot exceed 140 characters.
   */
  public Tweet(String tweetID, String userID, String datetime, String content) {
    this.tweetID = tweetID;
    this.userID = userID;
    this.datetime = datetime;
    if (content.length() <= 140) {
      this.content = content;
    } else {
      throw new IllegalArgumentException("Tweet cannot exceed 140 characters.");
    }
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
