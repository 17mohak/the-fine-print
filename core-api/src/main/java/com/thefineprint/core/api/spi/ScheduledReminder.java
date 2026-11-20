package com.thefineprint.core.api.spi;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One entry of a {@link ReminderIndex}.
 *
 * @param commitmentId opaque id of a confirmed commitment
 * @param fireAt UTC instant at which the reminder becomes due
 */
public record ScheduledReminder(UUID commitmentId, Instant fireAt) {

    public ScheduledReminder {
        Objects.requireNonNull(commitmentId, "commitmentId");
        Objects.requireNonNull(fireAt, "fireAt");
    }
}
