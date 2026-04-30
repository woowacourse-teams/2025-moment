import { useEffect, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import * as S from './index.styles';

interface Report {
  generatedAt: string;
  analyzedFiles: number;
  components: Record<string, { importPath: string; importCount: number; usedInFiles: string[] }>;
  hardcodedStyles: {
    total: number;
    byType: { hexColor: number; pxValue: number; tailwindArbitrary: number };
    byFile: Record<string, number>;
  };
  tokenUsage: { total: number; byCategory: Record<string, number> };
  adoptionRate: { tokenCount: number; hardcodedCount: number; tokenAdoptionPercent: number };
}

const COLORS = ['#F1C40F', '#60A5FA', '#4ADE80', '#F87171', '#A78BFA', '#FB923C', '#34D399'];
const TOOLTIP_STYLE = {
  backgroundColor: '#1E293B',
  border: '1px solid #374151',
  borderRadius: 8,
  color: '#fff',
  fontSize: 13,
};
const TICK_COLOR = '#9CA3AF';

function shortName(path: string) {
  return path.split('/').pop() ?? path;
}

function shortFile(path: string) {
  const parts = path.split('/');
  return parts.length > 2 ? `…/${parts.slice(-2).join('/')}` : path;
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function DesignAuditPage() {
  const [report, setReport] = useState<Report | null>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    fetch('/design-system-report/report.json')
      .then(res => {
        if (!res.ok) throw new Error('not found');
        return res.json() as Promise<Report>;
      })
      .then(setReport)
      .catch(() => setError(true));
  }, []);

  if (error) {
    return (
      <S.Page>
        <S.ErrorBox>
          <S.ErrorTitle>분석 데이터가 없습니다</S.ErrorTitle>
          <S.ErrorDesc>아래 명령어를 먼저 실행해주세요</S.ErrorDesc>
          <S.RunHint>pnpm run analyze:design</S.RunHint>
        </S.ErrorBox>
      </S.Page>
    );
  }

  if (!report) {
    return (
      <S.Page>
        <S.LoadingBox>데이터 로딩 중...</S.LoadingBox>
      </S.Page>
    );
  }

  const {
    adoptionRate: ar,
    hardcodedStyles: h,
    tokenUsage: t,
    components,
    analyzedFiles,
    generatedAt,
  } = report;

  const adoptionPieData = [
    { name: '토큰 사용', value: ar.tokenCount },
    { name: '하드코딩', value: ar.hardcodedCount },
  ];

  const hardcodedTypePieData = [
    { name: 'px 값', value: h.byType.pxValue },
    { name: 'hex 색상', value: h.byType.hexColor },
    { name: 'Tailwind 임의값', value: h.byType.tailwindArbitrary },
  ].filter(d => d.value > 0);

  const componentBarData = Object.entries(components)
    .slice(0, 8)
    .map(([key, val]) => ({ name: shortName(key), count: val.importCount }));

  const topFilesData = Object.entries(h.byFile)
    .slice(0, 10)
    .map(([file, count]) => ({ name: shortFile(file), count }))
    .reverse();

  const tokenCategoryData = Object.entries(t.byCategory)
    .sort(([, a], [, b]) => b - a)
    .map(([key, val]) => ({ name: `theme.${key}`, count: val }));

  const featureAreaData = (() => {
    const map: Record<string, number> = {};
    for (const [file, count] of Object.entries(h.byFile)) {
      const parts = file.split('/');
      const area = parts[0] === 'src' ? parts[1] : parts[0];
      map[area] = (map[area] ?? 0) + count;
    }
    return Object.entries(map)
      .sort(([, a], [, b]) => b - a)
      .map(([name, value]) => ({ name, value }));
  })();

  return (
    <S.Page>
      <S.Header>
        <S.Title>디자인 시스템 감사 리포트</S.Title>
        <S.Subtitle>
          {analyzedFiles}개 파일 분석 완료 · 생성: {formatDate(generatedAt)}
        </S.Subtitle>
      </S.Header>

      <S.StatGrid>
        <S.StatCard>
          <S.StatLabel>분석 파일 수</S.StatLabel>
          <S.StatValue>
            {analyzedFiles}
            <S.StatUnit>개</S.StatUnit>
          </S.StatValue>
        </S.StatCard>
        <S.StatCard>
          <S.StatLabel>토큰 채택률</S.StatLabel>
          <S.StatValue $accent>
            {ar.tokenAdoptionPercent}
            <S.StatUnit>%</S.StatUnit>
          </S.StatValue>
        </S.StatCard>
        <S.StatCard>
          <S.StatLabel>하드코딩 스타일 값</S.StatLabel>
          <S.StatValue>
            {h.total.toLocaleString()}
            <S.StatUnit>개</S.StatUnit>
          </S.StatValue>
        </S.StatCard>
        <S.StatCard>
          <S.StatLabel>공통 컴포넌트 종류</S.StatLabel>
          <S.StatValue>
            {Object.keys(components).length}
            <S.StatUnit>종</S.StatUnit>
          </S.StatValue>
        </S.StatCard>
      </S.StatGrid>

      <S.ChartGrid $cols={3}>
        <S.ChartCard>
          <S.ChartTitle>토큰 채택률</S.ChartTitle>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={adoptionPieData}
                cx="50%"
                cy="50%"
                innerRadius={50}
                outerRadius={80}
                paddingAngle={3}
                dataKey="value"
              >
                <Cell fill="#F1C40F" />
                <Cell fill="#374151" />
              </Pie>
              <Tooltip contentStyle={TOOLTIP_STYLE} />
              <Legend
                formatter={val => <span style={{ color: '#D1D5DB', fontSize: 12 }}>{val}</span>}
              />
            </PieChart>
          </ResponsiveContainer>
        </S.ChartCard>

        <S.ChartCard>
          <S.ChartTitle>하드코딩 유형별 분포</S.ChartTitle>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={hardcodedTypePieData}
                cx="50%"
                cy="50%"
                innerRadius={50}
                outerRadius={80}
                paddingAngle={3}
                dataKey="value"
              >
                {hardcodedTypePieData.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip contentStyle={TOOLTIP_STYLE} />
              <Legend
                formatter={val => <span style={{ color: '#D1D5DB', fontSize: 12 }}>{val}</span>}
              />
            </PieChart>
          </ResponsiveContainer>
        </S.ChartCard>

        <S.ChartCard>
          <S.ChartTitle>공통 컴포넌트 사용 횟수</S.ChartTitle>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={componentBarData} layout="vertical" margin={{ left: 8, right: 20 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" horizontal={false} />
              <XAxis type="number" tick={{ fill: TICK_COLOR, fontSize: 11 }} />
              <YAxis
                type="category"
                dataKey="name"
                tick={{ fill: TICK_COLOR, fontSize: 11 }}
                width={68}
              />
              <Tooltip contentStyle={TOOLTIP_STYLE} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
              <Bar dataKey="count" name="사용 횟수" fill="#F1C40F" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </S.ChartCard>
      </S.ChartGrid>

      <S.ChartGrid $cols={3}>
        <S.ChartCard>
          <S.ChartTitle>토큰 카테고리별 사용 횟수</S.ChartTitle>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={tokenCategoryData} layout="vertical" margin={{ left: 8, right: 20 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" horizontal={false} />
              <XAxis type="number" tick={{ fill: TICK_COLOR, fontSize: 11 }} />
              <YAxis
                type="category"
                dataKey="name"
                tick={{ fill: TICK_COLOR, fontSize: 10 }}
                width={90}
              />
              <Tooltip contentStyle={TOOLTIP_STYLE} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
              <Bar dataKey="count" name="사용 횟수" radius={[0, 4, 4, 0]}>
                {tokenCategoryData.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </S.ChartCard>

        <S.ChartCard>
          <S.ChartTitle>하드코딩 집중 파일 TOP 10</S.ChartTitle>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={topFilesData} layout="vertical" margin={{ left: 8, right: 20 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" horizontal={false} />
              <XAxis type="number" tick={{ fill: TICK_COLOR, fontSize: 11 }} />
              <YAxis
                type="category"
                dataKey="name"
                tick={{ fill: TICK_COLOR, fontSize: 10 }}
                width={140}
              />
              <Tooltip contentStyle={TOOLTIP_STYLE} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
              <Bar dataKey="count" name="하드코딩 개수" fill="#F87171" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </S.ChartCard>

        <S.ChartCard>
          <S.ChartTitle>피처 영역별 하드코딩 분포</S.ChartTitle>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={featureAreaData} margin={{ left: 8, right: 16 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" vertical={false} />
              <XAxis dataKey="name" tick={{ fill: TICK_COLOR, fontSize: 11 }} />
              <YAxis tick={{ fill: TICK_COLOR, fontSize: 11 }} />
              <Tooltip contentStyle={TOOLTIP_STYLE} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
              <Bar dataKey="value" name="하드코딩 개수" radius={[4, 4, 0, 0]}>
                {featureAreaData.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </S.ChartCard>
      </S.ChartGrid>
    </S.Page>
  );
}
