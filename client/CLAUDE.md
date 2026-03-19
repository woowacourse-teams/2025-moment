# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Development
pnpm dev                    # Start dev server
pnpm build                  # Production build
pnpm dev:analyze            # Dev with bundle analyzer

# Code Quality
pnpm lint                   # ESLint check
pnpm lint:fix               # ESLint auto-fix
pnpm format                 # Prettier format
pnpm check                  # lint + format:check

# Testing
pnpm test                   # Run Jest tests
pnpm test:watch             # Jest watch mode
pnpm test:coverage          # Jest with coverage
pnpm cypress:open           # Cypress E2E (interactive)
pnpm cypress:run            # Cypress E2E (headless)

# Storybook
pnpm storybook              # Start on port 6006
```

## Architecture

**React 19 + TypeScript + Webpack** application using feature-based architecture.

```
src/
├── app/           # App setup: routes, layout, api config, queryClient
├── features/      # Feature modules (auth, moment, group, comment, notification, my, complaint)
├── pages/         # Page components matching routes
├── shared/
│   ├── design-system/  # Button, Card, Input, Modal, TextArea
│   ├── store/          # Custom store using useSyncExternalStore
│   ├── hooks/          # useToast, useToggle, useFileUpload, useFunnel
│   ├── styles/         # Theme tokens (colors, typography, spacing)
│   └── ui/             # ErrorBoundary, FileUpload, Toast, Skeleton
└── widgets/       # Reusable widget components
```

## Key Patterns

**State Management**: React Query for server state, custom `createStore<T>()` for client state (`shared/store/core.ts`)

**API**: Axios with auto token refresh interceptor (`app/lib/api.ts`). Features expose hooks like `useLoginMutation`, `useMomentsQuery`.

**Styling**: Emotion CSS-in-JS with theme tokens. Component styles in `*.styles.ts` files.

**Path Alias**: `@/` maps to `src/`

## Git Conventions

- **Commit messages**: Korean, conventional commits (`feat:`, `fix:`, `refactor:`, `chore:`, `docs:`, `style:`, `test:`)
- **Atomic commits**: Split by responsibility (design-system / API / UI changes)
- **No Co-Authored-By**: Do not include co-author lines
- **Branch naming**: `feat/#<issue>`, `fix/#<issue>`
