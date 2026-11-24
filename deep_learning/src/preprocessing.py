"""Preprocessing helpers matching the Channel 1 CNN and ConvLSTM notebooks."""

from __future__ import annotations

import numpy as np
import pandas as pd


def decode_patch(patch_bytes, channels: int = 2, height: int = 64, width: int = 64):
    """Decode float16 patch bytes into (channels, height, width)."""
    values = np.frombuffer(patch_bytes, dtype=np.float16)
    expected = channels * height * width
    if values.size != expected:
        raise ValueError(f"Expected {expected} float16 values, received {values.size}.")
    return values.reshape(channels, height, width)


def extract_channel1(patch_bytes, height: int = 64, width: int = 64) -> np.ndarray:
    """Extract Channel 1 as float32 with shape (height, width)."""
    return decode_patch(patch_bytes, height=height, width=width)[0].astype(np.float32)


def assign_splits_to_patches(labels_df: pd.DataFrame, patches_df: pd.DataFrame) -> pd.DataFrame:
    """Attach the existing cyclone-level split to each patch using SID."""
    required_labels = {"SID", "SPLIT"}
    required_patches = {"SID", "T", "PATCH"}
    if not required_labels.issubset(labels_df.columns):
        raise ValueError(f"labels_df must contain {sorted(required_labels)}")
    if not required_patches.issubset(patches_df.columns):
        raise ValueError(f"patches_df must contain {sorted(required_patches)}")

    cyclone_splits = labels_df[["SID", "SPLIT"]].drop_duplicates()
    if cyclone_splits.groupby("SID")["SPLIT"].nunique().gt(1).any():
        raise ValueError("At least one cyclone appears in multiple data splits.")

    result = patches_df.merge(
        cyclone_splits, on="SID", how="left", validate="many_to_one"
    )
    return result[result["SPLIT"].isin(["1_train", "2_val", "3_test"])].copy()


def build_channel1_images(patches_with_split: pd.DataFrame) -> pd.DataFrame:
    """Decode patches and return SID, T, SPLIT, IMAGE records; skip all-missing images."""
    required = {"SID", "T", "SPLIT", "PATCH"}
    if not required.issubset(patches_with_split.columns):
        raise ValueError(f"Input must contain {sorted(required)}")

    records = []
    for row in patches_with_split.itertuples(index=False):
        try:
            image = extract_channel1(row.PATCH)
        except (ValueError, TypeError):
            continue
        if not np.isfinite(image).any():
            continue
        records.append({
            "SID": row.SID,
            "T": pd.to_datetime(row.T, errors="coerce"),
            "SPLIT": row.SPLIT,
            "IMAGE": image,
        })
    result = pd.DataFrame(records, columns=["SID", "T", "SPLIT", "IMAGE"])
    return result.dropna(subset=["T"]).reset_index(drop=True)


def match_images_to_targets(
    images_df: pd.DataFrame, labels_df: pd.DataFrame
) -> pd.DataFrame:
    """Match image timestamp T to label ISO_TIME and retain TARGET_WIND."""
    required_images = {"SID", "T", "SPLIT", "IMAGE"}
    required_labels = {"SID", "ISO_TIME", "TARGET_WIND"}
    if not required_images.issubset(images_df.columns):
        raise ValueError(f"images_df must contain {sorted(required_images)}")
    if not required_labels.issubset(labels_df.columns):
        raise ValueError(f"labels_df must contain {sorted(required_labels)}")

    images = images_df.copy()
    labels = labels_df.copy()
    images["T"] = pd.to_datetime(images["T"], errors="coerce")
    labels["ISO_TIME"] = pd.to_datetime(labels["ISO_TIME"], errors="coerce")
    labels = labels.dropna(subset=["SID", "ISO_TIME", "TARGET_WIND"])
    if labels.duplicated(["SID", "ISO_TIME"]).any():
        raise ValueError("Duplicate (SID, ISO_TIME) target rows exist.")

    matched = images.merge(
        labels[["SID", "ISO_TIME", "TARGET_WIND"]],
        left_on=["SID", "T"],
        right_on=["SID", "ISO_TIME"],
        how="inner",
        validate="many_to_one",
    )
    return matched[["SID", "T", "SPLIT", "IMAGE", "TARGET_WIND"]].reset_index(drop=True)


