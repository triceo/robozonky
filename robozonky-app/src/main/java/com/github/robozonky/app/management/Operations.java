/*
 * Copyright 2017 The RoboZonky Project
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

package com.github.robozonky.app.management;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.robozonky.api.notifications.Event;
import com.github.robozonky.api.notifications.InvestmentDelegatedEvent;
import com.github.robozonky.api.notifications.InvestmentMadeEvent;
import com.github.robozonky.api.notifications.InvestmentPurchasedEvent;
import com.github.robozonky.api.notifications.InvestmentRejectedEvent;
import com.github.robozonky.api.notifications.SaleOfferedEvent;

class Operations implements OperationsMBean {

    private final Map<Integer, BigDecimal> successfulInvestments = new LinkedHashMap<>(0),
            delegatedInvestments = new LinkedHashMap<>(0), rejectedInvestments = new LinkedHashMap<>(0),
            purchasedInvestments = new LinkedHashMap<>(0), offeredInvestments = new LinkedHashMap<>(0);
    private OffsetDateTime lastInvestmentRunTimestamp;

    @Override
    public Map<Integer, BigDecimal> getSuccessfulInvestments() {
        return this.successfulInvestments;
    }

    @Override
    public Map<Integer, BigDecimal> getDelegatedInvestments() {
        return this.delegatedInvestments;
    }

    @Override
    public Map<Integer, BigDecimal> getRejectedInvestments() {
        return this.rejectedInvestments;
    }

    @Override
    public Map<Integer, BigDecimal> getPurchasedInvestments() {
        return this.purchasedInvestments;
    }

    @Override
    public Map<Integer, BigDecimal> getOfferedInvestments() {
        return this.offeredInvestments;
    }

    @Override
    public OffsetDateTime getLatestUpdatedDateTime() {
        return this.lastInvestmentRunTimestamp;
    }

    void handle(final InvestmentMadeEvent event) {
        this.successfulInvestments.put(event.getInvestment().getLoanId(), event.getInvestment().getOriginalPrincipal());
        registerInvestmentRun(event);
    }

    void handle(final InvestmentDelegatedEvent event) {
        this.delegatedInvestments.put(event.getLoan().getId(), event.getRecommendation());
        registerInvestmentRun(event);
    }

    void handle(final InvestmentRejectedEvent event) {
        this.rejectedInvestments.put(event.getLoan().getId(), event.getRecommendation());
        registerInvestmentRun(event);
    }

    void handle(final SaleOfferedEvent event) {
        this.offeredInvestments.put(event.getInvestment().getLoanId(), event.getInvestment().getOriginalPrincipal());
        registerInvestmentRun(event);
    }

    void handle(final InvestmentPurchasedEvent event) {
        this.purchasedInvestments.put(event.getInvestment().getLoanId(), event.getInvestment().getOriginalPrincipal());
        registerInvestmentRun(event);
    }

    private void registerInvestmentRun(final Event event) {
        this.lastInvestmentRunTimestamp = event.getCreatedOn();
    }
}
