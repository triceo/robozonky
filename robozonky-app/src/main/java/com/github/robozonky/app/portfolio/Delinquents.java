/*
 * Copyright 2017 The RoboZonky Project
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

package com.github.robozonky.app.portfolio;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.robozonky.api.notifications.LoanDefaultedEvent;
import com.github.robozonky.api.notifications.LoanNoLongerDelinquentEvent;
import com.github.robozonky.api.notifications.LoanRepaidEvent;
import com.github.robozonky.api.remote.entities.RawInvestment;
import com.github.robozonky.api.remote.entities.sanitized.Development;
import com.github.robozonky.api.remote.entities.sanitized.Investment;
import com.github.robozonky.api.remote.entities.sanitized.Loan;
import com.github.robozonky.api.remote.enums.PaymentStatus;
import com.github.robozonky.api.remote.enums.PaymentStatuses;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.app.Events;
import com.github.robozonky.app.authentication.Authenticated;
import com.github.robozonky.app.util.LoanCache;
import com.github.robozonky.internal.api.Defaults;
import com.github.robozonky.internal.api.State;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Historical record of which {@link RawInvestment}s have been delinquent and when. This class updates shared state
 * (implemented via {@link State}) which can then be retrieved through static methods, such as {@link #getDelinquents()}
 * and {@link #getLastUpdateTimestamp()}.
 */
public class Delinquents {

    private static final Logger LOGGER = LoggerFactory.getLogger(Delinquents.class);
    private static final String LAST_UPDATE_PROPERTY_NAME = "lastUpdate";
    private static final String TIME_SEPARATOR = ":::";
    private static final Pattern TIME_SPLITTER = Pattern.compile("\\Q" + TIME_SEPARATOR + "\\E");

    private Delinquents() {
        // no need for an instance
    }

    private static String toString(final Delinquency d) {
        return d.getFixedOn()
                .map(fixedOn -> d.getPaymentMissedDate() + TIME_SEPARATOR + fixedOn)
                .orElse(d.getPaymentMissedDate().toString());
    }

    private static Stream<String> toString(final Delinquent d) {
        return d.getDelinquencies().map(Delinquents::toString);
    }

    private static void add(final Delinquent d, final String delinquency) {
        final String[] parts = TIME_SPLITTER.split(delinquency);
        if (parts.length == 1) {
            d.addDelinquency(LocalDate.parse(parts[0]));
        } else if (parts.length == 2) {
            d.addDelinquency(LocalDate.parse(parts[0]), LocalDate.parse(parts[1]));
        } else {
            throw new IllegalStateException("Unexpected number of dates: " + parts.length);
        }
    }

    private static Delinquent add(final int loanId, final List<String> delinquencies) {
        final Delinquent d = new Delinquent(loanId);
        delinquencies.forEach(delinquency -> add(d, delinquency));
        return d;
    }

    private static boolean related(final Delinquent d, final Investment i) {
        return d.getLoanId() == i.getLoanId();
    }

    private static State.ClassSpecificState getState() {
        return State.forClass(Delinquents.class);
    }

    /**
     * Retrieve the time when the internal state of this class was modified.
     * @return {@link Instant#EPOCH} when no internal state.
     */
    public static OffsetDateTime getLastUpdateTimestamp() {
        return getState().getValue(LAST_UPDATE_PROPERTY_NAME)
                .map(OffsetDateTime::parse)
                .orElse(OffsetDateTime.ofInstant(Instant.EPOCH, Defaults.ZONE_ID));
    }

    /**
     * @return Active loans that are now, or at some point have been, tracked as delinquent.
     */
    public static Stream<Delinquent> getDelinquents() {
        final State.ClassSpecificState state = getState();
        return state.getKeys().stream()
                .filter(StringUtils::isNumeric) // skip any non-loan metadata
                .map(key -> {
                    final int loanId = Integer.parseInt(key);
                    final List<String> rawDelinquencies =
                            state.getValues(key).orElseThrow(() -> new IllegalStateException("Impossible."));
                    return add(loanId, rawDelinquencies);
                });
    }

    private static Collection<Delinquency> persistAndReturnActiveDelinquents(final Stream<Delinquent> delinquents) {
        LOGGER.trace("Starting delinquency state update.");
        final State.Batch stateUpdate = getState().newBatch(true)
                .set(LAST_UPDATE_PROPERTY_NAME, OffsetDateTime.now().toString());
        final Collection<Delinquency> allPresent = delinquents
                .peek(d -> stateUpdate.set(String.valueOf(d.getLoanId()), toString(d)))
                .flatMap(d -> d.getActiveDelinquency().map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toSet());
        stateUpdate.call(); // persist
        LOGGER.trace("Delinquency state update finished.");
        return allPresent;
    }

