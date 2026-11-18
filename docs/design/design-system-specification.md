# BDIX Tester — Design System Specification (Phase 1)

**Design Direction:** *Precision Diagnostic Minimalism — Material Design 3*  
**Brand Identity:** Preserved (Ocean Cyan-Sky Diagnostic Palette · Crisp Alabaster Light · Deep Ocean Slate Dark)  
**Dynamic Color:** `false` by default (preserves deterministic WCAG AA diagnostic contrast).

---

## 1. Color System & Verified WCAG 2.2 AA Contrast Matrix

### 1.1 Light Theme — *Crisp Alabaster* (`ui/theme/Color.kt`, `ui/theme/Theme.kt`)

| Role / Token | Current HEX | Target HEX | Foreground / Background Pair Verified | Calculated Ratio | WCAG 2.2 AA |
|---|---:|---:|---|---:|---|
| `LightBackground` | `#F4F7FA` | `#F4F7FA` | `#0B1521` on `#F4F7FA` | **17.07:1** | PASS AAA |
| `LightSurfaceContainerLow` | `#FFFFFF` | `#FFFFFF` | `#0B1521` on `#FFFFFF` | **18.28:1** | PASS AAA |
| `LightSurfaceContainer` | `#EBF1F7` | `#EBF1F7` | `#475A6E` on `#EBF1F7` | **6.25:1** | PASS AA |
| `CyanPrimaryLight` (`primary`) | `#0277B5` | **`#03669C`** | `#03669C` on `#EBF1F7` / `#DCEFFD` (nav pill) / `#FFFFFF` | **5.45 / 5.26 / 6.20:1** | PASS AA |
| `LightOnSurface` | `#0B1521` | `#0B1521` | `#0B1521` on `#EBF1F7` | **16.10:1** | PASS AAA |
| `LightOnSurfaceVariant` | `#475A6E` | `#475A6E` | `#475A6E` on `#FFFFFF` / `#F4F7FA` | **7.11 / 6.68:1** | PASS AA |
| `LightSemanticColors.reachable` | `#059669` | **`#047857`** | `#047857` on `#D1FAE5` / `#FFFFFF` | **4.84 / 5.48:1** | PASS AA |
| `LightSemanticColors.reachableContainer` | `#D1FAE5` | `#D1FAE5` | — | — | Container |
| `LightSemanticColors.restricted` | `#D97706` | **`#B45309`** | `#B45309` on `#FEF3C7` / `#FFFFFF` | **4.51 / 5.02:1** | PASS AA |
| `LightSemanticColors.restrictedContainer` | `#FEF3C7` | `#FEF3C7` | — | — | Container |
| `LightSemanticColors.unreachable` / `ErrorLight` | `#DC2626` | **`#C81E1E`** | `#C81E1E` on `#FEE2E2` / `#FFFFFF` | **4.70 / 5.74:1** | PASS AA |
| `LightSemanticColors.unreachableContainer` | `#FEE2E2` | `#FEE2E2` | — | — | Container |
| `LightSemanticColors.ipAccent` (NEW) | `#0277B5` | **`#035E91`** | `#035E91` on `#EBF1F7` | **6.12:1** | PASS AA |
| `LightSemanticColors.ctaGradientStart` (NEW) | `#06B6D4` | **`#0369A1`** | `#FFFFFF` on `#0369A1` | **5.93:1** | PASS AA |
| `LightSemanticColors.ctaGradientEnd` (NEW) | `#2563EB` | **`#1D4ED8`** | `#FFFFFF` on `#1D4ED8` | **6.70:1** | PASS AA |
| `LightSemanticColors.ctaContent` (NEW) | hardcoded White | **`#FFFFFF`** | Across full `#0369A1 → #1D4ED8` gradient | **≥ 5.93:1** | PASS AA |
| `LightSemanticColors.stopGradientStart/End` (NEW) | `#DC2626` | **`#C81E1E → #991B1B`** | `#FFFFFF` on `#C81E1E` / `#991B1B` | **5.74 – 8.31:1** | PASS AA |

### 1.2 Dark Theme — *Deep Ocean Slate* (`ui/theme/Color.kt`, `ui/theme/Theme.kt`)

