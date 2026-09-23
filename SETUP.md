# Setup guide

[Home](README.md) · [Using the app](OPERATION.md)

Choose **Docker** for persistent use, **local Java + PostgreSQL** for development, or **temporary showcase** for a demonstration without installing a database. Run commands from the repository root.

## 1. Persistent setup with Docker

Install Docker with Compose and start its engine. The image build supplies Java and frontend build tools.

```powershell
git clone https://github.com/SenjuroKanez/TodoAppJava.git
cd TodoAppJava
Copy-Item .env.example .env
```

Edit `.env`: replace the example `DATABASE_PASSWORD` with a unique password. Leave `COOKIE_SECURE=false` for localhost HTTP. On macOS/Linux, use `cp .env.example .env` instead.

```sh
docker compose up --build -d --wait
docker compose ps
```

Open <http://localhost:8080/login> and create an account. The first build downloads dependencies and may take several minutes. PostgreSQL data is stored in the `todoapp-data` named volume.

```sh
# Inspect application logs
docker compose logs --tail=100 app
# Stop services while preserving the database volume
docker compose down
# Start again
docker compose up -d --wait
```

Do not use `docker compose down --volumes` unless you intend to erase the database. Changing `DATABASE_PASSWORD` in `.env` does not change the password inside an already initialized PostgreSQL volume; update the database credentials deliberately instead of deleting data to resolve a mismatch.

## 2. Local Java + PostgreSQL

Install JDK 21 and PostgreSQL (the Compose configuration uses PostgreSQL 17). Set `JAVA_HOME` to the JDK installation and put its `bin` directory on PATH. Confirm `java -version` reports Java 21. Maven is supplied by the wrapper. Node 24 is recommended if running frontend checks separately; Vaadin manages a compatible Node runtime for its build.

Using a PostgreSQL administrator connection, create a database and user (replace the password):

```sql
CREATE USER todoapp WITH PASSWORD 'replace-with-your-own-password';
CREATE DATABASE todoapp OWNER todoapp;
```

Configure and start in PowerShell:

```powershell
$env:DATABASE_URL='jdbc:postgresql://localhost:5432/todoapp'
$env:DATABASE_USER='todoapp'
$env:DATABASE_PASSWORD='replace-with-your-own-password'
$env:COOKIE_SECURE='false'
./mvnw.cmd -B -ntp -Pproduction package
java -jar target/todoapp-2.0.0.jar
```

On macOS/Linux:

```sh
export DATABASE_URL='jdbc:postgresql://localhost:5432/todoapp'
export DATABASE_USER='todoapp'
export DATABASE_PASSWORD='replace-with-your-own-password'
export COOKIE_SECURE=false
chmod +x mvnw
./mvnw -B -ntp -Pproduction package
java -jar target/todoapp-2.0.0.jar
```

Open <http://localhost:8080/login>. Stop with Ctrl+C in the server terminal. Flyway creates the schema automatically. For development, use `./mvnw.cmd spring-boot:run` (or `./mvnw spring-boot:run`) with the same environment.

The Java process does **not** read `.env` automatically; those values must be environment variables. Compose reads `.env` itself. Do not commit credentials or `.env`.

## 3. Temporary showcase without PostgreSQL

This runs the test-only H2 server with production frontend assets. It requires JDK 21 and internet access on the first build, but no database installation or database password. It is for demonstrations, not persistent storage: **all server accounts and tasks disappear when the process stops**. H2 is not included in the production JAR.

PowerShell:

```powershell
./mvnw.cmd -B -ntp -Pproduction package dependency:build-classpath '-Dmdep.outputFile=target/showcase-classpath.txt'
$showcaseClasspath='target/test-classes;target/classes;'+(Get-Content target/showcase-classpath.txt -Raw).Trim()
java -cp $showcaseClasspath com.todoapp.TestServer --server.address=127.0.0.1
```

macOS/Linux:

```sh
./mvnw -B -ntp -Pproduction package dependency:build-classpath -Dmdep.outputFile=target/showcase-classpath.txt
showcase_classpath="target/test-classes:target/classes:$(< target/showcase-classpath.txt)"
java -cp "$showcase_classpath" com.todoapp.TestServer --server.address=127.0.0.1
```

Open <http://localhost:8080/login>, create a demo account, and follow the [showcase walkthrough](OPERATION.md#suggested-showcase-walkthrough). Stop with Ctrl+C. After a restart, use a fresh browser profile or clear this site's old browser data before creating a fresh account; do not mistake a cached offline snapshot for persisted server data.

## Configuration

| Variable | Purpose/default |
| --- | --- |
| `DATABASE_URL` | JDBC URL; defaults to `jdbc:postgresql://localhost:5432/todoapp`. |
| `DATABASE_USER` | Database login; defaults to `todoapp`. |
| `DATABASE_PASSWORD` | Required for the regular application; no built-in password. |
| `PORT` | Application port; defaults to `8080` for local Java. Compose maps 8080 explicitly. |
| `COOKIE_SECURE` | Defaults to `true` in the application; use `false` only for localhost HTTP. |
| `VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY` | Optional Web Push key pair. |
| `VAPID_SUBJECT` | Push contact, such as a real `mailto:` address. |

For push, generate a key pair using `npx web-push generate-vapid-keys`, configure the keys and contact, restart, and enable browser reminders in Settings. Keep the private key secret. In-app reminders do not require VAPID keys. Public hosting needs HTTPS, `COOKIE_SECURE=true`, and a properly configured reverse proxy. The supplied Compose ports bind only to localhost; this repository does not provision a public deployment.

## Checks

The final local verification on September 23, 2026 passed 20 Java tests, 8 frontend tests, 7 browser tests in Edge, TypeScript validation, and the production build. The npm audit reported zero known vulnerabilities at that time. Docker/PostgreSQL integration and real push-provider delivery were not run locally; Docker was unavailable.

```powershell
./mvnw.cmd test
npm.cmd run typecheck
npm.cmd test
# Requires a running Docker engine:
./mvnw.cmd -Pproduction,integration verify
# Against a running disposable app instance:
npx.cmd playwright install chromium
npm.cmd run test:e2e
```

Run a Maven frontend build before the TypeScript check so generated Hilla clients exist. On macOS/Linux, use `./mvnw`, `npm`, and `npx`. To use installed Edge, set `$env:PLAYWRIGHT_CHANNEL='msedge'` and skip the browser download. Set `TEST_BASE_URL` for a different instance. Browser tests create accounts/tasks, so point them at disposable data.

## Troubleshooting

- **Java or JAVA_HOME missing:** install JDK 21 and reopen the terminal after configuring PATH.
- **PowerShell blocks npm.ps1:** use `npm.cmd` and `npx.cmd`; no execution-policy change is needed.
- **Port already in use:** stop your earlier app terminal, or choose another local Java port with `$env:PORT='8081'`. Update the browser URL and `TEST_BASE_URL` accordingly.
- **Database connection refused:** start PostgreSQL, confirm database/user/password and port, and check the JDBC URL. With Compose, inspect `docker compose logs db`.
- **Login does not persist on localhost:** check `COOKIE_SECURE=false` and use one consistent hostname rather than switching between `localhost` and `127.0.0.1`.
- **Sync paused:** reconnect, sign in again with the same account, then click the sync status. Export pending edits before clearing browser data.
- **Old UI after an update:** close other app tabs and reload online so the service worker can update. Export unsynced work before clearing site storage as a last resort.

The old multi-module application has no automatic data migration into this schema. Preserve any old data before upgrading. JSON device export currently has no automatic import interface; use PostgreSQL backup tooling for persistent server backups.
