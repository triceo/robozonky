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
import java.util.Objects;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.Ratio;
import com.github.robozonky.internal.test.DateUtil;

final class Blocked {

    private final int id;
    private final Money amount;
    private final Ratio rating;
    private final boolean persistent;
    private final ZonedDateTime storedOn = DateUtil.zonedNow();

    Blocked(final int id, final Money amount, final Ratio rating) {
        this(id, amount, rating, false);
    }

    public Blocked(final int id, final Money amount, final Ratio rating, final boolean persistent) {
        this.id = id;
        this.amount = Money.from(amount.getValue()
            .abs());
        this.rating = rating;
        this.persistent = persistent;
    }

    public int getId() {
        return id;
    }

    public Money getAmount() {
        return amount;
    }

    public Ratio getInterestRate() {
        return rating;
    }

    public boolean isValid(final RemoteData remoteData) {
        return persistent || storedOn.toOffsetDateTime()
            .isAfter(remoteData.getStatistics()
                .getTimestamp());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !Objects.equals(getClass(), o.getClass())) {
            return false;
        }
        final Blocked blocked = (Blocked) o;
        return id == blocked.id &&
                Objects.equals(amount, blocked.amount) &&
                Objects.equals(rating, blocked.rating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, rating);
    }

    @Override
    public String toString() {
        return "BlockedAmount{" +
                "amount=" + amount +
                ", id=" + id +
                ", persistent=" + persistent +
                ", rating=" + rating +
                ", storedOn=" + storedOn +
                '}';
    }
}