| Role / Token | Current HEX | Target HEX | Foreground / Background Pair Verified | Calculated Ratio | WCAG 2.2 AA |
|---|---:|---:|---|---:|---|
| `DarkBackground` | `#081018` | `#081018` | `#F1F5F9` on `#081018` | **17.44:1** | PASS AAA |
| `DarkSurfaceContainerLow` | `#0D1722` | `#0D1722` | `#F1F5F9` on `#0D1722` | **16.29:1** | PASS AAA |
| `DarkSurfaceContainer` | `#12202E` | `#12202E` | `#9FB0C4` on `#12202E` | **7.46:1** | PASS AAA |
| `CyanPrimaryDark` (`primary`) | `#38BDF8` | `#38BDF8` | `#38BDF8` on `#12202E` / `#0E334C` (nav pill) | **7.71 / 6.14:1** | PASS AA |
| `DarkSemanticColors.reachable` | `#34D399` | `#34D399` | `#34D399` on `#064E3B` | **5.06:1** | PASS AA |
| `DarkSemanticColors.restricted` | `#FBBF24` | `#FBBF24` | `#FBBF24` on `#5C3409` | **6.45:1** | PASS AA |
| `DarkSemanticColors.unreachable` | `#F87171` | `#F87171` | `#F87171` on `#5C1919` | **4.72:1** | PASS AA |
| `DarkSemanticColors.ctaGradientStart` (NEW) | `#22D3EE` | `#22D3EE` | `#041E2D` on `#22D3EE` | **9.46:1** | PASS AAA |
| `DarkSemanticColors.ctaGradientMid` (NEW) | `#38BDF8` | `#38BDF8` | `#041E2D` on `#38BDF8` | **7.98:1** | PASS AAA |
| `DarkSemanticColors.ctaGradientEnd` (NEW) | `#3B82F6` | `#3B82F6` | `#041E2D` on `#3B82F6` | **4.65:1** | PASS AA |
| `DarkSemanticColors.ctaContent` (NEW) | hardcoded White (1.81:1!) | **`#041E2D`** (`NavyDeep`) | Across full `#22D3EE → #38BDF8 → #3B82F6` | **≥ 4.65:1** | PASS AA |
| `DarkSemanticColors.stopGradientStart/End` (NEW) | `#F87171 → #DC2626` | **`#B91C1C → #991B1B`** | `#FFFFFF` on `#B91C1C` / `#991B1B` | **6.47 – 8.31:1** | PASS AA |

### 1.3 Alpha Compositing & Non-Color Redundancy Rules
- Never lower body/caption text opacity below `1.0f` when it drops composited contrast below `4.5:1` (fixes `SettingsVersionFooter` `APPLICATION_ID` from `0.72f` alpha = `3.47:1` to full `onSurfaceVariant` = `6.68:1`).
- Every status indicator pairs **semantic color + distinct icon + localized text label** (`CheckCircle` / `Warning` or `Lock` / `Cancel` or `ErrorOutline`).

---

## 2. Typography System (`ui/theme/Type.kt`)

### 2.1 Font Pairing (Unchanged)
- **English UI:** `Plus Jakarta Sans` (`res/font/plus_jakarta_sans*.ttf`)
- **Bengali UI:** `Noto Sans Bengali` (`res/font/noto_sans_bengali*.ttf`)
- **Numeric / URL / IP / Ping:** `JetBrains Mono` (`res/font/jetbrains_mono*.ttf`)

### 2.2 Font Weight Resolution Strategy (Fixes Verified Static-TTF Bug)
- **Audit Finding:** Existing `.ttf` files are static `usWeightClass = 400` (`variable = False`).
- **Implementation Plan:**
  - Verify in Phase 2 whether multi-weight TTFs (`400/500/600/700`) can be installed for the exact same 3 families via `font-util` (without changing families).
  - If separate weights are available, map each `FontWeight` to its true `.ttf` resource.
  - Otherwise, declare only `Font(resId = ..., weight = FontWeight.Normal)` inside each `FontFamily` (removing no-op `FontVariation.Settings`) so Android's font engine renders true visual weight hierarchy via synthetic emboldening instead of forcing 400 Regular everywhere.

### 2.3 Type Scale & Bengali Script Rules

| Style Role | Size | Weight | English LH | Bengali LH | English Tracking | Bengali Tracking | Font Family |
|---|---:|---|---:|---:|---:|---:|---|
| `MonoHeroNumberStyle` | `44.sp` | Bold | `52.sp` | `52.sp` | `-1.0.sp` | `-1.0.sp` (ASCII digits) | `JetBrainsMono` (`tnum`) |
| `headlineMedium` | `24.sp` | Bold | `32.sp` | `36.sp` | `-0.2.sp` | **`0.sp`** | Body Family |
| `headlineSmall` | `22.sp` | SemiBold | `28.sp` | `32.sp` | `-0.1.sp` | **`0.sp`** | Body Family |
| `titleLarge` | `20.sp` | SemiBold | `26.sp` | `30.sp` | `0.sp` | **`0.sp`** | Body Family |
| `titleMedium` | `16.sp` | SemiBold | `22.sp` | `24.sp` | `0.1.sp` | **`0.sp`** | Body Family |
| `titleSmall` | `14.sp` | SemiBold | `20.sp` | `22.sp` | `0.1.sp` | **`0.sp`** | Body Family |
| `bodyLarge` | `15.sp` | Normal | `22.sp` | `24.sp` | `0.sp` | **`0.sp`** | Body Family |
| `bodyMedium` | `14.sp` | Normal | `20.sp` | `22.sp` | `0.sp` | **`0.sp`** | Body Family |
| `bodySmall` | `12.5.sp` | Normal | `18.sp` | `20.sp` | `0.sp` | **`0.sp`** | Body Family |
| `labelLarge` | `14.sp` | SemiBold | `20.sp` | `22.sp` | `0.1.sp` | **`0.sp`** | Body Family |
| `labelMedium` | `12.sp` | Medium | `16.sp` | `18.sp` | `0.15.sp` | **`0.sp`** | Body Family |
| `labelSmall` | `11.sp` | SemiBold | `16.sp` | `17.sp` | `0.15.sp` | **`0.sp`** | **Body Family** (NO Mono!) |
| `MonoUrlStyle` | `12.5.sp` | Normal | `18.sp` | `18.sp` | `0.sp` | `0.sp` | `JetBrainsMono` (LTR) |
| `MonoBadgeStyle` (NEW) | `11.sp` | SemiBold | `16.sp` | `16.sp` | `0.sp` | `0.sp` | `JetBrainsMono` (`tnum`, LTR) |

