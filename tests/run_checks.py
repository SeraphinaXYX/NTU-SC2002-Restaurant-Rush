#!/usr/bin/env python3
"""Offline JDK 11 checks. Only --regenerate replaces golden transcripts."""
import argparse
import hashlib
import io
import json
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
import tempfile

ROOT = Path(__file__).resolve().parents[1]
TESTS = ROOT / 'tests'
REVISIONS = {
    'stage1': '053e096e16a8043fb012c5506fa8d0b3c72d4bc6',
    'stage2': 'feb7e6abcf17856f9b6a12c3e904293fd42ee852',
    'stage3': 'bae23ff8be8ab3b4307a4b46b91bb7a0dfdf9e6a',
    'head': 'b62d196d392b8c998c22690af98ade03df30b5bf',
}

# Pinned current working source; historical stage commits remain unchanged.
CURRENT_SOURCE_SHA256 = 'f9583ac910d1d9012b2f5f7dcca9e34c03191cc2dcbca2fc0f00b2465ba96fbe'

def run(command, **kwargs):
    result = subprocess.run(command, text=True, capture_output=True, check=False, **kwargs)
    if result.returncode:
        raise RuntimeError(f'{command[0]} exited {result.returncode}\n{result.stdout}\n{result.stderr}')
    return result.stdout


def source_hash(root):
    digest = hashlib.sha256()
    for path in sorted((root / 'src').rglob('*.java')):
        digest.update(path.relative_to(root).as_posix().encode() + b'\0')
        digest.update(path.read_bytes() + b'\0')
    return digest.hexdigest()


def metrics(output, victory):
    pattern = (r'Paid: (\d+) \| Served: (\d+) \| Unhappy departures: (\d+) '
               r'\| Revenue: \$(\d+)\.(\d{2}) \| Paid satisfaction: (N/A|\d+\.\d+) \| Turns used: (\d+)')
    matches = re.findall(pattern, output)
    assert len(matches) == 1, 'Exactly one final metric line required'
    paid, served, left, dollars, cents, satisfaction, turns = matches[0]
    value = {'paid': int(paid), 'served': int(served), 'departures': int(left),
             'revenue_cents': int(dollars) * 100 + int(cents),
             'paid_satisfaction_display': satisfaction, 'turns': int(turns)}
    assert value['turns'] == 18
    assert ('FINAL: VICTORY' in output) == victory
    assert ('FINAL: DEFEAT' in output) != victory
    if victory:
        assert 4 <= value['paid'] <= 5 and value['revenue_cents'] >= 4500
        assert float(satisfaction) >= 60
    else:
        assert value['paid'] == value['served'] == value['revenue_cents'] == 0
        assert satisfaction == 'N/A' and value['departures'] == 2
    return value


