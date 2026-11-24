"""Regression evaluation helpers for wind-speed predictions in knots."""

from __future__ import annotations

import numpy as np


def regression_metrics(actual, predicted) -> dict:
    """Calculate MAE and RMSE from actual and predicted wind speeds."""
    actual = np.asarray(actual, dtype=float).reshape(-1)
    predicted = np.asarray(predicted, dtype=float).reshape(-1)
    if actual.size == 0:
        raise ValueError("Cannot evaluate an empty target array.")
    if actual.shape != predicted.shape:
        raise ValueError("Actual and predicted arrays must have the same length.")
    if not np.isfinite(actual).all() or not np.isfinite(predicted).all():
        raise ValueError("Actual and predicted values must all be finite.")

    errors = predicted - actual
    return {
        "mae": float(np.mean(np.abs(errors))),
        "rmse": float(np.sqrt(np.mean(errors ** 2))),
    }


def evaluate_regression(model, X_test, y_test) -> dict:
    """Predict with a Keras model and return MAE, RMSE, and predictions."""
    predictions = np.asarray(model.predict(X_test, verbose=0)).reshape(-1)
    actual = np.asarray(y_test, dtype=float).reshape(-1)
    metrics = regression_metrics(actual, predictions)
    metrics["predictions"] = predictions
    return metrics
