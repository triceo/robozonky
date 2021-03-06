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

package com.github.robozonky.strategy.natural.wrappers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalInt;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.Ratio;
import com.github.robozonky.api.remote.entities.Investment;
import com.github.robozonky.api.remote.enums.DetailLabel;
import com.github.robozonky.api.remote.enums.LoanHealth;
import com.github.robozonky.api.remote.enums.MainIncomeType;
import com.github.robozonky.api.remote.enums.Purpose;
import com.github.robozonky.api.remote.enums.Region;
import com.github.robozonky.api.remote.enums.SellStatus;
import com.github.robozonky.api.strategies.InvestmentDescriptor;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.internal.remote.entities.InvestmentImpl;

final class InvestmentWrapper extends AbstractWrapper<InvestmentDescriptor> {

    private final Investment investment;

    public InvestmentWrapper(final InvestmentDescriptor original, final PortfolioOverview portfolioOverview) {
        super(original, portfolioOverview);
        this.investment = original.item();
    }

    @Override
    public long getId() {
        return investment.getId();
    }

    @Override
    public long getLoanId() {
        return investment.getLoan()
            .getId();
    }

    @Override
    public boolean isInsuranceActive() {
        return investment.getLoan()
            .getDetailLabels()
            .contains(DetailLabel.CURRENTLY_INSURED);
    }

    @Override
    public Region getRegion() {
        return investment.getLoan()
            .getBorrower()
            .getRegion();
    }

    @Override
    public Optional<LoanHealth> getHealth() {
        return Optional.of(InvestmentImpl.determineHealth(investment));
    }

    @Override
    public String getStory() {
        return investment.getLoan()
            .getStory()
            .orElseGet(() -> getOriginal().related()
                .getStory());
    }

    @Override
    public MainIncomeType getMainIncomeType() {
        return investment.getLoan()
            .getBorrower()
            .getPrimaryIncomeType()
            .orElseGet(() -> getOriginal().related()
                .getMainIncomeType());
    }

    @Override
    public Ratio getInterestRate() {
        return investment.getLoan()
            .getInterestRate();
    }

    @Override
    public Purpose getPurpose() {
        return investment.getLoan()
            .getPurpose();
    }

    @Override
    public int getOriginalTermInMonths() {
        return investment.getLoan()
            .getPayments()
            .getTotal();
    }

    @Override
    public int getRemainingTermInMonths() {
        return investment.getLoan()
            .getPayments()
            .getUnpaid();
    }

    @Override
    public int getOriginalAmount() {
        return getOriginal().related()
            .getAmount()
            .getValue()
            .intValue();
    }

    @Override
    public int getOriginalAnnuity() {
        return investment.getLoan()
            .getAnnuity()
            .orElseThrow(() -> new IllegalStateException("Investment has no annuity: " + investment))
            .getValue()
            .intValue();
    }

    @Override
    public BigDecimal getRemainingPrincipal() {
        return investment.getPrincipal()
            .getUnpaid()
            .getValue();
    }

    @Override
    public Optional<BigDecimal> getReturns() { // FIXME add penalties when Zonky API supports it again
        var interest = investment.getInterest()
            .getPaid();
        var principal = investment.getPrincipal()
            .getPaid();
        return Optional.of(interest.add(principal)
            .subtract(getSellFee().orElse(BigDecimal.ZERO))
            .getValue());
    }

    @Override
    public Optional<BigDecimal> getSellFee() {
        if (!investment.getSellStatus()
            .isSellable()) {
            return Optional.empty();
        }
        if (investment.getSellStatus() == SellStatus.SELLABLE_WITHOUT_FEE) {
            return Optional.of(BigDecimal.ZERO);
        }
        var fee = investment.getSmpSellInfo()
            .map(sellInfo -> sellInfo.getFee()
                .getValue())
            .orElse(Money.ZERO)
            .getValue();
        return Optional.of(fee);
    }

    @Override
    public Optional<BigDecimal> getOriginalPurchasePrice() {
        return Optional.of(investment.getSmpSellInfo()
            .map(sellInfo -> sellInfo.getBoughtFor()
                .getValue())
            .orElseGet(this::getRemainingPrincipal));
    }

    @Override
    public Optional<BigDecimal> getSellPrice() {
        if (!investment.getSellStatus()
            .isSellable()) {
            return Optional.empty();
        }
        return Optional.of(InvestmentImpl.determineSellPrice(investment)
            .getValue());
    }

    @Override
    public OptionalInt getCurrentDpd() {
        return OptionalInt.of(investment.getLoan()
            .getDpd());
    }

    @Override
    public OptionalInt getLongestDpd() {
        if (InvestmentImpl.determineHealth(investment) == LoanHealth.HEALTHY) { // Avoids HTTP request.
            return OptionalInt.of(0);
        }
        return investment.getLoan()
            .getHealthStats()
            .map(s -> OptionalInt.of(s.getLongestDaysDue()))
            .orElseGet(OptionalInt::empty);
    }

    @Override
    public OptionalInt getDaysSinceDpd() {
        if (InvestmentImpl.determineHealth(investment) == LoanHealth.HEALTHY) { // Avoids HTTP request.
            return OptionalInt.of(0);
        }
        return investment.getLoan()
            .getHealthStats()
            .map(s -> OptionalInt.of(s.getDaysSinceLastInDue()))
            .orElseGet(OptionalInt::empty);
    }

    @Override
    public String toString() {
        return "Wrapper for loan #" + getLoanId() + ", investment #" + getId();
    }
}
