# Migration from react-scripts to Vite - Summary

## Problem
- Dependabot was failing due to dependency conflicts with `react-scripts@5.0.1`
- `react-scripts` required `js-yaml@^4.1.0`, but security fixes required `js-yaml@3.14.2+`, creating an unresolvable conflict
- Error: `security_update_not_possible` for `js-yaml` dependency

## Solution
Migrated the frontend build system from `react-scripts` to **Vite** to resolve dependency conflicts and modernize the build tooling.

## Key Changes Made

### 1. Dependencies (`ui/package.json`)
**Removed:**
- `react-scripts@5.0.1`

**Added:**
- `vite@^5.1.0`
- `@vitejs/plugin-react@^4.2.1`
- `vite-plugin-svgr@^4.2.0` (for SVG React component imports)
- `vitest@^1.0.0` (replaced Jest)
- `jsdom@^23.0.0` (for Vitest test environment)
- ESLint plugins: `eslint`, `eslint-plugin-react`, `eslint-plugin-react-hooks`, `@typescript-eslint/eslint-plugin`, `@typescript-eslint/parser`

### 2. Configuration Files

#### `vite.config.mts` (created, renamed from `.ts` to `.mts` for ESM)
- React plugin with Emotion support
- SVGR plugin for SVG imports
- Path alias `@` → `./src`
- Build output: `build/` directory (same as before)
- Test configuration for Vitest

#### `index.html` (moved from `public/` to root)
- Removed `%PUBLIC_URL%` references (Vite uses `/` directly)
- Added script tag: `<script type="module" src="/src/index.tsx"></script>`

#### `tsconfig.json` (updated)
- Updated for Vite bundler mode
- Added `tsconfig.node.json` for Vite config file

#### `src/vite-env.d.ts` (created, replaced `react-app-env.d.ts`)
- Type definitions for Vite environment variables
- Asset import type definitions (SVG, PNG, etc.)

### 3. Environment Variables
**Changed:**
- `REACT_APP_SERVER_ADDRESS` → `VITE_SERVER_ADDRESS`
- `process.env.NODE_ENV` → `import.meta.env.DEV` (for development check)
- `process.env.REACT_APP_*` → `import.meta.env.VITE_*`

**Files updated:**
- `src/consts/env.ts`
- `src/api/starter.ts`

### 4. Import Paths
**Changed all bare imports to use `@` alias:**
- `api/starter` → `@/api/starter`
- `hooks/useApiCall` → `@/hooks/useApiCall`
- `consts/env` → `@/consts/env`
- `components/...` → `@/components/...`

**Files updated:**
- All component files
- All test files
- `src/api/starter.ts`

### 5. Asset Imports
**SVG imports:**
- Changed from `ReactComponent` import to: `import TapirLogo from '@/assets/tapir-hex.svg?react'`
- Added type definitions for `*.svg?react` imports

**Image imports:**
- Updated to use `@` alias: `@/assets/sml-logo-1024.png`

### 6. Test Setup
**Updated `src/setupTests.ts`:**
- Changed from `@testing-library/jest-dom/vitest` to `@testing-library/jest-dom` (v5.x doesn't have `/vitest` export)

**Updated test files:**
- Changed `jest.fn()` → `vi.fn()` from Vitest
- Changed `jest.Mock` → `ReturnType<typeof vi.fn>`
- Updated Material-UI Select queries: `button` → `combobox` role
- Added `waitFor` calls to wait for async component initialization
- Added `mockClear()` to reset fetch mocks between tests

### 7. Scripts (`package.json`)
**Updated:**
- `start` / `dev`: `react-scripts start` → `vite`
- `build`: `react-scripts build` → `tsc && vite build`
- `test`: `react-scripts test` → `vitest run`
- Added `test:watch`: `vitest`
- Added `preview`: `vite preview`

### 8. ESLint Configuration
**Updated `package.json` eslintConfig:**
- Removed `react-app` and `react-app/jest` presets
- Added: `eslint:recommended`, `plugin:react/recommended`, `plugin:react-hooks/recommended`, `plugin:@typescript-eslint/recommended`
- Added parser and plugins configuration

### 9. Code Quality Fixes
Fixed ESLint errors:
- Changed `String` type to `string` in `CommonSnackbar.component.tsx`
- Removed unused parameters (changed `_` to `()`)
- Changed `let` to `const` where appropriate
- Changed `any` types to proper types
- Removed unused `theme` parameter
- Fixed Prettier formatting in `vite-env.d.ts`

## Important Notes

### Environment Variables
- **Old format:** `REACT_APP_SERVER_ADDRESS`
- **New format:** `VITE_SERVER_ADDRESS`
- Update `.env` files and CI/CD pipelines accordingly
- Variables must be prefixed with `VITE_` to be exposed to client code

### Build Output
- Still outputs to `build/` directory (no CI/CD changes needed)
- Uses same structure as before

### Testing
- Tests now use Vitest instead of Jest
- Material-UI Select components use `combobox` role, not `button`
- Tests need to wait for async component initialization (use `waitFor`)

### SVG Imports
- Use `?react` suffix: `import Logo from '@/assets/logo.svg?react'`
- Regular SVG imports work as URLs: `import logoUrl from '@/assets/logo.svg'`

### File Structure
- `index.html` is now in root (not in `public/`)
- `public/` folder still contains static assets (favicon, images, manifest.json)
- `vite.config.mts` is in root (ESM module format)

## Migration Status
✅ All tests passing (141 tests)
✅ Linting passing
✅ Build successful
✅ Development server working
✅ Production build working

## Files Modified
- `ui/package.json`
- `ui/vite.config.mts` (created)
- `ui/index.html` (moved from `public/`)
- `ui/tsconfig.json`
- `ui/tsconfig.node.json` (created)
- `ui/src/vite-env.d.ts` (created)
- `ui/src/setupTests.ts`
- `ui/src/consts/env.ts`
- `ui/src/api/starter.ts`
- All component files (import path updates)
- All test files (Vitest migration)
- `ui/.gitignore` (added `/dist` and `.vite`)

## Files Removed
- `ui/public/index.html` (moved to root)
- `ui/src/react-app-env.d.ts` (replaced by `vite-env.d.ts`)

## Next Steps (if needed)
1. Update CI/CD pipelines to use `VITE_*` environment variables
2. Consider upgrading `@testing-library/jest-dom` to v6+ for better Vitest support
3. Consider migrating to Vitest's native test UI for better DX

## Common Issues & Solutions

### Issue: Tests can't find elements
**Solution:** Add `waitFor` to wait for async component initialization. Material-UI Select uses `combobox` role, not `button`.

### Issue: Environment variables not working
**Solution:** Ensure variables are prefixed with `VITE_` and accessed via `import.meta.env.VITE_*`

### Issue: SVG imports failing
**Solution:** Use `?react` suffix for React components, or use SVGR plugin configuration

### Issue: CJS build deprecation warning
**Solution:** Use `.mts` extension for Vite config and use ESM syntax (`import.meta.url` instead of `__dirname`)

