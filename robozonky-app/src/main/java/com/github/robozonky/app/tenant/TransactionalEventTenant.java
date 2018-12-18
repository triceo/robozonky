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

package com.github.robozonky.app.tenant;

import java.util.concurrent.CompletableFuture;

import com.github.robozonky.api.notifications.SessionEvent;
import com.github.robozonky.common.tenant.LazyEvent;
import com.github.robozonky.common.tenant.TransactionalTenant;

/**
 * Instances of this interface should never get to users outside of the application, otherwise they would be able to
 * fire events.
 */
public interface TransactionalEventTenant extends TransactionalTenant, EventTenant {

    /**
     * Do not block on the return value of this method, unless some other thread is still able to call
     * {@link #commit()}. Otherwise this is a self-inflicted. deadlock.
     *
     * @param event
     * @return
     */
    @Override
    CompletableFuture<Void> fire(SessionEvent event);

    /**
     * Do not block on the return value of this method, unless some other thread is still able to call
     * {@link #commit()}. Otherwise this is a self-inflicted. deadlock.
     *
     * @param event
     * @return
     */
    @Override
    CompletableFuture<Void> fire(LazyEvent<? extends SessionEvent> event);
}
