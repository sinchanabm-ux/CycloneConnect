"""CNN and ConvLSTM regression architectures matching the experiment notebooks."""


def build_cnn_model(input_shape=(64, 64, 1)):
    """Build the single-image CNN baseline used in 01_CNN_Channel1.ipynb."""
    from tensorflow.keras import Sequential
    from tensorflow.keras.layers import (
        Input, Conv2D, MaxPooling2D, BatchNormalization,
        GlobalAveragePooling2D, Dense, Dropout,
    )

    model = Sequential([
        Input(shape=input_shape),
        Conv2D(16, (3, 3), padding="same", activation="relu"),
        BatchNormalization(),
        MaxPooling2D((2, 2)),
        Conv2D(32, (3, 3), padding="same", activation="relu"),
        BatchNormalization(),
        MaxPooling2D((2, 2)),
        Conv2D(64, (3, 3), padding="same", activation="relu"),
        BatchNormalization(),
        GlobalAveragePooling2D(),
        Dense(32, activation="relu"),
        Dropout(0.2),
        Dense(1, activation="linear"),
    ])
    model.compile(
        optimizer="adam",
        loss="mean_squared_error",
        metrics=["mean_absolute_error"],
    )
    return model


def build_convlstm_model(input_shape=(4, 64, 64, 1)):
    """Build the four-frame ConvLSTM model used in 02_ConvLSTM_Channel1.ipynb."""
    from tensorflow.keras import Sequential
    from tensorflow.keras.layers import (
        Input, ConvLSTM2D, BatchNormalization, Conv2D,
        GlobalAveragePooling2D, Dense, Dropout,
    )

    model = Sequential([
        Input(shape=input_shape),
        ConvLSTM2D(
            filters=16,
            kernel_size=(3, 3),
            padding="same",
            activation="tanh",
            return_sequences=False,
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
        metrics=["mean_absolute_error"],
    )
    return model
