// The Fine Print — Evidence Integrity Lab client.
// This script only collects input, calls the Java backend (/algo/*) and renders what the server returns.
// It never runs the assessed algorithms. All DOM content is built with textContent: no innerHTML with user input.
"use strict";

const EXAMPLE_TEXT =
  "SCHEDULE OF BENEFITS (synthetic example)\n" +
  "4.2 Notice of claim. The Policyholder shall give written notice of any claim within 30 days of discharge.\n" +
  "4.3 Renewal. The Insurer may decline renewal only on grounds of fraud or non-disclosure. " +
  "If the Policyholder shall give written notice of cancellation, any unearned premium is refunded.\n" +
  "4.4 Waiting period. Pre-existing conditions are covered after 36 months of continuous coverage.";
const EXAMPLE_QUOTE = "The Policyholder shall give written notice";

// Short synthetic clause wordings; one is deliberately repeated to show attempts versus distinct members.
const EXAMPLE_FINGERPRINTS = [
  "A grace period of 30 days is allowed.", "A grace period of 15 days is allowed.",
  "Notice of claim within 30 days of discharge.", "Notice of claim within 30 days of discharge.",
  "Free look period of 15 days.", "Cooling-off period of 3 days.",
  "Deposit refunded within 30 days of vacating.", "Renews unless cancelled 7 days before renewal.",
  "Warranty of 12 months from purchase.", "Prepayment charge of 2 per cent.",
];
const EXAMPLE_PROBES = [
  "A grace period of 15 days is allowed.", "Notice of claim within 30 days of discharge.",
  "Warranty of 12 months from purchase.", "A grace period of 17 days is allowed.",
  "Notice of claim within 45 days of discharge.", "Free look period of 30 days.",
  "Cooling-off period of 5 days.", "Deposit refunded within 60 days of vacating.",
  "Renews unless cancelled 3 days before renewal.", "Warranty of 24 months from purchase.",
  "Prepayment charge of 3 per cent.", "Late payment charge of 2 per cent.",
];

// ---------- Evidence check examples (synthetic) ----------

const POLICY_EN = [
  "SCHEDULE OF BENEFITS (synthetic example)",
  "4.1 Grace period. A grace period of 17 days is allowed for payment of the renewal premium.",
  "4.2 Notice of claim. The Policyholder shall give written notice of any claim within 30 days of discharge.",
  "4.3 Renewal. The Insurer may decline renewal only on grounds of fraud or non-disclosure.",
  "4.4 Waiting period. Pre-existing conditions are covered after 36 months of continuous coverage.",
  "4.5 Cashless facility. Cashless treatment is available only at network hospitals listed in the Annexure.",
].join("\n");
// Hindi text is written with escapes so the file does not depend on how it is decoded.
const POLICY_HI = [
  "नियम और शर्तें (synthetic example)",
  "5.1 बीमित व्यक्ति को दावे"
    + " की सूचना 30 दिनों के भीतर"
    + " देनी होगी।",
  "5.2 पॉलिसी का नवीनीकरण केवल"
    + " धोखाधड़ी के आधार पर"
    + " अस्वीकार किया जा सकता"
    + " है।",
].join("\n");

const EVIDENCE_EXAMPLES = {
  // Unique occurrence; clause 4.2's wording is identical to a reference entry.
  verified: [POLICY_EN, "The Policyholder shall give written notice of any claim within 30 days"],
  // Clause 4.1 says 17 days; the library holds 15 and 21. The Bloom filter says "possibly"; exact comparison says no.
  variant: [POLICY_EN, "A grace period of 17 days is allowed"],
  // "renewal" occurs in clauses 4.1 and 4.3: present, but which occurrence is meant cannot be established.
  twice: [POLICY_EN, "renewal"],
  // "दावे की सूचना 30 दिनों के भीतर" in clause 5.1, whose wording is in the Hindi part of the library.
  hindi: [POLICY_HI, "दावे की सूचना 30 दिनों"
    + " के भीतर"],
  // "दाव" without its vowel sign: the code points occur inside "दावे", but the match would split a character.
  syllable: [POLICY_HI, "दाव"],
};

async function postJson(path, body) {
  const response = await fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json", "Accept": "application/json" },
    body: JSON.stringify(body),
  });
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const detail = data && (data.detail || data.title);
    throw new Error(detail || `Request failed (${response.status})`);
  }
  return data;
}

