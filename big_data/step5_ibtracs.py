import sys
from pyspark.sql import SparkSession, functions as F, Window

spark = SparkSession.builder.appName("Step5_IBTrACS").getOrCreate()
spark.sparkContext.setLogLevel("ERROR")

raw = spark.read.option("header", True).csv("/project/raw/ibtracs/ibtracs.csv")

# Trim every column; a blank value (" ") becomes null
df = raw
for c in raw.columns:
    t = F.trim(F.col(c))
    df = df.withColumn(c, F.when(t == "", None).otherwise(t))

# Drop the units row (its SID is blank, so it is now null)
df = df.filter(F.col("SID").isNotNull())

df = (df
  .withColumn("ISO_TIME", F.to_timestamp("ISO_TIME"))
  .withColumn("YEAR", F.year("ISO_TIME"))
  .withColumn("LAT", F.col("LAT").cast("double"))
  .withColumn("LON", F.col("LON").cast("double"))
  .withColumn("WIND_KT", F.col("WMO_WIND").cast("double"))
  .withColumn("PRES_MB", F.col("WMO_PRES").cast("double"))
  .drop("WMO_WIND", "WMO_PRES")
  .filter(F.col("ISO_TIME").isNotNull())
  .dropDuplicates(["SID", "ISO_TIME"]))

df.write.mode("overwrite").parquet("/project/curated/ibtracs")
df = spark.read.parquet("/project/curated/ibtracs")

print("\n=== Rows and storms ===")
print("rows:", df.count(), " storms:", df.select("SID").distinct().count())

print("\n=== Q1: Top 10 years by number of cyclones ===")
(df.groupBy("YEAR").agg(F.countDistinct("SID").alias("num_cyclones"))
   .orderBy(F.desc("num_cyclones")).show(10))

year = int(sys.argv[1]) if len(sys.argv) > 1 else 2020
print(f"\n=== Q2: Cyclones per basin in {year} ===")
(df.filter(F.col("YEAR") == year)
   .groupBy("BASIN")
   .agg(F.countDistinct("SID").alias("num_cyclones"),
        F.max("WIND_KT").alias("peak_wind_kt"))
   .orderBy(F.desc("num_cyclones")).show())

print("\n=== Q3: Most affected basin in each of the last 10 years ===")
w = Window.partitionBy("YEAR").orderBy(F.desc("num_cyclones"))
(df.groupBy("YEAR", "BASIN").agg(F.countDistinct("SID").alias("num_cyclones"))
   .withColumn("rank", F.row_number().over(w))
   .filter("rank = 1").drop("rank")
   .orderBy(F.desc("YEAR")).show(10))

spark.stop()
