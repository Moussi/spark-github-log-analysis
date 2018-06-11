package com.moussi

import org.apache.spark.sql.SparkSession

/**
  * Created by moussi on 09/06/18.
  */
package object SparkUtils {

  def sparkSession(appName: String) = {
    SparkSession.builder()
      .appName(appName)
      .master("local[*]")
      .getOrCreate()
  }
}
