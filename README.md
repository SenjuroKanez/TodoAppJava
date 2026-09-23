# TodoApp

A Java 21 / Spring Boot application with a responsive Vaadin Hilla + React workspace and PostgreSQL storage. One application serves the UI and authenticated endpoints on port 8080.

A portfolio showcase of task planning, collaboration, and offline synchronization.

**[Setup guide](SETUP.md) · [Operation guide](OPERATION.md)**

## Screenshots

My Day in dark mode:

![My Day workspace in dark mode](ss/Screenshot%202026-09-23%20200356.png)

<details>
<summary>Explore the other screens</summary>

### Sign in

![Sign-in screen](ss/Screenshot%202026-09-23%20200253.png)

### Task details

![Task editor with project, priority, dates, and assignment](ss/Screenshot%202026-09-23%20200423.png)

### Inbox

![Inbox with a completed task](ss/Screenshot%202026-09-23%20200534.png)

### Today

![Tasks due today](ss/Screenshot%202026-09-23%20200556.png)

### Upcoming

![Upcoming tasks view](ss/Screenshot%202026-09-23%20200622.png)

### Overdue

![Overdue tasks view](ss/Screenshot%202026-09-23%20200647.png)

</details>

The screenshots show the local showcase instance. Accounts and task content are demonstration data.

## Features

- Inbox, My Day, Today, Upcoming, Overdue, assigned/shared tasks, completed tasks, and Trash.
- Projects with owner/editor/viewer permissions and invitations for registered accounts.
- Priorities, tags, descriptions, dates and timezones, estimates, assignments, ordered subtasks, comments, and activity history.
- List and calendar views, search, combined filters, saved views, bulk actions, keyboard capture (`n`), and search (`Ctrl/Cmd+K`).
- Daily, weekly, monthly, and yearly recurrence; completion creates the next future occurrence.
- In-app reminders and optional Web Push, light/dark/system themes, and JSON export.
- IndexedDB task storage and a durable offline edit queue. Synchronization checks versions, deduplicates retries, preserves conflicts as separate tasks, and retains rejected edits for export.

## Run with Docker

Install Docker with Compose, copy `.env.example` to `.env`, and replace `DATABASE_PASSWORD` with your own value. Then:

```sh
docker compose up --build -d --wait
```

Open <http://localhost:8080> and create an account. Stop with `docker compose down`; the PostgreSQL volume is retained. Do not add `--volumes` unless you intend to erase the database.

Compose ports bind to localhost. For public deployment, configure an HTTPS reverse proxy and set `COOKIE_SECURE=true`. Set VAPID keys and a real contact subject to enable push; in-app reminders work without those keys.

## Run locally

Requirements: JDK 21, PostgreSQL, and internet access for the first Maven/frontend build. The Maven wrapper and Vaadin manage build dependencies. Node 24 is recommended for frontend checks.

Create a `todoapp` database and user. Set `DATABASE_URL` (default `jdbc:postgresql://localhost:5432/todoapp`), `DATABASE_USER` (default `todoapp`), and `DATABASE_PASSWORD`. For localhost HTTP only, set `COOKIE_SECURE=false`. The Java process does not automatically load `.env`; Compose does.

```sh
./mvnw -Pproduction package
java -jar target/todoapp-2.0.0.jar
```

On Windows use `./mvnw.cmd`; set environment variables with `$env:NAME='value'`. For development, use `./mvnw spring-boot:run` after configuring the database. Flyway initializes the schema.

## Verification

```sh
./mvnw test
npm run typecheck
npm test
# Requires Docker for PostgreSQL Testcontainers:
./mvnw -Pproduction,integration verify
# With a running app on localhost:8080:
npx playwright install chromium
npm run test:e2e
```

Use `npm.cmd` / `npx.cmd` on Windows if PowerShell blocks the `.ps1` shims. Set `TEST_BASE_URL` to test another local instance. Browser tests create disposable accounts/tasks in the selected database. CI includes PostgreSQL integration, Compose startup, and browser checks.

To test with an installed Edge browser instead of downloading Chromium, set `PLAYWRIGHT_CHANNEL=msedge`.

## Behavior and limits

First sign-in and asset download require a connection. Once installed and synchronized, the browser can reopen cached tasks and queue edits offline. Project administration, invitations, settings, and account operations require a connection. Keep the application open or reopen it online to synchronize; closed-browser background replay is not guaranteed. Browser storage can be cleared or evicted, so synchronize regularly and export important unsynced work.

Reminders run on the server after synchronization. Push delivery depends on VAPID configuration, permission, browser support, and the push provider. Recurrence skips missed dates and clamps invalid month/year dates to the last valid day; later occurrences use the resulting date as their anchor. Trash is retained for 30 days. Project deletion is permanent.

Authentication uses an HttpOnly session cookie and CSRF protection. Cached task data remains on the device for offline access until sign-out; use a trusted device. Account recovery, email verification, attachments, and native desktop clients are not implemented.

This version uses a new schema. It does not automatically migrate the old in-memory H2 application. Back up old data before switching. Legacy module folders are no longer part of the root build; previously tracked build artifacts are retained to preserve existing local changes.

## Design sources

Planning features are informed by [Masicampo and Baumeister's research on making specific plans for unfulfilled goals](https://users.wfu.edu/masicaej/MasicampoBaumeister2011JPSP.pdf). My Day, dates, and small steps are product interpretations of that research, not a claim that the app itself has been scientifically validated.

Offline persistence follows the separation of cached assets and structured local data described in [Google's PWA guidance](https://web.dev/learn/pwa/assets-and-data). Keyboard controls, labels, contrast, and dialog behavior are guided by [W3C WCAG](https://www.w3.org/TR/wcag/); automated accessibility tests are a baseline, not a complete accessibility certification.

## Structure

`src/main/java/com/todoapp` contains authentication, authorization, task commands, synchronization, recurrence, and reminder services. `src/main/frontend` contains the workspace and offline store. `src/main/resources/db/migration` contains the database schema. Tests live in `src/test/java` and `tests`.
