# Performance Optimization

## Model Selection Strategy

> 공식 문서 기준: https://platform.claude.com/docs/en/about-claude/models/overview (2026-02-06 확인)

### Current Models (최신 모델)

**Opus 4.6** — 최신 플래그십, 가장 지능적인 모델 (`claude-opus-4-6`):
- 복잡한 아키텍처 설계 및 추론이 필요한 작업
- 대규모 코드베이스 에이전틱 작업 (SWE-bench 80.8%)
- 장문 컨텍스트 분석 및 리서치 (1M 토큰 beta 지원)
- 최대 출력: 128K 토큰 / 컨텍스트: 200K (1M beta)
- Adaptive Thinking + Extended Thinking 지원
- 가격: $5/$25 per MTok / 지식 기준: May 2025

**Sonnet 4.5** — 속도와 지능의 최적 균형 (`claude-sonnet-4-5`):
- 메인 개발 작업, 코딩 태스크
- 멀티 에이전트 워크플로우 오케스트레이션
- 빠른 응답이 필요한 복잡한 코딩 작업
- 최대 출력: 64K 토큰 / 컨텍스트: 200K (1M beta)
- Extended Thinking 지원
- 가격: $3/$15 per MTok / 지식 기준: Jan 2025

**Haiku 4.5** — 가장 빠른 모델, 근접-프론티어 지능 (`claude-haiku-4-5`):
- 경량 에이전트, 빈번한 호출이 필요한 작업
- 페어 프로그래밍 및 코드 생성 (Worker 에이전트)
- 고볼륨, 비용 효율 우선 작업
- 최대 출력: 64K 토큰 / 컨텍스트: 200K
- Extended Thinking 지원
- 가격: $1/$5 per MTok / 지식 기준: Feb 2025

### 에이전트 모델 매핑

에이전트 frontmatter의 `model:` 필드는 tier alias로 최신 모델에 자동 매핑됨:
- `model: opus` → Claude Opus 4.6
- `model: sonnet` → Claude Sonnet 4.5
- `model: haiku` → Claude Haiku 4.5

### Legacy Models (참고)

- **Opus 4.5** ($5/$25): Opus 4.6 출시로 레거시 전환. 200K 컨텍스트, 64K 출력.
- **Opus 4.1** ($15/$75): 이전 세대. Opus 4.5/4.6 대비 3배 비쌈.
- **Sonnet 4** ($3/$15): Sonnet 4.5로 마이그레이션 권장.

## Context Window Management

Avoid last 20% of context window for:
- Large-scale refactoring
- Feature implementation spanning multiple files
- Debugging complex interactions

Lower context sensitivity tasks:
- Single-file edits
- Independent utility creation
- Documentation updates
- Simple bug fixes

## Ultrathink + Plan Mode

For complex tasks requiring deep reasoning:
1. Use `ultrathink` for enhanced thinking
2. Enable **Plan Mode** for structured approach
3. "Rev the engine" with multiple critique rounds
4. Use split role sub-agents for diverse analysis

## Build Troubleshooting

If build fails:
1. Use **build-error-resolver** agent
2. Analyze error messages
3. Fix incrementally
4. Verify after each fix
