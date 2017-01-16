/**
  * Created by shobhikapanda on 11/21/16.
  */

import org.apache.spark.{SparkConf, SparkContext}

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.tree.configuration.Algo._
import org.apache.spark.mllib.tree.impurity.Gini
object BoschProd {
  def main(args: Array[String]) = {

 /*   val data = sc.textFile("/Users/shobhikapanda/Downloads/Machine Learning/ml dataset kaggle/train_numeric.csv")
    val tData = data.map { line =>
      val token = line.split(',')
      LabeledPoint(token(969).toDouble,Vectors.dense(token.drop(0).toDouble))
    }
*/
 val conf = new SparkConf().setAppName("Ml Test").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
   val df = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "true") // Use first line of all files as header
      .option("inferSchema", "true") // Automatically infer data types
      .load("/Users/shobhikapanda/Downloads/Machine Learning Downloads/ml dataset kaggle/newData.csv")

    val df2 = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "true") // Use first line of all files as header
      .option("inferSchema", "true") // Automatically infer data types
      .load("/Users/shobhikapanda/Downloads/Machine Learning Downloads/ml dataset kaggle/test_numeric_pre.csv")
    //val df3 = df2.na.fill(0)
    val rows: RDD[Row] = df.rdd
  val rows2: RDD[Row] = df2.rdd
    val anotherSet = rows2.map{
      line =>
        val lineString = line.toString
        val subString = lineString.substring(1,lineString.length-1)
        val tokens = subString.split(',')
      tokens(0)
    }
    val originalData = rows.map{
      line=>
        val lineString = line.toString
        val subString = lineString.substring(1,lineString.length-1)
        val tokens = subString.split(',')
        (LabeledPoint(tokens(tokens.length-1).toDouble,Vectors.dense(tokens.drop(1).map(_.toDouble))))
    }.cache()
    val testinData = rows2.map{
      line=>
        val lineString = line.toString
        val subString = lineString.substring(1,lineString.length-1)
        val tokens = subString.split(',')
        (Vectors.dense(tokens.drop(1).map(_.toDouble)))
    }.cache()
    //val data = sc.textFile("/Users/shobhikapanda/Downloads/Machine Learning Downloads/ml dataset kaggle/train_numeric.csv")
    //data.mapPartitionsWithIndex { (idx, iter) => if (idx == 0) iter.drop(1) else iter }
    //val data = MLUtils.loadLibSVMFile(sc, "/Users/shobhikapanda/Downloads/hw3datasetnew/glass.data")

    //val splits = originalData.randomSplit(Array(0.7, 0.3))
    val splits = originalData.randomSplit(Array(0.7, 0.3))
    val (trainingData, testData) = (splits(0), splits(1))
    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 5
    val maxBins = 32

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)
    val model2 = DecisionTree.trainClassifier(originalData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)
    // Evaluate model on test instances and compute test error
    val labelAndPreds2 = testinData.map { point =>

      val prediction = model2.predict(point)
      (prediction)
    }
    var newRdd = labelAndPreds2.map{
      x => x
    }
   // anotherSet.saveAsTextFile("src/main/scala/outputID")
    newRdd.saveAsTextFile("src/main/scala/outputPredTest51")
   // println("prediction on test data")
    //labelAndPreds2.take(10).foreach(x=>println(x._2))
    val labelAndPreds = testData.map { point =>

      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

   println("prediction on validation set")
    //labelAndPreds.take(10).foreach(println)
    val accuracy =  100.0 *labelAndPreds.filter(x => x._1 == x._2).count.toDouble / testData.count
    println("Accuracy of Decision Tree is= " + accuracy+"%")


   /* df.limit(100).write

      .format("com.databricks.spark.csv")
      .option("header", "true")
      .save("trainnumeric.csv")
*/
  }
}
