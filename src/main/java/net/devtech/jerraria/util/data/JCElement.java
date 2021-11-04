package net.devtech.jerraria.util.data;

public record JCElement<T>(NativeJCType<T> type, T value) {
}
