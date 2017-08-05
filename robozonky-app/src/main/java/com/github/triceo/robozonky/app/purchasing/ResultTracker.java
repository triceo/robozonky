/*
 * Copyright 2017 Lukáš Petrovický
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

package com.github.triceo.robozonky.app.purchasing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.github.triceo.robozonky.api.remote.entities.Investment;
import com.github.triceo.robozonky.api.remote.entities.Participation;
import com.github.triceo.robozonky.api.strategies.ParticipationDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResultTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            ResultTracker.class);

    /**
     * We are using volatile so that the write operation is guaranteed to be atomic.
     */
    private volatile Collection<Investment> investmentsMade = new ArrayList<>(0);

    public Collection<ParticipationDescriptor> acceptLoansFromMarketplace(final Collection<Participation> items) {
        if (items == null) {
            ResultTracker.LOGGER.info("Marketplace returned null, possible Zonky downtime.");
            return Collections.emptyList();
        }
        return items.stream()
                .filter(l -> investmentsMade.stream().noneMatch(i -> i.getLoanId() == l.getId()))
                .map(ParticipationDescriptor::new)
                .collect(Collectors.toList());
    }

    public void acceptInvestmentsFromRobot(final Collection<Investment> investments) {
        investments.stream()
                .filter(i -> investmentsMade.stream().noneMatch(i2 -> i2.getLoanId() == i.getLoanId()))
                .forEach(i -> investmentsMade.add(i));
    }

    public Collection<Investment> getInvestmentsMade() {
        return Collections.unmodifiableCollection(investmentsMade);
    }
}
