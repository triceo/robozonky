/*
 * Copyright 2020 The RoboZonky Project
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

package com.github.robozonky.app.tenant;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.Ratio;
import com.github.robozonky.api.remote.entities.Statistics;
import com.github.robozonky.internal.remote.Zonky;
import com.github.robozonky.internal.tenant.Tenant;
import com.github.robozonky.internal.test.DateUtil;
import com.github.robozonky.internal.util.functional.Tuple;
import com.github.robozonky.internal.util.functional.Tuple2;

final class RemoteData {

    private static final Logger LOGGER = LogManager.getLogger(RemoteData.class);

    private final Statistics statistics;
    private final Map<Integer, Tuple2<Ratio, Money>> blocked;
    private final ZonedDateTime retrievedOn = DateUtil.zonedNow();

    private RemoteData(final Statistics statistics, final Map<Integer, Tuple2<Ratio, Money>> blocked) {
        this.statistics = statistics;
        this.blocked = blocked;
    }

    public static RemoteData load(final Tenant tenant) {
        LOGGER.debug("Loading the latest Zonky portfolio information.");
        final Statistics statistics = tenant.call(Zonky::getStatistics);
        final Map<Integer, Tuple2<Ratio, Money>> blocked = getAmountsBlocked(tenant);
        LOGGER.debug("Finished.");
        return new RemoteData(statistics, blocked);
    }

    static Map<Integer, Tuple2<Ratio, Money>> getAmountsBlocked(final Tenant tenant) {
        return tenant.call(Zonky::getPendingInvestments)
            .peek(investment -> LOGGER.debug("Found: {}.", investment))
            .collect(Collectors.toMap(i -> i.getLoan()
                .getId(),
                    i -> Tuple.of(i.getLoan()
                        .getInterestRate(),
                            i.getPrincipal()
                                .getUnpaid())));
    }

    public ZonedDateTime getRetrievedOn() {
        return retrievedOn;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public Map<Integer, Tuple2<Ratio, Money>> getBlocked() {
        return Collections.unmodifiableMap(blocked);
    }

    @Override
    public String toString() {
        return "RemoteData{" +
                "blocked=" + blocked +
                ", retrievedOn=" + retrievedOn +
                ", statistics=" + statistics +
                '}';
    }
}
