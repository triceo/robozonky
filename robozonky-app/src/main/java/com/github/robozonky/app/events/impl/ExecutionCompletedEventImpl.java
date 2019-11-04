/*
 * Copyright 2019 The RoboZonky Project
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

package com.github.robozonky.app.events.impl;

import com.github.robozonky.api.notifications.ExecutionCompletedEvent;
import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.strategies.PortfolioOverview;

import java.util.Collection;
import java.util.Collections;

final class ExecutionCompletedEventImpl extends AbstractEventImpl implements ExecutionCompletedEvent {

    private final Collection<Loan> investments;
    private final PortfolioOverview portfolioOverview;

    public ExecutionCompletedEventImpl(final Collection<Loan> investment, final PortfolioOverview portfolio) {
        super("investments");
        this.investments = Collections.unmodifiableCollection(investment);
        this.portfolioOverview = portfolio;
    }

    @Override
    public PortfolioOverview getPortfolioOverview() {
        return portfolioOverview;
    }

    @Override
    public Collection<Loan> getLoansInvestedInto() {
        return investments;
    }
}