function el(tag, options = {}, ...children) {
  const node = document.createElement(tag);
  if (options.className) node.className = options.className;
  if (options.text !== undefined) node.textContent = options.text;
  for (const [key, value] of Object.entries(options.attrs || {})) node.setAttribute(key, value);
  for (const child of children) node.append(child);
  return node;
}

function fact(term, value) {
  return el("div", {}, el("dt", { text: term }), el("dd", { text: value }));
}

function setBusy(form, busy) {
  const button = form.querySelector("button[type=submit]");
  button.setAttribute("aria-busy", String(busy));
  button.disabled = busy;
}

function showError(node, message) {
  node.textContent = message;
  node.hidden = !message;
}

// ---------- Evidence check: verify, screen, state ----------

const evidenceForm = document.getElementById("evidence-form");
const evidenceSource = document.getElementById("evidence-source");
const evidenceQuote = document.getElementById("evidence-quote");

document.querySelectorAll("[data-example]").forEach((button) => {
  button.addEventListener("click", () => {
    const [source, quote] = EVIDENCE_EXAMPLES[button.dataset.example];
    evidenceSource.value = source;
    evidenceQuote.value = quote;
    evidenceQuote.focus();
  });
});

evidenceForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const errorNode = document.getElementById("evidence-error");
  showError(errorNode, "");
  if (!evidenceQuote.value) {
    showError(errorNode, "Enter the quotation to check.");
    evidenceQuote.focus();
    return;
  }
  setBusy(evidenceForm, true);
  try {
    const sourceText = evidenceSource.value;
    const result = await postJson("/algo/evidence-check", { sourceText, claimedQuotation: evidenceQuote.value });
    renderEvidence(sourceText, result);
  } catch (error) {
    document.getElementById("evidence-result").hidden = true;
    showError(errorNode, error.message);
  } finally {
    setBusy(evidenceForm, false);
  }
});

const VERDICT_VIEW = {
  VERIFIED: ["found", "✓", "Verified: the quotation occurs exactly once"],
  AMBIGUOUS: ["caution", "≡", "Occurs more than once: not uniquely verified"],
  BOUNDARY_NOT_ESTABLISHED: ["rejected", "✕", "Not verified: character boundaries not established"],
  NOT_FOUND: ["missing", "∅", "Not found: the source does not contain this exact quotation"],
};

function renderEvidence(sourceText, result) {
  const v = result.verification;
  const [className, icon, headline] = VERDICT_VIEW[v.verdict];
  const verdict = document.getElementById("verify-verdict");
  verdict.className = "verdict " + className;
  verdict.replaceChildren(
    el("span", { className: "icon", text: icon, attrs: { "aria-hidden": "true" } }),
    el("div", {}, el("strong", { text: headline }),
      el("p", { text: result.evidence.verified.statements[0] })));

  document.getElementById("evidence-paper").replaceChildren(...buildEvidencePaper(sourceText, result));
  document.getElementById("verify-facts").replaceChildren(
    fact("Match class", v.matchClass ?? "none"),
    fact("Source length n", v.sourceLength.toLocaleString()),
    fact("Quotation length m", v.quotationLength.toLocaleString()),
    fact("Occurrences found", String(v.occurrences.length)),
    fact("Accepted", String(v.occurrences.filter((o) => o.boundariesEstablished).length)));

  const list = document.getElementById("occurrence-list");
  list.replaceChildren(...v.occurrences.map((o) => {
    const clauses = o.clauseIds.length ? ` in clause ${o.clauseIds.join(", ")}` : "";
    const item = el("li", { className: o.boundariesEstablished ? "accepted" : "rejected" },
      chip(o.boundariesEstablished ? ["confirmed", "✓", "Accepted"] : ["false-positive", "✕", "Rejected"]),
      el("span", { className: "offsets", text: ` [${o.start}, ${o.end})${clauses}` }));
    for (const issue of o.boundaryIssues) item.append(el("p", { className: "issue", text: "Boundary " + issue + "." }));
    return item;
  }), ...v.nearMisses.map((hint) => el("li", { className: "near-miss", text: hint })));

  renderScreening(result.screening);
  renderStatement(result);
  document.getElementById("evidence-result").hidden = false;
}

/**
 * Builds the source as clause spans containing marks. Offsets are code points, so the text is split with
 * Array.from (one element per code point), never by UTF-16 index.
 */
