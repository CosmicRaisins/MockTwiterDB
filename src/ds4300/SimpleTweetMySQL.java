package ds4300;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SimpleTweetMySQL implements JankyTweetAPI {

  private Connection connection;

  public SimpleTweetMySQL(String address, String user, String password) {
    try {
      this.connection = DriverManager.getConnection(
          address, user, password);
    } catch (Exception e){
      System.out.println(e);
    }
  }

  @Override

  public ArrayList<Tweet> getTimeline(String user, int numTweets) {
    ResultSet resultSet = null;
    ArrayList<Tweet> tweets = new ArrayList<>();

    String sql = "SELECT tweet_id, user_id, tweet_ts, tweet_text "
        + "FROM tweets JOIN followers ON tweets.user_id = followers.user_id "
        + String.format("WHERE follows_id = '%s' ", user)
        + String.format("ORDER BY tweets.tweet_ts DESC LIMIT %s", numTweets);

    try {
      resultSet = this.connection.createStatement().executeQuery(sql);
      while (resultSet.next()) {
        tweets.add(new Tweet(
            resultSet.getString("tweet_id"),
            resultSet.getString("user_id"),
            resultSet.getString("tweet_ts"),
            resultSet.getString("tweet_text")));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tweets;
  }





  /**
   *
   * @param user
   * @param numTweets
   * @return
   */
  @Override
  public ArrayList<Tweet> getTweets(String user, int numTweets) {

    ResultSet resultSet = null;
    ArrayList<Tweet> tweets = new ArrayList<>();

    String sql = String.format("SELECT * FROM tweets WHERE user_id = '%s' ORDER BY tweet_ts DESC LIMIT %s", user, numTweets);
    try {
      resultSet = this.connection.createStatement().executeQuery(sql);
      while (resultSet.next()) {
        tweets.add(new Tweet(
            resultSet.getString("tweet_id"),
            resultSet.getString("user_id"),
            resultSet.getString("tweet_ts"),
            resultSet.getString("tweet_text")));
      }
    } catch (SQLException e) {
      System.out.println("Error retrieving tweets: " + e);
    }
    return tweets;
  }

  @Override
  public ArrayList<String> getFollows(String user) {

    ResultSet resultSet = null;
    ArrayList<String> follows = new ArrayList<>();

    String sql = String.format("SELECT follows_id FROM followers WHERE user_id = '%s'", user);
    try {
      resultSet = this.connection.createStatement().executeQuery(sql);
      while (resultSet.next()) {
       follows.add(resultSet.getString("follows_id"));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return follows;
  }

  @Override
  public void insertOneTweet(Tweet tweet) {
    String sql = String.format(
        "INSERT INTO tweets (tweet_id,user_id,tweet_ts,tweet_text) VALUES ('%s','%s','%s','%s')",
        tweet.getTweetID(), tweet.getUserID(), tweet.getDatetime(), tweet.getContent()
    );
    try {
      this.connection.createStatement().executeUpdate(sql);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void insertXTweets(ArrayList<Tweet> tweets) {
    StringBuilder sql =
        new StringBuilder("INSERT INTO tweets (tweet_id,user_id,tweet_ts,tweet_text) VALUES ");
    Tweet first = tweets.remove(0);

    sql.append(String.format("('%s','%s','%s','%s')",
        first.getTweetID(),
        first.getUserID(),
        first.getDatetime(),
        first.getContent()));

    for (Tweet tweet : tweets) {
      sql.append(String.format(",('%s','%s','%s','%s')",
          tweet.getTweetID(),
          tweet.getUserID(),
          tweet.getDatetime(),
          tweet.getContent()));
    }

    try {
      Statement statement = this.connection.createStatement();
      statement.executeUpdate(sql.toString());
      statement.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void addFollowerMethod1(String file) {
    BufferedReader reader;
    String line;

    try {
      reader = new BufferedReader(new FileReader(file));
      while ((line = reader.readLine()) != null) {
        this.addOneFollow(line.split(","));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void addFollowerMethod2(String file, int x) {
    BufferedReader reader;
    ArrayList<String[]> toAdd = new ArrayList<>();
    String line;
    int lim = x - 1;

    try {
      reader = new BufferedReader(new FileReader(file));
      while ((line = reader.readLine()) != null) {
        if (toAdd.size() == lim) {
          toAdd.add(line.split(","));
          this.addXFollows(toAdd);
          toAdd = new ArrayList<>();
        } else {
          toAdd.add(line.split(","));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   *
   * @param follow
   */
  @Override
  public void addOneFollow(String[] follow) {
    String sql =
        String.format("INSERT INTO followers (user_id,follows_id) VALUES ('%s','%s')", follow[0], follow[1]);

    try {
      Statement statement = this.connection.createStatement();
      statement.executeUpdate(sql);
      statement.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }



  private void addXFollows(ArrayList<String[]> follow) {
    StringBuilder sql = new StringBuilder("INSERT INTO followers (user_id,follows_id) VALUES ");
    String[] first = follow.remove(0);
    sql.append(String.format("(%s,%s)", first[0], first[1]));

    for (String[] pair : follow) {
      sql.append(String.format(",(%s,%s)", pair[0], pair[1]));
    }

    try {
      Statement statement = this.connection.createStatement();
      statement.executeUpdate(sql.toString());
      statement.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }



  private void addTweetsMethod2(String file, int x) {
    BufferedReader reader;
    ArrayList<Tweet> toAdd = new ArrayList<>();
    String line;
    int lim = x - 1;

    try {
      reader = new BufferedReader(new FileReader(file));
      this.connection.createStatement().executeUpdate("CREATE INDEX idx_user_id ON tweets (user_id)");
      while ((line = reader.readLine()) != null) {
        String[] params = line.split(",");
        Tweet tweet = new Tweet(params[0], params[1], params[2], params[3]);

        if (toAdd.size() == lim) {
          toAdd.add(tweet);
          this.insertXTweets(toAdd);
          toAdd = new ArrayList<>();
        } else {
          toAdd.add(tweet);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    SimpleTweetMySQL test = new SimpleTweetMySQL(
        "jdbc:mysql://localhost:3306/twitter",
        "CosmicRaisins",
        "Raisins1984");



    String follows = "resources\\followers.csv";
    String tweets = "resources\\tweets.csv";
    BufferedReader reader;
    String line = "";
    ArrayList<Tweet> allTweets = new ArrayList<>();

//    System.out.println(test.getTimeline("1", 10));

//    test.addFollowerMethod1(follows);
    test.addFollowerMethod2(follows, 2582);

//    long loadTweetStartTime = System.nanoTime();
//
//    test.addTweetsMethod2(tweets, 2500);
//    long loadTweetStopTime = System.nanoTime();
//    System.out.println(String.format("Time took to insert tweets: %s",
//        loadTweetStopTime - loadTweetStartTime));



//    try {
//      reader = new BufferedReader(new FileReader(tweets));
//      while ((line = reader.readLine()) != null) {
//        String[] tweet = line.split(",");
//        allTweets.add(new Tweet(tweet[0], tweet[1], tweet[2], tweet[3]));
//      }
//    } catch (Exception e) {
//      System.out.println(e);
//    }
//
//    long loadTweetStartTime = System.nanoTime();
//
//    for (Tweet twt : allTweets){
//      test.insertTweet(twt);
//    }
//
//    long loadTweetStopTime = System.nanoTime();
//    System.out.println(String.format("Time took to insert tweets: %s",
//         loadTweetStopTime - loadTweetStartTime));







  }
}
