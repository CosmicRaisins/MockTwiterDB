package ds4300;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Strategy 2: keep track of followers and broadcast to every follower's timeline whenever a new tweet is posted.
 */
public class SimpleTweetRedisStrat2 implements LessJankyTweetAPI {

    private final Jedis jedis;

    /**
     * default constructor for the mock Twitter Reids API, using local instance of Redis.
     */
    public SimpleTweetRedisStrat2() {
        this.jedis = new Jedis("localhost");
    }

    /**
     * construct a Redis twitter api with a user defined host.
     * @param host redis server
     */
    public SimpleTweetRedisStrat2(String host) {
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
        List<Tweet> timeline = new ArrayList<>();

        List<String> tweetKeys = this.jedis.lrange("timeline:" + user, 0, numTweets - 1);
        List<String> allTweets = this.jedis.mget(tweetKeys.toArray(new String[numTweets]));
        for (String tweet : allTweets) {
            String key[] = tweetKeys.remove(0).substring(6).split(":");
            timeline.add(new Tweet(
                    key[1], // tweetID
                    key[0], // userID
                    tweet.substring(0, 19), // tweetDatetime
                    tweet.substring(20) //tweetContent
            ));
        }
        return timeline;
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

        Set<String> followers = this.jedis.smembers("follower:" + tweet.getUserID());
        Pipeline pipeline = this.jedis.pipelined();
        for (String follower : followers) {
            pipeline.lpush("timeline:" + follower, keyValue[0]);
        }
        pipeline.sync();
    }

    @Override
    public void insertXTweets(List<Tweet> tweets) {

        for (Tweet tweet : tweets) {
            this.insertOneTweet(tweet);
        }

//        failed experiment at pipelining... :(

//        Set<Response<Set<String>>> responses = new HashSet<>();
//        Pipeline pipeline = this.jedis.pipelined();
//        for (Tweet tweet : tweets) {
//            String[] keyValue = this.parseTweet(tweet);
//            pipeline.set(keyValue[0], keyValue[1]);
//            responses.add(pipeline.smembers("follower:" + tweet.getUserID()));
//        }
//        pipeline.sync();
//
//        Set<String> followers = this.parseResponses(responses);
//        for (Tweet tweet : tweets) {
//            String[] keyValue = this.parseTweet(tweet);
//            for (String follower : followers) {
//                pipeline.lpush("timeline:" + follower, keyValue[0]);
//            }
//        }
//        pipeline.sync();
    }

    private Set<String> parseResponses(Set<Response<Set<String>>> responses) {
        Set<String> result = new HashSet<>();
        for (Response<Set<String>> response : responses) {
            result.addAll(response.get());
        }
        System.out.println(result.size());
        return result;
    }


    @Override
    public void addOneFollow(String[] follow) {
        this.jedis.sadd("follow:" + follow[0], follow[1]);
        this.jedis.sadd("follower:" + follow[1], follow[0]);
    }

    @Override
    public void addXFollows(List<String[]> follows) {
        Pipeline pipeline = this.jedis.pipelined();
        while (!follows.isEmpty()) {
            String[] keyValue = follows.remove(0);
            pipeline.sadd("follow:" + keyValue[0], keyValue[1]);
            pipeline.sadd("follower:" + keyValue[1], keyValue[0]);
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

        SimpleTweetRedisStrat2 api = new SimpleTweetRedisStrat2();

        api.reset();

        api.insertFollowFromFile(follows, 1000);

        long loadTweetStartTime = System.currentTimeMillis();
        api.insertTweetFromFile(tweets, 1000);
        long loadTweetFinishTime = System.currentTimeMillis();
        System.out.println(String.format("Load tweet time: %s milliseconds", (loadTweetFinishTime - loadTweetStartTime)));

        long timelineStartTime = System.currentTimeMillis();
        for (int i = 0; i < 500000; i++)
            System.out.println(api.getTimeline("CosmicRaisins3493", 10).toString());
        long timelineFinishTime = System.currentTimeMillis();
        System.out.println(String.format("Time took for 500k timeline retrieval: %s milliseconds",
                (timelineFinishTime - timelineStartTime)));

    }
}
