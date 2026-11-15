import sys
from pyspark.sql import SparkSession, functions as F, Window

spark = SparkSession.builder.appName("Demo_Queries").getOrCreate()
spark.sparkContext.setLogLevel("ERROR")
df = spark.read.parquet("/project/curated/ibtracs")

year = int(sys.argv[1]) if len(sys.argv) > 1 else 2020
basin = sys.argv[2] if len(sys.argv) > 2 else "North India"

print("\n=== Q1: Year with the most cyclones ===")
df.groupBy("YEAR").agg(F.countDistinct("SID").alias("num_cyclones")).orderBy(F.desc("num_cyclones")).show(3)

print(f"\n=== Q2: Most affected basin in {year} ===")
df.filter(F.col("YEAR") == year).groupBy("BASIN").agg(
    F.countDistinct("SID").alias("num_cyclones"), F.max("WIND_KT").alias("peak_wind_kt")
).orderBy(F.desc("num_cyclones")).show()

print(f"\n=== MODIFIED: cyclones per year in basin '{basin}' (peak wind >= 34 kt), top 5 ===")
storms = df.groupBy("SID", "BASIN", "YEAR").agg(F.max("WIND_KT").alias("peak"))
(storms.filter((F.col("BASIN") == basin) & (F.col("peak") >= 34))
       .groupBy("YEAR").agg(F.count("*").alias("num_cyclones"))
       .orderBy(F.desc("num_cyclones")).show(5))
spark.stop()
