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

package com.github.robozonky.internal.remote.entities;

import java.util.StringJoiner;

import javax.xml.bind.annotation.XmlElement;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.remote.entities.Restrictions;

public class RestrictionsImpl extends BaseEntity implements Restrictions {

    @XmlElement
    private boolean cannotInvest;
    @XmlElement
    private boolean cannotAccessSmp;
    @XmlElement
    private Money minimumInvestmentAmount = Money.from(200);
    @XmlElement
    private Money maximumInvestmentAmount = Money.from(5_000);
    @XmlElement
    private Money investmentStep = Money.from(200);

    public RestrictionsImpl(final boolean permissive) {
        this.cannotAccessSmp = !permissive;
        this.cannotInvest = !permissive;
    }

    public RestrictionsImpl() {
        this(false);
    }

    @Override
    public boolean isCannotInvest() {
        return cannotInvest;
    }

    @Override
    public boolean isCannotAccessSmp() {
        return cannotAccessSmp;
    }

    @Override
    public Money getMinimumInvestmentAmount() {
        return minimumInvestmentAmount;
    }

    @Override
    public Money getInvestmentStep() {
        return investmentStep;
    }

    @Override
    public Money getMaximumInvestmentAmount() {
        return maximumInvestmentAmount;
    }

    public void setCannotInvest(final boolean cannotInvest) {
        this.cannotInvest = cannotInvest;
    }

    public void setCannotAccessSmp(final boolean cannotAccessSmp) {
        this.cannotAccessSmp = cannotAccessSmp;
    }

    public void setMinimumInvestmentAmount(final Money minimumInvestmentAmount) {
        this.minimumInvestmentAmount = minimumInvestmentAmount;
    }

    public void setMaximumInvestmentAmount(final Money maximumInvestmentAmount) {
        this.maximumInvestmentAmount = maximumInvestmentAmount;
    }

    public void setInvestmentStep(final Money investmentStep) {
        this.investmentStep = investmentStep;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RestrictionsImpl.class.getSimpleName() + "[", "]")
            .add("cannotAccessSmp=" + cannotAccessSmp)
            .add("cannotInvest=" + cannotInvest)
            .add("investmentStep=" + investmentStep)
            .add("maximumInvestmentAmount=" + maximumInvestmentAmount)
            .add("minimumInvestmentAmount=" + minimumInvestmentAmount)
            .toString();
    }
}