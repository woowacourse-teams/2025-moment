import csv from 'csv-parser';
import { createReadStream, writeFile, mkdirSync, existsSync } from 'fs';
import path from 'path';

const dataArray = [];
const outputDir = 'src/converter/ts';
const outputFile = path.join(outputDir, 'profanityWords.ts');

if (!existsSync(outputDir)) {
  mkdirSync(outputDir, { recursive: true });
}

createReadStream('src/converter/csv/filteringList.csv')
  .pipe(
    csv({
      headers: false,
      skipEmptyLines: true,
    }),
  )
  .on('data', row => {
    const word = row[0]?.trim();
    if (word) {
      dataArray.push(word);
    }
  })
  .on('end', () => {
    const content = `export const PROFANITY_WORDS = ${JSON.stringify(dataArray, null, 2)};`;

    writeFile(outputFile, content, 'utf8', err => {
      if (err) {
        console.error('파일 생성 실패:', err);
        process.exit(1);
      } else {
        console.log(`변환 완료: ${dataArray.length}개 단어`);
      }
    });
  })
  .on('error', err => {
    console.error('CSV 파일 읽기 실패:', err);
    process.exit(1);
  });