function buildEvidencePaper(sourceText, result) {
  const symbols = Array.from(sourceText);
  const markOf = new Array(symbols.length).fill(0); // 0 none, 1 rejected occurrence, 2 accepted occurrence
  const boundaries = displayBoundaries(sourceText, symbols.length);
  for (const o of result.verification.occurrences) {
    // A rejected occurrence starts or ends inside a character. Drawing a mark exactly there would split the
    // character on screen (a vowel sign would float on its own), so for display only the dashed mark is widened
    // to the browser's grapheme boundaries. The offsets listed below the source are the server's, unchanged.
    let start = o.start;
    let end = o.end;
    if (!o.boundariesEstablished) {
      while (start > 0 && !boundaries.has(start)) start--;
      while (end < symbols.length && !boundaries.has(end)) end++;
    }
    for (let i = start; i < end; i++) markOf[i] = Math.max(markOf[i], o.boundariesEstablished ? 2 : 1);
  }
  const quotedIds = new Set(result.screening.clauses.filter((c) => c.containsQuotation).map((c) => c.clauseId));
  const nodes = [];
  let cursor = 0;
  const runs = (from, to) => {
    const out = [];
    let i = from;
    while (i < to) {
      let j = i;
      while (j < to && markOf[j] === markOf[i]) j++;
      const text = symbols.slice(i, j).join("");
      if (markOf[i] === 2) out.push(el("mark", { text }));
      else if (markOf[i] === 1) out.push(el("mark", { className: "rejected", text,
        attrs: { title: "Rejected: starts or ends inside a character" } }));
      else out.push(document.createTextNode(text));
      i = j;
    }
    return out;
  };
  for (const clause of result.clauses) {
    nodes.push(...runs(cursor, clause.start));
    const span = el("span", { className: "clause" + (quotedIds.has(clause.id) ? " quoted" : ""),
      attrs: { "data-clause": clause.id } }, ...runs(clause.start, clause.end));
    nodes.push(span);
    cursor = clause.end;
  }
  nodes.push(...runs(cursor, symbols.length));
  return nodes;
}

/**
 * Code-point offsets at which the browser would break graphemes (display only; never used as evidence).
 * Falls back to "not before a combining mark" where Intl.Segmenter is unavailable.
 */
function displayBoundaries(text, length) {
  const set = new Set([0, length]);
  if (typeof Intl !== "undefined" && Intl.Segmenter) {
    let cp = 0;
    for (const { segment } of new Intl.Segmenter("und", { granularity: "grapheme" }).segment(text)) {
      set.add(cp);
      cp += Array.from(segment).length;
    }
    set.add(cp);
    return set;
  }
  Array.from(text).forEach((symbol, i) => { if (!/\p{M}|‍|‌/u.test(symbol)) set.add(i); });
  return set;
}

const SCREEN_BLOOM = {
  DEFINITELY_ABSENT: ["absent", "✕", "Definitely absent"],
  POSSIBLY_PRESENT: ["possible", "?", "Possibly present"],
};
const SCREEN_EXACT = {
  NOT_NEEDED: ["not-needed", "–", "Not needed"],
  IDENTICAL_WORDING: ["confirmed", "✓", "Identical wording"],
  FALSE_POSITIVE: ["false-positive", "!", "False positive: no match"],
};

function renderScreening(s) {
  const lib = s.library;
  const clauses = s.clauses.length;
  document.getElementById("screen-summary").textContent = clauses === 0
    ? "The source has no clauses to screen."
    : `${clauses} clause${clauses === 1 ? "" : "s"} screened. ${s.definitelyAbsent} definitely absent, so no exact `
      + `lookup was needed; ${s.exactLookups} exact comparison${s.exactLookups === 1 ? "" : "s"}: `
      + `${s.confirmedMatches} identical, ${s.falsePositivesRejected} false positive${s.falsePositivesRejected === 1 ? "" : "s"} rejected.`;
  document.getElementById("screen-rows").replaceChildren(...s.clauses.map((c) => el("tr",
    { className: c.containsQuotation ? "quoted" : "" },
    el("th", { text: c.clauseId, attrs: { scope: "row" } }),
    el("td", { text: c.containsQuotation ? "Contains it" : "—" }),
    el("td", {}, chip(SCREEN_BLOOM[c.bloomVerdict])),
    el("td", {}, chip(SCREEN_EXACT[c.exactVerdict])),
    el("td", { className: "reference", text: c.referenceId ? `${c.referenceId} · ${c.referenceTitle}` : "—" }))));
  document.getElementById("library-facts").replaceChildren(
    fact("Reference clauses", lib.referenceClauses.toLocaleString()),
    fact("Bits m / hashes k", `${lib.bitCount.toLocaleString()} / ${lib.hashCount}`),
    fact("Filter size", `${lib.bloomBytes.toLocaleString()} bytes`),
    fact("Expected FPR", formatRate(lib.expectedFalsePositiveRate)),
    fact("Seed", String(lib.seed)));
}

