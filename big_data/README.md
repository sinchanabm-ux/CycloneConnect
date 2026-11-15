# Big Data pipeline (Hadoop, YARN, Spark)

Two-node cluster (master + worker1) over Tailscale. HDFS holds IBTrACS and about 174 GB of GridSat-B1 imagery (2016-2026).

| Script | What it does |
|---|---|
| step5_ibtracs.py | Cleans IBTrACS to Parquet, answers the cyclone queries |
| step6_match.py | Matches storms to imagery, builds the 6 h forecast samples and the train/val/test split |
| step7_patches.py | Cuts 64x64 IR and water-vapour patches around each storm (160 GB down to 106 MB) |

Run with `spark-submit --master yarn` using Python 3.10 on both nodes.
Results are in `results/`, cluster screenshots in `screenshots/`.
