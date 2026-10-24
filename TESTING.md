# Reproducible functional evidence

These fixtures exercise **#1's code**, not #2's implementation. Run from the project directory with JDK 11 (or newer supporting `--release 11`), Git and Python 3. Python is only a test tool; running the game needs Java alone. No downloads, libraries or game source edits are needed.

```sh
python3 tests/run_checks.py
```

Ordinary verification compiles each historical snapshot separately, runs its saved input, checks independent outcome assertions and compares the entire output with the saved transcript. It also compiles the current working source and checks its source digest against the reviewed freeze-candidate hash pinned in `tests/run_checks.py`. Documentation changes do not affect that comparison. A temporary directory contains archived sources and class files and is removed on success or failure. Tests do not create or move Git tags.

## Recorded runs

Each name below identifies `tests/fixtures/<name>.in` and its matching `.out` transcript. All runs reach turn 18. The victory fixtures explicitly select staff actions; the defeat fixtures explicitly wait throughout, rather than relying on EOF behaviour.

| Fixture | Source revision | Paid / served | Revenue | Paid satisfaction | Unhappy departures | Ending |
| --- | --- | --- | --- | --- | --- | --- |
| `stage1-victory` | `053e096e16a8043fb012c5506fa8d0b3c72d4bc6` | 5 / 5 | $66.00 | 79.2 | 0 | Victory |
| `stage1-defeat` | same Stage 1 revision | 0 / 0 | $0.00 | N/A | 2 | Defeat |
| `stage2-victory` | `feb7e6abcf17856f9b6a12c3e904293fd42ee852` | 5 / 5 | $64.50 | 78.0 | 0 | Victory |
| `stage2-defeat` | same Stage 2 revision | 0 / 0 | $0.00 | N/A | 2 | Defeat |
| `stage3-victory` | `bae23ff8be8ab3b4307a4b46b91bb7a0dfdf9e6a` | 5 / 5 | $77.40 | 81.6 | 0 | Victory |
| `stage3-defeat` | same Stage 3 revision | 0 / 0 | $0.00 | N/A | 2 | Defeat |
| `head-victory` | Reviewed integration source based on `b62d196`, with order announcement; exact identity in receipt | 5 / 5 | $77.40 | 81.6 | 0 | Victory |
| `head-defeat` | same reviewed working tree | 0 / 0 | $0.00 | N/A | 2 | Defeat |
| `head-invalid` | same reviewed working tree | 5 / 5 | $77.40 | 81.6 | 0 | Victory |
| `head-no-happy-hour` | same reviewed working tree | 5 / 5 | $80.10 | 80.6 | 0 | Victory |

`head-invalid` inserts four rejected choices into `head-victory`: invalid Happy Hour text, an unknown Host customer, serving before an order exists, and an unknown Chef customer. The three rejected staff tasks preserve their allowance; the final results equal the valid run. The runner explicitly asserts both the four errors and the unchanged metrics.

For every current-source fixture, the runner also checks that successful orders announce the customer, chosen meal, preparation units and locked bill before that turn's first Chef prompt. It checks the VIP's $10.80 Happy Hour bill ($13.50 without the policy), combo preparation, and absence of announcements in the wait-only defeat. This regression failed against the pre-fix source before the patch.

The stage fixtures use deterministic customer IDs and meal selection from this implementation; they do not claim to be random-seed experiments. Stage 2 includes VIP C2 and Critic C3. In Stage 3, Happy Hour is activated on turn 4: C2's Burger is locked at $10.80, and the Critic chooses the three-unit $24.30 combo. A sixth customer arrives on turn 16 and does not pay by closing. Waiting defeats have only two departures by closing because later arrivals have not all exhausted their patience.

`head-no-happy-hour` uses exactly the same staff commands as `head-victory`, with every Happy Hour answer set to `off`. Fresh execution gives 5 paid customers, $80.10 revenue and 80.6 paid satisfaction, versus $77.40 and 81.6 when Happy Hour starts on turn 4. The pricing policy trades $2.70 revenue for one satisfaction point in this schedule; both runs win. It demonstrates a measured choice consequence, not a victory/defeat reversal or an optimal strategy claim.

## Independent rule checks

`tests/RuleChecks.java` performs 43 checks independently of transcript equality:

- Combo composition: Salad + Pasta costs 2430 cents and needs three preparation units.
- VIP Burger pricing: 1350 cents normally, then 1080 with Happy Hour; combo with Happy Hour is 1944 cents.
- Happy Hour's two-turn duration, rejection of reuse, recovery cap, immunity of departed/paid customers, and prices frozen both before activation and after expiry.
- Regular/VIP/Critic waiting losses of 8/5/12 and the Critic's longest-preparation selection rule.
- Abandonment at zero satisfaction cancels the unserved order.
- Staff allowances, invalid action retries, Host seating plus Waiter ordering in one turn, unpaid revenue exclusion and payment satisfaction snapshots.
- Exact victory thresholds: four paid customers, 4500 cents and satisfaction total 240; each just-below case fails.

The worked regular Pasta scenario directly exercises domain objects, allowing the requested two-unit dish independently of the arrival schedule. It seats on turn 1, orders/prepares on turn 2, becomes READY on turn 3, is served on turn 4, becomes READY_TO_PAY on turn 5 and pays on turn 6. It asserts a bill of 1800 cents and satisfaction 76 (three waiting turns), rejection of same-turn service/payment, and release of the table after payment. This is a controlled rule scenario, not a claim that the CLI fixture seats C3 on turn 1.

## Evidence maintenance and limits

`tests/evidence/results.json` records the latest pass/failure, JDK identity, exact source commit when `src` matches HEAD (otherwise an explicit uncommitted-working-tree identity), original base revision, SHA-256 of source/input/output and observed metrics for every CLI run. Documentation-only commits do not change the source fingerprint. `tests/evidence/rule-checks.txt` lists each successful domain assertion. On failure, the receipt preserves the error and `failure-output.txt` preserves the last execution output; a verification failure does not replace any expected transcript.

To intentionally rebuild expected transcripts after reviewing a legitimate output change:

```sh
python3 tests/run_checks.py --regenerate
python3 tests/run_checks.py
```

Regeneration still requires every semantic assertion to pass. Review the resulting transcript diff; do not use regeneration to approve an unexplained regression. Previous audit inputs seeded these fixtures, but every retained output here was regenerated from the stated source revision.

This focused suite verifies compilation, ten scripted CLI journeys and the listed domain rules. It does not exhaust all manager strategies, automate a video, prove the published stage tags point to these revisions or substitute for group review. Only the CLI's successful-order announcement changed; domain and controller code are unchanged. The frozen candidate source digest is `f9583ac910d1d9012b2f5f7dcca9e34c03191cc2dcbca2fc0f00b2465ba96fbe`. Historical stage snapshots remain unchanged. Disposable storage budget: 30 MiB; retained test evidence budget: 2 MiB. There are no retained class files or copied stage source trees.
