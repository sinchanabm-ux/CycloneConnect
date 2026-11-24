"""Create four-image sequences matching the ConvLSTM notebook."""

from __future__ import annotations

import pandas as pd


def create_sequences(
    images_df: pd.DataFrame,
    labels_df: pd.DataFrame,
    sequence_length: int = 4,
    interval_hours: int = 3,
) -> pd.DataFrame:
    """Return sequence records with SID, END_TIME, SPLIT, IMAGES, TARGET_WIND.

    A valid sequence has four images exactly three hours apart, all within the
    same split. The target is TARGET_WIND at the final image timestamp.
    """
    required_images = {"SID", "T", "SPLIT", "IMAGE"}
    required_labels = {"SID", "ISO_TIME", "TARGET_WIND"}
    if not required_images.issubset(images_df.columns):
        raise ValueError(f"images_df must contain {sorted(required_images)}")
    if not required_labels.issubset(labels_df.columns):
        raise ValueError(f"labels_df must contain {sorted(required_labels)}")
    if sequence_length < 2 or interval_hours <= 0:
        raise ValueError("sequence_length must be >= 2 and interval_hours must be positive.")

    images = images_df.copy()
    labels = labels_df.copy()
    images["T"] = pd.to_datetime(images["T"], errors="coerce")
    labels["ISO_TIME"] = pd.to_datetime(labels["ISO_TIME"], errors="coerce")
    labels = labels.dropna(subset=["SID", "ISO_TIME", "TARGET_WIND"])

    if labels.duplicated(["SID", "ISO_TIME"]).any():
        raise ValueError("Duplicate (SID, ISO_TIME) target rows exist.")

    target_lookup = labels.set_index(["SID", "ISO_TIME"])["TARGET_WIND"]
    required_delta = pd.Timedelta(hours=interval_hours)
    records = []

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
            if pd.isna(target):
                continue

            records.append({
                "SID": sid,
                "END_TIME": end_time,
                "SPLIT": window["SPLIT"].iloc[-1],
                "IMAGES": window["IMAGE"].tolist(),
                "TARGET_WIND": float(target),
            })

    return pd.DataFrame(
        records, columns=["SID", "END_TIME", "SPLIT", "IMAGES", "TARGET_WIND"]
    )
