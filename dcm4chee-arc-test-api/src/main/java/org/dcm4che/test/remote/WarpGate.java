package org.dcm4che.test.remote;

import java.util.concurrent.Future;
import java.util.function.Supplier;

public interface WarpGate {
    <R> R warp(Supplier<R> warpable);

    void warp(Runnable warpable);

    <R> Future<R> warpAsync(Supplier<R> warpable);

    Future<Void> warpAsync(Runnable warpable);
}
