package com.thefineprint.core.api.spi;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Due-time index for <b>confirmed</b> commitment reminders (SYSTEM_DESIGN section 9; CIA matrix A3).
 *
 * <p><b>Contract</b>
 * <ul>
 *   <li>Each commitment has at most one entry. {@link #schedule} inserts it, or replaces its fire time if it
 *       already exists.</li>
 *   <li>{@link #pollDue} removes and returns up to {@code limit} entries with {@code fireAt <= now}, ordered by
 *       {@code fireAt} ascending, then by {@code commitmentId} ascending.</li>
 *   <li>Times are UTC instants. Calendar and time-zone rules are computed elsewhere.</li>
 *   <li>Only user-confirmed commitments may be scheduled (D-18). Tenant scoping and dispatch are outside this
 *       interface (SECURITY_AND_PRIVACY 6.3).</li>
 * </ul>
 *
 * <p><b>Implementations:</b> none in P0. The production default is DB-backed (P4); assessed implementations
 * are Track C only.
 */
public interface ReminderIndex {

    /** Inserts or reschedules the reminder for {@code commitmentId}. */
    void schedule(UUID commitmentId, Instant fireAt);

    /** Removes the reminder for {@code commitmentId}; returns {@code true} if one existed. */
    boolean cancel(UUID commitmentId);

    /**
     * Removes and returns due entries.
     *
     * @param now entries with {@code fireAt <= now} are due
     * @param limit maximum number of entries to return; must be positive
     * @return due entries in contract order; never {@code null}
     */
    List<ScheduledReminder> pollDue(Instant now, int limit);
}
