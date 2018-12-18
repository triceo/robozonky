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

package com.github.robozonky.app.authentication;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.github.robozonky.api.SessionInfo;
import com.github.robozonky.api.notifications.SessionEvent;
import com.github.robozonky.api.remote.entities.Restrictions;
import com.github.robozonky.api.strategies.InvestmentStrategy;
import com.github.robozonky.api.strategies.PurchaseStrategy;
import com.github.robozonky.api.strategies.SellStrategy;
import com.github.robozonky.common.remote.Zonky;
import com.github.robozonky.common.state.InstanceState;
import com.github.robozonky.common.tenant.LazyEvent;
import com.github.robozonky.common.tenant.RemotePortfolio;
import com.github.robozonky.common.tenant.TransactionalTenant;
import com.github.robozonky.common.tenant.ZonkyScope;
import com.github.robozonky.util.Reloadable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TransactionalTokenBasedTenant implements TransactionalEventTenant {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalTokenBasedTenant.class);

    private final EventTenant parent;
    private final TransactionalTenant transactional;
    private final Reloadable<DelayedFiring> delayedFiring = Reloadable.of(DelayedFiring::new);

    public TransactionalTokenBasedTenant(final EventTenant parent, final TransactionalTenant transactional) {
        this.parent = parent;
        this.transactional = transactional;
    }

    private DelayedFiring getDelayedFiring() {
        return delayedFiring.get().getOrElseThrow(() -> new IllegalStateException("Can not happen."));
    }

    @Override
    public CompletableFuture<Void> fire(final SessionEvent event) {
        LOGGER.trace("Event stored within transaction: {}.", event);
        return getDelayedFiring().delay(() -> parent.fire(event));
    }

    @Override
    public CompletableFuture<Void> fire(final LazyEvent<? extends SessionEvent> event) {
        LOGGER.trace("Lazy event stored within transaction: {}.", event);
        return getDelayedFiring().delay(() -> parent.fire(event));
    }

    @Override
    public void commit() {
        transactional.commit();
        LOGGER.debug("Replaying transaction.");
        getDelayedFiring().run();
    }

    @Override
    public void abort() {
        transactional.abort();
        getDelayedFiring().cancel();
        delayedFiring.clear();
    }

    @Override
    public <T> T call(final Function<Zonky, T> operation, final ZonkyScope scope) {
        return parent.call(operation, scope);
    }

    @Override
    public boolean isAvailable(final ZonkyScope scope) {
        return parent.isAvailable(scope);
    }

    @Override
    public RemotePortfolio getPortfolio() {
        return parent.getPortfolio();
    }

    @Override
    public Restrictions getRestrictions() {
        return parent.getRestrictions();
    }

    @Override
    public SessionInfo getSessionInfo() {
        return parent.getSessionInfo();
    }

    @Override
    public Optional<InvestmentStrategy> getInvestmentStrategy() {
        return parent.getInvestmentStrategy();
    }

    @Override
    public Optional<SellStrategy> getSellStrategy() {
        return parent.getSellStrategy();
    }

    @Override
    public Optional<PurchaseStrategy> getPurchaseStrategy() {
        return parent.getPurchaseStrategy();
    }

    @Override
    public <T> InstanceState<T> getState(final Class<T> clz) {
        return transactional.getState(clz);
    }

    @Override
    public void close() throws Exception {
        transactional.close();
        if (getDelayedFiring().isPending()) {
            throw new IllegalStateException("There are uncommitted events.");
        }
    }
}
