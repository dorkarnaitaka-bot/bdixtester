# BDIX Tester — Screen-by-Screen Redesign Specification (Phase 1)

Every screen preserves 100% of existing business logic, ViewModels, Room entities, and direct External Browser launch. Zero separate Server Metadata / Details screen is re-introduced.

---

## 1. Home Screen (`feature/dashboard/DashboardScreen.kt`)

### Preserved Elements
- `HomeBrandTopBar` with brand badge + live IP pill (`home_top_ip_pill`).
- `HeroTesterRing` (`home_tester_ring`) with `workingCount / totalCount`, live `%` during scan, and `VIEW` button (`home_view_servers_button`).
- `PrimaryTestActionButton` (`home_start_test_button` / `home_stop_test_button`) + Wi-Fi warning dialog.
- Permanently absent: `home_menu_button`, `home_scan_mode_selector`, `home_network_summary_card`.

### Specification Changes
1. **Calm-Idle / Energetic-Active Ring & Top Bar (P1):**
   - **Idle:** Static precision outer gradient ring + crisp 36-tick telemetry ring + soft static radial backdrop. **Zero** continuous sonar pulses, **zero** orbital spin, **zero** top-bar icon spin, **zero** CTA shimmer loop.
   - **Active Scan (`isScanning || isStarting`):** If `!LocalReducedMotion.current`, run orbital sweep, luminous comet head, expanding sonar waves, and gentle top-bar radar rotation. If `LocalReducedMotion.current`, render deterministic animated progress arc without continuous spin/sonar loops.
   - **Completion:** Animations settle cleanly to idle state.
2. **WCAG AA CTA Gradient & Text (P0):**
   - Light Start: `#0369A1 → #1D4ED8` + `#FFFFFF` text/icon (`≥ 5.93:1`).
   - Dark Start: `#22D3EE → #38BDF8 → #3B82F6` + `#041E2D` text/icon (`≥ 4.65:1`).
   - Light/Dark Stop: `#C81E1E → #991B1B` / `#B91C1C → #991B1B` + `#FFFFFF` text/icon (`≥ 5.74:1`).
3. **Subtle Scan Mode & Post-Scan Feedback Caption (P3):**
   - Compact non-interactive status caption below the ring or CTA sourced strictly from `DashboardUiState`:
     - Idle (never tested): `Quick Scan · 369 Servers` / `Full Scan · 369 Servers` (localized).
     - Completed (`!isScanning && testedCount > 0`): `Scan Complete · 41 Working` (+ `· Avg 12 ms` only when `averageReachableLatencyMs != null`).
4. **Responsive Layout (P1):**
   - Compact (`< 600dp`): full-height vertical portrait canvas (`isCompactHeight < 500dp` scrollable fallback).
   - Medium / Expanded (`>= 600dp`): balanced 2-column split paired with `NavigationRail`.

---

## 2. Servers & Bangladesh OTT Screen (`feature/servers/ServersScreen.kt`, `ServersViewModel.kt`)

### Preserved Elements
- 3 Status Tabs (`Working`, `Not Tested`, `Not Working`) with live tabular counts (`servers_tab_*`).
- Category filter bar (`All`, `OTT`, `TV`, `FTP`, `Movie`), search toggle, sync button, add custom server dialog.
- 26 verified Bangladesh & Global OTT platforms (`BdOttPlatforms.kt`) with region filter chips.
- Direct External Browser launch on every card & URL footer tap.

### Specification Changes
1. **Touch Targets (P1):** Raise `ott_region_*` `FilterChip` minimum height from `40.dp` to `48.dp`.
2. **Status & Latency Contrast (P0):** Inherit tightened Light Emerald (`#047857`), Amber (`#B45309`), and Crimson (`#C81E1E`) tokens; use `MonoBadgeStyle` for numeric latency chips (`12 ms`) and `labelSmall` (body font) for Bengali/English text badges (`বাংলাদেশ`, `খুলুন`, category labels).
3. **Fastest Ping & A–Z Sorting (P2):**
   - Wire existing `ServersViewModel.updateSortOrder(...)` to a compact `>= 48.dp` sort action/menu in BDIX mode (`Default`, `Fastest Ping`, `A–Z`).
   - Harden `ServerSortOrder.LATENCY` so `null` or `< 0L` latencies always sort after valid numeric pings (`primaryLatencyMs?.takeIf { it >= 0L } ?: Long.MAX_VALUE`).
4. **Shared Card Footer & Optional Productivity Actions (P2–P3):**
   - Extract `RichCardUrlFooter` to shared `ui/components/RichServerCardComponents.kt`.
   - Support long-press on `RichCardUrlFooter` (or card copy button) to copy URL to clipboard + Snackbar confirmation.
   - Optional `ModalBottomSheet` diagnostics preview (only for servers with real `ServerTestResult != null`, triggered via explicit long-press/overflow—never blocking regular tap-to-browser).
   - Add `TopAppBarDefaults.enterAlwaysScrollBehavior()` with `nestedScroll` so the list gains vertical space while scrolling without breaking sticky status/category usability.

---

## 3. Saved Servers Screen (`feature/favorites/FavoritesScreen.kt`, `ui/components/ServerCard.kt`)

### Preserved Elements
- Search, Pin/Unpin, Unbookmark, Export (JSON/CSV/TXT), Direct External Browser launch.
- Existing `testTag`s (`favorites_screen`, `saved_search_input`, `favorites_empty_state`, `server_card_*`, `pin_toggle_*`, `favorite_toggle_*`, `visit_website_button_*`).

