MusicRecommendationSystem
=========================

A collection of algorithms and a testing framework for evluating an offline music recommendation system.

How to run ?
============

In my system, I am currently running it using the following command from command line. Make sure your clases are compiling to target/classes folder.

1) This tests the algorithms on the test dataset "msd_test" stored in a MYSQL table, recommends top 10 songs for each user and adopts a 5-fold cross-validation approach
/usr/lib/jvm/java-6-openjdk-i386/bin/java -classpath /home/excelsior/workspace/MusicRecommendationSystem/target/classes:/home/excelsior/workspace/MusicRecommendationSystem/lib/guava.jar:/home/excelsior/workspace/MusicRecommendationSystem/lib/mysql-connector-java.jar:/home/excelsior/workspace/MusicRecommendationSystem/conf/log4j.properties:/home/excelsior/workspace/MusicRecommendationSystem/lib/log4j.jar MusicRecommender msd_test 10 5
