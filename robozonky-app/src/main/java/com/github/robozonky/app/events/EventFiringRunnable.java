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

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Payload for the {@link EventFiringQueue}'s queue-polling thread.
 */
final class EventFiringRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventFiringRunnable.class);

    private final BlockingQueue<Runnable> queue;

    public EventFiringRunnable(final BlockingQueue<Runnable> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        boolean repeat = true;
        do {
            try {
                queue.take().run();
            } catch (final InterruptedException ex) {
                LOGGER.debug("Interrupted while waiting for an event to fire.", ex);
                repeat = false;
            }
        } while (repeat);
    }
}
