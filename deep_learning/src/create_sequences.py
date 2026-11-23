"""Create four-frame sequences from prepared image records.

Expected input DataFrame columns:
SID, T, SPLIT, IMAGE, and (optionally) TARGET_WIND.
A separate labels DataFrame must contain SID, ISO_TIME, TARGET_WIND.

The time window must consist of four images exactly 3 hours apart, matching
the source notebook's sequence construction.
"""
from __future__ import annotations

import pandas as pd


def create_sequences(images_df: pd.DataFrame, labels_df: pd.DataFrame,
                     sequence_length: int = 4, interval_hours: int = 3) -> pd.DataFrame:
    """Return sequence records with SID, END_TIME, SPLIT, IMAGES, TARGET_WIND."""
    required_images = {"SID", "T", "SPLIT", "IMAGE"}
    required_labels = {"SID", "ISO_TIME", "TARGET_WIND"}
    if not required_images.issubset(images_df.columns):
        raise ValueError(f"images_df must contain columns: {sorted(required_images)}")
    if not required_labels.issubset(labels_df.columns):
        raise ValueError(f"labels_df must contain columns: {sorted(required_labels)}")

    images = images_df.copy()
    labels = labels_df.copy()
    images["T"] = pd.to_datetime(images["T"], errors="coerce")
    labels["ISO_TIME"] = pd.to_datetime(labels["ISO_TIME"], errors="coerce")
    labels = labels.drop_duplicates(["SID", "ISO_TIME"])
    target_lookup = labels.set_index(["SID", "ISO_TIME"])["TARGET_WIND"]

    records = []
    required_delta = pd.Timedelta(hours=interval_hours)
    for sid, group in images.groupby("SID"):
        group = group.sort_values("T").reset_index(drop=True)
        for end_idx in range(sequence_length - 1, len(group)):
            window = group.iloc[end_idx - sequence_length + 1:end_idx + 1]
            times = window["T"].tolist()
            if any(pd.isna(t) for t in times):
                continue
            if any(times[i] - times[i - 1] != required_delta for i in range(1, len(times))):
                continue
            if window["SPLIT"].nunique(dropna=False) != 1:
                continue

            end_time = times[-1]
            key = (sid, end_time)
            if key not in target_lookup.index:
                continue
            target = target_lookup.loc[key]
            if hasattr(target, "iloc"):
                target = target.iloc[0]
            if pd.isna(target):
                continue

            records.append({
                "SID": sid,
                "END_TIME": end_time,
                "SPLIT": window["SPLIT"].iloc[-1],
                "IMAGES": [img for img in window["IMAGE"]],
                "TARGET_WIND": float(target),
            })
    return pd.DataFrame(records, columns=[
        "SID", "END_TIME", "SPLIT", "IMAGES", "TARGET_WIND"
    ])
