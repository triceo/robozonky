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

package com.github.robozonky.notifications.email;

import java.util.Map;

import com.github.robozonky.api.notifications.DelinquencyBased;
import com.github.robozonky.api.notifications.Event;

abstract class AbstractLoanTerminatedEmailingListener<T extends Event & DelinquencyBased> extends
                                                                                          AbstractEmailingListener<T> {

    protected AbstractLoanTerminatedEmailingListener(final ListenerSpecificNotificationProperties properties) {
        super(properties);
        registerFinisher(event -> DelinquencyTracker.INSTANCE.unsetDelinquent(event.getInvestment()));
    }

    @Override
    boolean shouldSendEmail(final T event) {
        return super.shouldSendEmail(event) &&
                DelinquencyTracker.INSTANCE.isDelinquent(event.getInvestment());
    }

    @Override
    protected Map<String, Object> getData(final T event) {
        return Util.getDelinquentData(event.getInvestment(), event.getLoan(), event.getCollectionActions(),
                                      event.getDelinquentSince());
    }
}
