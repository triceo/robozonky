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

import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.remote.entities.Investment;
import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.app.AbstractZonkyLeveragingTest;
import com.github.robozonky.internal.remote.Zonky;
import com.github.robozonky.internal.remote.entities.InvestmentImpl;
import com.github.robozonky.internal.remote.entities.InvestmentLoanDataImpl;
import com.github.robozonky.internal.remote.entities.LoanImpl;
import com.github.robozonky.internal.tenant.Tenant;
import com.github.robozonky.internal.test.DateUtil;
import com.github.robozonky.internal.util.functional.Tuple;
import com.github.robozonky.test.mock.MockLoanBuilder;

class RemoteDataTest extends AbstractZonkyLeveragingTest {

    @Test
    void getters() {
        final Zonky zonky = harmlessZonky();
        final Tenant tenant = mockTenant(zonky);
        final RemoteData data = RemoteData.load(tenant);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(data.getStatistics())
                .isNotNull();
            softly.assertThat(data.getBlocked())
                .isEmpty();
            softly.assertThat(data.getRetrievedOn())
                .isBeforeOrEqualTo(DateUtil.zonedNow());
        });
    }

    @Test
    void amountsBlocked() {
        final Zonky zonky = harmlessZonky();
        final Tenant tenant = mockTenant(zonky);
        final Loan loan = new MockLoanBuilder()
            .set(LoanImpl::setInterestRate, Rating.D.getInterestRate())
            .build();
        Investment i = new InvestmentImpl(new InvestmentLoanDataImpl(loan), Money.from(10));
        when(zonky.getPendingInvestments()).thenReturn(Stream.of(i));
        var result = RemoteData.getAmountsBlocked(tenant);
        Assertions.assertThat(result)
            .containsOnly(Map.entry(i.getLoan()
                .getId(), Tuple.of(Rating.D.getInterestRate(), Money.from(10))));
    }

}
