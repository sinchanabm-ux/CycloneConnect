"""ConvLSTM regression architecture used in the CycloneConnect notebook."""


def build_model(input_shape=(4, 64, 64, 1)):
    """Build and compile the notebook's ConvLSTM wind-speed regression model."""
    from tensorflow.keras.models import Sequential
    from tensorflow.keras.layers import (
        Input, ConvLSTM2D, BatchNormalization, Conv2D,
        GlobalAveragePooling2D, Dense, Dropout
    )

    model = Sequential([
        Input(shape=input_shape),
        ConvLSTM2D(
            filters=16,
            kernel_size=(3, 3),
            padding="same",
            activation="tanh",
            return_sequences=False
        ),
        BatchNormalization(),
        Conv2D(filters=16, kernel_size=(3, 3), padding="same", activation="relu"),
        GlobalAveragePooling2D(),
        Dense(32, activation="relu"),
        Dropout(0.2),
        Dense(1, activation="linear"),
    ])
    model.compile(
        optimizer="adam",
        loss="mean_squared_error",
        metrics=["mean_absolute_error"]
    )
    return model
