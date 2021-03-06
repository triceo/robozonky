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

package com.github.robozonky.strategy.natural.conditions;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.wrappers.Wrapper;

public final class LoanInterestRateCondition extends AbstractRangeCondition<Ratio> {

    private LoanInterestRateCondition(final RangeCondition<Ratio> condition) {
        super(condition, false);
    }

    public static LoanInterestRateCondition lessThan(final Ratio threshold) {
        final RangeCondition<Ratio> c = RangeCondition.lessThan(Wrapper::getInterestRate, RATE_DOMAIN, threshold);
        return new LoanInterestRateCondition(c);
    }

    public static LoanInterestRateCondition moreThan(final Ratio threshold) {
        final RangeCondition<Ratio> c = RangeCondition.moreThan(Wrapper::getInterestRate, RATE_DOMAIN, threshold);
        return new LoanInterestRateCondition(c);
    }

    public static LoanInterestRateCondition exact(final Ratio rate) {
        return exact(rate, rate);
    }

    public static LoanInterestRateCondition exact(final Ratio minimumThreshold, final Ratio maximumThreshold) {
        final RangeCondition<Ratio> c = RangeCondition.exact(Wrapper::getInterestRate, RATE_DOMAIN,
                minimumThreshold, maximumThreshold);
        return new LoanInterestRateCondition(c);
    }
}
