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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.Ratio;
import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.remote.entities.Participation;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.api.strategies.ParticipationDescriptor;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.api.strategies.PurchaseStrategy;
import com.github.robozonky.internal.remote.entities.LoanImpl;
import com.github.robozonky.internal.remote.entities.ParticipationImpl;
import com.github.robozonky.strategy.natural.conditions.MarketplaceFilter;
import com.github.robozonky.strategy.natural.conditions.MarketplaceFilterCondition;
import com.github.robozonky.test.AbstractMinimalRoboZonkyTest;
import com.github.robozonky.test.mock.MockLoanBuilder;

class NaturalLanguagePurchaseStrategyTest extends AbstractMinimalRoboZonkyTest {

    private static ParticipationDescriptor mockDescriptor() {
        return mockDescriptor(mockParticipation());
    }

    private static ParticipationDescriptor mockDescriptor(final Participation participation) {
        final Loan l = new MockLoanBuilder()
            .set(LoanImpl::setAmount, Money.from(100_000))
            .build();
        return new ParticipationDescriptor(participation, () -> l);
    }

    private static Participation mockParticipation() {
        Participation participation = mock(ParticipationImpl.class);
        when(participation.getInterestRate()).thenReturn(Rating.D.getInterestRate());
        return participation;
    }

    @Test
    void unacceptablePortfolioDueToOverInvestment() {
        final DefaultValues v = new DefaultValues(DefaultPortfolio.EMPTY);
        v.setTargetPortfolioSize(1000);
        final ParsedStrategy p = new ParsedStrategy(v);
        final PurchaseStrategy s = new NaturalLanguagePurchaseStrategy(p);
        final PortfolioOverview portfolio = mock(PortfolioOverview.class);
        when(portfolio.getInvested()).thenReturn(p.getMaximumInvestmentSize());
        final boolean result = s.recommend(mockDescriptor(), () -> portfolio, mockSessionInfo());
        assertThat(result).isFalse();
    }

    @Test
    void noLoansApplicable() {
        final MarketplaceFilter filter = MarketplaceFilter.of(MarketplaceFilterCondition.alwaysAccepting());
        final DefaultValues v = new DefaultValues(DefaultPortfolio.PROGRESSIVE);
        final FilterSupplier w = new FilterSupplier(v, Collections.emptySet(), Collections.singleton(filter));
        final ParsedStrategy p = new ParsedStrategy(v, Collections.emptySet(), Collections.emptyMap(),
                Collections.emptyMap(), w);
        final PurchaseStrategy s = new NaturalLanguagePurchaseStrategy(p);
        final PortfolioOverview portfolio = mock(PortfolioOverview.class);
        when(portfolio.getShareOnInvestment(any())).thenReturn(Ratio.ZERO);
        when(portfolio.getInvested()).thenReturn(p.getMaximumInvestmentSize()
            .subtract(1));
        final boolean result = s.recommend(mockDescriptor(), () -> portfolio, mockSessionInfo());
        assertThat(result).isFalse();
    }

    @Test
    void nothingRecommendedDueToRatingOverinvested() {
        final ParsedStrategy p = new ParsedStrategy(DefaultPortfolio.EMPTY);
        final PurchaseStrategy s = new NaturalLanguagePurchaseStrategy(p);
        final PortfolioOverview portfolio = mock(PortfolioOverview.class);
        when(portfolio.getInvested()).thenReturn(p.getMaximumInvestmentSize()
            .subtract(1));
        when(portfolio.getShareOnInvestment(any())).thenReturn(Ratio.ZERO);
        final Participation l = mockParticipation();
        doReturn(Rating.A.getInterestRate()).when(l)
            .getInterestRate();
        final boolean result = s.recommend(mockDescriptor(l), () -> portfolio, mockSessionInfo());
        assertThat(result).isFalse();
    }

    @Test
    void recommendationIsMade() {
        final DefaultValues v = new DefaultValues(DefaultPortfolio.PROGRESSIVE);
        final ParsedStrategy p = new ParsedStrategy(v, Collections.emptyList(), Collections.emptyMap(),
                Collections.emptyMap(),
                new FilterSupplier(v, null, Collections.emptySet()));
        final PurchaseStrategy s = new NaturalLanguagePurchaseStrategy(p);
        final PortfolioOverview portfolio = mock(PortfolioOverview.class);
        when(portfolio.getInvested()).thenReturn(p.getMaximumInvestmentSize()
            .subtract(1));
        when(portfolio.getShareOnInvestment(any())).thenReturn(Ratio.ZERO);
        final Participation participation = mockParticipation();
        doReturn(Money.from(100_000)).when(participation)
            .getRemainingPrincipal(); // not recommended for balance
        doReturn(Rating.A.getInterestRate()).when(participation)
            .getInterestRate();
        final Participation p2 = mockParticipation();
        final int amount = 199; // check amounts under Zonky investment minimum
        doReturn(Money.from(amount)).when(p2)
            .getRemainingPrincipal();
        doReturn(Rating.A.getInterestRate()).when(p2)
            .getInterestRate();
        final ParticipationDescriptor pd = mockDescriptor(p2);
        final boolean result = s.recommend(mockDescriptor(participation), () -> portfolio, mockSessionInfo());
        assertThat(result).isFalse();
        final boolean result2 = s.recommend(pd, () -> portfolio, mockSessionInfo());
        assertThat(result2).isTrue();
    }
}