    private static Collection<Delinquent> getKnownDelinquents(final Collection<Investment> presentlyDelinquent,
                                                              final Collection<Investment> noLongerActive,
                                                              final Function<Integer, Investment> investmentSupplier,
                                                              final Function<Investment, Loan> loanSupplier,
                                                              final Function<Loan, Collection<Development>>
                                                                      collectionsSupplier,
                                                              final PortfolioOverview portfolioOverview) {
        final LocalDate now = LocalDate.now();
        // find out loans that are no longer delinquent, either through payment or through default
        return getDelinquents().parallel()
                .filter(Delinquent::hasActiveDelinquency) // last known state was delinquent
                .filter(d -> presentlyDelinquent.stream().noneMatch(i -> related(d, i))) // no longer is delinquent
                .flatMap(d -> d.getActiveDelinquency().map(Stream::of).orElse(Stream.empty()))
                .peek(d -> d.setFixedOn(now.minusDays(1))) // end the delinquency
                .map(Delinquency::getParent)
                .peek(d -> {  // notify
                    final int loanId = d.getLoanId();
                    final LocalDate since = d.getLatestDelinquency().get().getPaymentMissedDate();
                    final Investment inv = investmentSupplier.apply(loanId);
                    final Loan l = loanSupplier.apply(inv);
                    final Collection<Development> collections = collectionsSupplier.apply(l);
                    if (noLongerActive.stream().anyMatch(i -> related(d, i))) {
                        final PaymentStatus s = inv.getPaymentStatus()
                                .orElseThrow(() -> new IllegalStateException("Invalid investment " + inv));
                        switch (s) {
                            case PAID_OFF:
                                Events.fire(new LoanDefaultedEvent(inv, l, since, collections));
                                break;
                            case PAID:
                                Events.fire(new LoanRepaidEvent(inv, l, portfolioOverview));
                                break;
                            default:
                                LOGGER.warn("Unsupported payment status '{}' for loan #{}.", s, l.getId());
                        }
                    } else {
                        Events.fire(new LoanNoLongerDelinquentEvent(inv, l, since, collections));
                    }
                }).collect(Collectors.toList());
    }

    private static Delinquent createDelinquent(final Investment i) {
        return i.getNextPaymentDate()
                .map(date -> new Delinquent(i.getLoanId(), date.toLocalDate()))
                .orElseThrow(() -> new IllegalStateException("Invalid investment " + i));
    }

    static void update(final Collection<Investment> presentlyDelinquent,
                       final Collection<Investment> noLongerActive,
                       final Function<Integer, Investment> investmentSupplier,
                       final Function<Investment, Loan> loanSupplier,
                       final Function<Loan, Collection<Development>> collectionsSupplier,
                       final PortfolioOverview portfolioOverview) {
        LOGGER.debug("Updating delinquent loans.");
        final Collection<Delinquent> knownDelinquents = getKnownDelinquents(presentlyDelinquent, noLongerActive,
                                                                            investmentSupplier, loanSupplier,
                                                                            collectionsSupplier, portfolioOverview);
        // assemble delinquencies past and present
        final Stream<Delinquent> delinquentInThePast = knownDelinquents.stream()
                .filter(d -> noLongerActive.stream().noneMatch(i -> related(d, i)));
        final Stream<Delinquent> nowDelinquent = presentlyDelinquent.stream()
                .map(i -> knownDelinquents.stream()
                        .filter(d -> related(d, i))
                        .findAny()
                        .orElse(createDelinquent(i)));
        final Stream<Delinquent> all = Stream.concat(delinquentInThePast, nowDelinquent).distinct();
        final Collection<Delinquency> result = persistAndReturnActiveDelinquents(all);
        // notify of new delinquencies over all known thresholds
        Stream.of(DelinquencyCategory.values())
                .forEach(c -> c.update(result, investmentSupplier, loanSupplier, collectionsSupplier));
        LOGGER.trace("Done.");
    }

    private static Collection<Investment> getWithPaymentStatus(final Portfolio portfolio,
                                                               final PaymentStatuses target) {
        return portfolio.getActiveWithPaymentStatus(target).collect(Collectors.toList());
    }

    /**
     * Updates delinquency information based on the information about loans that are either currently delinquent or no
     * longer active. Will fire events on new delinquencies and/or on loans no longer delinquent.
     * @param auth The API that will be used to retrieve the loan instances.
     * @param portfolio Holds information about investments.
     */
    public static void update(final Authenticated auth, final Portfolio portfolio) {
        update(getWithPaymentStatus(portfolio, PaymentStatus.getDelinquent()),
               getWithPaymentStatus(portfolio, PaymentStatus.getDone()),
               portfolio::lookupOrFail,
               id -> auth.call(z -> LoanCache.INSTANCE.getLoan(id, z)),
               l -> auth.call(z -> z.getDevelopments(l).collect(Collectors.toList())),
               portfolio.calculateOverview());
    }
}
