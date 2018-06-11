package com.moussi.app0

import com.moussi.SparkUtils._

import scala.io.Source.fromFile

/**
  * Created by moussi on 09/06/18.
  */
object GithubDay {

  def main(args: Array[String]): Unit = {
    /**
      * Since Spark 2.0, both contexts sparkContext and SqlContext
      * are merged into a single class: SparkSession
      */
    val spark = sparkSession("Github push Counter")
    val inputJsonPath = "/media/moussi/Data1/0.Training/BigData/Manip/github-archive-sia/*.json"
    val inputCsvPath = "/media/moussi/Data1/0.Training/BigData/Manip/mock_data.csv"
    /**
      * Load txt file on a Set
      */
    val filePath = "./src/main/resources/ghEmployees.txt"
    val employees = Set() ++ (for (line <- fromFile(filePath).getLines()) yield line.trim)
    /**
      * broadcast variables
      * Broadcast variables allow you to send a variable exactly once to each node in a cluster.
      * Moreover, the variable is automatically cached in memory on the cluster nodes, ready to be
      * used during the execution of the program.
      */
    val bcEmployees = spark.sparkContext.broadcast(employees)

    /**
      * user-defined functions (UDFs)
      * when Spark goes to execute the UDF, it will take all of its dependencies
      * (only the employees set, in this case) and send them along with each and every task,
      * to be executed on a cluster.
      */

    println("Load json file ...")
    val ghLog = spark.read.json(inputJsonPath)
    val eventsLog = ghLog.filter("type='PushEvent'")
    val groupedEvents = eventsLog.groupBy("actor.login").count
    val orderedEvents = groupedEvents.orderBy(groupedEvents("count").desc)

    import spark.implicits._
    val isEmp = (user:String) => bcEmployees.value.contains(user)
    val isEmployee = spark.udf.register("isEmpUdf", isEmp)
    val filtered = orderedEvents.filter(isEmployee($"login"))
    filtered.show()


  }

}
