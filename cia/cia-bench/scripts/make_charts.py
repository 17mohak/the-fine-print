#!/usr/bin/env python3
"""Generate SVG charts from the committed benchmark results (no third-party packages).

Usage:  python cia/cia-bench/scripts/make_charts.py
Reads   cia/cia-bench/results/jmh-results.json and results/bloom-experiment.csv
Writes  cia/cia-bench/results/charts/*.svg
"""
import csv
import json
import math
from pathlib import Path

RESULTS = Path(__file__).resolve().parent.parent / "results"
CHARTS = RESULTS / "charts"
COLOURS = {"kmp": "#9c3f1d", "naive": "#3f4a5a", "jdkIndexOf": "#25603a",
           "bloom": "#9c3f1d", "hashSet": "#3f4a5a", "theory": "#7a4b00"}
W, H, L, R, T, B = 640, 360, 70, 160, 40, 50


def esc(s):
    return str(s).replace("&", "&amp;").replace("<", "&lt;")


def line_chart(path, title, xlabel, ylabel, series, log_y=True, log_x=True):
    """series: {name: [(x, y), ...]}"""
    xs = [x for pts in series.values() for x, _ in pts]
    ys = [y for pts in series.values() for _, y in pts if y > 0]
    fx = (lambda v: math.log10(v)) if log_x else (lambda v: v)
    fy = (lambda v: math.log10(v)) if log_y else (lambda v: v)
    x0, x1 = fx(min(xs)), fx(max(xs))
    y0, y1 = fy(min(ys)), fy(max(ys))
    if log_y:
        y0, y1 = math.floor(y0), math.ceil(y1)
    else:
        y0, y1 = 0, y1 * 1.1
    px = lambda v: L + (fx(v) - x0) / ((x1 - x0) or 1) * (W - L - R)
    py = lambda v: H - B - (fy(v) - y0) / ((y1 - y0) or 1) * (H - T - B)
    out = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" font-family="system-ui,sans-serif" '
           f'font-size="12"><rect width="{W}" height="{H}" fill="#fffdf8"/>',
           f'<text x="{L}" y="22" font-size="14" font-weight="600" fill="#1d1f23">{esc(title)}</text>',
           f'<line x1="{L}" y1="{H-B}" x2="{W-R}" y2="{H-B}" stroke="#999"/>',
           f'<line x1="{L}" y1="{T}" x2="{L}" y2="{H-B}" stroke="#999"/>']
    if log_y:
        for e in range(int(y0), int(y1) + 1):
            y = H - B - (e - y0) / ((y1 - y0) or 1) * (H - T - B)
            out.append(f'<line x1="{L}" y1="{y:.1f}" x2="{W-R}" y2="{y:.1f}" stroke="#eee"/>'
                       f'<text x="{L-6}" y="{y+4:.1f}" text-anchor="end" fill="#4a4d55">1e{e}</text>')
    else:
        for i in range(5):
            v = y0 + (y1 - y0) * i / 4
            y = py(v) if v > 0 else H - B
            out.append(f'<line x1="{L}" y1="{y:.1f}" x2="{W-R}" y2="{y:.1f}" stroke="#eee"/>'
                       f'<text x="{L-6}" y="{y+4:.1f}" text-anchor="end" fill="#4a4d55">{v:.3g}</text>')
    for x in sorted(set(xs)):
        out.append(f'<text x="{px(x):.1f}" y="{H-B+16}" text-anchor="middle" fill="#4a4d55">{x:,}</text>')
    out.append(f'<text x="{(L+W-R)/2}" y="{H-12}" text-anchor="middle" fill="#1d1f23">{esc(xlabel)}</text>')
    out.append(f'<text transform="translate(16,{(T+H-B)/2}) rotate(-90)" text-anchor="middle" '
               f'fill="#1d1f23">{esc(ylabel)}</text>')
    for i, (name, pts) in enumerate(series.items()):
        colour = COLOURS.get(name.split(" ")[0], "#555")
        pts = sorted(p for p in pts if p[1] > 0)
        d = " ".join(f"{'M' if j == 0 else 'L'}{px(x):.1f},{py(y):.1f}" for j, (x, y) in enumerate(pts))
        dash = ' stroke-dasharray="5 4"' if "theory" in name else ""
        out.append(f'<path d="{d}" fill="none" stroke="{colour}" stroke-width="2"{dash}/>')
        out += [f'<circle cx="{px(x):.1f}" cy="{py(y):.1f}" r="3" fill="{colour}"/>' for x, y in pts]
        ly = T + 10 + i * 20
        out.append(f'<line x1="{W-R+12}" y1="{ly}" x2="{W-R+32}" y2="{ly}" stroke="{colour}" stroke-width="2"{dash}/>'
                   f'<text x="{W-R+38}" y="{ly+4}" fill="#1d1f23">{esc(name)}</text>')
    out.append("</svg>")
    path.write_text("\n".join(out), encoding="utf-8")
    print("wrote", path)


