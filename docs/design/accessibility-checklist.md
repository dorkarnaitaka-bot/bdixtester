# BDIX Tester — WCAG 2.2 AA & Android Accessibility Checklist (Phase 1)

## 1. Color Contrast Audit (WCAG 2.2 SC 1.4.3 & SC 1.4.11)

| Check | Current State | Target State | Verification Status |
|---|---|---|---|
| Light Start CTA (`#FFFFFF` on `#0369A1 → #1D4ED8`) | **FAIL** (2.43:1 at `#06B6D4`) | **5.93:1 – 6.70:1** | Verified (Math) — Pending Code Fix (P0) |
| Dark Start CTA (`#041E2D` on `#22D3EE → #38BDF8 → #3B82F6`) | **FAIL** (1.81:1 with White) | **4.65:1 – 9.46:1** | Verified (Math) — Pending Code Fix (P0) |
| Light & Dark Stop CTA (`#FFFFFF` on `#C81E1E/#B91C1C → #991B1B`) | **FAIL** Dark (2.77:1 on `#F87171`) | **5.74:1 – 8.31:1** | Verified (Math) — Pending Code Fix (P0) |
| Light Primary on `SurfaceContainer` / Nav Pill | **FAIL** (4.28:1) | **5.45:1 / 5.26:1** (`#03669C`) | Verified (Math) — Pending Code Fix (P0) |
| Light Reachable Badge (`#047857` on `#D1FAE5`) | **FAIL** (3.32:1) | **4.84:1** | Verified (Math) — Pending Code Fix (P0) |
| Light Restricted Badge (`#B45309` on `#FEF3C7`) | **FAIL** (2.86:1) | **4.51:1** | Verified (Math) — Pending Code Fix (P0) |
| Light Unreachable Badge (`#C81E1E` on `#FEE2E2`) | **FAIL** (3.95:1) | **4.70:1** | Verified (Math) — Pending Code Fix (P0) |
| Light Settings IP Label (`#035E91` on `#EBF1F7`) | **FAIL** (4.28:1) | **6.12:1** | Verified (Math) — Pending Code Fix (P0) |
| Light Settings Footer App ID (full `onSurfaceVariant`) | **FAIL** (3.47:1 at 0.72 alpha) | **6.68:1** | Verified (Math) — Pending Code Fix (P0) |
| System Status & Navigation Bar Icons on Manual Theme Switch | **FAIL** (stuck to OS theme) | Dynamic `SystemBarStyle` | Verified (Code) — Pending Code Fix (P0) |

---

## 2. Touch Target Size (WCAG 2.2 SC 2.5.8 & Android 48×48dp Guideline)

| Interactive Component | File | Current Size | Status |
|---|---|---|---|
| Home `Start Test` / `Stop` CTA | `DashboardScreen.kt` | `minHeight = 58.dp`, full width | **Verified PASS** |
| Home `VIEW` button | `DashboardScreen.kt` | `minHeight = 48.dp` + `minimumInteractiveComponentSize()` | **Verified PASS** |
| Servers Status Tabs | `ServersScreen.kt` | `minHeight = 52.dp` | **Verified PASS** |
| Servers BDIX Category Chips | `ServersScreen.kt` | `minHeight = 48.dp` | **Verified PASS** |
| Servers OTT Region Chips | `ServersScreen.kt:417` | `minHeight = 40.dp` | **Verified FAIL → Fix to 48dp (P1)** |
| Card URL Footer (`RichCardUrlFooter`) | `ServersScreen.kt`, `ServerCard.kt`, `FavoritesScreen.kt` | `minHeight = 48.dp` | **Verified PASS** |
| Bookmark / Pin / Delete IconButtons | All list screens | `minimumInteractiveComponentSize()` (48dp) | **Verified PASS** |
| Settings Navigation / Switch / Dialog Rows | `SettingsComponents.kt` | `52–64.dp` min height | **Verified PASS** |

---

## 3. Semantics, TalkBack & Screen Reader Support

| Requirement | Current Implementation | Gap / Action |
|---|---|---|
| Meaningful `contentDescription` on interactive icons | Present on TopBar actions, Bookmark, Pin, Delete, Nav items | Verify new Sort selector, Copy URL action, and Diagnostics sheet close/trigger icons all receive localized EN/BN descriptions. |
| Decorative icons marked `contentDescription = null` | Consistently applied on category/leading/chevron icons | Maintain across all refactored unified cards. |
| Merged semantics on Hero Ring metrics | `DashboardScreen.kt:994` uses `semantics(mergeDescendants = true)` + `LiveRegionMode.Polite` | Keep intact; avoid duplicate announcement on live progress ticks. |
| Non-color active state cues | 4 of 5 Nav tabs share identical filled icons | **Fix (P1):** Pair Filled + Outlined icons on all 5 destinations. |
| LTR isolation for URLs, IPs, Ping, Timestamps | `LtrContent { ... }` wraps URLs, IPs, numeric counters | Maintain across all unified cards & new Sync timestamp / Scan summary captions. |
| Physical TalkBack audio traversal | Headless JVM environment cannot run live TalkBack | **Unverified** — Documented explicitly as requiring manual device verification. |

---

## 4. Typography, Bengali Script & Dynamic Font Scaling (100%–200%)

| Requirement | Current State | Action |
|---|---|---|
| Bengali `letterSpacing = 0.sp` | **FAIL** — inherits non-zero English tracking | **Fix (P0):** Force `0.sp` across all `BdixTypographyBengali` styles. |
| Bengali `labelSmall` font family | **FAIL** — hardcoded to `JetBrainsMonoFamily` | **Fix (P0):** Use `bodyFontFamily` for `labelSmall`; isolate `MonoBadgeStyle` for ASCII metrics. |
| Real font weight hierarchy | **FAIL** — static 400 TTFs bound to `FontVariation` | **Fix (P0):** Supply real weights or bind `FontWeight.Normal` only so synthetic bold works. |
| Font scale 100%, 130%, 150%, 200% | Mostly uses `heightIn(min = ...)` rather than fixed `height(...)` | Audit all cards, top bars, and Hero Ring core at `fontScale = 1.5f` and `2.0f` in Robolectric UI tests to verify zero clipping/overlap. |

---

## 5. Reduced Motion (WCAG 2.2 SC 2.2.2 & SC 2.3.3)

| Requirement | Current State | Target State |
|---|---|---|
| Respect system `ANIMATOR_DURATION_SCALE == 0f` | Not checked | Read safely via `Settings.Global.getFloat(..., 1f)` (no permission needed) in `rememberReducedMotionEnabled`. |
| In-app `Reduce Animations` setting | Not implemented | Add DataStore boolean toggle in Settings (`Appearance & Language`). |
| Calm idle state (no continuous decorative motion) | **FAIL** — 9 concurrent infinite animations on idle Home | **Fix (P1):** Zero sonar/orbital/radar-spin/shimmer animations while idle; stop immediately when scan completes or reduced motion is enabled. |
| Low-end hardware GPU frame profiling | Cannot run Perfetto/FrameMetrics on headless JVM | **Unverified** — Documented explicitly. |