def check_order_announcements(output, case):
    pattern = (r'Order placed for C(\d+): (.+?), preparation units (\d+), '
               r'locked bill \$(\d+)\.(\d{2})')
    announcements = list(re.finditer(pattern, output))
    expected = [] if case == 'defeat' else [
        (1, 'Salad', 1, 900),
        (2, 'Burger', 1, 1350 if case == 'no-happy-hour' else 1080),
        (3, 'Salad + Pasta', 3, 2430),
        (4, 'Salad + Pasta', 3, 2430),
        (5, 'Salad', 1, 900),
    ]
    actual = [(int(m[1]), m[2], int(m[3]), int(m[4]) * 100 + int(m[5]))
              for m in announcements]
    assert actual == expected, 'New order announcements must identify the meal, prep and locked bill'
    for announcement in announcements:
        turn_start = output.rfind('\nTurn ', 0, announcement.start())
        chef_prompt = output.find('Chef [cook ID | wait]:', turn_start)
        assert turn_start >= 0 and announcement.end() < chef_prompt, \
            'Announce the chosen order before the same-turn Chef prompt'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--regenerate', action='store_true', help='replace expected CLI evidence after review')
    args = parser.parse_args()
    evidence = TESTS / 'evidence'
    evidence.mkdir(exist_ok=True)
    receipt = {'status': 'running', 'mode': 'regenerate' if args.regenerate else 'verify',
               'java': subprocess.run(['java', '-version'], capture_output=True, text=True).stderr.strip(),
               'compiler': run(['javac', '-version']).strip(),
               'compile_options': ['--release', '11', '-Xlint:all'], 'cases': []}
    active_output = ''
    try:
        with tempfile.TemporaryDirectory(prefix='restaurant-rush-tests-') as temporary:
            scratch = Path(temporary)
            for stage, revision in REVISIONS.items():
                checkout = scratch / stage
                checkout.mkdir()
                archive = subprocess.run(['git', 'archive', revision, 'src'], cwd=ROOT,
                                         capture_output=True, check=True).stdout
                with tarfile.open(fileobj=io.BytesIO(archive)) as files:
                    files.extractall(checkout)
                if stage == 'head':
                    shutil.rmtree(checkout / 'src')
                    shutil.copytree(ROOT / 'src', checkout / 'src')
                digest = source_hash(checkout)
                if stage == 'head':
                    assert digest == CURRENT_SOURCE_SHA256, 'Working source differs from reviewed freeze candidate'
                    source_changed = subprocess.run(
                        ['git', 'diff', '--quiet', 'HEAD', '--', 'src'], cwd=ROOT, check=False)
                    assert source_changed.returncode in (0, 1), 'Cannot identify current source revision'
                    current_revision = ('uncommitted-working-tree' if source_changed.returncode
                                        else run(['git', 'rev-parse', 'HEAD'], cwd=ROOT).strip())
                classes = checkout / 'classes'
                classes.mkdir()
                sources = sorted(str(path) for path in (checkout / 'src').rglob('*.java'))
                run(['javac', '--release', '11', '-Xlint:all', '-d', str(classes)] + sources)
                cases = ['victory', 'defeat'] + (['invalid', 'no-happy-hour'] if stage == 'head' else [])
                for case in cases:
                    name = stage + '-' + case
                    fixture = TESTS / 'fixtures' / (name + '.in')
                    active_output = run(['java', '-cp', str(classes), 'restaurantrush.Main'],
                                        input=fixture.read_text(), timeout=15)
                    observed = metrics(active_output, case != 'defeat')
                    if case != 'defeat':
                        expected_revenue, expected_satisfaction = {
                            'stage1': (6600, '79.2'), 'stage2': (6450, '78.0'),
                            'stage3': (7740, '81.6'), 'head': (7740, '81.6')}[stage]
                        if case == 'no-happy-hour':
                            expected_revenue, expected_satisfaction = 8010, '80.6'
                        assert observed['paid'] == observed['served'] == 5
                        assert observed['departures'] == 0
                        assert observed['revenue_cents'] == expected_revenue
                        assert observed['paid_satisfaction_display'] == expected_satisfaction
                    if case == 'invalid':
                        assert active_output.count('Invalid choice:') == 4
                        assert active_output.count('no task was used') == 3
                        assert observed == receipt['cases'][-2]['metrics'], 'Retries changed successful-run outcome'
                    else:
                        assert 'Invalid choice:' not in active_output
                    if stage == 'head':
                        check_order_announcements(active_output, case)
                    expected = TESTS / 'fixtures' / (name + '.out')
                    if args.regenerate:
                        expected.write_text(active_output)
                    else:
                        assert expected.read_text() == active_output, name + ' transcript differs'
                    receipt['cases'].append({'case': name,
                        'code_revision': current_revision if stage == 'head' else revision,
                        'base_revision': revision,
                        'source_sha256': digest, 'input_sha256': hashlib.sha256(fixture.read_bytes()).hexdigest(),
                        'transcript_sha256': hashlib.sha256(active_output.encode()).hexdigest(),
                        'metrics': observed, 'status': 'passed'})
                if stage == 'head':
                    run(['javac', '--release', '11', '-Xlint:all', '-cp', str(classes), '-d', str(classes),
                         str(TESTS / 'RuleChecks.java')])
                    active_output = run(['java', '-cp', str(classes), 'RuleChecks'], timeout=15)
                    checks = int(re.search(r'CHECKS=(\d+)', active_output).group(1))
                    receipt['domain_checks'] = checks
                    (evidence / 'rule-checks.txt').write_text(active_output)
            receipt['temporary_peak_bytes'] = sum(path.stat().st_size for path in scratch.rglob('*') if path.is_file())
            assert receipt['temporary_peak_bytes'] < 30 * 1024 * 1024, 'Disposable budget exceeded'
            receipt['status'] = 'passed'
    except Exception as error:
        receipt['status'] = 'failed'
        receipt['error'] = str(error)
        (evidence / 'failure-output.txt').write_text(active_output)
        raise
    finally:
        receipt['temporary_directory_removed'] = not Path(temporary).exists() if 'temporary' in locals() else True
        (evidence / 'results.json').write_text(json.dumps(receipt, indent=2) + '\n')
    print(f"PASS {len(receipt['cases'])} CLI cases and {receipt['domain_checks']} domain checks")
    print('Results: tests/evidence/results.json')


if __name__ == '__main__':
    main()
