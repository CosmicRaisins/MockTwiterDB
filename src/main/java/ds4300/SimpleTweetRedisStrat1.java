package ds4300;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Strategy 1: insert and retrieve like the MySQL implementation
 */
public class SimpleTweetRedisStrat1 implements LessJankyTweetAPI {

    private final Jedis jedis;

    /**
     * default constructor for the mock Twitter Reids API, using local instance of Redis.
     */
    public SimpleTweetRedisStrat1() {
        this.jedis = new Jedis("localhost");
    }

    /**
     * construct a Redis twitter api with a user defined host.
     * @param host redis server
     */
    public SimpleTweetRedisStrat1(String host) {
        this.jedis = new Jedis(host);
    }

    /**
     * reset the current database.
     */
    public void reset() {
        jedis.flushDB();
    }

    /**
     * parse a tweet from object into redis format.
     * @param tweet the tweet to be parsed.
     * @return a String[], String[0]: key(tweet:userID,tweetDatetime+tweetID), String[1] value(tweetContent).
     */
    private String[] parseTweet(Tweet tweet) {
        String time  = tweet.getDatetime().replaceAll("[^\\d]", "");
        String key   = String.format("tweet:%s:%s",tweet.getUserID() ,tweet.getTweetID());
        String value = tweet.getDatetime() + ":" + tweet.getContent();
        return new String[] {key, value};
    }

    @Override
    public List<Tweet> getTimeline(String user, int numTweets) {
        Set<String> follows = this.getFollows(user);
        return getTweetsFromKetPairs(getKeyPairs(follows, numTweets), numTweets);
    }

    // helper function for getTimeline for testing purposes. Store key pairs in a TreeMap to maintain time order and for
    // ease of reference later.
    private TreeMap<Long, String> getKeyPairs(Set<String> follows, int numTweets) {
        TreeMap<Long, String> tweetAndUserIDs = new TreeMap<>(Collections.reverseOrder());
        for (String id : follows) {
            List<String> tweetKeyQueryResult = new ArrayList<String>(this.jedis.keys(String.format("tweet:%s:*", id)));

            for (String tweetKey : tweetKeyQueryResult) {
                // add new pair to the treemap.
                tweetAndUserIDs.put(
                        Long.parseLong(tweetKey.substring(tweetKey.lastIndexOf(":") + 1)), // tweetID
                        tweetKey.substring(6, tweetKey.lastIndexOf(":"))); // userID
                // remove last item if tweets over desired amount
                if (tweetAndUserIDs.size() > numTweets) {
                    tweetAndUserIDs.pollLastEntry();
                }
            }
        }
        return tweetAndUserIDs;
    }

    // helper function for getTimeline for testing purposes. Parse a Map into a List of Tweets.
    private List<Tweet> getTweetsFromKetPairs(Map<Long, String> pairs, int numTweets) {
        List<Tweet> result = new ArrayList<>();
        List<String> queryParams = new ArrayList();
        for (Map.Entry<Long, String> entry : pairs.entrySet()) {
            queryParams.add(String.format("tweet:%s:%s", entry.getValue(), entry.getKey()));
        }
        List<String> datetimeAndContents = this.jedis.mget(queryParams.toArray(new String[numTweets]));

        for (Map.Entry<Long, String> entry : pairs.entrySet()) {
            String datetimeAndContent = datetimeAndContents.remove(0);
            result.add(new Tweet(
                    entry.getKey().toString(),
                    entry.getValue(),
                    datetimeAndContent.substring(0, 19),
                    datetimeAndContent.substring(20)));
        }
        return result;
    }

    @Override
    public ArrayList<Tweet> getTweets(String user, int numTweets) {
        return null;
    }

    @Override
    public Set<String> getFollows(String user) {
        return new HashSet<>(this.jedis.smembers("follow:" + user));
    }

    @Override
    public void insertOneTweet(Tweet tweet) {
        String[] keyValue = this.parseTweet(tweet);
        this.jedis.set(keyValue[0], keyValue[1]);
    }

    @Override
    public void insertXTweets(List<Tweet> tweets) {
        Pipeline pipeline = this.jedis.pipelined();
        for (Tweet tweet : tweets) {
            String[] keyValue = this.parseTweet(tweet);
            pipeline.set(keyValue[0], keyValue[1]);
        }
        pipeline.sync();
    }


    @Override
    public void addOneFollow(String[] follow) {
        this.jedis.sadd("follow:" + follow[0], follow[1]);
    }

    @Override
    public void addXFollows(List<String[]> follows) {
        Pipeline pipeline = this.jedis.pipelined();
        while (!follows.isEmpty()) {
            String[] keyValue = follows.remove(0);
            pipeline.sadd("follow:" + keyValue[0], keyValue[1]);
        }
        pipeline.sync();
    }

    /**
     * parse and insert follower relationship from a csv file.
     * @param file csv file address
     * @param batchSize how many key value pair to insert in one operation
     */
    private void insertFollowFromFile(String file, int batchSize) {
        BufferedReader reader;
        ArrayList<String[]> toAdd = new ArrayList<>();
        String line;
        int lim = batchSize - 1;

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
     * parse and insert tweets from a csv file.
     * @param file csv file address
     * @param batchSize how tweets to insert in one operation
     */
    private void insertTweetFromFile(String file, int batchSize) {
        BufferedReader reader;
        ArrayList<Tweet> toAdd = new ArrayList<>();
        String line;
        int lim = batchSize - 1;

        try {
            reader = new BufferedReader(new FileReader(file));
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

    public static void main(String[] args) throws Exception {
        String follows = "resources/followers.csv";
        String tweets = "resources/tweets.csv";

        SimpleTweetRedisStrat1 api = new SimpleTweetRedisStrat1();

        api.reset();
        api.insertFollowFromFile(follows, 1000);

        long loadTweetStartTime = System.currentTimeMillis();
        api.insertTweetFromFile(tweets, 2000);
        long loadTweetFinishTime = System.currentTimeMillis();
        System.out.println(String.format("Time took for 1m tweet insertions: %s milliseconds",
                (loadTweetFinishTime - loadTweetStartTime)));

        long timelineStartTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++)
        System.out.println(api.getTimeline("JuicyBody4963", 10).toString());
        long timelineFinishTime = System.currentTimeMillis();
        System.out.println(String.format("Time took for 3 timeline retrieval: %s milliseconds",
                (timelineFinishTime - timelineStartTime)));
    }
}
