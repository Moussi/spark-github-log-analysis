package com.moussi.app0

import com.moussi.SparkUtils._

import scala.collection.mutable

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

    /**
      * reduce by key is a particular case of aggregatebykey, in the sense that the result of the
      * combination (e.g. a sum) is of the same type that the values
      *
      */
    val keyedTranByCust = transByCustPricing.reduceByKey((a, b) => a ++ b)
    /**
      * fold by key
      */
    val array = Array("")
    val foldedKeyedTranByCust = transByCustPricing.foldByKey(array)((a, b) => a ++ b)

    val tranByCustPrices = transByCust.mapValues(tran => tran(5).toDouble)
    val amounts = tranByCustPrices.foldByKey(0)((a,b) => a+b).collect
    val mostPayedCustomer = amounts.toSeq.sortBy(tran => tran._2).last

    val list = List(("a", 10), ("a",20),("a", 5),("b",25))
    val listRdd = sc.makeRDD(list)
    val foldedList = listRdd.foldByKey(10)((a,b) => a+b).collect /** Output (a,50) (b,35) **/
    /**
      * aggregate by key: same as reduce by key with initial val and possible different type from the values
      * 0 is initial value, _+_ inside partition, _+_ between partitions
      * val resAgg = pairs.aggregateByKey(0)(_+_,_+_)
      * https://stackoverflow.com/questions/24804619/how-does-spark-aggregate-function-aggregatebykey-work
      */
    val aggregatedList = listRdd.aggregateByKey(0)((a,b) => a+b, (a,b) => a+b).collect
    val aggregatedListDifferentType = listRdd.aggregateByKey(new mutable.HashSet[Int]())(_+_, _++_)
    aggregatedList.foreach(println)  /** Output (a,35) (b,25) **/
    aggregatedListDifferentType.foreach(println) /** Output (a,Set(5, 20, 10)) (b,Set(25)) **/

  }
}