function renderStatement(result) {
  const part = (id, label, data, className) => {
    const node = document.getElementById(id);
    node.className = "statement-part " + className;
    node.replaceChildren(el("h4", { text: label }), el("p", { className: "part-status", text: data.status }),
      el("ul", {}, ...data.statements.map((s) => el("li", { text: s }))));
  };
  const verdict = result.verification.verdict;
  part("part-verified", "Verified", result.evidence.verified, verdict === "VERIFIED" ? "positive" : "neutral");
  const screened = result.evidence.screened.status;
  part("part-screened", "Screened", result.evidence.screened,
    screened === "Identical reference wording" ? "positive" : "neutral");
  document.getElementById("part-not-established").replaceChildren(
    ...result.evidence.notEstablished.map((s) => el("li", { text: s })));
  document.getElementById("method-list").replaceChildren(...result.method.map((s) => el("li", { text: s })));
}

// ---------- A1: quote verification ----------

const quoteForm = document.getElementById("quote-form");
const sourceInput = document.getElementById("source-text");
const quoteInput = document.getElementById("quote-text");

document.getElementById("quote-example").addEventListener("click", () => {
  sourceInput.value = EXAMPLE_TEXT;
  quoteInput.value = EXAMPLE_QUOTE;
  quoteInput.focus();
});

quoteForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const errorNode = document.getElementById("quote-error");
  showError(errorNode, "");
  if (!quoteInput.value) {
    showError(errorNode, "Enter a passage to look for.");
    quoteInput.focus();
    return;
  }
  setBusy(quoteForm, true);
  try {
    const text = sourceInput.value;
    const result = await postJson("/algo/quote-match", { text, quote: quoteInput.value });
    renderQuoteResult(text, result);
  } catch (error) {
    document.getElementById("quote-result").hidden = true;
    showError(errorNode, error.message);
  } finally {
    setBusy(quoteForm, false);
  }
});

function renderQuoteResult(text, result) {
  const count = result.matches.length;
  const verdict = document.getElementById("quote-verdict");
  verdict.replaceChildren();
  verdict.className = "verdict " + (count > 0 ? "found" : "missing");
  verdict.append(
    el("span", { className: "icon", text: count > 0 ? "✓" : "∅", attrs: { "aria-hidden": "true" } }),
    el("div", {},
      el("strong", { text: count > 0
        ? `Found ${count} exact occurrence${count === 1 ? "" : "s"}`
        : "Not found: the source does not contain this exact passage" }),
      el("p", { text: count > 0
        ? "Every highlighted span below matches the quote symbol for symbol."
        : "Exact matching does not paraphrase or fold case, so a quote that is \"almost\" present is reported as absent." })));

  // Offsets are code points, so slice with Array.from (which splits by code point), not by UTF-16 index.
  const symbols = Array.from(text);
  const highlight = document.getElementById("quote-highlight");
  highlight.replaceChildren(...buildHighlighted(symbols, mergeRanges(result.matches)));

  document.getElementById("quote-facts").replaceChildren(
    fact("Text length n", result.textLength.toLocaleString()),
    fact("Quote length m", result.quoteLength.toLocaleString()),
    fact("Occurrences", String(count)),
    fact("Offsets [start, end)", count ? result.matches.slice(0, 6).map((m) => `[${m.start}, ${m.end})`).join(" ")
      + (count > 6 ? " …" : "") : "—"),
    fact("Comparisons", result.characterComparisons.toLocaleString()),
    fact("Bound 2n", result.comparisonBound.toLocaleString()),
    fact("Server time", `${result.serverMicros} µs`));

  renderPrefixTable(result);
  document.getElementById("quote-result").hidden = false;
}

