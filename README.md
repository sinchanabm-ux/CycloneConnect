# CycloneConnect — Deep Learning

## Overview
This component of CycloneConnect explores cyclone wind speed prediction using satellite imagery. It implements and compares two deep learning models: Convolutional Neural Network (CNN) and Convolutional LSTM (ConvLSTM).

## Workflow
1. **Data Preparation:** Use Spark-generated datasets (`samples_6h` and `patches_full`).
2. **Preprocessing:** Prepare satellite patches as 64 × 64 images and align them with cyclone observations using storm identifiers and timestamps.
3. **Channel Selection:** Use Channel 1 because Channel 2 contains substantially more missing data.
4. **Model Training:** Train CNN and ConvLSTM models to predict `TARGET_WIND`.
5. **Evaluation:** Compare both models using MAE and RMSE on the same test observations.

## Models

**CNN — Baseline Model**
- Uses a single 64 × 64 satellite image.
- Learns spatial features to predict wind speed.

**ConvLSTM — Temporal Model**
- Uses four satellite images separated by three hours.
- Learns spatial and temporal patterns across a nine-hour sequence.

Both models were evaluated on 1,013 test observations using the same target-label definition.

## Results

| Metric | CNN | ConvLSTM |
|---|---:|---:|
| Test observations | 1,013 | 1,013 |
| MAE | 12.8231 knots | 12.5253 knots |
| RMSE | 17.6671 knots | 17.0156 knots |

**Conclusion:** ConvLSTM performed slightly better, reducing MAE by 2.32% and RMSE by 3.69% compared with CNN on the evaluated test observations.

## Project Structure

```text
deep_learning/
├── notebooks/
│   ├── 01_CNN_Channel1.ipynb
│   ├── 02_ConvLSTM_Channel1.ipynb
│   └── 03_Model_Comparison.ipynb
└── src/
    ├── __init__.py
    ├── preprocessing.py
    ├── create_sequences.py
    ├── model.py
    ├── train.py
    └── evaluate.py
```

- **notebooks/** — Model training, evaluation, and comparison experiments.
- **src/** — Python modules intended for reusable preprocessing, sequence creation, model building, training, and evaluation.

## Technologies
Python, TensorFlow/Keras, NumPy, Pandas, Matplotlib, and PyArrow.

## Running the Experiments
The notebooks were developed in Google Colab and require access to the prepared datasets.

Run the notebooks in order:
1. `01_CNN_Channel1.ipynb`
2. `02_ConvLSTM_Channel1.ipynb`
3. `03_Model_Comparison.ipynb`

Install dependencies from the repository root:

```bash
pip install -r requirements.txt
```

## Future Improvements
- Experiment with additional satellite channels and sequence lengths.
- Explore alternative model architectures and hyperparameters.
- Improve prediction accuracy across different cyclone conditions.