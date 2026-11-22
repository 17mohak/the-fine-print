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

const EXAMPLE_FINGERPRINTS = [
  "POL-HLT-2026-0001", "POL-HLT-2026-0002", "POL-HLT-2026-0003", "POL-MTR-2026-0101",
  "POL-MTR-2026-0102", "LN-EDU-2026-5501", "LN-PER-2026-7302", "RENT-MH-2026-0044",
  "SUB-OTT-2026-9001", "WAR-ELEC-2026-3307",
];
const EXAMPLE_PROBES = [
  "POL-HLT-2026-0002", "LN-EDU-2026-5501", "WAR-ELEC-2026-3307", "POL-HLT-2026-0999",
  "POL-MTR-2026-0199", "LN-PER-2026-7399", "RENT-KA-2026-0044", "SUB-OTT-2026-9099",
  "WAR-ELEC-2026-3399", "POL-HLT-2025-0001", "LN-EDU-2026-5599", "RENT-MH-2026-0045",
];

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
    fact("Capacity n", p.expectedInsertions.toLocaleString()),
    fact("Target p", formatRate(p.targetFalsePositiveRate)),
    fact("Bits m", p.bitCount.toLocaleString()),
    fact("Hashes k", String(p.hashCount)),
    fact("Bits per element", p.bitsPerElement.toFixed(2)),
    fact("Fill ratio", formatRate(result.fillRatio)),
    fact("Expected FPR now", formatRate(result.expectedFalsePositiveRate)),
    fact("Possible hits", `${possible} (${falsePositives} false)`));
  document.getElementById("saturation").hidden = !result.overCapacity;

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
