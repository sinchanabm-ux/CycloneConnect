# CycloneConnect — Deep Learning

This folder contains the ConvLSTM deep-learning experiment for cyclone wind-speed forecasting.

## What the notebook does

1. Loads the Spark-generated Parquet datasets (`samples_6h` and `patches_full`).
2. Checks label timestamps, satellite-patch alignment, dataset splits, and missing pixel values.
3. Uses **Channel 1** because Channel 2 has substantial missing data.
4. Decodes each satellite patch into a 64 × 64 image.
5. Builds a sequence of **four images**, each three hours apart.
6. Uses the matched future `TARGET_WIND` label for the prediction target.
7. Calculates normalization statistics from training images only.
8. Trains a ConvLSTM regression model and saves the trained model.

The notebook is the primary, end-to-end record of the experiment.

## Notebook environment

The notebook was developed for Google Colab and expects the project data archive in Google Drive at:

`/content/drive/MyDrive/cyclone/cyclone_data.tar.gz`

After mounting Drive, it extracts the archive to `/content/data` and expects these folders:

- `/content/data/samples_6h`
- `/content/data/patches_full`

Update those paths if your Drive or archive layout is different.

## Python modules

The `src/` directory contains reusable helper functions extracted into small modules. They are intended to make key steps easier to understand and reuse; the notebook remains the complete workflow and may contain additional dataset-specific preparation.

- `preprocessing.py` — decode satellite patch bytes and select Channel 1.
- `create_sequences.py` — create four-frame, three-hourly sequences with matching targets.
- `model.py` — build the ConvLSTM model architecture used in the notebook.
- `train.py` — train a compiled Keras model with early stopping and learning-rate reduction.
- `evaluate.py` — calculate regression metrics on a supplied test set.

These modules do not automatically download the dataset or reproduce the entire notebook on their own. They require prepared data/arrays as described in their docstrings.

## Install dependencies

In a Python environment, install the listed packages:

```bash
pip install -r requirements.txt
```

TensorFlow may require a Python version supported by the TensorFlow release for your operating system. Google Colab is the simplest environment for running the original notebook.

## Model input and output

- Input shape per example: `(4, 64, 64, 1)`
- Four sequential Channel 1 satellite images
- Output: one predicted wind-speed value
- The notebook saves the trained model to `/content/drive/MyDrive/cyclone/convlstm_cyclone_model.keras`

## Important result-reporting note

Only report metrics that are present in the executed notebook outputs. Validation performance is not the same as final test performance; do not describe a test result unless the model has actually been evaluated on `X_test` and `y_test`.

## Suggested GitHub layout

```text
deep_learning/
├── README.md
├── requirements.txt
├── CycloneConnect.ipynb
└── src/
    ├── __init__.py
    ├── preprocessing.py
    ├── create_sequences.py
    ├── model.py
    ├── train.py
    └── evaluate.py
```