def kmp_charts(results):
    for kind, m in (("ORDINARY", "64"), ("ORDINARY", "512"), ("ADVERSARIAL", "64"), ("ADVERSARIAL", "512")):
        series = {}
        for r in results:
            name = r["benchmark"].rsplit(".", 1)[1]
            p = r["params"]
            if not r["benchmark"].endswith(("kmp", "naive", "jdkIndexOf")) or "input" not in p:
                continue
            if p["input"] == kind and p["patternLength"] == m:
                series.setdefault(name, []).append((int(p["textLength"]), r["primaryMetric"]["score"]))
        if series:
            line_chart(CHARTS / f"kmp-{kind.lower()}-m{m}.svg",
                       f"Find all occurrences — {kind.lower()} input, pattern length {m}",
                       "text length n (symbols)", "µs per search (log)", series)


def bloom_throughput_chart(results):
    series = {}
    for r in results:
        name = r["benchmark"].rsplit(".", 1)[1]
        p = r["params"]
        if name in ("bloomQuery", "hashSetQuery") and p.get("bitsPerElement") == "10":
            series.setdefault(name, []).append((int(p["elements"]), r["primaryMetric"]["score"]))
    if series:
        line_chart(CHARTS / "bloom-query-throughput.svg", "Membership queries (10 bits/element for Bloom)",
                   "elements n", "queries per µs (log)", series)


def bloom_experiment_charts():
    rows = [r for r in csv.DictReader(l for l in (RESULTS / "bloom-experiment.csv").open(encoding="utf-8")
                                      if not l.startswith("#"))]
    n_max = max(int(r["elements"]) for r in rows)
    fpr = {"bloom measured": [], "theory": []}
    for r in rows:
        if int(r["elements"]) == n_max:
            b = int(r["bitsPerElement"])
            fpr["bloom measured"].append((b, float(r["measuredFpr"])))
            fpr["theory"].append((b, float(r["theoreticalFpr"])))
    line_chart(CHARTS / "bloom-fpr.svg", f"False-positive rate at n = {n_max:,} (1M negative probes)",
               "bits per element", "false-positive rate (log)", fpr, log_x=False)
    mem = {"bloom (10 bits/elem)": [], "hashSet<Long> (approx.)": []}
    for r in rows:
        if r["bitsPerElement"] == "10":
            n = int(r["elements"])
            mem["bloom (10 bits/elem)"].append((n, int(r["bloomBytes"]) / n))
            mem["hashSet<Long> (approx.)"].append((n, int(r["hashSetBytesApprox"]) / n))
    line_chart(CHARTS / "bloom-memory.svg", "Memory per element", "elements n", "bytes per element (log)", mem)


def main():
    CHARTS.mkdir(parents=True, exist_ok=True)
    jmh = RESULTS / "jmh-results.json"
    if jmh.exists():
        results = json.loads(jmh.read_text(encoding="utf-8"))
        kmp_charts(results)
        bloom_throughput_chart(results)
    bloom_experiment_charts()


if __name__ == "__main__":
    main()
