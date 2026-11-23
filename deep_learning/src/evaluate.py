"""Regression metrics for a trained Keras model."""
from __future__ import annotations

import numpy as np


def evaluate_regression(model, X_test, y_test):
    """Return MAE and RMSE in target units (wind-speed knots in this project)."""
    predictions = np.asarray(model.predict(X_test, verbose=0)).reshape(-1)
    actual = np.asarray(y_test).reshape(-1)
    if len(predictions) != len(actual):
        raise ValueError("Prediction count must equal target count.")
    mae = float(np.mean(np.abs(predictions - actual)))
    rmse = float(np.sqrt(np.mean((predictions - actual) ** 2)))
    return {"mae": mae, "rmse": rmse, "predictions": predictions}
