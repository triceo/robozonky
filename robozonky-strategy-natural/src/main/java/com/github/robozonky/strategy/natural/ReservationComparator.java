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

package com.github.robozonky.strategy.natural;

import java.util.Comparator;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.api.remote.entities.Reservation;
import com.github.robozonky.api.strategies.ReservationDescriptor;

final class ReservationComparator implements Comparator<ReservationDescriptor> {

    private final Comparator<Reservation> comparator;

    public ReservationComparator(Comparator<Ratio> ratingByDemandComparator) {
        this.comparator = Comparator.comparing(Reservation::getInterestRate, ratingByDemandComparator);
    }

    @Override
    public int compare(final ReservationDescriptor loan1, final ReservationDescriptor loan2) {
        return comparator.compare(loan1.item(), loan2.item());
    }
}
