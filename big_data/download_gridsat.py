#!/usr/bin/env python3
import os, sys, subprocess, shutil, time
from concurrent.futures import ThreadPoolExecutor, as_completed
import threading

BASE = "https://www.ncei.noaa.gov/data/geostationary-ir-channel-brightness-temperature-gridsat-b1/access/"
HDFS_DIR = "/project/raw/gridsat"
TMP = os.path.expanduser("~/gridsat_tmp")
MIN_FREE_GB = 50
MIN_SIZE = 20000000

list_path = os.path.expanduser(sys.argv[1])
workers = int(sys.argv[2])
limit = int(sys.argv[3]) if len(sys.argv) > 3 else None

with open(list_path) as f:
    files = [l.strip() for l in f if l.strip()]
if limit:
    files = files[:limit]

os.makedirs(TMP, exist_ok=True)
for old in os.listdir(TMP):
    os.remove(os.path.join(TMP, old))

def free_gb():
    try:
        return shutil.disk_usage("/mnt/c").free / 1e9
    except Exception:
        return 999.0

years = sorted({p.split("/")[0] for p in files})
subprocess.run(["hdfs", "dfs", "-mkdir", "-p"] + [HDFS_DIR + "/" + y for y in years], check=True)

res = subprocess.run(["hdfs", "dfs", "-ls", "-R", HDFS_DIR], capture_output=True, text=True)
existing = set()
for line in res.stdout.splitlines():
    parts = line.split()
    if parts and parts[-1].endswith(".nc"):
        existing.add(parts[-1].split(HDFS_DIR + "/")[-1])

todo = [p for p in files if p not in existing]
total = len(todo)
stop = threading.Event()

def process(path):
    name = path.split("/")[1]
    local = os.path.join(TMP, name)
    err = ""
    for attempt in (1, 2, 3):
        if stop.is_set():
            return "stopped"
        if free_gb() < MIN_FREE_GB:
            stop.set()
            return "lowdisk"
        try:
            r = subprocess.run(["curl", "-sS", "--fail", "--retry", "3", "--retry-delay", "5",
                                "--connect-timeout", "30", "--max-time", "1200",
                                "-o", local, BASE + path], capture_output=True, text=True)
            if r.returncode != 0:
                raise RuntimeError("curl: " + r.stderr.strip()[:200])
            size = os.path.getsize(local)
            with open(local, "rb") as fh:
                head = fh.read(4)
            if size < MIN_SIZE or not (head.startswith(b"\x89HDF") or head.startswith(b"CDF")):
                raise RuntimeError("bad file, size " + str(size))
            r = subprocess.run(["hdfs", "dfs", "-D", "dfs.replication=1", "-put", "-f",
                                local, HDFS_DIR + "/" + path], capture_output=True, text=True)
            if r.returncode != 0:
                raise RuntimeError("hdfs put: " + r.stderr.strip()[:200])
            os.remove(local)
            return size
        except Exception as e:
            err = str(e)
            if os.path.exists(local):
                os.remove(local)
            time.sleep(10 * attempt)
    return "failed: " + err

print(len(files), "in list,", len(files) - total, "already in HDFS,", total, "to download,", workers, "parallel", flush=True)
start = time.time()
n = ok = bad = 0
nbytes = 0
with ThreadPoolExecutor(max_workers=workers) as ex:
    futs = {ex.submit(process, p): p for p in todo}
    for fut in as_completed(futs):
        p = futs[fut]
        res = fut.result()
        n += 1
        if isinstance(res, int):
            ok += 1
            nbytes += res
        elif res in ("stopped", "lowdisk"):
            pass
        else:
            bad += 1
            print("FAILED", p, res, flush=True)
        if n % 10 == 0 or n == total:
            el = time.time() - start
            print("[%d/%d] %.1f GB in %.1f min (%.1f MB/s), C: free %.0f GB" %
                  (n, total, nbytes / 1e9, el / 60, nbytes / 1e6 / max(el, 1), free_gb()), flush=True)

if stop.is_set():
    print("STOPPED: free space on C: fell below", MIN_FREE_GB, "GB")
print("DONE:", ok, "downloaded,", bad, "failed,", total - ok - bad, "not done. Re-run the same command to retry.")
