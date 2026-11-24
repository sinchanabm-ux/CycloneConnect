"""Training helper matching the CNN and ConvLSTM experiment settings."""


def train_model(
    model,
    X_train,
    y_train,
    X_val,
    y_val,
    epochs=50,
    batch_size=32,
    patience=8,
    verbose=1,
):
    """Train a compiled Keras regression model with early stopping and LR reduction.

    Use batch_size=32 for the CNN and batch_size=16 for ConvLSTM, as in the
    corresponding notebooks.
    """
    from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau

    callbacks = [
        EarlyStopping(
            monitor="val_loss",
            patience=patience,
            restore_best_weights=True,
            verbose=verbose,
        ),
        ReduceLROnPlateau(
            monitor="val_loss",
            factor=0.5,
            patience=4,
            min_lr=1e-6,
            verbose=verbose,
        ),
    ]
    return model.fit(
        X_train,
        y_train,
        validation_data=(X_val, y_val),
        epochs=epochs,
        batch_size=batch_size,
        callbacks=callbacks,
        verbose=verbose,
    )
