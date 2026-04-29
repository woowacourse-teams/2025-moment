import path from 'node:path';
import { collectFiles } from './fileCollector.js';
import { analyzeComponents } from './componentAnalyzer.js';
import { analyzeStyles } from './styleAnalyzer.js';
import { analyzeTokens } from './tokenAnalyzer.js';
import { generateReport } from './reporter.js';

const PROJECT_ROOT = process.cwd();
const SRC_DIR = path.resolve(PROJECT_ROOT, 'src');

async function main() {
  console.log('디자인 시스템 분석 시작...');

  const files = collectFiles(SRC_DIR);
  process.stdout.write(`파일 수집: ${files.length}개`);

  const componentResult = analyzeComponents(files, PROJECT_ROOT);
  process.stdout.write(' → 컴포넌트 분석 완료');

  const styleResult = analyzeStyles(files, PROJECT_ROOT);
  process.stdout.write(' → 스타일 분석 완료');

  const tokenResult = analyzeTokens(files, PROJECT_ROOT);
  process.stdout.write(' → 토큰 분석 완료\n');

  generateReport(PROJECT_ROOT, files, componentResult, styleResult, tokenResult);
}

main().catch((err) => {
  console.error('\n분석 실패:', err);
  process.exit(1);
});
