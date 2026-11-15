import numpy as np, pyarrow.parquet as pq
t = pq.read_table("/home/sinchu/patches_test").to_pandas()
print("rows:", len(t))
for _, r in t.iloc[[0, len(t) // 2, len(t) - 1]].iterrows():
    p = np.frombuffer(r["PATCH"], dtype=np.float16).reshape(2, 64, 64).astype(float)
    ir, wv = p[0], p[1]
    print(r["SID"], r["T"], "lat", r["LAT"], "lon", r["LON"])
    print("  IR  min %.1f mean %.1f max %.1f  nan %d" % (np.nanmin(ir), np.nanmean(ir), np.nanmax(ir), np.isnan(ir).sum()))
    print("  WV  min %.1f mean %.1f max %.1f  nan %d" % (np.nanmin(wv), np.nanmean(wv), np.nanmax(wv), np.isnan(wv).sum()))