### Specification Changes
1. **Unified 20dp Elevated Rich Card (P2):**
   - Upgrade `ServerCard` to `20.dp` corner radius, `2.dp` elevation + `1.dp` border, `12.dp` category squircle badge, unified status/latency chips, and shared `RichCardUrlFooter` (`Open ↗` pill).
2. **Sectioned Layout & Smart Search Visibility (P2):**
   - Split `favoriteItems` into `Pinned` and `All Saved` sections using `BdixSectionHeader` (render section headers **only** when pinned items exist).
   - Hide `saved_search_input` when total favorites are empty and `searchQuery` is blank, eliminating dead vertical space above the empty state.
3. **Undo Snackbar (P2):**
   - Removing a favorite or unpinning triggers a Snackbar with `Undo` that calls `favoritesViewModel.restoreFavorite(snapshot)` to restore exact DB state (including `isPinned`, `displayOrder`, `addedAtEpochMs`).
4. **Adaptive Grid (P2):**
   - Use 2-column `LazyVerticalGrid` when `maxWidth >= 700.dp` (matching `ServersScreen`).

---

## 4. Testing History Screen (`feature/favorites/FavoritesScreen.kt`, `FavoritesViewModel.kt`)

### Preserved Elements
- Search, Status & Protocol `FilterChip`s, `Clear History` confirmation dialog, Export, Single Delete, Direct External Browser launch.
- Existing `testTag`s (`history_screen`, `history_search_input`, `history_clear_all_button`, `history_empty_state`, `history_item_*`, `history_delete_*`, `history_url_link_*`).

### Specification Changes
1. **Unified 20dp Elevated Rich Card (P2):**
   - Upgrade `HistoryRowCard` to `20.dp` Elevated Rich Card with protocol/category squircle icon badge, `StatusBadge`, tabular metrics, and shared `RichCardUrlFooter`.
2. **Real Date Grouping (P2):**
   - Group `historyItems` by real `entry.testedAtEpochMs` relative to local device midnight: `Today`, `Yesterday`, `Earlier` (localized in EN/BN). Never fabricate timestamps if missing.
3. **Undo Snackbar on Single Delete (P2):**
   - Deleting a single history record caches the `ServerTestResult` snapshot and shows an `Undo` Snackbar; confirming Undo re-inserts the record via `catalogRepository.restoreHistoryEntry(entry)`.
4. **Smart Empty/Filter States (P2):**
   - Differentiate "No history recorded yet" vs "No history matches current filters" (with `Clear filters` action).

---

## 5. Settings Screen (`feature/settings/SettingsScreen.kt`, `SettingsComponents.kt`, `SettingsViewModel.kt`)

### Preserved Elements
- App Profile & Live IP Header Card (`settings_app_header_card`), Theme & Language modal pickers, Default Scan Mode picker, Max Concurrency slider, Speed-Test Cap picker, Manual Network Profile Label, Remote Signed Catalog Sync settings, History Retention & Destructive Bulk Actions with confirmation dialogs, Privacy Policy modal, Facebook Community link, Share App chooser, Developer row, Version/Build footer.

### Specification Changes
1. **Color & Contrast Fixes (P0):**
   - App Profile Header icon badge uses `semantic.ctaGradientStart/End` + `semantic.ctaContent` (`≥ 4.65:1`).
   - Live IP text uses `semantic.ipAccent` (`#035E91` Light = `6.12:1`; `#38BDF8` Dark = `7.71:1`).
   - Version Footer `APPLICATION_ID` uses full-opacity `onSurfaceVariant` (`6.68:1` Light).
2. **Consolidate Duplicate Sync Action & Rich Sync Telemetry (P2):**
   - Deduplicate `Update Server List` vs `Sync Catalog Now`: clarify Quick Actions vs Remote Signed Catalog Sync so there are no confusing duplicate sync buttons.
   - Display formatted `Last Successful Sync` timestamp from `sync.lastSuccessfulSyncAtEpochMs` when available, plus clear inline error surface + `Retry Sync` label when `lastSyncStatus` indicates failure.
   - Gate sync icon rotation inside `if (uiState.isSyncing && !LocalReducedMotion.current)`.
3. **Reduce Animations Preference (P1–P2):**
   - Add `SettingsSwitchRow` for `Reduce Animations` (`settings_reduce_animations_switch`) inside `Appearance & Language` card, persisted via `SettingsDataStoreRepository`.

---

## 6. Root Navigation (`navigation/BdixNavRoutes.kt`, `navigation/BdixTesterAppRoot.kt`)

1. **Filled vs Outlined Navigation Icons (P1):** Distinct selected/unselected icons for all 5 destinations.
2. **600dp Adaptive Breakpoint (P1):** Switch from `NavigationBar` to `NavigationRail` at `maxWidth >= 600.dp` (Medium & Expanded Window Size Classes).
3. **Reduced-Motion Tab Transitions (P1):** Switch `NavHost` enter/exit transitions to `EnterTransition.None` / `ExitTransition.None` (or subtle fade) when `LocalReducedMotion.current == true`, and disable bouncy nav-icon scale pop when reduced motion is active.
4. **Tap Active Tab to Scroll to Top (P3):** Re-tapping the currently selected tab emits an incremented scroll-to-top counter consumed cleanly by `Servers`, `Saved`, `History`, and `Settings` lazy lists without resetting tab back-stack state.