def compute_pixel_stats(images) -> tuple[float, float]:
    """Compute mean and standard deviation over finite pixels only."""
    pixel_sum = 0.0
    pixel_squared_sum = 0.0
    pixel_count = 0

    for image in images:
        valid = np.asarray(image)[np.isfinite(image)].astype(np.float64)
        pixel_sum += float(valid.sum())
        pixel_squared_sum += float(np.square(valid).sum())
        pixel_count += valid.size

    if pixel_count == 0:
        raise ValueError("No finite training pixels are available.")

    mean = pixel_sum / pixel_count
    variance = max(pixel_squared_sum / pixel_count - mean**2, 0.0)
    std = float(np.sqrt(variance))
    if not np.isfinite(std) or std == 0:
        raise ValueError(f"Invalid pixel standard deviation: {std}")
    return float(mean), std


def normalize_image(image, mean: float, std: float) -> np.ndarray:
    """Fill non-finite pixels with the training mean, then standardize."""
    if not np.isfinite(std) or std <= 0:
        raise ValueError("std must be finite and greater than zero.")
    result = np.asarray(image, dtype=np.float32).copy()
    result[~np.isfinite(result)] = mean
    return (result - mean) / std


def prepare_cnn_arrays(dataset_df: pd.DataFrame):
    """Build CNN train/validation/test arrays using training-only pixel statistics.

    Returns (arrays, mean, std). Each X array has shape (N, 64, 64, 1).
    """
    required = {"SPLIT", "IMAGE", "TARGET_WIND"}
    if not required.issubset(dataset_df.columns):
        raise ValueError(f"dataset_df must contain {sorted(required)}")

    train_rows = dataset_df[dataset_df["SPLIT"] == "1_train"]
    if train_rows.empty:
        raise ValueError("No matched training images found.")

    mean, std = compute_pixel_stats(train_rows["IMAGE"])
    arrays = {}
    for split_label, short_name in [
        ("1_train", "train"), ("2_val", "val"), ("3_test", "test")
    ]:
        part = dataset_df[dataset_df["SPLIT"] == split_label]
        if part.empty:
            raise ValueError(f"No matched samples found for split {split_label}.")
        X = np.stack([
            normalize_image(img, mean, std)[..., np.newaxis]
            for img in part["IMAGE"]
        ]).astype(np.float32)
        y = part["TARGET_WIND"].to_numpy(dtype=np.float32)
        arrays[f"X_{short_name}"] = X
        arrays[f"y_{short_name}"] = y

    return arrays, mean, std


def prepare_convlstm_arrays(sequences_df: pd.DataFrame):
    """Build ConvLSTM arrays using training-sequence pixels for normalization.

    Returns (arrays, mean, std). Each X array has shape (N, 4, 64, 64, 1).
    """
    required = {"SPLIT", "IMAGES", "TARGET_WIND"}
    if not required.issubset(sequences_df.columns):
        raise ValueError(f"sequences_df must contain {sorted(required)}")

    train_sequences = sequences_df[sequences_df["SPLIT"] == "1_train"]
    if train_sequences.empty:
        raise ValueError("No training sequences were created.")

    training_images = [
        image
        for images in train_sequences["IMAGES"]
        for image in images
    ]
    mean, std = compute_pixel_stats(training_images)

    arrays = {}
    for split_label, short_name in [
        ("1_train", "train"), ("2_val", "val"), ("3_test", "test")
    ]:
        part = sequences_df[sequences_df["SPLIT"] == split_label]
        if part.empty:
            raise ValueError(f"No valid sequences for split {split_label}.")
        X = np.stack([
            np.stack([
                normalize_image(image, mean, std) for image in images
            ])[..., np.newaxis]
            for images in part["IMAGES"]
        ]).astype(np.float32)
        y = part["TARGET_WIND"].to_numpy(dtype=np.float32)
        arrays[f"X_{short_name}"] = X
        arrays[f"y_{short_name}"] = y

    return arrays, mean, std
