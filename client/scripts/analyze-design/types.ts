export interface ComponentUsage {
  importPath: string;
  importCount: number;
  usedInFiles: string[];
}

export type HardcodedType = 'hexColor' | 'pxValue' | 'tailwindArbitrary';

export interface HardcodedDetail {
  file: string;
  type: HardcodedType;
  value: string;
  line: number;
}

export interface ComponentAnalysisResult {
  components: Record<string, ComponentUsage>;
}

export interface StyleAnalysisResult {
  details: HardcodedDetail[];
}

export interface TokenAnalysisResult {
  total: number;
  byCategory: Record<string, number>;
}

export interface Report {
  generatedAt: string;
  analyzedFiles: number;
  components: Record<string, ComponentUsage>;
  hardcodedStyles: {
    total: number;
    byType: Record<HardcodedType, number>;
    byFile: Record<string, number>;
    details: HardcodedDetail[];
  };
  tokenUsage: {
    total: number;
    byCategory: Record<string, number>;
  };
  adoptionRate: {
    tokenCount: number;
    hardcodedCount: number;
    tokenAdoptionPercent: number;
  };
}
