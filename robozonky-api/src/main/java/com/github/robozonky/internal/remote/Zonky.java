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

package com.github.robozonky.internal.remote;

import static java.util.Collections.singleton;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.robozonky.api.remote.entities.Consents;
import com.github.robozonky.api.remote.entities.Investment;
import com.github.robozonky.api.remote.entities.LastPublishedItem;
import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.remote.entities.Participation;
import com.github.robozonky.api.remote.entities.ParticipationDetail;
import com.github.robozonky.api.remote.entities.Reservation;
import com.github.robozonky.api.remote.entities.ReservationPreferences;
import com.github.robozonky.api.remote.entities.Restrictions;
import com.github.robozonky.api.remote.entities.Statistics;
import com.github.robozonky.api.remote.entities.ZonkyApiToken;
import com.github.robozonky.api.remote.enums.Resolution;
import com.github.robozonky.internal.Settings;
import com.github.robozonky.internal.remote.endpoints.ControlApi;
import com.github.robozonky.internal.remote.endpoints.EntityCollectionApi;
import com.github.robozonky.internal.remote.endpoints.LoanApi;
import com.github.robozonky.internal.remote.endpoints.ParticipationApi;
import com.github.robozonky.internal.remote.endpoints.PortfolioApi;
import com.github.robozonky.internal.remote.endpoints.ReservationApi;
import com.github.robozonky.internal.remote.entities.InvestmentImpl;
import com.github.robozonky.internal.remote.entities.InvestmentRequest;
import com.github.robozonky.internal.remote.entities.LoanImpl;
import com.github.robozonky.internal.remote.entities.ParticipationImpl;
import com.github.robozonky.internal.remote.entities.PurchaseRequest;
import com.github.robozonky.internal.remote.entities.ReservationPreferencesImpl;
import com.github.robozonky.internal.remote.entities.ResolutionRequest;
import com.github.robozonky.internal.remote.entities.Resolutions;
import com.github.robozonky.internal.remote.entities.SellRequest;
import com.github.robozonky.internal.util.stream.PagingStreams;

/**
 * Represents an instance of Zonky API that is fully authenticated and ready to perform operations on behalf of the
 * user.
 * <p>
 * Zonky implements API quotas on their endpoints. Reaching the quota will result in receiving "HTTP 429 Too Many
 * Requests". The following operations have their own quotas:
 *
 * <ul>
 * <li>{@link #getLastPublishedLoanInfo()} allows for 3000 requests, with one request cleared every second. Therefore
 * we do not request-count this API, as we will only ever request this value once every second.</li>
 * <li>{@link #getAvailableParticipations(Select)} ()} is in the same situation.</li>
 * <li>Everything else is on the same quote and therefore is request-counted.</li>
 * </ul>
 * <p>
 * Request counting does not actually limit anything. It just gives us an idea through logs how close or how far we are
 * from reaching the quota.
 */
public class Zonky {

    private static final Logger LOGGER = LogManager.getLogger(Zonky.class);

    private final Api<ControlApi> controlApi;
    private final Api<ReservationApi> reservationApi;
    private final PaginatedApi<LoanImpl, LoanApi> loanApi;
    private final PaginatedApi<ParticipationImpl, ParticipationApi> participationApi;
    private final PaginatedApi<InvestmentImpl, PortfolioApi> portfolioApi;

    Zonky(final ApiProvider api, final Supplier<ZonkyApiToken> tokenSupplier) {
        this.controlApi = api.control(tokenSupplier);
        this.loanApi = api.marketplace(tokenSupplier);
        this.reservationApi = api.reservations(tokenSupplier);
        this.participationApi = api.secondaryMarketplace(tokenSupplier);
        participationApi.setSortString("-deadline"); // Order participations from the newest one.
        this.portfolioApi = api.portfolio(tokenSupplier);
    }

    private static <X, T extends X, S extends EntityCollectionApi<T>> Stream<X> getStream(final PaginatedApi<T, S> api,
            final Function<S, List<T>> function, final Select select) {
        var pageSize = Settings.INSTANCE.getDefaultApiPageSize();
        return PagingStreams.build(new EntityCollectionPageSource<>(api, function, select, pageSize))
            .map(x -> x);
    }

    public void invest(final Loan loan, final int amount) {
        LOGGER.debug("Investing into loan #{}.", loan.getId());
        var request = new InvestmentRequest(loan.getId(), amount);
        controlApi.run(api -> api.invest(request));
    }

    public void cancel(final Investment investment) {
        LOGGER.debug("Cancelling offer to sell investment in loan #{}.", investment.getLoan()
            .getId());
        controlApi.run(api -> api.cancel(investment.getId()));
    }

