package com.moussi.app0

import com.moussi.SparkUtils._
/**
  * Created by moussi on 11/06/18.
  */
object MarketingComplimentaryProducts {
  def main(args: Array[String]): Unit = {
    val spark = sparkSession("Marketing Complimentary Products Counter")
    val sc = spark.sparkContext
    val transFile = sc.textFile("./src/main/resources/data_transactions.txt")
    val transData = transFile.map(_.split("#"))
    val transByCust = transData.map(transaction => (transaction(2), transaction))
    val transByCustKeys = transByCust.keys.distinct.count
    val transByCustCountedByKey = transByCust.countByKey
    val mostTransCust = transByCustCountedByKey.toSeq.sortBy(_._2).last
    println(transByCustCountedByKey.values.sum)
  }
}
