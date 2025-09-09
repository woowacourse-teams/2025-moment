import csv from 'csv-parser';
import { createReadStream, writeFile, mkdirSync, existsSync } from 'fs';
import path from 'path';

const dataArray = [];
const outputDir = 'src/converter/ts';
const outputFile = path.join(outputDir, 'profanityWords.ts');

if (!existsSync(outputDir)) {
  mkdirSync(outputDir, { recursive: true });
  console.log(`디렉토리 생성: ${outputDir}`);
}

createReadStream('src/converter/csv/filteringList.csv')
  .pipe(
    csv({
      headers: false,
      skipEmptyLines: true,
    }),
  )
  .on('data', row => {
    const word = row[0];
    if (word && word.trim()) {
      dataArray.push(word.trim());
    }
  })
  .on('end', () => {
    writeFile(
      outputFile,
      `export const PROFANITY_WORDS = ${JSON.stringify(dataArray, null, 2)};`,
      'utf8',
      function (err) {
        if (err) {
          console.error('파일 생성 실패:', err);
        } else {
          console.log(`profanityWords.csv -> profanityWords.ts 변환 성공`);
          console.log(`생성된 파일: ${outputFile}`);
          console.log(`변환된 단어 수: ${dataArray.length}개`);
        }
      },
    );
  })
  .on('error', err => {
    console.error('CSV 파일 읽기 실패:', err);
  });
