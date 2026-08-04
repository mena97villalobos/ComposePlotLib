# Wiki source

These files are the source of truth for the
[ComposePlotLib GitHub wiki](https://github.com/mena97villalobos/ComposePlotLib/wiki).

They live in the main repository — not in the wiki — so wiki content is reviewable in pull requests,
versioned alongside the code it documents, and impossible to silently drift from it.
`.github/workflows/wiki-sync.yml` pushes this folder to the wiki repository on every change to
`main`.

> **This file is not published.** GitHub wikis have no concept of a folder README; `Home.md` is the
> landing page. `README.md` would appear as a stray "README" page, so the sync workflow copies it
> along with everything else — if that bothers you, delete this file. It is kept because the note
> above is worth having in the repo.

## Editing

Edit the files here and open a PR. **Do not edit pages in the wiki web UI** — the next sync
overwrites them.

## Layout

| File | Role |
|------|------|
| `Home.md` | Wiki landing page |
| `_Sidebar.md` | Navigation, rendered on every page |
| `_Footer.md` | Footer, rendered on every page |
| `<Page-Name>.md` | A page; the filename becomes the URL slug and the title |

## Link rules

Wiki pages are served from a different repository than the code, so relative paths do not resolve:

- **Another wiki page** — `[Installation](Installation)` (the filename without `.md`)
- **A repo file** — an absolute URL:
  `https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/api-and-state.md`
- **An image** — an absolute raw URL:
  `https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/line-chart.png`

A relative link such as `./docs/foo.md` will 404. Images will not render until the repository is
public.

## First-time setup

GitHub does not create the `.wiki.git` repository until the wiki has at least one page. Open the
repository's **Wiki** tab and save any page once — the first sync overwrites it. Until then the sync
job logs a warning and skips rather than failing the build.

## Scope

The wiki is an onboarding and navigation layer. Exhaustive reference material belongs in
[`docs/`](https://github.com/mena97villalobos/ComposePlotLib/tree/main/docs), which the wiki links
into. Adding a chart family means updating
`docs/chart-families-overview.md` **and** `wiki/Chart-Gallery.md`.
