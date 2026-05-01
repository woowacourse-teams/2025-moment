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
  const sortedByFile = Object.fromEntries(Object.entries(byFile).sort(([, a], [, b]) => b - a));

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
  const json = JSON.stringify(report, null, 2);

  const outDir = path.resolve(projectRoot, OUTPUT_DIR);
  fs.mkdirSync(outDir, { recursive: true });
  fs.writeFileSync(path.join(outDir, OUTPUT_FILE), json, 'utf-8');

  // Also copy to public/ so the dev server can serve it to the dashboard page
  const publicDir = path.resolve(projectRoot, 'public', OUTPUT_DIR);
  fs.mkdirSync(publicDir, { recursive: true });
  fs.writeFileSync(path.join(publicDir, OUTPUT_FILE), json, 'utf-8');

  // ── console summary (3-column table) ──────────────────────────────────
  const componentCount = Object.keys(sortedComponents).length;
  const totalImports = Object.values(sortedComponents).reduce((s, c) => s + c.importCount, 0);

  const C1 = 26,
    C2 = 22,
    C3 = 22;

  // Visual width accounting for Hangul double-width chars
  function vw(s: string): number {
    let w = 0;
    for (const c of s) {
      const cp = c.codePointAt(0) ?? 0;
      w += cp >= 0xac00 && cp <= 0xd7af ? 2 : 1;
    }
    return w;
  }
  const pe = (s: string, n: number) => s + ' '.repeat(Math.max(0, n - vw(s)));
  const ps = (s: string, n: number) => ' '.repeat(Math.max(0, n - vw(s))) + s;
  const peAnsi = (s: string, visW: number, n: number) => s + ' '.repeat(Math.max(0, n - visW));

  const row = (a: string, b: string, c: string) => `║ ${pe(a, C1)} ║ ${pe(b, C2)} ║ ${pe(c, C3)} ║`;
  const rowAnsi3 = (a: string, b: string, c: string, cVisW: number) =>
    `║ ${pe(a, C1)} ║ ${pe(b, C2)} ║ ${peAnsi(c, cVisW, C3)} ║`;

  const SEP = `╠${'═'.repeat(C1 + 2)}╬${'═'.repeat(C2 + 2)}╬${'═'.repeat(C3 + 2)}╣`;
  const TOP = `╔${'═'.repeat(C1 + 2)}╦${'═'.repeat(C2 + 2)}╦${'═'.repeat(C3 + 2)}╗`;
  const BOT = `╚${'═'.repeat(C1 + 2)}╩${'═'.repeat(C2 + 2)}╩${'═'.repeat(C3 + 2)}╝`;

  const barFilled = Math.round((tokenAdoptionPercent / 100) * 14);
  const barStr =
    '\x1b[33m' +
    '█'.repeat(barFilled) +
    '\x1b[90m' +
    '░'.repeat(14 - barFilled) +
    '\x1b[0m' +
    `  ${tokenAdoptionPercent}%`;
  const barVisW = 14 + 2 + String(tokenAdoptionPercent).length + 1; // █░ + '  X%'

  const shortKey = (k: string) => k.split('/').pop() ?? k;
  const topComps = Object.entries(sortedComponents).slice(0, 5);
  const maxRows = Math.max(topComps.length, 4);

  const c1: string[] = topComps.map(
    ([k, v]) => `${ps(String(v.importCount), 3)}회  ${shortKey(k)}`,
  );
  const c2: string[] = [
    `px값     ${ps(String(byType.pxValue), 5)}개`,
    `hex      ${ps(String(byType.hexColor), 5)}개`,
    `tailwind ${ps(String(byType.tailwindArbitrary), 5)}개`,
  ];
  const c3Plain: string[] = ['', '', `토큰      ${tokenCount}회`, `하드코딩  ${hardcodedCount}개`];

  console.log('\n' + TOP);
  console.log(
    row(
      `컴포넌트  ${componentCount}종 / ${totalImports}회`,
      `하드코딩  총 ${hardcodedCount}개`,
      '토큰 채택률',
    ),
  );
  console.log(SEP);
  for (let i = 0; i < maxRows; i++) {
    const a = c1[i] ?? '';
    const b = c2[i] ?? '';
    if (i === 0) {
      console.log(rowAnsi3(a, b, barStr, barVisW));
    } else {
      console.log(row(a, b, c3Plain[i] ?? ''));
    }
  }
  console.log(BOT);
  console.log(`\n  ${files.length}개 파일 분석 완료  →  ${path.join(OUTPUT_DIR, OUTPUT_FILE)}\n`);
}