---

## 3. Iconography System (`navigation/BdixNavRoutes.kt`)

| Destination | Selected (`Icons.Rounded.*`) | Unselected (`Icons.Outlined.*`) | Dependency Required |
|---|---|---|---|
| Home | `Icons.Rounded.Home` | `Icons.Outlined.Home` | Existing `material-icons-extended` |
| Servers | `Icons.Rounded.Dns` | `Icons.Outlined.Dns` | Existing `material-icons-extended` |
| Saved | `Icons.Rounded.Bookmark` | `Icons.Outlined.BookmarkBorder` | Existing `material-icons-extended` |
| History | `Icons.Rounded.History` | `Icons.Outlined.Schedule` (or `Icons.Outlined.History`) | Existing `material-icons-extended` |
| Settings | `Icons.Rounded.Settings` | `Icons.Outlined.Settings` | Existing `material-icons-extended` |

*Note:* Verify exact visual distinctness between `Icons.Rounded.History` and `Icons.Outlined.History` vs `Icons.Outlined.Schedule`; paired with bold label + tonal pill indicator so active state never relies on color alone.

---

## 4. Spacing, Shape & Elevation Tokens (`ui/theme/Shape.kt`)

| Token Group | Token Name | Value | Usage |
|---|---|---|---|
| **Spacing** | `BdixSpacing.xs / sm / md / lg / xl / xxl` | `4 / 8 / 12 / 16 / 24 / 32.dp` | Consistent 4/8dp grid across all screens |
| **Shapes** | `BdixCardShapes.chip` | `RoundedCornerShape(8.dp)` | Category/region/latency badges |
| | `BdixCardShapes.iconSquircle` | `RoundedCornerShape(12.dp)` | 44dp card icon badge & 36dp settings icon |
| | `BdixCardShapes.input` | `RoundedCornerShape(14.dp)` | Search fields & modal text inputs |
| | `BdixCardShapes.richCard` | `RoundedCornerShape(20.dp)` | Unified Servers, OTT, Saved, History & Settings groups |
| | `BdixCardShapes.profileHeader` | `RoundedCornerShape(24.dp)` | Settings App Profile Header Card |
| | `BdixCardShapes.pill` | `RoundedCornerShape(50)` | Primary CTA, Open ↗ pill, IP pill |
| **Elevation** | `BdixElevation.flat / bar / card / modal` | `0 / 2 / 2 / 6.dp` | Cards always pair `2.dp` elevation + `1.dp` `outlineVariant` border |

---

## 5. Unified Card & Motion Architecture

### 5.1 Shared URL Card Components (`ui/components/RichServerCardComponents.kt`)
- `BdixCategoryIconBadge(icon, contentDescription = null, size = 44.dp)`
- `RichCardUrlFooter(url, urlA11y, testTag, onClick, onLongClick = null)` — `surfaceContainer` strip, `minHeight = 48.dp`, LTR `MonoUrlStyle` URL + `Open ↗` pill.
- Used consistently across: `ServerListCard` (`Servers`), `OttPlatformCard` (`OTT`), `ServerCard` (`Saved`), and `HistoryRowCard` (`History`).

### 5.2 Motion & Reduced Motion Governance (`ui/components/BdixMotion.kt`)
- `LocalReducedMotion = staticCompositionLocalOf { false }` provided at `BdixTesterTheme` / `MainActivity` root.
- Effective value = `settings.reduceAnimations || systemAnimatorDurationScale == 0f`.
- **Keep:** Spring press (`0.96f`/`0.975f`), `220ms` tab crossfade (instant when reduced), `animateItem()`, bookmark/pin spring, active scan progress sweep.
- **Remove/Gate:** Idle sonar waves, idle orbital rotation, idle top-bar radar spin, idle CTA shimmer sweep, idle empty-state bobbing when `reducedMotion == true`, idle settings sync rotation.
