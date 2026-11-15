import numpy as np, pyarrow.parquet as pq
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

t = pq.read_table("/home/sinchu/patches_full").to_pandas()
print("rows:", len(t))

# pick the storm point with the coldest (most intense-looking) IR centre, plus 5 random ones
def centre_temp(b):
    p = np.frombuffer(b, dtype=np.float16).reshape(2, 64, 64).astype(float)
    return np.nanmean(p[0, 24:40, 24:40])
t["c"] = t["PATCH"].apply(centre_temp)
pick = list(t.nsmallest(3, "c").index) + list(t.sample(5, random_state=1).index)

fig, ax = plt.subplots(2, 4, figsize=(14, 7))
for a, i in zip(ax.ravel(), pick):
    r = t.loc[i]
    p = np.frombuffer(r["PATCH"], dtype=np.float16).reshape(2, 64, 64).astype(float)
    a.imshow(p[0], cmap="gray_r", origin="lower")
    a.axhline(32, color="red", lw=0.5); a.axvline(32, color="red", lw=0.5)
    a.set_title("%s\n%s lat %.1f lon %.1f" % (r["SID"], r["T"][:13], r["LAT"], r["LON"]), fontsize=8)
    a.axis("off")
plt.tight_layout()
plt.savefig("/home/sinchu/patch_check.png", dpi=110)
print("saved /home/sinchu/patch_check.png")
