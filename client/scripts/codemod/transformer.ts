import fs from 'node:fs';
import * as babelParser from '@babel/parser';
import _traverse from '@babel/traverse';
import type { Node } from '@babel/types';
import { colorTokenExpr, normalizeHexPublic, type TokenMap } from './tokenMap.js';

const traverse = ((_traverse as any).default ?? _traverse) as typeof _traverse;

const HEX_RE = /#[0-9a-fA-F]{3,8}(?![0-9a-fA-F])/g;

export interface Replacement {
  /** absolute start/end positions in the source string */
  start: number;
  end: number;
  original: string;
  replacement: string;
  tokenKey: string;
  line: number;
  context: 'template' | 'jsx-style';
}

export interface Unmatched {
  value: string; // normalized hex
  line: number;
  file: string;
}

export interface FileAnalysis {
  file: string;
  source: string;
  replacements: Replacement[];
  unmatched: Unmatched[];
  /** whether we need to add `import { theme } from '@/shared/styles/theme'` */
  needsThemeImport: boolean;
}

function isCssTag(node: Node): boolean {
  if (node.type === 'Identifier') {
    return node.name === 'css' || node.name === 'keyframes' || node.name === 'injectGlobal';
  }
  if (node.type === 'MemberExpression') {
    return node.object.type === 'Identifier' && (node.object as any).name === 'styled';
  }
  if (node.type === 'CallExpression') {
    const callee = node.callee;
    if (callee.type === 'Identifier' && (callee as any).name === 'styled') return true;
    if (callee.type === 'MemberExpression') return isCssTag(callee.object as Node);
    if (callee.type === 'CallExpression') return isCssTag(callee as Node);
  }
  return false;
}

function findHexInQuasi(
  raw: string,
  quasiSourceStart: number, // quasi.start + 1
  line: number,
  context: 'template' | 'jsx-style',
  tokenMap: TokenMap,
  file: string,
): { replacements: Replacement[]; unmatched: Unmatched[] } {
  const replacements: Replacement[] = [];
  const unmatched: Unmatched[] = [];

  HEX_RE.lastIndex = 0;
  let m: RegExpExecArray | null;

  while ((m = HEX_RE.exec(raw)) !== null) {
    const normalized = normalizeHexPublic(m[0]);
    const tokenKey = tokenMap.colorToKey.get(normalized);

    if (tokenKey) {
      const start = quasiSourceStart + m.index;
      replacements.push({
        start,
        end: start + m[0].length,
        original: m[0],
        replacement: colorTokenExpr(tokenKey, context),
        tokenKey,
        line,
        context,
      });
    } else {
      unmatched.push({ value: normalized, line, file });
    }
  }

  return { replacements, unmatched };
}

export function analyzeFile(file: string, tokenMap: TokenMap): FileAnalysis {
  const source = fs.readFileSync(file, 'utf-8');
  const replacements: Replacement[] = [];
  const unmatched: Unmatched[] = [];
  let hasThemeImport = false;
  let jSXStyleNeedsTheme = false;

  let ast: ReturnType<typeof babelParser.parse>;
  try {
    ast = babelParser.parse(source, {
      sourceType: 'module',
      plugins: ['typescript', 'jsx'],
      errorRecovery: true,
    });
  } catch {
    return { file, source, replacements, unmatched, needsThemeImport: false };
  }

  // ── Check for existing theme import ──────────────────────────────────────
  traverse(ast, {
    ImportDeclaration(nodePath) {
      const src = nodePath.node.source.value;
      if (!src.includes('styles/theme')) return;
      const hasTheme = nodePath.node.specifiers.some(
        (s) => s.type === 'ImportSpecifier' && (s.imported as any).name === 'theme',
      );
      if (hasTheme) hasThemeImport = true;
    },
  });

  // ── Find replacements ─────────────────────────────────────────────────────
  traverse(ast, {
    // styled.div`...`, css`...`, keyframes`...`
    TaggedTemplateExpression(nodePath) {
      if (!isCssTag(nodePath.node.tag as Node)) return;

      for (const quasi of nodePath.node.quasi.quasis) {
        const raw = quasi.value.raw;
        // quasi.start is the position of the backtick or `}` delimiter
        const contentStart = (quasi.start ?? 0) + 1;
        const line = quasi.loc?.start.line ?? 0;

        const found = findHexInQuasi(raw, contentStart, line, 'template', tokenMap, file);
        replacements.push(...found.replacements);
        unmatched.push(...found.unmatched);
      }
    },

    // <div style={{ color: '#fff' }}>
    JSXAttribute(nodePath) {
      const attrName = nodePath.node.name;
      if (attrName.type !== 'JSXIdentifier' || attrName.name !== 'style') return;

      const attrValue = nodePath.node.value;
      if (!attrValue || attrValue.type !== 'JSXExpressionContainer') return;

      const expr = attrValue.expression;
      if (expr.type !== 'ObjectExpression') return;

      for (const prop of expr.properties) {
        if (prop.type !== 'ObjectProperty' && (prop as any).type !== 'Property') continue;
        const val = (prop as any).value;
        if (val.type !== 'StringLiteral') continue;
        if (!val.value.match(/^#[0-9a-fA-F]{3,8}$/)) continue;

        const normalized = normalizeHexPublic(val.value);
        const tokenKey = tokenMap.colorToKey.get(normalized);
        const line: number = val.loc?.start.line ?? 0;

        if (tokenKey) {
          // Replace the full StringLiteral node (including quotes) → bare expression
          replacements.push({
            start: val.start,
            end: val.end,
            original: source.slice(val.start, val.end),
            replacement: colorTokenExpr(tokenKey, 'jsx'),
            tokenKey,
            line,
            context: 'jsx-style',
          });
          jSXStyleNeedsTheme = true;
        } else {
          unmatched.push({ value: normalized, line, file });
        }
      }
    },
  });

  // Deduplicate unmatched (same hex may appear in multiple quasis)
  const seenUnmatched = new Set<string>();
  const dedupedUnmatched = unmatched.filter(({ value, line }) => {
    const key = `${value}:${line}`;
    if (seenUnmatched.has(key)) return false;
    seenUnmatched.add(key);
    return true;
  });

  return {
    file,
    source,
    replacements,
    unmatched: dedupedUnmatched,
    needsThemeImport: jSXStyleNeedsTheme && !hasThemeImport,
  };
}

/** Apply collected replacements to source (last→first to preserve positions) */
export function applyToSource(analysis: FileAnalysis): string {
  const sorted = [...analysis.replacements].sort((a, b) => b.start - a.start);

  let result = analysis.source;
  for (const rep of sorted) {
    result = result.slice(0, rep.start) + rep.replacement + result.slice(rep.end);
  }

  if (analysis.needsThemeImport) {
    // Insert after the last existing import line
    const lastImportEnd = findLastImportEnd(result);
    const importLine = `import { theme } from '@/shared/styles/theme';\n`;
    result = result.slice(0, lastImportEnd) + importLine + result.slice(lastImportEnd);
  }

  return result;
}

function findLastImportEnd(source: string): number {
  const lines = source.split('\n');
  let lastImportLine = 0;
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].trimStart().startsWith('import ')) lastImportLine = i;
  }
  // Return the character offset after the last import line
  return lines.slice(0, lastImportLine + 1).join('\n').length + 1;
}
