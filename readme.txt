*
****Machine Learning*****

Name: Shobhika Panda,Srinivas Lingamgunta, Mahima JayPrakash,Lahari Ganesha,JayPrakash Rout*	
Net ID: sxp150031 ,sxl154630, lxg150730 ,mxj151430 ,jxr152730  

How to run??


1.Install Intellij ide, with spark and scala configurations.


2. Config settings
name := "GradientBoost"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "1.6.1","org.apache.spark" %% "spark-sql" % "1.6.1","com.databricks" %% "spark-csv" % "1.4.0", "org.apache.spark"  % "spark-mllib_2.10" % "2.0.0")



3. Config Settings
name := "MachineLearning"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "1.6.1","org.apache.spark" %% "spark-sql" % "1.6.1","com.databricks" %% "spark-csv" % "1.4.0", "org.apache.spark"  % "spark-mllib_2.10" % "2.0.2")



4. Build project with sbt

5. Add these imports
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.RandomForest

and run the code


6. How to run R code?
Just select and run, install and require packages.



R scripts:
-----------

1) GGplot.R - Run it from RStudio with the respective datasets in place.
2) XGB.R - Run it directly from R Studio  
3) flowpathStation.R - Run it and generate the plot to understand the flow of different parts get distributed among the stations.