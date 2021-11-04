package net.devtech.jerraria.data;

public record JCElement<T>(NativeJCType<T> type, T value) {
}
