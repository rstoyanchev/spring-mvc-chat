
### Overview

A distributed version of the Spring MVC 3.2 chat sample using Redis for persistence.

### Note

There is a bug in Tomcat that affects this sample. Please use Tomcat 7.0.32 or higher.


### Instructions

For this version of the sample you will need to [download](http://redis.io/download), install, and start a Redis instance, which can be done in just a few minutes following the [quickstart](http://redis.io/topics/quickstart) steps. Later on you may also find it helpful to refer to the [Spring Redis](http://www.springsource.org/spring-data/redis) reference [documentation](http://static.springsource.org/spring-data/data-redis/docs/current/reference/).

Eclipse users, run `mvn eclipse:eclipse` and import the project. Or just import the code as a Maven project into IntelliJ, NetBeans, or Eclipse.

### Tips

While the sample is running, you can send commands through the Redis command-line interface:
````
$ redis-cli
````

For example to view the chat backlog:
````
redis 127.0.0.1:6379> LRANGE chat:archive 0 -1
````

To clear the chat backlog:
````
redis 127.0.0.1:6379> DEL chat:archive
````

To post a message and send a notification to all running spring-mvc-chat web applications:
````
redis 127.0.0.1:6379> RPUSH chat:archive "hello from the redis cli"
redis 127.0.0.1:6379> PUBLISH chat "a new chat message was posted"
````

You should see the effects of these commands through the browser interface or in the log output.

