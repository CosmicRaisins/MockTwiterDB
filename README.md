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
# Current Design
Generated tweets akin to "I can eat glass it doesnt hurt me." with word banks and a custom sentence generator.  
user_id represented by integers.  
500 users, each follows around 50 people. The amount of followers each person has follows a standard distribution.  
Insert 2500 tweets in one statement with "INSERT INTO tweets (f1,f2,f3,f4) VALUES (a1,b1,x1,y1),(a2,b2,x2,y2),..."  

Insertion speed: 58,788.95 tweets/s  
Timeline retrieval speed: 1.67 timelines/s  

Batch insertion dramatically increased insertion rate. I was really confused as the previous speed was around 300 per second but my CPU usage was barely 5%.  
Indexing the tweets table increased Timeline retrieval speed by from around 3 second per timeline to 0.56 second per timeline.  
# Changes from previous Iteration
1. User_id generation was more complex using a system inspired by Xbox Live's gamer tag system with the same word banks. Exmaples: "TastyWater3344" "AvuncularIce4894" "CosmicVeggies9375." This was later sacrified for performance and an integer based system was used instead. Such a shame, I really loved how the wacky names and tweets worked together.

2. Tried retriving the top 10% of the whole tweet database first before joining the followers table to increase timelnie retrival speed. Yieled significant improvement around 500%-1000% at the cost of accuracy. The performance difference was minimal after I discovered indexing....

3. Inserted multiple tweets with one update. This dramatically increased insertion performance by over 200 times. Batch size influence performance with a significant diminishing return after 2000 tweets per update. "executeBatch" from JDBC was also experimented with but yielded little to no performance increase at the cost of dry eyes and neck pain.
