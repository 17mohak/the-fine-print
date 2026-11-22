/**
 * THE FINE PRINT shared API: service-provider interfaces (SPIs) and data-transfer types used by both the
 * platform and the isolated academic track.
 *
 * <p><b>Rules (ARCHITECTURE_OVERVIEW section 2, section 5; D-21):</b>
 * <ul>
 *   <li>This module contains interfaces, records and enums only. It contains no business logic.</li>
 *   <li>It depends only on {@code java.*} (enforced by ArchUnit in {@code :architecture-tests}).</li>
 *   <li>No implementation of any SPI exists in P0. Production adapters are P1+ work; assessed implementations
 *       are Track C work. Neither is authorised under D-21.</li>
 * </ul>
 */
package com.thefineprint.core.api;