    public void purchase(final Participation participation) {
        var request = new PurchaseRequest(participation.getPrice(), participation.getDiscount(),
                participation.getRemainingPrincipal());
        LOGGER.debug("Purchasing participation #{} in loan #{} ({}).", participation.getId(), participation.getLoanId(),
                request);
        controlApi.run(api -> api.purchase(participation.getId(), request));
    }

    public void sell(final Investment investment) {
        var request = new SellRequest(investment);
        LOGGER.debug("Offering to sell investment in loan #{} ({}).", investment.getLoan()
            .getId(), request);
        controlApi.run(api -> api.offer(request));
    }

    public void accept(final Reservation reservation) {
        final ResolutionRequest r = new ResolutionRequest(reservation.getMyReservation()
            .getId(), Resolution.ACCEPTED);
        final Resolutions rs = new Resolutions(singleton(r));
        controlApi.run(c -> c.accept(rs));
    }

    /**
     * Retrieve reservations that the user has to either accept or reject.
     * 
     * @return All items from the remote API, lazy-loaded.
     */
    public Stream<Reservation> getPendingReservations() {
        return reservationApi.call(ReservationApi::items)
            .getReservations()
            .stream();
    }

    /**
     * Retrieve investments from user's portfolio via {@link PortfolioApi}, in a given order.
     * 
     * @param select Rules to filter the selection by.
     * @return All items from the remote API, lazy-loaded.
     */
    public Stream<Investment> getInvestments(final Select select) {
        return getStream(portfolioApi, PortfolioApi::items, select)
            .map(i -> new AutoExtendingInvestmentImpl(i, this));
    }

    public Stream<Investment> getPendingInvestments() {
        final Select s = new Select()
            .equals("investmentStatus", "AWAITING_INVESTMENT");
        return getInvestments(s);
    }

    public Stream<Investment> getSoldInvestments() {
        final Select s = Select.unrestricted()
            .equals("investmentStatus", "ACTIVE")
            .in("sellStatus", "SOLD");
        return getInvestments(s);
    }

    public Stream<Investment> getSellableInvestments() {
        var select = Select.unrestricted()
            .equals("investmentStatus", "ACTIVE")
            .in("sellStatus", "SELLABLE_WITH_FEE", "SELLABLE_WITHOUT_FEE");
        // And now actually filter out the non-sellable stuff.
        // It makes no sense, but we have already seen Zonky return these here.
        return getInvestments(select)
            .filter(i -> i.getSellStatus()
                .isSellable());
    }

    public Stream<Investment> getDelinquentInvestments() {
        final Select s = Select.unrestricted()
            .equals("investmentStatus", "ACTIVE")
            .in("loanHealth", "ONE_TO_FIFTEEN_DPD", "SIXTEEN_TO_THIRTY_DPD", "THIRTY_ONE_TO_SIXTY_DPD",
                    "SIXTY_ONE_TO_NINETY_DPD", "MORE_THAN_NINETY_DPD", "PAID_OFF");
        return getInvestments(s);
    }

    public Loan getLoan(final int id) {
        return loanApi.execute(api -> api.item(id));
    }

    public Investment getInvestment(final long id) {
        return portfolioApi.execute(api -> api.getInvestment(id));
    }

    public LastPublishedItem getLastPublishedLoanInfo() {
        return loanApi.execute(LoanApi::lastPublished, false);
    }

    public LastPublishedItem getLastPublishedParticipationInfo() {
        return participationApi.execute(ParticipationApi::lastPublished, false);
    }

    public ParticipationDetail getParticipationDetail(final int loanId) {
        return participationApi.execute(pa -> pa.getDetail(loanId));
    }

    /**
     * Retrieve loans from marketplace via {@link LoanApi}.
     * 
     * @param select Rules to filter the selection by.
     * @return All items from the remote API, lazy-loaded.
     */
    public Stream<Loan> getAvailableLoans(final Select select) {
        return getStream(loanApi, LoanApi::items, select);
    }

    /**
     * Retrieve participations from secondary marketplace via {@link ParticipationApi}.
     * 
     * @param select Rules to filter the selection by.
     * @return All items from the remote API, lazy-loaded.
     */
    public Stream<Participation> getAvailableParticipations(final Select select) {
        return getStream(participationApi, ParticipationApi::items, select);
    }

    public ReservationPreferences getReservationPreferences() {
        return reservationApi.call(ReservationApi::preferences);
    }

    public void setReservationPreferences(final ReservationPreferencesImpl preferences) {
        controlApi.run(c -> c.setReservationPreferences(preferences));
    }

    public Restrictions getRestrictions() {
        return controlApi.call(ControlApi::restrictions);
    }

    public Consents getConsents() {
        return controlApi.call(ControlApi::consents);
    }

    public Statistics getStatistics() {
        return portfolioApi.execute(PortfolioApi::getStatistics);
    }

}
