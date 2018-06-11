package com.moussi.app0

import scala.io.Source.fromFile
import org.apache.spark.sql.SparkSession


/**
  * Created by moussi on 09/06/18.
  */
object GithubDayCluster {
    def main(args : Array[String]) {
      val spark = SparkSession.builder().getOrCreate()

      val sc = spark.sparkContext

      val ghLog = spark.read.json(args(0))

      val pushes = ghLog.filter("type = 'PushEvent'")
      val grouped = pushes.groupBy("actor.login").count
      val ordered = grouped.orderBy(grouped("count").desc)

      val employees = Set() ++ (
        for {
          line <- fromFile(args(1)).getLines
        } yield line.trim
        )
      val bcEmployees = sc.broadcast(employees)

      import spark.implicits._
      val isEmp = (user:String) => bcEmployees.value.contains(user)
      val sqlFunc = spark.udf.register("SetContainsUdf", isEmp)
      val filtered = ordered.filter(sqlFunc($"login"))

      filtered.write.format(args(3)).save(args(2))
    }
}
