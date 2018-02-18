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

package com.github.robozonky.api.notifications;

import java.math.BigDecimal;

import com.github.robozonky.api.remote.entities.sanitized.Investment;
import com.github.robozonky.api.remote.entities.sanitized.Loan;
import com.github.robozonky.api.strategies.RecommendedInvestment;
import com.github.robozonky.api.strategies.SellStrategy;

/**
 * Fired immediately after {@link SellStrategy} has recommended a particular investment. {@link SaleRequestedEvent} may
 * be fired next.
 */
public final class SaleRecommendedEvent extends Event implements InvestmentBased,
                                                                 Recommending {

    private final Investment investment;
    private final Loan loan;
    private final BigDecimal recommendation;

    public SaleRecommendedEvent(final RecommendedInvestment recommendation) {
        this.investment = recommendation.descriptor().item();
        this.loan = recommendation.descriptor().related();
        this.recommendation = recommendation.amount();
    }

    @Override
    public Loan getLoan() {
        return loan;
    }

    @Override
    public Investment getInvestment() {
        return investment;
    }

    @Override
    public BigDecimal getRecommendation() {
        return recommendation;
    }
}
