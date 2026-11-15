from pyspark.sql import SparkSession, functions as F, Window

spark = SparkSession.builder.appName("Step6_Match").getOrCreate()
spark.sparkContext.setLogLevel("ERROR")

tr = spark.read.parquet("/project/curated/ibtracs").filter("YEAR >= 2016")
img = (spark.read.text("/project/curated/gridsat_times.txt")
       .select(F.to_timestamp(F.trim("value"), "yyyy.MM.dd.HH").alias("ISO_TIME"))
       .distinct())

# keep only track points that have an image at exactly that time
s = (tr.join(F.broadcast(img), "ISO_TIME")
       .filter("LAT IS NOT NULL AND LON IS NOT NULL"))

w = Window.partitionBy("SID").orderBy("ISO_TIME")
t = F.col("ISO_TIME").cast("long")
s = (s.withColumn("back", t - F.lag(t, 7).over(w))
      .withColumn("fwd", F.lead(t, 2).over(w) - t)
      .withColumn("TARGET_WIND", F.lead("WIND_KT", 2).over(w))
      .withColumn("STORM_YEAR", F.min("YEAR").over(Window.partitionBy("SID"))))

# 8 consecutive 3-hourly images ending now (21 h back), target wind exactly 6 h ahead
ok = s.filter((F.col("back") == 75600) & (F.col("fwd") == 21600) & F.col("TARGET_WIND").isNotNull())
ok = ok.withColumn("SPLIT",
        F.when(F.col("STORM_YEAR") <= 2022, "1_train")
         .when(F.col("STORM_YEAR") == 2023, "2_val")
         .otherwise("3_test"))

print("\n=== Storms with at least one image ===")
print(s.select("SID").distinct().count())

print("\n=== Usable ConvLSTM samples per split ===")
(ok.groupBy("SPLIT").agg(
      F.countDistinct("SID").alias("storms"),
      F.count("*").alias("samples"),
      F.sum((F.col("TARGET_WIND") >= 64).cast("int")).alias("hurricane_kt64plus"),
      F.sum((F.col("TARGET_WIND") >= 96).cast("int")).alias("major_kt96plus"))
   .orderBy("SPLIT").show())

print("\n=== Samples per basin ===")
ok.groupBy("BASIN").count().orderBy(F.desc("count")).show(truncate=False)

(ok.select("SID", "BASIN", "ISO_TIME", "LAT", "LON", "WIND_KT", "TARGET_WIND", "SPLIT")
   .write.mode("overwrite").parquet("/project/curated/samples_6h"))
spark.stop()
