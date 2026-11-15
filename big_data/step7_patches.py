import sys, io, re
import numpy as np
from pyspark.sql import SparkSession, functions as F
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, BinaryType

spark = SparkSession.builder.appName("Step7_Patches").getOrCreate()
sc = spark.sparkContext
sc.setLogLevel("ERROR")
limit = int(sys.argv[1]) if len(sys.argv) > 1 else 0
out = sys.argv[2] if len(sys.argv) > 2 else "/project/curated/patches"

def key(p):
    m = re.search(r"GRIDSAT-B1\.(\d{4})\.(\d{2})\.(\d{2})\.(\d{2})\.", p)
    return "%s-%s-%s %s:00:00" % m.groups()

paths = [r.value.strip() for r in spark.read.text("/project/curated/gridsat_paths.txt").collect() if r.value.strip()]
path_by_time = {key(p): p for p in paths}

ids = spark.read.parquet("/project/curated/samples_6h").select("SID").distinct()
tr = spark.read.parquet("/project/curated/ibtracs").filter("YEAR >= 2016").join(ids, "SID")
rows = (tr.filter("LAT IS NOT NULL AND LON IS NOT NULL")
          .select("SID", F.date_format("ISO_TIME", "yyyy-MM-dd HH:mm:ss").alias("T"), "LAT", "LON")
          .collect())

by_time = {}
for r in rows:
    if r["T"] in path_by_time:
        by_time.setdefault(r["T"], []).append((r["SID"], float(r["LAT"]), float(r["LON"])))

times = sorted(by_time)
if limit:
    times = times[:limit]
print("track points with imagery:", sum(len(by_time[t]) for t in times), " files to read:", len(times))
bc = sc.broadcast({t: by_time[t] for t in times})

def extract(item):
    import xarray as xr
    path, data = item
    t = key(path)
    try:
        ds = xr.open_dataset(io.BytesIO(data), engine="h5netcdf")
        lat = ds["lat"].values
        lon = ds["lon"].values
        ir = ds["irwin_cdr"].values[0]
        wv = ds["irwvp"].values[0]
        ds.close()
    except Exception as e:
        import sys
        sys.stderr.write("BAD_FILE " + path + " " + str(e)[:80] + "\n")
        return []
    nlat, nlon = len(lat), len(lon)
    dlat = (lat[-1] - lat[0]) / (nlat - 1)
    dlon = (lon[-1] - lon[0]) / (nlon - 1)
    res = []
    for sid, la, lo in bc.value.get(t, []):
        lo = ((lo + 180.0) % 360.0) - 180.0
        i = int(round((la - lat[0]) / dlat))
        j = int(round((lo - lon[0]) / dlon))
        if i - 32 < 0 or i + 32 > nlat:
            continue
        cols = np.arange(j - 32, j + 32) % nlon
        p = np.stack([ir[i-32:i+32][:, cols], wv[i-32:i+32][:, cols]]).astype(np.float16)
        res.append((sid, t, la, lo, p.tobytes()))
    return res

files = ",".join(path_by_time[t] for t in times)
rdd = sc.binaryFiles(files, minPartitions=32).flatMap(extract)
schema = StructType([StructField("SID", StringType()), StructField("T", StringType()),
                     StructField("LAT", DoubleType()), StructField("LON", DoubleType()),
                     StructField("PATCH", BinaryType())])
spark.createDataFrame(rdd, schema).write.mode("overwrite").parquet(out)

df = spark.read.parquet(out)
print("patches written:", df.count(), " storms:", df.select("SID").distinct().count())
print("bytes per patch (expect 16384):", df.select(F.length("PATCH")).first()[0])
spark.stop()
