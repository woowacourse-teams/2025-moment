import fs from 'node:fs';
import path from 'node:path';
import { collectFiles } from '../analyze-design/fileCollector.js';
import { buildTokenMap } from './tokenMap.js';
import { analyzeFile, applyToSource, type Unmatched } from './transformer.js';

const PROJECT_ROOT = process.cwd();
const SRC_DIR = path.resolve(PROJECT_ROOT, 'src');

const isApply = process.argv.includes('--apply');

// ── run ──────────────────────────────────────────────────────────────────────
async function main() {
  const tokenMap = buildTokenMap(PROJECT_ROOT);
  console.log(`\n토큰 색상 맵: ${tokenMap.colorToKey.size}개 로드\n`);

  const files = collectFiles(SRC_DIR);

  let totalReplacements = 0;
  let modifiedFiles = 0;
  const allUnmatched = new Map<string, { count: number; files: Set<string> }>();

  for (const file of files) {
    const analysis = analyzeFile(file, tokenMap);

    if (analysis.replacements.length === 0 && analysis.unmatched.length === 0) continue;

    // ── collect unmatched token candidates ──
    for (const u of analysis.unmatched) {
      const entry = allUnmatched.get(u.value) ?? { count: 0, files: new Set() };
      entry.count++;
      entry.files.add(path.relative(PROJECT_ROOT, u.file));
      allUnmatched.set(u.value, entry);
    }

    if (analysis.replacements.length === 0) continue;

    const relFile = path.relative(PROJECT_ROOT, file);
    totalReplacements += analysis.replacements.length;
    modifiedFiles++;

    // ── preview ──
    if (!isApply) {
      console.log(`\x1b[36m${relFile}\x1b[0m  (${analysis.replacements.length}개)`);
      for (const rep of analysis.replacements) {
        console.log(
          `  L${rep.line}  \x1b[31m${rep.original}\x1b[0m → \x1b[32m${rep.replacement}\x1b[0m`,
        );
      }
      if (analysis.needsThemeImport) {
        console.log(`  \x1b[33m+ import { theme } from '@/shared/styles/theme' 추가 예정\x1b[0m`);
      }
    }

    // ── apply ──
    if (isApply) {
      const modified = applyToSource(analysis);
      fs.writeFileSync(file, modified, 'utf-8');
      console.log(`\x1b[32m✓\x1b[0m ${relFile}  (${analysis.replacements.length}개 적용)`);
    }
  }

  // ── summary ──────────────────────────────────────────────────────────────
  console.log('\n' + '─'.repeat(60));

  if (isApply) {
    console.log(
      `\x1b[32m완료\x1b[0m  ${modifiedFiles}개 파일, ${totalReplacements}개 교체\n`,
    );
  } else {
    console.log(
      `\x1b[33m[미리보기]\x1b[0m  ${modifiedFiles}개 파일, ${totalReplacements}개 교체 예정`,
    );
    console.log(`적용하려면: \x1b[36mpnpm run codemod:apply\x1b[0m\n`);
  }

  // ── 신규 토큰 후보 ─────────────────────────────────────────────────────
  if (allUnmatched.size > 0) {
    console.log('─'.repeat(60));
    console.log(`\x1b[33m신규 토큰 후보 (${allUnmatched.size}개)\x1b[0m — 토큰에 없는 색상값:\n`);

    const sorted = [...allUnmatched.entries()].sort(([, a], [, b]) => b.count - a.count);
    for (const [hex, { count, files }] of sorted) {
      const fileList = [...files].slice(0, 3).join(', ');
      const more = files.size > 3 ? ` 외 ${files.size - 3}개` : '';
      console.log(`  ${hex}  (${count}회)  ← ${fileList}${more}`);
    }

    // Write unmatched report
    const reportDir = path.resolve(PROJECT_ROOT, 'design-system-report');
    fs.mkdirSync(reportDir, { recursive: true });
    const report = {
      generatedAt: new Date().toISOString(),
      unmatchedColors: Object.fromEntries(
        sorted.map(([hex, { count, files }]) => [hex, { count, files: [...files] }]),
      ),
    };
    fs.writeFileSync(
      path.join(reportDir, 'token-candidates.json'),
      JSON.stringify(report, null, 2),
      'utf-8',
    );
    console.log(`\n→ design-system-report/token-candidates.json\n`);
  }
}

main().catch((err) => {
  console.error('codemod 실패:', err);
  process.exit(1);
});
