"""Helpers for decoding satellite patch bytes and preparing Channel 1 images."""
from __future__ import annotations

import numpy as np


def decode_patch(patch_bytes, channels: int = 2, height: int = 64, width: int = 64):
    """Decode a float16 binary patch to shape (channels, height, width).

    The source notebook stores two 64x64 channels as float16 bytes.
    """
    values = np.frombuffer(patch_bytes, dtype=np.float16)
    expected = channels * height * width
    if values.size != expected:
        raise ValueError(f"Expected {expected} values, received {values.size}.")
    return values.reshape(channels, height, width)


def extract_channel1(patch_bytes, height: int = 64, width: int = 64):
    """Return Channel 1 as float32 with shape (height, width)."""
    patch = decode_patch(patch_bytes, height=height, width=width)
    return patch[0].astype(np.float32)


def is_completely_missing(image) -> bool:
    """Return True when every pixel in an image is NaN."""
    return bool(np.isnan(np.asarray(image)).all())


def normalize_image(image, mean: float, std: float):
    """Normalize an image using statistics calculated from training data."""
    if not np.isfinite(std) or std == 0:
        raise ValueError("Normalization standard deviation must be finite and non-zero.")
    return (np.asarray(image, dtype=np.float32) - mean) / std
