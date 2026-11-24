#!/usr/bin/env python3
"""Replay the sample requests in data/samples/requests against a running cia-demo and record or check responses.

Usage (the demo must be running: ./gradlew :cia:cia-demo:bootRun):
    python ops/scripts/replay_samples.py --check            # compare with data/samples/responses (exit 1 on any difference)
    python ops/scripts/replay_samples.py --record           # overwrite data/samples/responses with the live output
    python ops/scripts/replay_samples.py --base-url http://127.0.0.1:8095 --check

Standard library only. Every sample is synthetic. Responses are deterministic (fixed seeds); the per-request `serverMicros` timing is ignored when comparing.
"""
import argparse
import json
import pathlib
import sys
import urllib.error
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[2] / "data" / "samples"


def send(base, sample):
    if "rawBody" in sample:
        data = sample["rawBody"].encode("utf-8")
    else:
        data = json.dumps(sample["request"], ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(base + sample["endpoint"], data=data, method="POST",
                                 headers={"Content-Type": "application/json", "Accept": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return resp.status, resp.headers.get("Content-Type", ""), json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as err:
        return err.code, err.headers.get("Content-Type", ""), json.loads(err.read().decode("utf-8"))


def stable(value):
    """Drop the per-request server timing so the comparison is deterministic."""
    if isinstance(value, dict):
        return {k: stable(v) for k, v in value.items() if k != "serverMicros"}
    if isinstance(value, list):
        return [stable(v) for v in value]
    return value


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    mode = ap.add_mutually_exclusive_group(required=True)
    mode.add_argument("--check", action="store_true")
    mode.add_argument("--record", action="store_true")
    ap.add_argument("--base-url", default="http://127.0.0.1:8090")
    args = ap.parse_args()

    out_dir = ROOT / "responses"
    out_dir.mkdir(exist_ok=True)
    failures = 0
    for path in sorted((ROOT / "requests").glob("*.json")):
        sample = json.loads(path.read_text(encoding="utf-8"))
        status, ctype, body = send(args.base_url, sample)
        record = {"name": sample["name"], "endpoint": sample["endpoint"], "status": status,
                  "contentType": ctype.split(";")[0], "response": body}
        target = out_dir / path.name
        problems = []
        if status != sample["expectedStatus"]:
            problems.append(f"status {status}, expected {sample['expectedStatus']}")
        if args.record:
            target.write_text(json.dumps(record, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        elif not target.exists():
            problems.append("no recorded response (run --record)")
        elif stable(json.loads(target.read_text(encoding="utf-8"))) != stable(record):
            problems.append("response differs from the recorded one")
        print(("FAIL " if problems else "ok   ") + f"{sample['name']:<42} HTTP {status}" + (" - " + "; ".join(problems) if problems else ""))
        failures += bool(problems)
    print(f"\n{'recorded' if args.record else 'checked'} {len(list((ROOT / 'requests').glob('*.json')))} samples, {failures} problem(s)")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
