package com.hoymiles.infrastructure.dtu.utils;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.BiPredicate;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class RxUtils {
    private static final Logger logger = LogManager.getLogger(RxUtils.class);

    @Contract(pure = true)
    public static @NotNull BiPredicate<Integer, Throwable> retryPredicate(int maxRetries) {
        return (retryCount, error) -> retryCount < maxRetries;
    }

    @Contract(pure = true)
    public static @NonNull @NotNull Function<? super Observable<Throwable>, ? extends ObservableSource<?>> retryPredicate(int maxRetries, int delaySec) {
        return (error) -> error.take(maxRetries).delay(delaySec, TimeUnit.SECONDS);
    }

    @Contract(pure = true)
    public static @NonNull @NotNull Function<? super Observable<Throwable>, ? extends ObservableSource<?>> retryWithExponentialBackoff(
            Predicate<? super Throwable> predicate,
            Integer maxTry)
    {
        return RxUtils.retryPredicate(predicate, maxTry, count -> (long) Math.pow(2, count - 1), TimeUnit.SECONDS);
    }

    /**
     * Retry Handler Support
     *
     * @param predicate      filter error
     * @param maxTry         max attempts
     * @param periodStrategy delay
     * @param timeUnit       unit
     */
    public static @NonNull Function<? super Observable<Throwable>, ? extends ObservableSource<?>> retryPredicate(
            Predicate<? super Throwable> predicate,
            Integer maxTry,
            Function<Long, Long> periodStrategy,
            TimeUnit timeUnit)
    {
        LongAdder errorCount = new LongAdder();
        return (error) -> error.doOnNext(e -> {
                    errorCount.increment();
                    long currentCount = errorCount.longValue();
                    boolean tryContinue = predicate.test(e) && currentCount < maxTry;
                    logger.info(String.format("No. of errors: %d %s, %s", currentCount, e.getClass(),
                            tryContinue
                                    ? String.format("please wait %d %s.", periodStrategy.apply(currentCount), timeUnit.name())
                                    : "skip and throw"));
                    if (!tryContinue)
                        throw e;
                })
                .flatMapSingle(e -> Single.timer(periodStrategy.apply(errorCount.longValue()), timeUnit));
    }
}
