# FreelanceReceipt

A modern Android app that turns paper receipts into a structured, exportable expense table — built for freelancers who hate accounting busywork.

Scan a receipt with your camera, the app automatically extracts merchant, date, amount, and VAT, categorizes it, and lets you export everything as CSV or PDF for your tax advisor.

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg)](https://www.android.com/)
[![Min SDK](https://img.shields.io/badge/min%20SDK-26-blue.svg)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.7-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

---

## Features

- **Camera-based OCR** — point, shoot, done. ML Kit recognizes text on-device (offline, privacy-friendly).
- **Smart parsing** — automatically extracts merchant, gross amount, VAT rate, date, and category from raw OCR text using regex heuristics tuned for German receipts.
- **Statistics** — pie chart by category, monthly bar chart, totals (net / VAT / gross).
- **CSV & PDF export** — branded PDF report or Excel-compatible CSV via Android's share sheet.
- **Biometric lock** — fingerprint / PIN gate on app start.
- **Multi-language** — fully localized in 7 languages: English, German, French, Spanish, Italian, Russian, Turkish.
- **Light / Dark mode** — Material 3 color system with custom light-blue brand identity.
- **Freemium model** — 10 free scans per calendar month; unlimited with one-time Premium purchase (€6.99 lifetime).

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM with unidirectional data flow |
| DI | Koin |
| Persistence | Room + DataStore (preferences) |
| Async | Kotlin Coroutines + Flow / StateFlow |
| Camera | CameraX 1.4 |
| OCR | Google ML Kit Text Recognition (on-device) |
| Auth | Firebase Auth (Email + Google Sign-In via Credentials API) |
| Billing | Google Play Billing Library 7.x |
| Biometrics | AndroidX Biometric |
| Navigation | Navigation Compose |
| Build | Gradle Kotlin DSL + KSP |

---

## Architecture

The app follows MVVM with Clean Architecture layering. Each layer has a single responsibility and no knowledge of layers above it.

```
+---------------------------------------------------------+
|  UI Layer (Jetpack Compose)                             |
|  - Screens (@Composable)                                |
|  - ViewModels (StateFlow + Coroutines)                  |
+----------------------+----------------------------------+
                       | Koin DI
+----------------------v----------------------------------+
|  Domain Layer                                           |
|  - Category enum, ReceiptParser, ReceiptStats           |
|  - Pure Kotlin, no Android imports                      |
+----------------------+----------------------------------+
                       |
+----------------------v----------------------------------+
|  Data Layer                                             |
|  - Repositories (Receipt, Export)                       |
|  - Room DAOs + Entities                                 |
|  - BillingManager (Play Billing)                        |
|  - PreferencesManager (DataStore)                       |
+---------------------------------------------------------+
```

Each screen has:
- A `@Composable` function that observes a `StateFlow<UiState>` and emits user events as lambdas.
- A `ViewModel` that exposes the `StateFlow` and contains the screen's business logic.
- A `UiState` data class — single source of truth, immutable, easy to test.

---

## Project Structure

```
app/src/main/java/com/alexandresamson/freelancereceipt/
├── FreelanceReceiptApp.kt          # Application class, Koin init, Billing connect
├── MainActivity.kt                 # Single Activity, installs splash screen
│
├── data/
│   ├── billing/
│   │   └── BillingManager.kt       # Google Play Billing wrapper
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database
│   │   ├── dao/ReceiptDao.kt       # Receipt CRUD
│   │   └── entity/ReceiptEntity.kt # Receipt table schema
│   ├── prefs/
│   │   └── PreferencesManager.kt   # DataStore: premium flag + monthly scan counter
│   └── repository/
│       ├── ReceiptRepository.kt    # Receipt business logic
│       └── ExportRepository.kt     # CSV / PDF generation
│
├── di/
│   └── AppModule.kt                # Koin module — single + viewModel bindings
│
├── domain/
│   ├── Category.kt                 # Localized category enum
│   ├── ReceiptParser.kt            # OCR text -> ParsedReceipt
│   └── ReceiptStats.kt             # Aggregated stats model
│
├── navigation/
│   └── AppNavigation.kt            # Nav graph, Screen sealed class, OcrResultHolder
│
└── ui/
    ├── addreceipt/                 # Edit receipt after OCR scan
    ├── auth/                       # Login / Register / Google Sign-In
    ├── biometric/                  # Fingerprint lock screen
    ├── camera/                     # CameraX + ML Kit OCR
    ├── common/                     # Reusable composables (TaxBreakdownCard)
    ├── dashboard/                  # Main list, Premium badge, free-tier banner
    ├── detail/                     # Edit existing receipt
    ├── export/                     # CSV / PDF export options
    ├── paywall/                    # Premium upgrade screen + Billing flow
    ├── settings/                   # Subscription status, privacy, support
    ├── stats/                      # Pie chart, bar chart, totals
    ├── theme/                      # Color.kt, Theme.kt, Type.kt
    └── welcome/                    # First-launch branded splash
```

---

## Building from Source

### Prerequisites

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK 35 with build-tools 35.0.0
- A Firebase project with `google-services.json` placed in `app/`

### Steps

```bash
git clone https://github.com/<your-username>/freelancereceipt.git
cd freelancereceipt
```

1. Add your own `google-services.json` to `app/`. Get one from the [Firebase Console](https://console.firebase.google.com/).
2. In the Firebase Console, enable **Email/Password** and **Google** as sign-in providers.
3. Replace the `default_web_client_id` in `app/src/main/res/values/strings.xml` with your own OAuth client ID.
4. Open the project in Android Studio and let Gradle sync.
5. Run on a device or emulator with API 26+.

### Google Play Billing setup

For the in-app purchase to work, you need to:

1. Upload at least one signed release to the Play Console (Internal Testing track works).
2. Create a managed product with ID **`premium_lifetime`** priced at €6.99.
3. Add Google accounts as license testers.

Without these steps, `BillingManager` will report "Product not loaded" — the rest of the app still works.

---

## Monetization Model

The app uses a **freemium + one-time purchase** model:

| Tier | Price | Limits |
|---|---|---|
| Free | €0 | 10 scans per calendar month, resets on the 1st |
| Premium | €6.99 one-time | Unlimited scans, lifetime access, no ads |

When the free user hits the cap, the camera screen shows an upgrade prompt instead of opening — they can still view, edit, export, and delete existing receipts. The counter resets automatically at month rollover (no server needed; logic lives in `PreferencesManager.scanCount`).

Premium purchases are restored automatically on reinstall (`BillingManager.queryExistingPurchases()` runs on every billing client connect).

---

## Localization

All user-facing strings live in `res/values*/strings.xml`. Adding a new language means:

1. Create `res/values-<locale>/strings.xml`.
2. Translate every key (about 90 strings).
3. Done — Android picks it up automatically based on system locale.

Categories are stored as language-independent English keys (`"travel"`, `"groceries"`, ...) in the database and converted to display names via `Category.dbKeyToDisplayName()` — so users can switch language without their data becoming gibberish.

---

## Privacy

- All OCR runs **on-device** via ML Kit — receipt images never leave the phone.
- All receipt data is stored locally in Room — never synced to a server.
- Firebase Auth is used only for sign-in. We do not store user profiles on a backend.
- Cloud backup is **disabled** for the database (see `res/xml/data_extraction_rules.xml`) — sensitive financial data stays on the device.
- Device-to-device transfer **is** allowed during phone setup, so users keep their receipts when switching phones.

---

## Roadmap

- [ ] In-app review prompt after Nth scan
- [ ] Encrypted Room database
- [ ] Receipt image storage (optional, premium-only)
- [ ] CSV import (for migration from other apps)
- [ ] Recurring expenses
- [ ] Multi-currency support
- [ ] WearOS companion for quick capture

---

## Contributing

PRs welcome. Before submitting:

- Make sure `./gradlew assembleDebug` succeeds.
- Run `./gradlew lint` and address any warnings on changed files.
- For UI changes, attach before/after screenshots in both light and dark mode.
- New strings must be added to **all** `values-*/strings.xml` files (or fall back gracefully to the base English).

---

## License

MIT License. See [LICENSE](LICENSE) for details.

---

## Acknowledgments

- [ML Kit](https://developers.google.com/ml-kit) for on-device text recognition
- [Material 3](https://m3.material.io/) for the design system
- [Koin](https://insert-koin.io/) for the DI framework
- The Jetpack Compose team at Google for making Android UI development enjoyable again
