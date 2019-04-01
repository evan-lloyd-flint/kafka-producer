import java.util.concurrent.LinkedBlockingQueue
import org.apache.kafka.clients.producer._
import com.google.common.collect.Lists

import com.twitter.hbc.ClientBuilder
//import com.twitter.hbc.core.Client
import com.twitter.hbc.core.Constants
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.processor.StringDelimitedProcessor
//import com.twitter.hbc.httpclient.auth.Authentication
import com.twitter.hbc.httpclient.auth.OAuth1



object Producer {
  def main (args: Array[String]): Unit = {

    import java.util.Properties

    val TOPIC = "twitter-test-scala"

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    //      props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    //      props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")


    val producer = new KafkaProducer[String, String](props)



    //hbc-twitter4j
    val queue = new LinkedBlockingQueue[String](1000)
    val endpoint = new StatusesFilterEndpoint()

    endpoint.trackTerms(Lists.newArrayList(
      "#Trump", "#2020"
    ))

    val consumerKey = API.CONSUMER_KEY_KEY
    val consumerSecret = API.CONSUMER_SECRET_KEY
    val accessToken = API.ACCESS_TOKEN_KEY
    val accessTokenSecret = API.ACCESS_TOKEN_SECRET_KEY
    val auth = new OAuth1(consumerKey, consumerSecret, accessToken, accessTokenSecret)

    val client = new ClientBuilder().hosts(Constants.STREAM_HOST)
      .endpoint(endpoint)
      .authentication(auth)
      .processor(new StringDelimitedProcessor(queue)).build()
    client.connect()


    for (i <- 1 to 1000) {
      val record = new ProducerRecord[String, String](TOPIC, queue.take())
      //      val record = new ProducerRecord(TOPIC, "key", s"hello $i")
      producer.send(record)
    }

    val record = new ProducerRecord(TOPIC, "key", s"the end " + new java.util.Date)
    producer.send(record)

    producer.close()
    client.stop()

  }
}
