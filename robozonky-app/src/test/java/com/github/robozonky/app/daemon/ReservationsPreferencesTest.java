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

import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.SessionInfo;
import com.github.robozonky.api.remote.enums.LoanTermInterval;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.api.strategies.ReservationDescriptor;
import com.github.robozonky.api.strategies.ReservationMode;
import com.github.robozonky.api.strategies.ReservationStrategy;
import com.github.robozonky.app.AbstractZonkyLeveragingTest;
import com.github.robozonky.internal.jobs.TenantPayload;
import com.github.robozonky.internal.remote.Zonky;
import com.github.robozonky.internal.remote.entities.ReservationPreferenceImpl;
import com.github.robozonky.internal.remote.entities.ReservationPreferencesImpl;
import com.github.robozonky.internal.tenant.Tenant;

class ReservationsPreferencesTest extends AbstractZonkyLeveragingTest {

    private static final ReservationStrategy WRONG_STRATEGY = new ReservationStrategy() {
        @Override
        public ReservationMode getMode() {
            return ReservationMode.ACCEPT_MATCHING;
        }

        @Override
        public boolean recommend(ReservationDescriptor reservationDescriptor,
                Supplier<PortfolioOverview> portfolioOverviewSupplier, SessionInfo sessionInfo) {
            return false;
        }
    };
    private static final ReservationStrategy CORRECT_STRATEGY = new ReservationStrategy() {
        @Override
        public ReservationMode getMode() {
            return ReservationMode.FULL_OWNERSHIP;
        }

        @Override
        public boolean recommend(ReservationDescriptor reservationDescriptor,
                Supplier<PortfolioOverview> portfolioOverviewSupplier, SessionInfo sessionInfo) {
            return false;
        }
    };

    @Test
    void noStrategy() {
        final Zonky z = harmlessZonky();
        final Tenant t = mockTenant(z);
        final TenantPayload p = new ReservationsPreferences();
        p.accept(t);
        verify(z, never()).getPendingReservations();
    }

    @Test
    void disabled() {
        final Zonky z = harmlessZonky();
        final Tenant t = mockTenant(z);
        when(t.getReservationStrategy()).thenReturn(Optional.of(WRONG_STRATEGY));
        final TenantPayload p = new ReservationsPreferences();
        p.accept(t);
        verify(z, never()).getReservationPreferences();
    }

    @Test
    void enable() {
        final Zonky z = harmlessZonky();
        when(z.getReservationPreferences()).thenReturn(new ReservationPreferencesImpl()); // disabled
        final Tenant t = mockTenant(z);
        when(t.getReservationStrategy()).thenReturn(Optional.of(CORRECT_STRATEGY));
        final TenantPayload p = new ReservationsPreferences();
        p.accept(t);
        verify(z).getReservationPreferences();
        verify(z).setReservationPreferences(any());
    }

    @Test
    void enabledAndDoesNotNeedChanging() {
        final Zonky z = harmlessZonky();
        when(z.getReservationPreferences()).thenReturn(ReservationPreferencesImpl.TOTAL.get());
        final Tenant t = mockTenant(z);
        when(t.getReservationStrategy()).thenReturn(Optional.of(CORRECT_STRATEGY));
        final TenantPayload p = new ReservationsPreferences();
        p.accept(t);
        verify(z).getReservationPreferences();
        verify(z, never()).setReservationPreferences(any());
    }

    @Test
    void enabledAndNeedsChanging() {
        final ReservationPreferenceImpl rp = new ReservationPreferenceImpl(LoanTermInterval.FROM_0_TO_12, Rating.AAAAA,
                false);
        final Zonky z = harmlessZonky();
        when(z.getReservationPreferences()).thenReturn(new ReservationPreferencesImpl(rp));
        final Tenant t = mockTenant(z);
        when(t.getReservationStrategy()).thenReturn(Optional.of(CORRECT_STRATEGY));
        final TenantPayload p = new ReservationsPreferences();
        p.accept(t);
        verify(z).getReservationPreferences();
        verify(z).setReservationPreferences(any());
    }

}