/** Merge overlapping or touching [start, end) ranges so overlapping matches render as one mark. */
function mergeRanges(matches) {
  const merged = [];
  for (const { start, end } of matches) {
    const last = merged[merged.length - 1];
    if (last && start <= last.end) {
      last.end = Math.max(last.end, end);
    } else {
      merged.push({ start, end });
    }
  }
  return merged;
}

function buildHighlighted(symbols, ranges) {
  const nodes = [];
  let cursor = 0;
  for (const { start, end } of ranges) {
    if (start > cursor) nodes.push(document.createTextNode(symbols.slice(cursor, start).join("")));
    nodes.push(el("mark", { text: symbols.slice(start, end).join("") }));
    cursor = end;
  }
  if (cursor < symbols.length) nodes.push(document.createTextNode(symbols.slice(cursor).join("")));
  return nodes;
}

function renderPrefixTable(result) {
  const table = document.getElementById("prefix-table");
  const caption = table.querySelector("caption");
  const indexRow = el("tr", {}, el("th", { text: "i", attrs: { scope: "row" } }));
  const symbolRow = el("tr", {}, el("th", { text: "symbol", attrs: { scope: "row" } }));
  const piRow = el("tr", {}, el("th", { text: "π[i]", attrs: { scope: "row" } }));
  result.quoteSymbols.forEach((symbol, i) => {
    indexRow.append(el("td", { text: String(i) }));
    symbolRow.append(el("td", { text: symbol === " " ? "␠" : symbol }));
    piRow.append(el("td", { text: String(result.prefixFunction[i]) }));
  });
  table.replaceChildren(caption, el("tbody", {}, indexRow, symbolRow, piRow));
  document.getElementById("prefix-note").textContent = result.prefixFunctionTruncated
    ? `Showing the first ${result.quoteSymbols.length} of ${result.quoteLength} symbols.`
    : "";
}

// ---------- A2: Bloom-filter screening ----------

const bloomForm = document.getElementById("bloom-form");
const fingerprintInput = document.getElementById("fingerprints");
const probeInput = document.getElementById("probes");

function lines(value) {
  return value.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
}

document.getElementById("bloom-example").addEventListener("click", () => {
  fingerprintInput.value = EXAMPLE_FINGERPRINTS.join("\n");
  probeInput.value = EXAMPLE_PROBES.join("\n");
  document.getElementById("fpr").value = "0.3";
  document.getElementById("capacity").value = "";
  document.getElementById("seed").value = "7";
});

bloomForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const errorNode = document.getElementById("bloom-error");
  showError(errorNode, "");
  const probes = lines(probeInput.value);
  if (probes.length === 0) {
    showError(errorNode, "Enter at least one probe.");
    probeInput.focus();
    return;
  }
  const capacity = document.getElementById("capacity").value;
  const seed = document.getElementById("seed").value;
  setBusy(bloomForm, true);
  try {
    const result = await postJson("/algo/membership", {
      fingerprints: lines(fingerprintInput.value),
      probes,
      targetFalsePositiveRate: Number(document.getElementById("fpr").value),
      expectedInsertions: capacity ? Number(capacity) : null,
      seed: seed ? Number(seed) : null,
    });
    renderBloomResult(result);
  } catch (error) {
    document.getElementById("bloom-result").hidden = true;
    showError(errorNode, error.message);
  } finally {
    setBusy(bloomForm, false);
  }
});

const BLOOM_LABELS = {
  DEFINITELY_ABSENT: ["absent", "✕", "Definitely absent"],
  POSSIBLY_PRESENT: ["possible", "?", "Possibly present"],
};
const EXACT_LABELS = {
  NOT_NEEDED: ["not-needed", "–", "Not needed"],
  CONFIRMED_PRESENT: ["confirmed", "✓", "Confirmed present"],
  FALSE_POSITIVE: ["false-positive", "!", "False positive"],
};

function chip([className, icon, label]) {
  return el("span", { className: "chip " + className },
    el("span", { text: icon, attrs: { "aria-hidden": "true" } }), label);
}

