\# CycloneConnect API contract (v0.1)



Base URL during the demo: http://127.0.0.1:8000

All answers are JSON. Times are ISO 8601 in UTC, e.g. 2024-09-01T12:00:00Z.



Intensity classes: 0 = D, 1 = DD, 2 = CS, 3 = SCS, 4 = VSCS



\## GET /health



Checks that the server is running.



Answer:



&#x20;   {"status": "ok"}



\## GET /storms



Lists the storms the app can replay (test-set storms only).

Optional filters added to the address: year (e.g. ?year=2024) and basin (e.g. ?basin=WP).



Answer (FAKE example data):



&#x20;   \[

&#x20;     {"sid": "SAMPLE0001", "name": "SAMPLE\_A", "basin": "WP", "year": 2024, "n\_frames": 42, "max\_wind\_kt": 105},

&#x20;     {"sid": "SAMPLE0002", "name": "SAMPLE\_B", "basin": "NI", "year": 2024, "n\_frames": 30, "max\_wind\_kt": 65}

&#x20;   ]



\## GET /storms/{sid}/timeline



The full replay of one storm. {sid} is replaced by a real storm ID,

e.g. /storms/SAMPLE0001/timeline. One entry for every 3 hours, in time order.



Answer (FAKE example data):



&#x20;   {

&#x20;     "sid": "SAMPLE0001",

&#x20;     "name": "SAMPLE\_A",

&#x20;     "basin": "WP",

&#x20;     "steps": \[

&#x20;       {"time": "2024-09-01T00:00:00Z", "lat": 15.2, "lon": 132.8, "true\_wind\_kt": 45, "true\_class": 1,

&#x20;        "pred\_cnn": 1, "pred\_gru": 1, "pred\_convlstm": 1, "frame": "/storms/SAMPLE0001/frame/2024090100"},

&#x20;       {"time": "2024-09-01T03:00:00Z", "lat": 15.6, "lon": 132.1, "true\_wind\_kt": 55, "true\_class": 2,

&#x20;        "pred\_cnn": 1, "pred\_gru": 2, "pred\_convlstm": 2, "frame": "/storms/SAMPLE0001/frame/2024090103"},

&#x20;       {"time": "2024-09-01T06:00:00Z", "lat": 16.0, "lon": 131.4, "true\_wind\_kt": 70, "true\_class": 3,

&#x20;        "pred\_cnn": 2, "pred\_gru": 3, "pred\_convlstm": 3, "frame": "/storms/SAMPLE0001/frame/2024090106"}

&#x20;     ]

&#x20;   }



\## GET /storms/{sid}/frame/{time}



The satellite image of one storm at one moment. {time} is written as YYYYMMDDHH,

e.g. /storms/SAMPLE0001/frame/2024090103 means 2024-09-01 at 03:00 UTC.

These are the exact addresses given in the "frame" field of the timeline.



Answer: a PNG image (not JSON).

\- 128 x 128 pixels, grayscale.

\- Same fixed temperature range for every image (190 K to 310 K), so images are comparable.

\- Cold cloud tops (strong storm clouds) look bright, warm sea looks dark.



If the image does not exist, the server answers with HTTP status 404 (not found).



\## GET /analytics/yearly



Storm counts per year (from the Spark SQL analysis).



Answer (FAKE example data):



&#x20;   \[

&#x20;     {"year": 2023, "n\_storms": 80, "n\_frames": 3900},

&#x20;     {"year": 2024, "n\_storms": 101, "n\_frames": 4364}

&#x20;   ]



\## GET /analytics/basin



Storm counts per ocean basin and year.

Optional filter: year (e.g. /analytics/basin?year=2024).

Basin codes: NA, EP, WP, NI, SI, SP.



Answer (FAKE example data):



&#x20;   \[

&#x20;     {"year": 2024, "basin": "WP", "n\_storms": 28},

&#x20;     {"year": 2024, "basin": "NI", "n\_storms": 5},

&#x20;     {"year": 2024, "basin": "NA", "n\_storms": 18}

&#x20;   ]



\## GET /metrics



Test-set scores for each model, averaged over the training runs (seeds).



Answer (FAKE example data):



&#x20;   \[

&#x20;     {"model": "cnn", "n\_seeds": 3, "accuracy": 0.50, "accuracy\_std": 0.02,

&#x20;      "within1": 0.87, "balanced\_acc": 0.42, "mean\_class\_err": 0.63},

&#x20;     {"model": "gru", "n\_seeds": 3, "accuracy": 0.56, "accuracy\_std": 0.02,

&#x20;      "within1": 0.92, "balanced\_acc": 0.48, "mean\_class\_err": 0.52},

&#x20;     {"model": "convlstm", "n\_seeds": 3, "accuracy": 0.58, "accuracy\_std": 0.03,

&#x20;      "within1": 0.93, "balanced\_acc": 0.50, "mean\_class\_err": 0.48}

&#x20;   ]



Model names match the timeline fields: cnn -> pred\_cnn, gru -> pred\_gru, convlstm -> pred\_convlstm.

Wind-speed regression scores (MAE and RMSE in knots) will be added later if that experiment is done.

