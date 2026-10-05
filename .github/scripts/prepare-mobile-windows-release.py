"""Publish only validated MSI/APK artifacts built together from the current main SHA."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import xml.etree.ElementTree as ET

version = os.environ['RELEASE_VERSION']
sha = os.environ['GITHUB_SHA']
run_id = os.environ['GITHUB_RUN_ID']
repo = os.environ['GITHUB_REPOSITORY']
root = Path('release-input')
out = Path('release-dist')
out.mkdir(exist_ok=True)
expected = [f'BOATFLIX-Windows-x64-{version}.msi'] + [
    f'BOATFLIX-Android-{abi}-{version}.apk'
    for abi in ['universal', 'arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64']
]
package_roots = [root / f'BOATFLIX-{platform}-{version}' for platform in ['Windows', 'Android']]
assert all(p.is_dir() for p in package_roots), 'Release package artifacts missing'
found = [p for directory in package_roots for p in directory.rglob('*')
         if p.suffix in ['.msi', '.apk']]
assert sorted(p.name for p in found) == sorted(expected), 'Unexpected/missing release packages'
packages = []
for source in sorted(found):
    assert source.stat().st_size > 1_000_000, f'Invalid package: {source.name}'
    with source.open('rb') as stream:
        digest = hashlib.file_digest(stream, 'sha256').hexdigest()
    recorded = Path(str(source) + '.sha256').read_text().split()[0].lower()
    assert digest == recorded, f'Checksum mismatch: {source.name}'
    if source.suffix == '.apk':
        signature = Path(str(source) + '.signature.txt').read_text()
        assert 'Verifies' in signature
        cert = re.search(r'Signer #1 certificate SHA-256 digest: (\S+)', signature)
        assert cert, 'APK signer certificate missing'
        manifest = Path(str(source) + '.manifest.txt').read_text()
        assert "name='com.wgodfather.boatflix'" in manifest
        assert f"versionName='{version}'" in manifest
        assert "versionCode='136'" in manifest
        packages.append(dict(name=source.name, sha256=digest, bytes=source.stat().st_size,
                             signing_certificate_sha256=cert[1]))
    else:
        packages.append(dict(name=source.name, sha256=digest, bytes=source.stat().st_size))
    shutil.copy2(source, out / source.name)
assert len({p['signing_certificate_sha256'] for p in packages if p['name'].endswith('.apk')}) == 1

def result(pattern, expected_text):
    files = list(root.glob(pattern))
    assert len(files) == 1, f'Missing/ambiguous QA result: {pattern}'
    text = files[0].read_text(encoding='utf-8-sig').strip()
    assert expected_text in text, text
    return text

win_upgrade = result('BOATFLIX-Windows-QA-*/build/boatflix-msi-qa/result.txt', f'1.33 -> {version}')
episode = result('BOATFLIX-Windows-QA-*/build/torrent-pack-qa/result.txt', 'PASS: E08/E09 playback')
android_upgrade = result('BOATFLIX-Android-QA-*/build/upgrade-qa/result.txt', f'1.33 to {version}')

def counts(pattern):
    paths = list(root.glob(pattern))
    assert paths, f'Missing test reports: {pattern}'
    suites = [ET.parse(p).getroot() for p in paths]
    assert all(int(s.attrib.get('failures', 0)) == 0 and int(s.attrib.get('errors', 0)) == 0 for s in suites)
    return dict(tests=sum(int(s.attrib['tests']) for s in suites),
                skipped=sum(int(s.attrib.get('skipped', 0)) for s in suites))

checks = dict(windows=counts('BOATFLIX-Windows-QA-*/composeApp/build/test-results/desktopTest/TEST-*.xml'),
              android_host=counts('BOATFLIX-Android-QA-*/composeApp/build/test-results/testAndroidHostTest/TEST-*.xml'),
              phone=counts('BOATFLIX-Android-QA-*/mobile-qa/androidTest-results/connected/**/TEST-*.xml'),
              tablet=counts('BOATFLIX-Android-QA-*/tablet-qa/androidTest-results/connected/**/TEST-*.xml'))
for label in ['phone', 'tablet']:
    assert checks[label]['tests'] > 0 and checks[label]['skipped'] == 0
manifest = dict(version=version, version_code=136, source_commit=sha, source_branch='main',
                source_url=f'https://github.com/{repo}/tree/{sha}', license='GPL-3.0',
                workflow_run=f'https://github.com/{repo}/actions/runs/{run_id}',
                platforms=['Windows x64', 'Android phone/tablet'], packages=packages, tests=checks,
                windows_upgrade=win_upgrade, android_upgrade=android_upgrade, episode_pack=episode)
(out / 'RELEASE_MANIFEST.json').write_text(json.dumps(manifest, indent=2) + '\n')
shutil.copy2('LICENSE', out / 'LICENSE.txt')
(out / 'QA_REPORT.md').write_text(
    f'# BOATFLIX {version} doğrulaması\n\nKaynak: `{sha}` (`main`).\n\n'
    f'[Derleme ve test kayıtları]({manifest["workflow_run"]})\n\n'
    + '\n'.join(f'- {label}: {value["tests"]} test, {value["skipped"]} atlandı.' for label, value in checks.items())
    + f'\n\n- {win_upgrade}\n- {android_upgrade}\n- {episode}\n\n'
    + 'Telefon ve tablet testleri emülatörlerde çalıştırıldı. 1.33 bölüm seçimi/indirme düzeltmeleri '
      've 1.35 kaynak listeleme/zaman aşımı/sunucu yalıtımı birlikte test edildi. '
      'Bu yayın Windows ve Android telefon/tablet paketlerini içerir.\n', encoding='utf-8')
with (out / 'SHA256SUMS.txt').open('w') as sums:
    for path in sorted(out.iterdir()):
        if path.name != 'SHA256SUMS.txt':
            with path.open('rb') as stream:
                sums.write(f'{hashlib.file_digest(stream, "sha256").hexdigest()}  {path.name}\n')
