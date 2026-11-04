# Zakat Misr - Phase 1 (Java, Swing UI)

1.Right-click `Main.java` > Run 'Main.main()'  -> the GUI opens.

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

## Patterns
Singleton `DatabaseManager` - Factory Method `DonationClient` and its concrete creators - Observer `DonationService`/`NotificationObserver` -
Strategy `communication/*` - Decorator `AnonymousDonor`/`RecurringDonor` - MVC `presentation/*`.

