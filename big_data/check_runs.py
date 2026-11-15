from datetime import datetime, timedelta
ts = sorted(datetime.strptime(l.strip(), "%Y.%m.%d.%H") for l in open("/home/sinchu/gridsat_times.txt") if l.strip())
runs, start, prev = [], ts[0], ts[0]
for t in ts[1:]:
    if t - prev > timedelta(hours=3):
        runs.append((start, prev)); start = t
    prev = t
runs.append((start, prev))
hours = [int((b - a).total_seconds() / 3600) + 3 for a, b in runs]
print("files:", len(ts), " continuous runs:", len(runs))
print("run length in hours: min", min(hours), " max", max(hours), " average", round(sum(hours) / len(hours)))
print("first 8 runs:")
for (a, b), h in list(zip(runs, hours))[:8]:
    print(" ", a, "->", b, " (", h, "h )")
