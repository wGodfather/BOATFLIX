#!/usr/bin/env bash
set -euo pipefail
mkdir -p tv-qa
fixture=tv-qa/episode-fixture.json
NUVIO_QA_EPISODE_PACK=1 NUVIO_QA_FIXTURE_OUTPUT="$fixture" python3 -u tools/qa_torrent_seed.py > tv-qa/episode-seed.log 2>&1 &
seed_pid=$!
trap 'kill "$seed_pid" 2>/dev/null || true' EXIT
for attempt in {1..30}; do
  test -s "$fixture" && break
  sleep 1
done
read_fixture() { python3 -c 'import json,sys; d=json.load(open(sys.argv[1])); print(d[sys.argv[2]])' "$fixture" "$1"; }
magnet=$(read_fixture magnet)
tracker=$(read_fixture tracker_url)
sha8=$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["episodes"]["8"]["sha256"])' "$fixture")
sha9=$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["episodes"]["9"]["sha256"])' "$fixture")
./gradlew :androidApp:connectedFullDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.nuvio.android.TorrentEpisodePackIntegrationTest \
  "-Pandroid.testInstrumentationRunnerArguments.packMagnet=$magnet" \
  "-Pandroid.testInstrumentationRunnerArguments.packTracker=$tracker" \
  "-Pandroid.testInstrumentationRunnerArguments.packSha8=$sha8" \
  "-Pandroid.testInstrumentationRunnerArguments.packSha9=$sha9" \
  -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true \
  -Pkotlin.compiler.execution.strategy=in-process --max-workers=1 --no-configuration-cache --no-daemon
grep -q 'metadata served' tv-qa/episode-seed.log
grep -q 'piece served:' tv-qa/episode-seed.log
