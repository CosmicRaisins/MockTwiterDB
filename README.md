# MockTwiterDB
Explore the performance limit of relational database with a Mock Twitter database and 1 million tweets.
# Schema
Twitter:  
tweets: tweet_id(BIGINT), user_id(BIGINT), tweet_ts(DATETIME), tweet_text(VARCHAR140)  
followers: user_id(BIGINT), follows_id(BIGINT)  
# PC Specs
CPU: AMD Ryzen Threadripper 2950X 16-Core @4.00 GHz  
RAM: 32GB @3200 MHz  
Storage: Samsung 850 EVO M.2 SSD, read @ 520 MB/s, write @ 540 MB/  

# Redis implementation
Tweets are stored in String-String pairs.  
Key: “tweet:userID:tweetID” Value: “tweetDatetime:tweetContent”  

In strategy 1, a users’ follows is stored in String-Set<String> pairs.  
Key: “follow:userID” Value: Set(“userID1”, “userID2”, ..., “userIDn”)  
In strategy 2, an additional follower pair is used to store users’ followers.  
Key: “follower:userID” Value: Set(“userID1”, “userID2”, ..., “userIDn”)  
In strategy 2, a String-List pair is used for a user’s timeline to maintain tweets’
datetime order. The tweet keys of the user’s home timeline is stored in the list.  
Key: “timeline:userID” Value: List(tweetKey1, tweetKey2, ..., tweetKeyn)  

Obeservations:
Pipelining significantly increased insertion performance with strategy one
and led to a five-fold speed increase compared to the similarly implemented MySQL
strategy.  
The getHomeTimeline method in strat1 first iterated over a user’s every
follow’s tweetKey and parsed/stored them in a TreeMap which is constantly trimed
according to the desired timeline length. It then retrieved values with those keys
and built and returned a list of custom Tweet class. The performance poor compared
to MySQL implementation possibly due to no secondary indexing.  
I could not get pipelining or multi to work with strat2 as Redis does not
allow the use of intermediate results of a transaction within the same transaction.
Could there be a clever workaround?  
Using mget for strat2’s timeline method allowed for a fast and simple
implementation.

# MySQL implementation
Generated tweets akin to "I can eat glass it doesnt hurt me." with word banks and a custom sentence generator.  
user_id represented by integers.  
500 users, each follows around 50 people. The amount of followers each person has follows a standard distribution.  
Insert 2500 tweets in one statement with "INSERT INTO tweets (f1,f2,f3,f4) VALUES (a1,b1,x1,y1),(a2,b2,x2,y2),..."  

Insertion speed: 58,788.95 tweets/s  
Timeline retrieval speed: 1.67 timelines/s  

Batch insertion dramatically increased insertion rate. I was really confused as the previous speed was around 300 per second but my CPU usage was barely 5%.  
Indexing the tweets table increased Timeline retrieval speed by from around 3 second per timeline to 0.56 second per timeline.  

