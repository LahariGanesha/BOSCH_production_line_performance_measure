import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.util.MLUtils


/**
  * Created by shobhikapanda on 11/25/16.
  */

object BoostAlgos{
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
    // val df = sc.textFile("/Users/shobhikapanda/Downloads/Machine Learning Downloads/ml dataset kaggle/train_numeric_pre.csv")
    val df2 = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "true") // Use first line of all files as header
      .option("inferSchema", "true") // Automatically infer data types
      .load("/Users/shobhikapanda/Downloads/Machine Learning Downloads/ml dataset kaggle/test_numeric_pre.csv")
    val df3 = df.na.fill(0)
    //val dfLimit = df.limit(6000)
    val rows: RDD[Row] = df3.rdd
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
        (LabeledPoint(tokens(tokens.length-1).toDouble,Vectors.dense(tokens.map(_.toDouble))))
    }.cache()
    val testinData = rows2.map{
      line=>
        val lineString = line.toString
        val subString = lineString.substring(1,lineString.length-1)
        val tokens = subString.split(',')
        (Vectors.dense(tokens.drop(1).map(_.toDouble)))
    }.cache()


    val splits = originalData.randomSplit(Array(0.7, 0.3))
    val (trainingData, testData) = (splits(0), splits(1))

    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 3 // Use more in practice.
    val featureSubsetStrategy = "auto" // Let the algorithm choose.
    val impurity = "gini"
    val maxDepth = 4
    val maxBins = 32

    val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    val model2 = RandomForest.trainClassifier(originalData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    val labelAndPreds2 = testinData.map { point =>
      val prediction = model2.predict(point)
      (prediction)
    }
    // Evaluate model on test instances and compute test error
    val labelAndPreds = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label,prediction)
    }
   // labelAndPreds2.take(10).foreach(println)
    labelAndPreds2.saveAsTextFile("src/main/scala/outputPreds3")


    val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
    println("Test Error = " + testErr)
    println("Learned classification forest model:\n" + model.toDebugString)


  }
}