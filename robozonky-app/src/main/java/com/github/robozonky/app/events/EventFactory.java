/*
 * Copyright 2018 The RoboZonky Project
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

package com.github.robozonky.app.events;

import java.time.LocalDate;
import java.util.Collection;

import com.github.robozonky.api.notifications.ExecutionCompletedEvent;
import com.github.robozonky.api.notifications.ExecutionStartedEvent;
import com.github.robozonky.api.notifications.InvestmentDelegatedEvent;
import com.github.robozonky.api.notifications.InvestmentMadeEvent;
import com.github.robozonky.api.notifications.InvestmentPurchasedEvent;
import com.github.robozonky.api.notifications.InvestmentRejectedEvent;
import com.github.robozonky.api.notifications.InvestmentRequestedEvent;
import com.github.robozonky.api.notifications.InvestmentSkippedEvent;
import com.github.robozonky.api.notifications.InvestmentSoldEvent;
import com.github.robozonky.api.notifications.LoanDefaultedEvent;
import com.github.robozonky.api.notifications.LoanDelinquent10DaysOrMoreEvent;
import com.github.robozonky.api.notifications.LoanDelinquent30DaysOrMoreEvent;
import com.github.robozonky.api.notifications.LoanDelinquent60DaysOrMoreEvent;
import com.github.robozonky.api.notifications.LoanDelinquent90DaysOrMoreEvent;
import com.github.robozonky.api.notifications.LoanLostEvent;
import com.github.robozonky.api.notifications.LoanNoLongerDelinquentEvent;
import com.github.robozonky.api.notifications.LoanNowDelinquentEvent;
import com.github.robozonky.api.notifications.LoanRecommendedEvent;
import com.github.robozonky.api.notifications.LoanRepaidEvent;
import com.github.robozonky.api.notifications.PurchaseRecommendedEvent;
import com.github.robozonky.api.notifications.PurchaseRequestedEvent;
import com.github.robozonky.api.notifications.PurchasingCompletedEvent;
import com.github.robozonky.api.notifications.PurchasingStartedEvent;
import com.github.robozonky.api.notifications.RoboZonkyCrashedEvent;
import com.github.robozonky.api.notifications.RoboZonkyDaemonFailedEvent;
import com.github.robozonky.api.notifications.RoboZonkyEndingEvent;
import com.github.robozonky.api.notifications.RoboZonkyExperimentalUpdateDetectedEvent;
import com.github.robozonky.api.notifications.RoboZonkyInitializedEvent;
import com.github.robozonky.api.notifications.RoboZonkyStartingEvent;
import com.github.robozonky.api.notifications.RoboZonkyTestingEvent;
import com.github.robozonky.api.notifications.RoboZonkyUpdateDetectedEvent;
import com.github.robozonky.api.notifications.SaleOfferedEvent;
import com.github.robozonky.api.notifications.SaleRecommendedEvent;
import com.github.robozonky.api.notifications.SaleRequestedEvent;
import com.github.robozonky.api.notifications.SellingCompletedEvent;
import com.github.robozonky.api.notifications.SellingStartedEvent;
import com.github.robozonky.api.remote.entities.sanitized.Development;
import com.github.robozonky.api.remote.entities.sanitized.Investment;
import com.github.robozonky.api.remote.entities.sanitized.Loan;
import com.github.robozonky.api.strategies.InvestmentDescriptor;
import com.github.robozonky.api.strategies.LoanDescriptor;
import com.github.robozonky.api.strategies.ParticipationDescriptor;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.api.strategies.RecommendedInvestment;
import com.github.robozonky.api.strategies.RecommendedLoan;
import com.github.robozonky.api.strategies.RecommendedParticipation;

public final class EventFactory {

    private EventFactory() {
        // no instances
    }

    public static ExecutionCompletedEvent executionCompleted(final Collection<Investment> investments,
                                                             final PortfolioOverview portfolioOverview) {
        return new ExecutionCompletedEventImpl(investments, portfolioOverview);
    }

    public static ExecutionStartedEvent executionStarted(final Collection<LoanDescriptor> loans,
                                                         final PortfolioOverview portfolioOverview) {
        return new ExecutionStartedEventImpl(loans, portfolioOverview);
    }

    public static InvestmentDelegatedEvent investmentDelegated(final RecommendedLoan recommendation,
                                                               final String confirmationProviderId) {
        return new InvestmentDelegatedEventImpl(recommendation, confirmationProviderId);
    }

    public static InvestmentMadeEvent investmentMade(final Investment investment, final Loan loan,
                                                     final PortfolioOverview portfolioOverview) {
        return new InvestmentMadeEventImpl(investment, loan, portfolioOverview);
    }

    public static InvestmentPurchasedEvent investmentPurchased(final Investment investment, final Loan loan,
                                                               final PortfolioOverview portfolioOverview) {
        return new InvestmentPurchasedEventImpl(investment, loan, portfolioOverview);
    }

    public static InvestmentRejectedEvent investmentRejected(final RecommendedLoan recommendation,
                                                             final String confirmationProviderId) {
        return new InvestmentRejectedEventImpl(recommendation, confirmationProviderId);
    }

    public static InvestmentRequestedEvent investmentRequested(final RecommendedLoan recommendation) {
        return new InvestmentRequestedEventImpl(recommendation);
    }

    public static InvestmentSkippedEvent investmentSkipped(final RecommendedLoan recommendation) {
        return new InvestmentSkippedEventImpl(recommendation);
    }

    public static InvestmentSoldEvent investmentSold(final Investment investment, final Loan loan,
                                                     final PortfolioOverview portfolioOverview) {
        return new InvestmentSoldEventImpl(investment, loan, portfolioOverview);
    }

    public static LoanDefaultedEvent loanDefaulted(final Investment investment, final Loan loan, final LocalDate since,
                                                   final Collection<Development> collections) {
        return new LoanDefaultedEventImpl(investment, loan, since, collections);
    }

    public static LoanNowDelinquentEvent loanNowDelinquent(final Investment investment, final Loan loan,
                                                           final LocalDate since,
                                                           final Collection<Development> collections) {
        return new LoanNowDelinquentEventImpl(investment, loan, since, collections);
    }

    public static LoanDelinquent10DaysOrMoreEvent loanDelinquent10plus(final Investment investment, final Loan loan,
                                                                       final LocalDate since,
                                                                       final Collection<Development> collections) {
        return new LoanDelinquent10DaysOrMoreEventImpl(investment, loan, since, collections);
    }

    public static LoanDelinquent30DaysOrMoreEvent loanDelinquent30plus(final Investment investment, final Loan loan,
                                                                       final LocalDate since,
                                                                       final Collection<Development> collections) {
        return new LoanDelinquent30DaysOrMoreEventImpl(investment, loan, since, collections);
    }

    public static LoanDelinquent60DaysOrMoreEvent loanDelinquent60plus(final Investment investment, final Loan loan,
                                                                       final LocalDate since,
                                                                       final Collection<Development> collections) {
        return new LoanDelinquent60DaysOrMoreEventImpl(investment, loan, since, collections);
    }

    public static LoanDelinquent90DaysOrMoreEvent loanDelinquent90plus(final Investment investment, final Loan loan,
                                                                       final LocalDate since,
                                                                       final Collection<Development> collections) {
        return new LoanDelinquent90DaysOrMoreEventImpl(investment, loan, since, collections);
    }

    public static LoanLostEvent loanLost(final Investment investment, final Loan loan) {
        return new LoanLostEventImpl(investment, loan);
    }

    public static LoanNoLongerDelinquentEvent loanNoLongerDelinquent(final Investment investment, final Loan loan) {
        return new LoanNoLongerDelinquentEventImpl(investment, loan);
    }

    public static LoanRecommendedEvent loanRecommended(final RecommendedLoan recommendation) {
        return new LoanRecommendedEventImpl(recommendation);
    }

    public static LoanRepaidEvent loanRepaid(final Investment investment, final Loan loan,
                                             final PortfolioOverview portfolioOverview) {
        return new LoanRepaidEventImpl(investment, loan, portfolioOverview);
    }

    public static PurchaseRecommendedEvent purchaseRecommended(final RecommendedParticipation recommendation) {
        return new PurchaseRecommendedEventImpl(recommendation);
    }

    public static PurchaseRequestedEvent purchaseRequested(final RecommendedParticipation recommendation) {
        return new PurchaseRequestedEventImpl(recommendation);
    }

    public static PurchasingCompletedEvent purchasingCompleted(final Collection<Investment> investment,
                                                               final PortfolioOverview portfolio) {
        return new PurchasingCompletedEventImpl(investment, portfolio);
    }

    public static PurchasingStartedEvent purchasingStarted(final Collection<ParticipationDescriptor> descriptors,
                                                           final PortfolioOverview portfolio) {
        return new PurchasingStartedEventImpl(descriptors, portfolio);
    }

    public static RoboZonkyCrashedEvent roboZonkyCrashed(final Throwable cause) {
        return new RoboZonkyCrashedEventImpl(cause);
    }

    public static RoboZonkyDaemonFailedEvent roboZonkyDaemonFailed(final Throwable cause) {
        return new RoboZonkyDaemonFailedEventImpl(cause);
    }

    public static RoboZonkyEndingEvent roboZonkyEnding() {
        return new RoboZonkyEndingEventImpl();
    }

    public static RoboZonkyExperimentalUpdateDetectedEvent roboZonkyExperimentalUpdateDetected(final String version) {
        return new RoboZonkyExperimentalUpdateDetectedEventImpl(version);
    }

    public static RoboZonkyInitializedEvent roboZonkyInitialized() {
        return new RoboZonkyInitializedEventImpl();
    }

    public static RoboZonkyStartingEvent roboZonkyStarting() {
        return new RoboZonkyStartingEventImpl();
    }

    public static RoboZonkyTestingEvent roboZonkyTesting() {
        return new RoboZonkyTestingEventImpl();
    }

    public static RoboZonkyUpdateDetectedEvent roboZonkyUpdateDetected(final String version) {
        return new RoboZonkyUpdateDetectedEventImpl(version);
    }

    public static SaleOfferedEvent saleOffered(final Investment investment, final Loan loan) {
        return new SaleOfferedEventImpl(investment, loan);
    }

    public static SaleRecommendedEvent saleRecommended(final RecommendedInvestment recommendation) {
        return new SaleRecommendedEventImpl(recommendation);
    }

    public static SaleRequestedEvent saleRequested(final RecommendedInvestment recommendation) {
        return new SaleRequestedEventImpl(recommendation);
    }

    public static SellingCompletedEvent sellingCompleted(final Collection<Investment> investments,
                                                         final PortfolioOverview portfolio) {
        return new SellingCompletedEventImpl(investments, portfolio);
    }

    public static SellingStartedEvent sellingStarted(final Collection<InvestmentDescriptor> investments,
                                                     final PortfolioOverview portfolio) {
        return new SellingStartedEventImpl(investments, portfolio);
    }
}
