package com.cloudera.sa.taxi360.sql.kudu

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}


object KuduAppEventSimpleSums {
  def main(args: Array[String]): Unit = {

    if (args.length == 0) {
      println("Args: <runLocal> <kuduMaster> " +
        "<kuduAppEventTableName> ")
      return
    }

    val runLocal = args(0).equalsIgnoreCase("l")
    val kuduMaster = args(1)
    val kuduAppEventTableName = args(2)

    val sc: SparkContext = if (runLocal) {
      val sparkConfig = new SparkConf()
      sparkConfig.set("spark.broadcast.compress", "false")
      sparkConfig.set("spark.shuffle.compress", "false")
      sparkConfig.set("spark.shuffle.spill.compress", "false")
      new SparkContext("local", "TableStatsSinglePathMain", sparkConfig)
    } else {
      val sparkConfig = new SparkConf().setAppName("TableStatsSinglePathMain")
      new SparkContext(sparkConfig)
    }

    val hiveContext = new HiveContext(sc)

    val kuduOptions = Map(
      "kudu.table" -> kuduAppEventTableName,
      "kudu.master" -> kuduMaster)

    hiveContext.read.options(kuduOptions).format("org.kududb.spark.kudu").load.
      registerTempTable("app_event_tmp")


    println("------------")
    val values = hiveContext.sql("select account_id, sum(purchase) from app_event_tmp group by account_id").
      take(100)
    println("------------")

    values.foreach(println)
    println("------------")

    sc.stop()
  }
}
