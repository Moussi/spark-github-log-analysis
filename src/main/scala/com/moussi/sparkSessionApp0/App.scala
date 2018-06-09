package com.moussi.sparkSessionApp0

/**
  * Created by moussi on 09/06/18.
  */
import org.apache.spark.sql.SparkSession

object App {
  def main(args : Array[String]) {
    /**
      * Since Spark 2.0, both contexts sparkContext and SqlContext
      * are merged into a single class: SparkSession
      */
    val spark = SparkSession.builder()
      .appName("GitHub push counter")
      .master("local[*]")
      .getOrCreate()

    val inputPath = "/media/moussi/Data1/0.Training/BigData/Manip/github-archive-sia/2015-03-01-0.json"
    /**
      * spark read json function is used to map json like files
      */
    val ghLog = spark.read.json(inputPath)
    val eventsLog = ghLog.filter("type='PushEvent'")
    val groupedEvents = eventsLog.groupBy("actor.login").count
    println(s"all Log = ${ghLog.count}")
    println(s"events only = ${eventsLog.count}")
    println(s"grouped Events = ${eventsLog.count}")
    eventsLog.show(2)
    groupedEvents.show(5)
  }
}
