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
    val discountedTransByCust = transByCust.mapValues(tran => {
      if (tran(3).toInt == 25 && tran(4).toDouble > 1)
        tran(5) = (tran(5).toDouble * 0.95).toString
      tran
    })

    val transByCustPricing = discountedTransByCust.flatMapValues(tran => {
      if (tran(3).toInt == 81 && tran(4).toDouble >= 4) {
        val cloned = tran.clone
        cloned(5) = "0.00"
        cloned(3) = "70"
        cloned(4) = "1"
        List(tran, cloned)
      } else
        List(tran)
    })
    println(transByCustPricing.countByKey.values.sum)


  }
}
