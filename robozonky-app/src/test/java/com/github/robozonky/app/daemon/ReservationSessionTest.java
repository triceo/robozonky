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

package com.github.robozonky.app.daemon;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Money;
import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.remote.entities.Reservation;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.api.strategies.ReservationDescriptor;
import com.github.robozonky.api.strategies.ReservationStrategy;
import com.github.robozonky.app.AbstractZonkyLeveragingTest;
import com.github.robozonky.app.tenant.PowerTenant;
import com.github.robozonky.internal.remote.Zonky;
import com.github.robozonky.internal.remote.entities.LoanImpl;
import com.github.robozonky.internal.remote.entities.MyReservationImpl;
import com.github.robozonky.internal.remote.entities.ReservationImpl;
import com.github.robozonky.internal.tenant.RemotePortfolio;
import com.github.robozonky.test.mock.MockLoanBuilder;
import com.github.robozonky.test.mock.MockReservationBuilder;

class ReservationSessionTest extends AbstractZonkyLeveragingTest {

    private static Reservation mockReservation(Loan loan) {
        final MyReservationImpl mr = mock(MyReservationImpl.class);
        when(mr.getReservedAmount()).thenReturn(Money.from(200));
        return new MockReservationBuilder()
            .set(ReservationImpl::setMyReservation, mr)
            .set(ReservationImpl::setId, loan.getId())
            .set(ReservationImpl::setInterestRate, loan.getInterestRate())
            .build();
    }

    @Test
    void empty() {
        final Zonky z = harmlessZonky();
        final PowerTenant auth = mockTenant(z);
        final Stream<Reservation> i = ReservationSession.process(auth, Stream.empty(), null);
        assertThat(i).isEmpty();
    }

    @Test
    void properReal() {
        final Loan l = new MockLoanBuilder()
            .set(LoanImpl::setAmount, Money.from(200))
            .set(LoanImpl::setInterestRate, Rating.D.getInterestRate())
            .set(LoanImpl::setRemainingInvestment, Money.from(200))
            .set(LoanImpl::setReservedAmount, Money.from(0))
            .set(LoanImpl::setMyInvestment, mockMyInvestment())
            .build();
        final int loanId = l.getId();
        final Reservation p = mockReservation(l);
        final ReservationStrategy s = mock(ReservationStrategy.class);
        when(s.recommend(any(), any(), any())).thenReturn(true);
        final Zonky z = harmlessZonky();
        when(z.getLoan(eq(l.getId()))).thenReturn(l);
        final PowerTenant auth = mockTenant(z, false);
        final ReservationDescriptor pd = new ReservationDescriptor(p, () -> l);
        final Stream<Reservation> i = ReservationSession.process(auth, Stream.of(pd), s);
        assertThat(i).hasSize(1);
        assertThat(getEventsRequested()).hasSize(3);
        verify(z).accept(eq(p));
        final RemotePortfolio rp = auth.getPortfolio();
        var rating = l.getInterestRate();
        verify(rp).simulateCharge(eq(loanId), eq(rating), any());
    }

    @Test
    void properDry() {
        final Loan l = new MockLoanBuilder()
            .set(LoanImpl::setAmount, Money.from(200))
            .set(LoanImpl::setInterestRate, Rating.D.getInterestRate())
            .set(LoanImpl::setRemainingInvestment, Money.from(200))
            .set(LoanImpl::setReservedAmount, Money.from(0))
            .set(LoanImpl::setMyInvestment, mockMyInvestment())
            .build();
        final int loanId = l.getId();
        final Reservation p = mockReservation(l);
        final ReservationStrategy s = mock(ReservationStrategy.class);
        when(s.recommend(any(), any(), any())).thenReturn(true);
        final Zonky z = harmlessZonky();
        when(z.getLoan(eq(loanId))).thenReturn(l);
        final PowerTenant auth = mockTenant(z);
        final ReservationDescriptor pd = new ReservationDescriptor(p, () -> l);
        final Stream<Reservation> i = ReservationSession.process(auth, Stream.of(pd), s);
        assertThat(i).hasSize(1);
        assertThat(getEventsRequested()).hasSize(3);
        verify(z, never()).accept(eq(p));
        final RemotePortfolio rp = auth.getPortfolio();
        var rating = l.getInterestRate();
        verify(rp).simulateCharge(eq(loanId), eq(rating), any());
        verify(auth).setKnownBalanceUpperBound(eq(Money.from(Integer.MAX_VALUE - 200)));
    }

    @Test
    void properFail() {
        final Loan l = new MockLoanBuilder()
            .set(LoanImpl::setAmount, Money.from(200))
            .set(LoanImpl::setInterestRate, Rating.D.getInterestRate())
            .set(LoanImpl::setRemainingInvestment, Money.from(200))
            .set(LoanImpl::setReservedAmount, Money.from(0))
            .set(LoanImpl::setMyInvestment, mockMyInvestment())
            .build();
        final int loanId = l.getId();
        final Reservation p = mockReservation(l);
        final ReservationStrategy s = mock(ReservationStrategy.class);
        when(s.recommend(any(), any(), any())).thenReturn(true);
        final Zonky z = harmlessZonky();
        when(z.getLoan(eq(loanId))).thenReturn(l);
        doThrow(IllegalStateException.class).when(z)
            .accept(any());
        final PowerTenant auth = mockTenant(z, false);
        final ReservationDescriptor pd = new ReservationDescriptor(p, () -> l);
        final Stream<Reservation> i = ReservationSession.process(auth, Stream.of(pd), s);
        assertThat(i).isEmpty();
        assertThat(getEventsRequested()).hasSize(2);
        verify(z).accept(eq(p));
        final RemotePortfolio rp = auth.getPortfolio();
        var rating = l.getInterestRate();
        verify(rp, never()).simulateCharge(eq(loanId), eq(rating), any());
        verify(auth).setKnownBalanceUpperBound(eq(Money.from(199)));
    }
}
