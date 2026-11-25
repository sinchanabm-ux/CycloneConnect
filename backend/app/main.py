from fastapi import FastAPI, HTTPException               # load FastAPI from the library we installed

app = FastAPI(title="CycloneConnect API")  # create the application; "app" is the name the server will look for

@app.get("/health")                        # "when someone asks (GET) for the address /health, run the function below"
def health():                              # a normal Python function, runs on each request
    return {"status": "ok"}                # a Python dictionary; FastAPI turns it into JSON text automatically

FAKE_STORMS = [                                     # a list of dictionaries, copied from the contract's example
    {"sid": "SAMPLE0001", "name": "SAMPLE_A", "basin": "WP", "year": 2024, "n_frames": 42, "max_wind_kt": 105},
    {"sid": "SAMPLE0002", "name": "SAMPLE_B", "basin": "NI", "year": 2024, "n_frames": 30, "max_wind_kt": 65},
]

@app.get("/storms")                                 # attach the function below to the address /storms
def list_storms(year: int | None = None, basin: str | None = None):
    # year and basin are OPTIONAL filters: "= None" means "not given". FastAPI reads them from the address,
    # e.g. /storms?year=2024. "int | None" means "a whole number or nothing".
    storms = FAKE_STORMS                            # start with all storms
    if year is not None:                            # if the caller gave a year...
        storms = [s for s in storms if s["year"] == year]   # ...keep only storms whose year matches
    if basin is not None:                           # same idea for the basin
        storms = [s for s in storms if s["basin"] == basin]
    return storms                                   # FastAPI turns the list into JSON

FAKE_TIMELINES = {                                  # a dictionary: storm ID -> that storm's timeline
    "SAMPLE0001": {
        "sid": "SAMPLE0001", "name": "SAMPLE_A", "basin": "WP",
        "steps": [                                  # one entry per 3 hours, copied from the contract
            {"time": "2024-09-01T00:00:00Z", "lat": 15.2, "lon": 132.8, "true_wind_kt": 45, "true_class": 1,
             "pred_cnn": 1, "pred_gru": 1, "pred_convlstm": 1, "frame": "/storms/SAMPLE0001/frame/2024090100"},
            {"time": "2024-09-01T03:00:00Z", "lat": 15.6, "lon": 132.1, "true_wind_kt": 55, "true_class": 2,
             "pred_cnn": 1, "pred_gru": 2, "pred_convlstm": 2, "frame": "/storms/SAMPLE0001/frame/2024090103"},
            {"time": "2024-09-01T06:00:00Z", "lat": 16.0, "lon": 131.4, "true_wind_kt": 70, "true_class": 3,
             "pred_cnn": 2, "pred_gru": 3, "pred_convlstm": 3, "frame": "/storms/SAMPLE0001/frame/2024090106"},
        ],
    },
}

@app.get("/storms/{sid}/timeline")                  # {sid} in curly braces is a placeholder filled from the address
def storm_timeline(sid: str):                       # FastAPI passes the text from the address in as "sid"
    if sid not in FAKE_TIMELINES:                   # "not in" checks whether the key exists in the dictionary
        raise HTTPException(status_code=404, detail="Storm not found")   # stop here and send a 404 error reply
    return FAKE_TIMELINES[sid]                      # otherwise look up and return that storm's timeline