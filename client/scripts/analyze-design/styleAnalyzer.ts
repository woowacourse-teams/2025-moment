import fs from 'node:fs';
import path from 'node:path';
import * as babelParser from '@babel/parser';
import _traverse from '@babel/traverse';
import type { Node } from '@babel/types';
import type { HardcodedDetail, StyleAnalysisResult } from './types.js';

const traverse = ((_traverse as any).default ?? _traverse) as typeof _traverse;

// ── regex patterns ──────────────────────────────────────────────────────────
// Hex color: #rgb, #rgba, #rrggbb, #rrggbbaa (not followed by more hex chars)
const HEX_RE = /#[0-9a-fA-F]{3,8}(?![0-9a-fA-F])/g;
// px value: positive integer or decimal, not 0px (resets aren't design decisions)
const PX_RE = /\b(?:[1-9]\d*|0\.\d+)(?:\.\d+)?px\b/g;
// Tailwind arbitrary: e.g. p-[13px], text-[#abc123]
const TAILWIND_RE = /[a-z]+-\[(?:\d+\.?\d*px|#[0-9a-fA-F]{3,8})\]/g;

function newlinesUpTo(text: string, index: number): number {
  let n = 0;
  for (let i = 0; i < index; i++) {
    if (text[i] === '\n') n++;
  }
  return n;
}

function extractFromText(
  text: string,
  startLine: number,
  relFile: string,
  details: HardcodedDetail[],
) {
  HEX_RE.lastIndex = 0;
  let m: RegExpExecArray | null;
  while ((m = HEX_RE.exec(text)) !== null) {
    details.push({ file: relFile, type: 'hexColor', value: m[0], line: startLine + newlinesUpTo(text, m.index) });
  }
  PX_RE.lastIndex = 0;
  while ((m = PX_RE.exec(text)) !== null) {
    details.push({ file: relFile, type: 'pxValue', value: m[0], line: startLine + newlinesUpTo(text, m.index) });
  }
  TAILWIND_RE.lastIndex = 0;
  while ((m = TAILWIND_RE.exec(text)) !== null) {
    details.push({ file: relFile, type: 'tailwindArbitrary', value: m[0], line: startLine + newlinesUpTo(text, m.index) });
  }
}

// Identify Emotion/styled-components CSS tagged-template tags:
// styled.div``, styled(Comp)``, css``, keyframes``
function isCssTag(node: Node): boolean {
  if (node.type === 'Identifier') {
    return node.name === 'css' || node.name === 'keyframes' || node.name === 'injectGlobal';
  }
  if (node.type === 'MemberExpression') {
    // styled.div, styled.button, etc.
    return node.object.type === 'Identifier' && (node.object as any).name === 'styled';
  }
  if (node.type === 'CallExpression') {
    // styled(Component)`...`  or  styled.div.attrs(...)`...`
    const callee = node.callee;
    if (callee.type === 'Identifier' && (callee as any).name === 'styled') return true;
    if (callee.type === 'MemberExpression') return isCssTag(callee.object as Node);
    if (callee.type === 'CallExpression') return isCssTag(callee as Node);
  }
  return false;
}

// Recursively scan style expression nodes (ObjectExpression, StringLiteral, TemplateLiteral)
function extractFromStyleExpr(node: Node, relFile: string, details: HardcodedDetail[]) {
  if (node.type === 'StringLiteral') {
    extractFromText(node.value, node.loc?.start.line ?? 1, relFile, details);
    return;
  }
  if (node.type === 'TemplateLiteral') {
    for (const quasi of node.quasis) {
      const text = quasi.value.cooked ?? quasi.value.raw;
      extractFromText(text, quasi.loc?.start.line ?? 1, relFile, details);
    }
    return;
  }
  if (node.type === 'ObjectExpression') {
    for (const prop of node.properties) {
      if (prop.type === 'ObjectProperty' || prop.type === 'Property') {
        extractFromStyleExpr((prop as any).value as Node, relFile, details);
      }
    }
  }
}

export function analyzeStyles(files: string[], projectRoot: string): StyleAnalysisResult {
  const details: HardcodedDetail[] = [];

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
      // styled.div`...`, css`...`, keyframes`...`
      TaggedTemplateExpression(nodePath) {
        if (!isCssTag(nodePath.node.tag as Node)) return;
        for (const quasi of nodePath.node.quasi.quasis) {
          const text = quasi.value.cooked ?? quasi.value.raw;
          extractFromText(text, quasi.loc?.start.line ?? 1, relFile, details);
        }
      },

      // <div style={{ color: '#fff' }}>  or  <div css={{ padding: '16px' }}>
      JSXAttribute(nodePath) {
        const name = nodePath.node.name;
        if (name.type !== 'JSXIdentifier') return;
        if (name.name !== 'style' && name.name !== 'css') return;

        const value = nodePath.node.value;
        if (!value || value.type !== 'JSXExpressionContainer') return;
        const expr = value.expression;
        if (expr.type === 'JSXEmptyExpression') return;

        extractFromStyleExpr(expr as Node, relFile, details);
      },
    });
  }

  return { details };
}
