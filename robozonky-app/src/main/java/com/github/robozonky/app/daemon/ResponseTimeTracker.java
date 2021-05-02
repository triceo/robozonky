/*
 * Copyright 2021 The RoboZonky Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robozonky.app.daemon;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.remote.entities.Participation;
import com.github.robozonky.internal.Defaults;

import io.micrometer.core.instrument.Timer;

/**
 * Used to debug how long it takes for a loan to be invested into, or a participation to be purchased.
 * The times are in nanosecond from the moment the robot was first notified of the item, to the moment that the robot
 * triggered the API request to invest or purchase.
 */
final class ResponseTimeTracker {

    private static final Logger LOGGER = LogManager.getLogger(ResponseTimeTracker.class);
    private static final ResponseTimeTracker INSTANCE = new ResponseTimeTracker();

    private final Timer loanTimer;
    private final Timer participationTimer;
    private final Map<Long, Long> loanRegistrations = new ConcurrentHashMap<>(0);
    private final Map<Long, Long> participationRegistrations = new ConcurrentHashMap<>(0);

    private ResponseTimeTracker() {
        this.loanTimer = Timer.builder("robozonky.strategy.response")
            .tag("marketplace", "primary")
            .register(Defaults.METER_REGISTRY);
        this.participationTimer = Timer.builder("robozonky.strategy.response")
            .tag("marketplace", "secondary")
            .register(Defaults.METER_REGISTRY);
    }

    public static CompletableFuture<Void> executeAsync(final BiConsumer<ResponseTimeTracker, Long> operation) {
        var nanotime = System.nanoTime(); // Store current nanotime, as we can't control when the operation will run.
        return CompletableFuture.runAsync(() -> operation.accept(INSTANCE, nanotime));
    }

    private static <Id extends Number> void dispatch(final long nanotime, final Id id,
            final Map<Id, Long> registrations, final Timer timer) {
        var registeredOn = registrations.remove(id);
        if (registeredOn == null) {
            LOGGER.trace("No registration found for #{}.", id);
            return;
        }
        var nanosDuration = nanotime - registeredOn;
        timer.record(nanosDuration, TimeUnit.NANOSECONDS);
    }

    /**
     * Register that a {@link Loan} entered the system at this time.
     *
     * @param nanotime
     * @param id
     */
    public void registerLoan(final long nanotime, final long id) {
        loanRegistrations.putIfAbsent(id, nanotime);
    }

    /**
     * Register that a {@link Participation} entered the system at this time.
     *
     * @param nanotime
     * @param id
     */
    public void registerParticipation(final long nanotime, final long id) {
        participationRegistrations.putIfAbsent(id, nanotime);
    }

    /**
     * Register that an investment attempt was made.
     *
     * @param nanotime
     * @param loan
     */
    public void dispatch(final long nanotime, final Loan loan) {
        dispatch(nanotime, (long) loan.getId(), loanRegistrations, loanTimer);
    }

    /**
     * Register that a purchase attempt was made.
     *
     * @param nanotime
     * @param participation
     */
    public void dispatch(final long nanotime, final Participation participation) {
        dispatch(nanotime, participation.getId(), participationRegistrations, participationTimer);
    }

    /**
     * To be called at the end of operations to write the results and clear any undispatched items.
     */
    public void clear() {
        loanRegistrations.clear();
        participationRegistrations.clear();
    }

}
