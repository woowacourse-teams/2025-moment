import fs from 'node:fs';
import path from 'node:path';
import * as babelParser from '@babel/parser';
import _traverse from '@babel/traverse';
import type { TokenAnalysisResult } from './types.js';

const traverse = ((_traverse as any).default ?? _traverse) as typeof _traverse;

// theme object property names that map to design token categories
const TOKEN_CATEGORIES = new Set([
  'colors',
  'semantic',
  'spacing',
  'typography',
  'breakpoints',
  'sizes',
]);

export function analyzeTokens(files: string[], projectRoot: string): TokenAnalysisResult {
  const result: TokenAnalysisResult = {
    total: 0,
    byCategory: {},
  };

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

    traverse(ast, {
      // Detect: theme.colors, theme.semantic, theme.spacing, etc.
      // Works for both theme.colors['xxx'] and theme.colors.xxx (computed + non-computed)
      // Also handles Emotion callback pattern: ({ theme }) => theme.colors[...]
      MemberExpression(nodePath) {
        const { object, property } = nodePath.node;

        // object must be the `theme` identifier directly
        if (object.type !== 'Identifier' || object.name !== 'theme') return;

        // property must be a known token category
        const propName =
          property.type === 'Identifier'
            ? property.name
            : property.type === 'StringLiteral'
              ? property.value
              : null;

        if (!propName || !TOKEN_CATEGORIES.has(propName)) return;

        result.total++;
        result.byCategory[propName] = (result.byCategory[propName] ?? 0) + 1;
      },
    });
  }

  return result;
}