function renderBloomResult(result) {
  const p = result.parameters;
  const falsePositives = result.results.filter((r) => r.exactVerdict === "FALSE_POSITIVE").length;
  const possible = result.results.filter((r) => r.bloomVerdict === "POSSIBLY_PRESENT").length;
  document.getElementById("bloom-facts").replaceChildren(
    fact("Submitted", result.insertionAttempts.toLocaleString()),
    fact("Distinct members", result.distinctFingerprints.toLocaleString()),
    fact("Capacity n", p.expectedInsertions.toLocaleString()),
    fact("Target p", formatRate(p.targetFalsePositiveRate)),
    fact("Bits m", p.bitCount.toLocaleString()),
    fact("Hashes k", String(p.hashCount)),
    fact("Bits per element", p.bitsPerElement.toFixed(2)),
    fact("Fill ratio", formatRate(result.fillRatio)),
    fact("Expected FPR (distinct)", formatRate(result.expectedFalsePositiveRate)),
    fact("Possible hits", `${possible} (${falsePositives} false)`));
  document.getElementById("saturation").hidden = !result.overCapacity;
  const duplicatesNote = document.getElementById("duplicates-note");
  duplicatesNote.hidden = result.duplicateAttempts === 0;
  duplicatesNote.textContent = result.duplicateAttempts === 0 ? "" :
    `${result.duplicateAttempts} of the ${result.insertionAttempts} submissions repeated an earlier member. Repeats `
    + `were passed to the filter but set no new bits, so the filter holds ${result.distinctFingerprints} distinct `
    + "members. Capacity, expected false-positive rate and saturation are based on that distinct count.";

  const rows = result.results.map((r) => {
    const row = el("tr", { attrs: { tabindex: "0" } },
      el("td", { text: r.probe }),
      el("td", {}, chip(BLOOM_LABELS[r.bloomVerdict])),
      el("td", {}, chip(EXACT_LABELS[r.exactVerdict])),
      el("td", { className: "positions", text: r.positions.join(", ") }));
    const select = () => {
      document.querySelectorAll("#verdict-rows tr").forEach((tr) => tr.classList.remove("selected"));
      row.classList.add("selected");
      drawBits(result, r.positions);
    };
    row.addEventListener("click", select);
    row.addEventListener("keydown", (e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); select(); } });
    return row;
  });
  document.getElementById("verdict-rows").replaceChildren(...rows);
  drawBits(result, []);
  document.getElementById("bloom-result").hidden = false;
}

function formatRate(value) {
  return value >= 0.01 ? `${(value * 100).toFixed(1)}%` : `${(value * 100).toFixed(3)}%`;
}

/** Draws the filter's bits; the selected probe's k positions are outlined. */
function drawBits(result, selected) {
  const figure = document.getElementById("bits-figure");
  const caption = document.getElementById("bits-caption");
  const canvas = document.getElementById("bits");
  const set = result.bits ? [...result.bits].filter((b) => b === "1").length : null;
  if (!result.bits) {
    figure.hidden = false;
    canvas.hidden = true;
    caption.textContent = `Bit array of ${result.parameters.bitCount.toLocaleString()} bits is too large to draw `
      + `(drawn up to 4,096). Fill ratio ${formatRate(result.fillRatio)}.`;
    return;
  }
  canvas.hidden = false;
  figure.hidden = false;
  const bits = result.bits;
  const columns = Math.min(64, bits.length);
  const rowsCount = Math.ceil(bits.length / columns);
  const cell = 10;
  canvas.width = columns * cell;
  canvas.height = rowsCount * cell;
  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  for (let i = 0; i < bits.length; i++) {
    const x = (i % columns) * cell;
    const y = Math.floor(i / columns) * cell;
    ctx.fillStyle = bits[i] === "1" ? "#1d1f23" : "#e9e2d4";
    ctx.fillRect(x + 1, y + 1, cell - 2, cell - 2);
  }
  ctx.strokeStyle = "#9c3f1d";
  ctx.lineWidth = 2;
  for (const position of selected) {
    const x = (position % columns) * cell;
    const y = Math.floor(position / columns) * cell;
    ctx.strokeRect(x + 1, y + 1, cell - 2, cell - 2);
  }
  const description = `${set} of ${bits.length} bits set (${formatRate(result.fillRatio)})`
    + (selected.length ? `; outlined: the ${selected.length} positions of the selected probe` : "; select a probe row to outline its positions");
  caption.textContent = "Bit array: " + description + ".";
  canvas.setAttribute("aria-label", "Bloom filter bit array. " + description);
}
