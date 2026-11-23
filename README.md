# CycloneConnect — Deep Learning

This module focuses on forecasting cyclone wind speed using satellite imagery and deep-learning models.

## Experiments

### 1. CNN Baseline
A Convolutional Neural Network (CNN) will be trained as the baseline model to learn spatial features from satellite images and predict wind speed. Its performance will provide a reference for comparison with the ConvLSTM model.

### 2. ConvLSTM
A Convolutional LSTM (ConvLSTM) model uses sequences of four satellite images, each separated by three hours, to learn spatial and temporal patterns and predict future cyclone wind speed.

## Data Preparation

- Uses Spark-generated Parquet datasets: `samples_6h` and `patches_full`.
- Checks timestamp alignment, missing values, and dataset splits.
- Converts satellite patches into 64 × 64 images.
- Matches image inputs with future `TARGET_WIND` labels.
- Calculates normalization statistics using training data only.

## Satellite Channel Selection

Channel 1 was selected because initial data-quality checks showed that Channel 2 contained substantially more missing data. This provides more usable imagery for the current experiments. It does not necessarily mean Channel 1 is more predictive; using both channels could be explored in future experiments.

## Project Structure

```text
deep_learning/
├── CycloneConnect.ipynb
└── src/
    ├── preprocessing.py
    ├── create_sequences.py
    ├── model.py
    ├── train.py
    └── evaluate.py
```

The notebook contains the primary experimental workflow. The `src/` directory contains reusable helper functions.

## Environment

The notebook is designed for Google Colab and uses the dataset archive at:

`/content/drive/MyDrive/cyclone/cyclone_data.tar.gz`

Install dependencies from the repository root:

```bash
pip install -r requirements.txt
```

## Evaluation

The CNN baseline and ConvLSTM will be compared using regression metrics such as Mean Absolute Error (MAE) and Root Mean Squared Error (RMSE).