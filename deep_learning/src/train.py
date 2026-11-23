"""Training helper for a compiled Keras regression model."""


def train_model(model, X_train, y_train, X_val, y_val,
                epochs=50, batch_size=16, patience=8, verbose=1):
    """Train with early stopping and learning-rate reduction; return History."""
    from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau

    callbacks = [
        EarlyStopping(
            monitor="val_loss", patience=patience,
            restore_best_weights=True, verbose=verbose
        ),
        ReduceLROnPlateau(
            monitor="val_loss", factor=0.5, patience=4,
            min_lr=1e-6, verbose=verbose
        ),
    ]
    return model.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=epochs,
        batch_size=batch_size,
        callbacks=callbacks,
        verbose=verbose,
    )
