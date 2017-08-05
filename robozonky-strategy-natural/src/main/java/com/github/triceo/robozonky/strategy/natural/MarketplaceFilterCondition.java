/*
 * Copyright 2017 Lukáš Petrovický
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

package com.github.triceo.robozonky.strategy.natural;

import java.util.Optional;
import java.util.function.Predicate;

public interface MarketplaceFilterCondition<T> extends Predicate<T> {

    /**
     * Describe the condition using eg. range boundaries.
     * @return If present, is a whole sentence. (Starting with capital letter, ending with a full stop.)
     */
    default Optional<String> getDescription() {
        return Optional.empty();
    }

    /**
     * Determine whether or not the item in question matches the condition represented by this class.
     * @param item Item in question.
     * @return True if item matches the condition.
     */
    default boolean test(final T item) {
        return false;
    }
}
