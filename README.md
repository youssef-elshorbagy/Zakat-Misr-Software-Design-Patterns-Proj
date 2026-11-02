# Zakat Misr - Phase 1 (Java, Swing UI)

## Run in IntelliJ IDEA
1. File > New > Project > Java (JDK 17 or newer) > name it ZakatMisr.
2. Delete the auto-generated Main.java (if any).
3. Copy EVERYTHING inside this zip's `src` folder into the project's `src` folder.
4. Right-click `Main.java` > Run 'Main.main()'  -> the GUI opens.
5. Optional: run `SelfTest.java` to check the business rules without the UI (45 checks).

## Demo accounts
| Role        | E-mail               | Password  |
|-------------|----------------------|-----------|
| Admin       | admin@zakatmisr.org  | admin123  |
| Donor       | ahmed@mail.com       | pass1234  |
| Donor       | mona@mail.com        | secret99  |
| Volunteer   | sara@mail.com        | sara2026  |
| Volunteer   | layla@mail.com       | layla2026 |
| Beneficiary | fatma@mail.com       | fatma2026 |

New donors / volunteers / beneficiaries can register from the login screen.

## What each role can do
* **Donor** - donate (Zakat / Sadaqah / In-kind, anonymous, recurring), receipts, history, events, profile.
* **Volunteer** - add skills and availability, apply for tasks, log hours, certificates, events, profile.
* **Beneficiary** - eligibility check, submit needs, aid received, impact report, events, profile.
* **Admin** - full user CRUD, lookup-table CRUD (donation categories, skills), events (create / open / cancel /
  reminders / check-in), volunteer tasks + matching, distribute donations to needs.
* **Everyone** - user control panel (view / update profile, change password, delete account).

## Patterns (Phase 1 only)
Singleton `DatabaseManager` - Factory `DonationFactory` - Observer `DonationService`/`NotificationObserver` -
Strategy `communication/*` - Decorator `AnonymousDonor`/`RecurringDonor` - MVC `presentation/*`.

See `docs/CHANGES.md` (what changed vs. the class diagram) and `docs/CRUD_Matrix.md`.
