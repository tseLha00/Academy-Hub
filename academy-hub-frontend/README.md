# Academy Hub Frontend

React + TypeScript single-page app, built with [Vite](https://vite.dev).

## Requirements

- Node.js 20.19+ (22 or 24 LTS recommended)
- The backend running on `http://localhost:8080` (see the root README)

## Getting started

```bash
cd academy-hub-frontend
npm install
npm run dev
```

The app runs on http://localhost:5173.

## Talking to the backend

During development, Vite proxies every request starting with `/api` to `http://localhost:8080`
(see `vite.config.ts`). The frontend therefore calls relative URLs like `/api/trainings`, and the
backend's session cookie and `XSRF-TOKEN` cookie work without any CORS configuration.

For `POST`/`PUT`/`PATCH`/`DELETE` requests, send `credentials: 'include'` and copy the value of the
`XSRF-TOKEN` cookie into the `X-XSRF-TOKEN` header (Spring Security CSRF protection).

## Scripts

| Command                | Description                          |
| ---------------------- | ------------------------------------ |
| `npm run dev`          | Start the dev server with hot reload |
| `npm run build`        | Type-check and build to `dist/`      |
| `npm run preview`      | Serve the production build locally   |
| `npm test`             | Run unit tests once (Vitest)         |
| `npm run test:watch`   | Run tests in watch mode              |
| `npm run lint`         | Lint with oxlint                     |
| `npm run format`       | Format all files with Prettier       |
| `npm run format:check` | Check formatting (e.g. in CI)        |

## Tech stack

- **React Router** for page navigation
- **i18next / react-i18next** for German and English (default: German)
- **lucide-react** for icons (matches the outline icons in the mockup)
- **Vitest + Testing Library** for tests, **oxlint** for linting, **Prettier** for formatting

## Project structure

Components follow [atomic design](https://atomicdesign.bradfrost.com/chapter-2/):

```
src/
  components/
    atoms/       smallest building blocks: button, input, checkbox, avatar ...
    molecules/   groups of atoms: form field (label + input), search field ...
    organisms/   page sections: top bar, sidebar, form cards ...
    templates/   page frames, e.g. layout with top bar + sidebar
    images/      images used by components, e.g. Logo.jpg
  pages/         one component per route, fills a template with organisms
  i18n/          language setup and texts (locales/de.ts, locales/en.ts)
  test/          test setup
  index.css      design tokens (colors, radius) and base styles
  App.tsx        route definitions: add each new page as a child route of AppLayout
  main.tsx       entry point
```

## Conventions

- No `any` in TypeScript (enforced by `npm run lint`) and no `!important` in CSS.
- Styles: one CSS file per atomic level (e.g. `components/atoms/atoms.css`), using the
  variables from `src/index.css`.
- All visible text goes through `t('...')`. Add every new text to both `de.ts` and `en.ts`;
  `npm run build` fails if a key is missing in English.
- To switch the language in code: `i18n.changeLanguage('en')`. The choice is saved in the browser.
