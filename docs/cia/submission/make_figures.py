#!/usr/bin/env python3
"""Regenerate the figures used in the technical report from the committed benchmark results.

Usage: python docs/cia/submission/make_figures.py        (needs matplotlib)
Reads  cia/cia-bench/results/{jmh-results.json,jmh-bloom-results.json,jmh-linking-results.json,bloom-experiment.csv}, docs/cia/evidence/api-latency.csv
Writes docs/cia/submission/figures/*.png
"""
import csv, json, statistics
from collections import defaultdict
from pathlib import Path
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

ROOT = Path(__file__).resolve().parents[3]
RES = ROOT / "cia/cia-bench/results"
OUT = Path(__file__).resolve().parent / "figures"
OUT.mkdir(exist_ok=True)
C = {"KMP (ours)": "#9c3f1d", "naive": "#3f4a5a", "String.indexOf": "#25603a"}
plt.rcParams.update({"font.size": 8, "axes.spines.top": False, "axes.spines.right": False, "font.family": "DejaVu Sans"})


def kmp_figure():
    data = json.loads((RES / "jmh-results.json").read_text())
    rows = defaultdict(dict)
    for b in data:
        name = b["benchmark"].rsplit(".", 1)[1]
        if name not in ("kmp", "naive", "jdkIndexOf"):
            continue
        p = b["params"]
        label = {"kmp": "KMP (ours)", "naive": "naive", "jdkIndexOf": "String.indexOf"}[name]
        rows[(p["input"], int(p["patternLength"]))].setdefault(label, []).append(
            (int(p["textLength"]), b["primaryMetric"]["score"] / 1000.0))  # us -> ms
    fig, axes = plt.subplots(1, 2, figsize=(7.0, 2.7), sharey=False)
    for ax, key, title in ((axes[0], ("ORDINARY", 512), "Ordinary text, m = 512"),
                           (axes[1], ("ADVERSARIAL", 512), "Adversarial (a…a vs a…ab), m = 512")):
        for label, pts in rows[key].items():
            pts.sort()
            ax.plot([x for x, _ in pts], [y for _, y in pts], marker="o", ms=3.5, lw=1.4, label=label, color=C[label])
        ax.set_xscale("log"); ax.set_yscale("log")
        ax.set_xlabel("text length n (symbols)"); ax.set_title(title, fontsize=8.5)
        ax.grid(alpha=0.25, which="both", lw=0.4)
    axes[0].set_ylabel("time per search (ms, log)")
    axes[1].legend(frameon=False, fontsize=7.5, loc="upper left")
    fig.tight_layout()
    fig.savefig(OUT / "fig_kmp.png", dpi=220); plt.close(fig)


def bloom_figure():
    by = defaultdict(list)
    for r in csv.DictReader(l for l in (RES / "bloom-experiment.csv").open() if not l.startswith("#")):
        by[(int(r["elements"]), int(r["bitsPerElement"]))].append(r)
    fig, axes = plt.subplots(1, 2, figsize=(7.0, 2.6))
    ax = axes[0]
    for bits, col in ((6, "#3f4a5a"), (10, "#9c3f1d"), (14, "#25603a")):
        ns = sorted(n for n, b in by if b == bits)
        meas = [statistics.mean(float(r["measuredFpr"]) for r in by[(n, bits)]) * 100 for n in ns]
        theo = [float(by[(n, bits)][0]["theoreticalFpr"]) * 100 for n in ns]
        ax.plot(ns, meas, "o-", ms=4, lw=1.3, color=col, label=f"measured, {bits} bits/elem")
        ax.plot(ns, theo, "x--", ms=5, lw=0.9, color=col, alpha=0.7)
    ax.plot([], [], "k--x", lw=0.9, label="textbook formula")
    ax.set_xscale("log"); ax.set_yscale("log")
    ax.set_xlabel("members n"); ax.set_ylabel("false-positive rate (%)")
    ax.set_title("FPR: measured (mean of 5 seeds) vs theory", fontsize=8.5)
    ax.legend(frameon=False, fontsize=6.5, loc="center right", bbox_to_anchor=(1.0, 0.42)); ax.grid(alpha=0.25, which="both", lw=0.4)
    ax = axes[1]
    ns = sorted(n for n, b in by if b == 10)
    bl = [float(by[(n, 10)][0]["bloomBytes"]) for n in ns]
    hs = [float(by[(n, 10)][0]["hashSetBytesApprox"]) for n in ns]
    ax.plot(ns, hs, "o-", ms=4, color="#3f4a5a", label="HashSet<Long> (approx.)")
    ax.plot(ns, bl, "o-", ms=4, color="#9c3f1d", label="Bloom filter (10 bits/elem)")
    ax.set_xscale("log"); ax.set_yscale("log")
    ax.set_xlabel("members n"); ax.set_ylabel("memory (bytes)")
    ax.set_title("Memory: Bloom filter vs exact set", fontsize=8.5)
    ax.legend(frameon=False, fontsize=7); ax.grid(alpha=0.25, which="both", lw=0.4)
    fig.tight_layout(); fig.savefig(OUT / "fig_bloom.png", dpi=220); plt.close(fig)


