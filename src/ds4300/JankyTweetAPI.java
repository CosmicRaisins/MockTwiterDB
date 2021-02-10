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
   * @param user the user whose follows will be retrieved
   * @return an ArrayList of Strings representing all the users that the user follows.
   */
  ArrayList<String> getFollows(String user);

  /**
   * insert one tweet into the database.
   * @param tweet the tweet to be inserted
   */
  void insertOneTweet(Tweet tweet);

  /**
   * insert multiple tweets to the database
   * @param tweets the list of tweets to be inserted
   */
  void insertXTweets(ArrayList<Tweet> tweets);

  /**
   * add one follower for a user.
   * @param follow an array representing who follows who
   */
  void addOneFollow(String[] follow);

  /**
   * add multiple follower for a user.
   * @param follow A list of arrays of pairs of followers and followees
   */
  void addXFollows(ArrayList<String[]> follow);

}
