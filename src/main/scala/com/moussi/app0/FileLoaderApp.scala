package com.moussi.app0

/**
  * Created by moussi on 09/06/18.
  */

import com.moussi.SparkUtils._

object FileLoaderApp {
  def main(args : Array[String]) {
    /**
      * Since Spark 2.0, both contexts sparkContext and SqlContext
      * are merged into a single class: SparkSession
      */
    val spark = sparkSession("GitHub push counter");

    val inputJsonPath = "/media/moussi/Data1/0.Training/BigData/Manip/github-archive-sia/*.json"
    val inputCsvPath = "/media/moussi/Data1/0.Training/BigData/Manip/mock_data.csv"
    /**
      * spark read json function is used to map json like files
      * The json method returns a DataFrame, which has many of the standard RDD methods
      * you used before, like filter, map, flatMap, collect, count, and so on
      */
    println("Load json file ...")
    val ghLog = spark.read.json(inputJsonPath)
    val eventsLog = ghLog.filter("type='PushEvent'")
    val groupedEvents = eventsLog.groupBy("actor.login").count
    val orderedEvents = groupedEvents.orderBy(groupedEvents("count").desc)
    println(s"all Log = ${ghLog.count}")
    println(s"events only = ${eventsLog.count}")
    println(s"grouped Events = ${eventsLog.count}")
    eventsLog.show(2)
    groupedEvents.show(5)
    orderedEvents.show(5)
    println("Load csv file ...")
    val csvRows = spark.read.csv(inputCsvPath)
    val femaleRows = csvRows.filter("_c4 = 'Female'")
    val genderGroupedBy = csvRows.groupBy("_c4").count
    val orderedGenderGroupedBy = genderGroupedBy.orderBy(genderGroupedBy("count").desc)
    println(s"all csv rows = ${csvRows.count}")
    println(s"all csv rows = ${femaleRows.count}")
    csvRows.show(5)
    femaleRows.show(5)
    genderGroupedBy.show(5)
    orderedGenderGroupedBy.show(5)
  }
}
