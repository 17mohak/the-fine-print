#!/usr/bin/env python3
"""End-to-end latency of the three /algo/* endpoints (HTTP round trip to a running cia-demo, loopback).

Usage: python ops/scripts/measure_api_latency.py [--base-url URL] [--runs 200] [--out docs/cia/evidence/api-latency.csv]
Reports median, p95 and max per scenario after a warm-up. Standard library only. These numbers include HTTP,
JSON and Tomcat overhead, so they are NOT algorithm benchmarks (those are in cia/cia-bench).
"""
import argparse, csv, json, pathlib, statistics, time, urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[2]
SCENARIOS = [  # (label, sample file) - sizes in the label are those of the request
    ("evidence-check, 6-clause policy", "01-evidence-verified"),
    ("quote-match, n=200 adversarial", "11-kmp-adversarial"),
    ("membership, 10 members / 12 probes", "21-bloom-low-rate"),
]


def post(base, endpoint, body):
    req = urllib.request.Request(base + endpoint, data=body, method="POST", headers={"Content-Type": "application/json"})
    t0 = time.perf_counter()
    with urllib.request.urlopen(req, timeout=30) as r:
        r.read()
    return (time.perf_counter() - t0) * 1000


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--base-url", default="http://127.0.0.1:8090")
    ap.add_argument("--runs", type=int, default=200)
    ap.add_argument("--out", default=str(ROOT / "docs/cia/evidence/api-latency.csv"))
    a = ap.parse_args()
    rows = []
    # extra scale scenarios: evidence check over a large source (400 clauses, the documented limit)
    big_src = "\n".join(f"{i}.1 Clause number {i}. The Policyholder shall give written notice within {i} days." for i in range(1, 401))
    extra = [("evidence-check, 400 clauses (limit)", "/algo/evidence-check",
              json.dumps({"sourceText": big_src, "claimedQuotation": "written notice within 399 days"}).encode()),
             ("quote-match, n=100,000 (limit)", "/algo/quote-match",
              json.dumps({"text": "a" * 100000, "quote": "a" * 511 + "b"}).encode())]
    jobs = []
    for label, name in SCENARIOS:
        s = json.loads((ROOT / "data/samples/requests" / f"{name}.json").read_text(encoding="utf-8"))
        jobs.append((label, s["endpoint"], json.dumps(s["request"], ensure_ascii=False).encode("utf-8")))
    jobs += extra
    for label, ep, body in jobs:
        for _ in range(30):
            post(a.base_url, ep, body)  # warm-up
        t = sorted(post(a.base_url, ep, body) for _ in range(a.runs))
        rows.append([label, ep, len(body), a.runs, round(statistics.median(t), 2), round(t[int(0.95 * len(t)) - 1], 2), round(t[-1], 2)])
        print(f"{label:<40} median {rows[-1][4]:>7} ms   p95 {rows[-1][5]:>7} ms   max {rows[-1][6]:>7} ms")
    out = pathlib.Path(a.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["scenario", "endpoint", "request_bytes", "runs", "median_ms", "p95_ms", "max_ms"])
        w.writerows(rows)
    print("wrote", out)


if __name__ == "__main__":
    main()
