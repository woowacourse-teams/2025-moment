import fs from 'node:fs';
import path from 'node:path';

export interface TokenMap {
  // normalized lowercase hex → token key (e.g. '#f1c40f' → 'yellow-500')
  colorToKey: Map<string, string>;
}

function normalizeHex(hex: string): string {
  const h = hex.toLowerCase().replace('#', '');
  if (h.length === 3) return '#' + h[0] + h[0] + h[1] + h[1] + h[2] + h[2];
  if (h.length === 4) return '#' + h[0] + h[0] + h[1] + h[1] + h[2] + h[2] + h[3] + h[3];
  return '#' + h;
}

export function buildTokenMap(projectRoot: string): TokenMap {
  const colorsFile = path.resolve(projectRoot, 'src/shared/styles/tokens/colors.ts');
  const content = fs.readFileSync(colorsFile, 'utf-8');

  const colorToKey = new Map<string, string>();

  // Match only pure hex values (skip color-mix, rgba, etc.)
  // Pattern: 'key-name': '#hexvalue', or key: '#hexvalue',
  const re = /['"]?([\w-]+)['"]?\s*:\s*'(#[0-9a-fA-F]{3,8})'/g;
  let m: RegExpExecArray | null;

  while ((m = re.exec(content)) !== null) {
    const key = m[1];
    const hex = normalizeHex(m[2]);
    if (!colorToKey.has(hex)) {
      colorToKey.set(hex, key);
    }
  }

  return { colorToKey };
}

/** Build the token access expression string */
export function colorTokenExpr(tokenKey: string, context: 'template' | 'jsx'): string {
  // Keys with hyphens/underscores need bracket notation
  const needsBracket = /[-_]/.test(tokenKey) || /^\d/.test(tokenKey);
  const access = needsBracket
    ? `theme.colors['${tokenKey}']`
    : `theme.colors.${tokenKey}`;

  return context === 'template'
    ? `\${({ theme }) => ${access}}`
    : access;
}

export function normalizeHexPublic(hex: string): string {
  return normalizeHex(hex);
}
