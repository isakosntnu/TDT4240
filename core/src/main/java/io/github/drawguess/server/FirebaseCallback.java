package io.github.drawguess.server;

public interface FirebaseCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception exception);
}
