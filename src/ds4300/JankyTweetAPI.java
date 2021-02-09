package ds4300;

import java.util.ArrayList;

/**
 * An API for connecting to a mock twitter database.
 */
public interface JankyTweetAPI {

  /**
   * retrieve a user's timeline of tweets from the database.
   * @param user the user whose timeline will be retrieved
   * @param numTweets number of tweets to be retrieved
   * @return the user's timeline as a ArrayList of Tweets.
   */
  ArrayList<Tweet> getTimeline(String user, int numTweets);

  /**
   * retrieve a user's own tweets.
   * @param user the user whose tweets will be retrieved
   * @param numTweets number of tweets to be retrieved
   * @return the user's tweets as a ArrayList of {@link Tweet}.
   */
  ArrayList<Tweet> getTweets(String user, int numTweets);

  /**
   * retrieve a user's own tweets.
   * @param user
   * @return
   */
  ArrayList<String> getFollows(String user);

  public void insertOneTweet(Tweet tweet);

  public void insertXTweets(ArrayList<Tweet> tweets);

  public void addOneFollow(String[] follow);

}
