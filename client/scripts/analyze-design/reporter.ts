import fs from 'node:fs';
import path from 'node:path';
import type {
  ComponentAnalysisResult,
  HardcodedType,
  Report,
  StyleAnalysisResult,
  TokenAnalysisResult,
} from './types.js';

const OUTPUT_DIR = 'design-system-report';
const OUTPUT_FILE = 'report.json';

export function generateReport(
  projectRoot: string,
  files: string[],
  componentResult: ComponentAnalysisResult,
  styleResult: StyleAnalysisResult,
  tokenResult: TokenAnalysisResult,
): void {
  // ── hardcoded styles aggregation ──────────────────────────────────────────
  const byType: Record<HardcodedType, number> = { hexColor: 0, pxValue: 0, tailwindArbitrary: 0 };
  const byFile: Record<string, number> = {};

  for (const detail of styleResult.details) {
    byType[detail.type]++;
    byFile[detail.file] = (byFile[detail.file] ?? 0) + 1;
  }

  // Sort byFile descending
  const sortedByFile = Object.fromEntries(
    Object.entries(byFile).sort(([, a], [, b]) => b - a),
  );

  // Sort components by importCount descending
  const sortedComponents = Object.fromEntries(
    Object.entries(componentResult.components).sort(
      ([, a], [, b]) => b.importCount - a.importCount,
    ),
  );

  const hardcodedCount = styleResult.details.length;
  const tokenCount = tokenResult.total;
  const total = tokenCount + hardcodedCount;
  const tokenAdoptionPercent = total === 0 ? 0 : Math.round((tokenCount / total) * 1000) / 10;

  const report: Report = {
    generatedAt: new Date().toISOString(),
    analyzedFiles: files.length,
    components: sortedComponents,
    hardcodedStyles: {
      total: hardcodedCount,
      byType,
      byFile: sortedByFile,
      details: styleResult.details,
    },
    tokenUsage: tokenResult,
    adoptionRate: {
      tokenCount,
      hardcodedCount,
      tokenAdoptionPercent,
    },
  };

  // ── write JSON ──────────────────────────────────────────────────────────
  const outDir = path.resolve(projectRoot, OUTPUT_DIR);
  fs.mkdirSync(outDir, { recursive: true });
  fs.writeFileSync(path.join(outDir, OUTPUT_FILE), JSON.stringify(report, null, 2), 'utf-8');

  // ── console summary ────────────────────────────────────────────────────
  const componentCount = Object.keys(sortedComponents).length;
  const totalImports = Object.values(sortedComponents).reduce((s, c) => s + c.importCount, 0);

  console.log('\n=== 디자인 시스템 분석 결과 ===\n');
  console.log(`분석 파일 수:    ${files.length}개`);
  console.log(
    `컴포넌트 사용:   ${componentCount}종 / 총 ${totalImports}회`,
  );

  if (componentCount > 0) {
    const top = Object.entries(sortedComponents)
      .slice(0, 5)
      .map(([k, v]) => `  ${v.importCount}회  ${k}`)
      .join('\n');
    console.log(top);
  }

  console.log(
    `\n하드코딩 값:     ${hardcodedCount}개  (hex: ${byType.hexColor}, px: ${byType.pxValue}, tailwind: ${byType.tailwindArbitrary})`,
  );
  console.log(`토큰 사용:       ${tokenCount}회`);
  console.log(`토큰 채택률:     ${tokenAdoptionPercent}%`);
  console.log(`\n→ ${path.join(OUTPUT_DIR, OUTPUT_FILE)}\n`);
}
