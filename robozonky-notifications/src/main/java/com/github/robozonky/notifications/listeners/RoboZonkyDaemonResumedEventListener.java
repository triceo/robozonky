/*
 * Copyright 2019 The RoboZonky Project
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

package com.github.robozonky.notifications.listeners;

import com.github.robozonky.api.notifications.RoboZonkyDaemonResumedEvent;
import com.github.robozonky.notifications.AbstractTargetHandler;
import com.github.robozonky.notifications.SupportedListener;

public class RoboZonkyDaemonResumedEventListener extends AbstractListener<RoboZonkyDaemonResumedEvent> {

    public RoboZonkyDaemonResumedEventListener(final SupportedListener listener, final AbstractTargetHandler handler) {
        super(listener, handler);
    }

    @Override
    String getSubject(final RoboZonkyDaemonResumedEvent event) {
        return "RoboZonky se zotavil.";
    }

    @Override
    String getTemplateFileName() {
        return "daemon-resumed.ftl";
    }

}
