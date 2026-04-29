import fs from 'node:fs';
import path from 'node:path';
import * as babelParser from '@babel/parser';
import _traverse from '@babel/traverse';
import type { ComponentAnalysisResult } from './types.js';

// @babel/traverse ships as CJS; handle ESM/CJS interop
const traverse = ((_traverse as any).default ?? _traverse) as typeof _traverse;

const COMPONENT_PREFIXES = ['@/shared/design-system', '@/shared/ui'];

export function analyzeComponents(files: string[], projectRoot: string): ComponentAnalysisResult {
  const components: ComponentAnalysisResult['components'] = {};

  for (const file of files) {
    let code: string;
    try {
      code = fs.readFileSync(file, 'utf-8');
    } catch {
      continue;
    }

    let ast: ReturnType<typeof babelParser.parse>;
    try {
      ast = babelParser.parse(code, {
        sourceType: 'module',
        plugins: ['typescript', 'jsx'],
        errorRecovery: true,
      });
    } catch {
      continue;
    }

    const relFile = path.relative(projectRoot, file);

    traverse(ast, {
      ImportDeclaration(nodePath) {
        const source = nodePath.node.source.value;
        if (!COMPONENT_PREFIXES.some((prefix) => source.startsWith(prefix))) return;

        if (!components[source]) {
          components[source] = { importPath: source, importCount: 0, usedInFiles: [] };
        }
        components[source].importCount++;
        if (!components[source].usedInFiles.includes(relFile)) {
          components[source].usedInFiles.push(relFile);
        }
      },
    });
  }

  return { components };
}
