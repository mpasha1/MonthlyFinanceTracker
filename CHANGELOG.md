# Changelog

All notable changes to the Monthly Finance Tracker project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - Branch: `ReadUPIMandatesms`

### Added
- **UPI Mandate SMS Auto-Matching**:
  - Automatically scans incoming and stored SMS messages for the current month.
  - Filters messages containing `"UPI Mandate: Sent"` and `"Sent Rs."` (including `Sent Rs`, `Sent INR`, `Sent ₹`).
  - Automatically extracts payment amounts and matches pending **Loan EMI** commitments.
  - Automatically logs payments in the local Room database and updates commitment statuses to **Paid**.
  - Asks for `READ_SMS` system permission on first app launch after installation.
- **SMS Tracker Screen (`SmsTrackerScreen.kt`)**:
  - Dedicated tool accessible under **Settings → UPI Mandates (SMS Tracker)**.
  - Lists current-month SMS messages with sender, timestamp, extracted amount, and message text.
  - Provides an **"Auto-Match & Mark Loan EMIs as Paid"** action button.
- **Unified "Other" Commitment Tracking**:
  - Added **"Other"** option in the Log Payment from SMS commitment selection dropdown.
  - Consolidates all "Other" payments under a single, unified `"Other"` commitment.
  - Automatically migrates and consolidates duplicate `"Other"` commitments created in prior builds into a single `"Other"` commitment on app startup.
- **Commitment Duration Feature**:
  - Added **"Duration / How Many Months?"** option when **Every Month** frequency is selected.
  - Preset duration options: Ongoing/Indefinite, 3, 6, 12, 24, 36, 60 Months, and Custom Months.
  - Automatic active month bounds filtering.
- **App Icon**:
  - Custom vector adaptive app icon (`ic_launcher_background.xml` & `ic_launcher_foreground.xml`) featuring a forest green gradient with a wallet and gold rupee coin.
- **Compact Top Bar**:
  - Streamlined `48.dp` height top app bar across all app screens to maximize content area.
- **Database Schema Migration**:
  - Added Room `MIGRATION_1_2` to preserve all user commitments, categories, and payment logs across app updates without data loss.

### Changed
- **Home Screen**:
  - Removed sample seed commitments to start with a clean state.
  - Updated empty state prompt to: *"No commitments added yet. Click 'Add' below to track your commitments!"*.
- **Payment Details**:
  - Added **Edit** and **Delete** actions in top app bar.
  - Hides "Log Payment" button for SMS cards if the payment has already been recorded, replacing it with a `✓ Already Logged` badge.
  - Removed custom text input field when selecting "Other" in SMS log payment dialog to streamline the workflow.

---

## [1.0.0] - Initial Release

### Added
- Monthly Dashboard with 4 summary metrics (Total Commitment, Paid, Remaining, Completed % meter).
- Month Picker navigation (`< Month Year >`).
- 5 Main Bottom Navigation tabs: Home, Calendar, Add, Reports, More.
- Interactive Category Wise Donut Chart and Monthly Breakdown tables.
- Due Date Calendar grid with status indicators.
- Past Months history view with completed percentage badges.
- Reminders management with local notification scheduling before 1 day and on due date.
- 100% Local-Only Storage powered by Room database.