def architecture_figure():
    fig, ax = plt.subplots(figsize=(7.0, 3.5))
    ax.set_xlim(0, 100); ax.set_ylim(0, 50); ax.axis("off")

    def box(x, y, w, h, text, fc="#fffdf8", ec="#3f4a5a", fs=6.8):
        ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.25,rounding_size=0.8", fc=fc, ec=ec, lw=1))
        ax.text(x + w / 2, y + h / 2, text, ha="center", va="center", fontsize=fs)

    def arrow(x0, y0, x1, y1, text=None, dy=1.6):
        ax.annotate("", xy=(x1, y1), xytext=(x0, y0), arrowprops=dict(arrowstyle="->", lw=1, color="#3f4a5a"))
        if text:
            ax.text((x0 + x1) / 2, (y0 + y1) / 2 + dy, text, ha="center", fontsize=6.3, color="#4a4d55")

    box(1, 20, 16, 11, "Browser UI\n(HTML/CSS/JS)\nstatic/app.js", fc="#eef2f7", fs=7.2)
    ax.add_patch(FancyBboxPatch((25, 2), 74, 46, boxstyle="round,pad=0.3,rounding_size=1", fc="#fbf6ec", ec="#9c3f1d", lw=1.2, ls="--"))
    ax.text(26.5, 45.3, "cia-demo  (Spring Boot 4.2, Java 21, 127.0.0.1:8090)", fontsize=7.4, fontweight="bold", color="#9c3f1d")
    arrow(17, 25.5, 28, 25.5, "JSON / HTTP", 1.8)
    rows = [(34, "EvidenceCheckController\nPOST /algo/evidence-check", "EvidenceCheckService\n+ BoundaryCheck\n+ ClauseSegmenter"),
            (24, "QuoteMatchController\nPOST /algo/quote-match", "QuoteMatchService"),
            (14, "MembershipController\nPOST /algo/membership", "MembershipScreening-\nService + Exact-\nFingerprintRegistry"),
            (4, "DocumentLinkController\nPOST /algo/document-links", "DocumentLinkService\n(identifier -> doc number)")]
    for y, ctl, svc in rows:
        box(28, y, 20, 7.5, ctl)
        box(54, y, 20, 7.5, svc, fs=6.4)
        arrow(48, y + 3.75, 54, y + 3.75)
    box(79, 34, 18.5, 7.5, "core-api SPI\nQuoteVerifier\nApproximateMembership", fc="#eef2f7", fs=6.4)
    box(79, 4, 18.5, 26, "cia-algorithms\n(assessed, own Java)\n\nA1 KmpMatcher\n\nA2 BloomFilter\n(BitArray, SeededHash64,\nProbeSequence)\n\nA3 DisjointSet\n(union by size,\npath halving)", fc="#f3e3d6", ec="#9c3f1d", fs=6.4)
    arrow(74, 37.75, 79, 37.75)
    arrow(88, 34, 88, 30)
    arrow(74, 27.75, 79, 22)
    arrow(74, 17.75, 79, 16)
    arrow(74, 7.75, 79, 8)
    ax.text(62, 0.2, "Algorithms run only on the server. No database, no outbound calls, synthetic data only.", ha="center", fontsize=6.4, style="italic", color="#4a4d55")
    fig.tight_layout(); fig.savefig(OUT / "fig_architecture.png", dpi=220); plt.close(fig)


def linking_figure():
    p = RES / "jmh-linking-results.json"
    if not p.exists():
        return
    data = json.loads(p.read_text())
    rows = defaultdict(dict)
    for b in data:
        name = b["benchmark"].rsplit(".", 1)[1]
        rows[name][int(b["params"]["nodes"])] = b["primaryMetric"]["score"] / 1000.0  # us -> ms
    fig, axes = plt.subplots(1, 2, figsize=(7.0, 2.6))
    for ax, (dsu, bfs, title) in zip(axes, (("batchDsu", "batchBfs", "Batch: count components of n nodes, n edges"),
                                            ("incrementalDsu", "incrementalBfs", "Incremental: 20 batches, 'connected?' after each"))):
        for key, label, col in ((dsu, "disjoint set (ours)", "#9c3f1d"), (bfs, "BFS (rebuilds each time)" if "incremental" in bfs else "BFS", "#3f4a5a")):
            pts = sorted(rows[key].items())
            ax.plot([x for x, _ in pts], [y for _, y in pts], "o-", ms=3.5, lw=1.4, color=col, label=label)
        ax.set_xscale("log"); ax.set_yscale("log")
        ax.set_xlabel("nodes n"); ax.set_title(title, fontsize=8)
        ax.grid(alpha=0.25, which="both", lw=0.4); ax.legend(frameon=False, fontsize=7)
    axes[0].set_ylabel("time (ms, log)")
    fig.tight_layout(); fig.savefig(OUT / "fig_linking.png", dpi=220); plt.close(fig)


def latency_figure():
    p = ROOT / "docs/cia/evidence/api-latency.csv"
    if not p.exists():
        return
    rows = list(csv.DictReader(p.open()))
    fig, ax = plt.subplots(figsize=(7.0, 2.0))
    ys = range(len(rows))
    ax.barh(list(ys), [float(r["median_ms"]) for r in rows], color="#9c3f1d", label="median")
    ax.scatter([float(r["p95_ms"]) for r in rows], list(ys), color="#3f4a5a", marker="|", s=120, label="p95", zorder=3)
    ax.set_yticks(list(ys)); ax.set_yticklabels([r["scenario"] for r in rows], fontsize=7)
    ax.invert_yaxis(); ax.set_xlabel("HTTP round trip (ms)"); ax.legend(frameon=False, fontsize=7, loc="lower right")
    fig.tight_layout(); fig.savefig(OUT / "fig_latency.png", dpi=220); plt.close(fig)


if __name__ == "__main__":
    kmp_figure(); bloom_figure(); architecture_figure(); linking_figure(); latency_figure()
    print("wrote", ", ".join(sorted(p.name for p in OUT.glob("*.png"))))
