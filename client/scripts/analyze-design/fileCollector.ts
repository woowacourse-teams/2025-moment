import fs from 'node:fs';
import path from 'node:path';

const EXCLUDED_DIRS = new Set([
  'node_modules',
  'dist',
  'build',
  'coverage',
  'cypress',
  '.storybook',
  '.next',
  '.git',
]);

const INCLUDED_EXTS = new Set(['.ts', '.tsx', '.js', '.jsx']);

const EXCLUDED_FILE_RE = /\.stories\.[jt]sx?$/;

export function collectFiles(rootDir: string): string[] {
  const results: string[] = [];

  function walk(dir: string) {
    let entries: fs.Dirent[];
    try {
      entries = fs.readdirSync(dir, { withFileTypes: true });
    } catch {
      return;
    }

    for (const entry of entries) {
      if (entry.isDirectory()) {
        if (!EXCLUDED_DIRS.has(entry.name)) {
          walk(path.join(dir, entry.name));
        }
      } else if (entry.isFile()) {
        const ext = path.extname(entry.name);
        if (INCLUDED_EXTS.has(ext) && !EXCLUDED_FILE_RE.test(entry.name)) {
          results.push(path.join(dir, entry.name));
        }
      }
    }
  }

  walk(rootDir);
  return results;
}
