# Repository Guidelines

## Project Structure & Module Organization

This repository is a Spring Boot 3.5.13 service named `healthy`, built with Gradle (Groovy DSL, `build.gradle`, single
module) and Java 17. All code is Java: application code lives in `src/main/java/com/junghaebom/geonganghaegym`, tests in
`src/test/java`. There is no Kotlin source.

Packages are organized by feature (`member`, `gym`, `trainer`, `course`, `schedule`, `lessonhistory`, `diet`,
`workout`, `point`, `home`, `notification`, `push`, `file`) plus shared `common` and `config`. Inside a feature the
layers are `presentation` (controllers, `dto/in`, `dto/out`), `application` (services), `domain` (entities) and
`repository` (Spring Data JPA + QueryDSL). Exceptions: `file` and `home` have no `domain`/`repository`, `point` has no
controller, and `trainer`'s repository package is spelled `respository`.

Runtime config is a single `src/main/resources/application.yml` (plus `logback-spring.xml`); local infrastructure is
defined in `docker-compose.yml`. For the architecture overview, domain model and core flows, see `README.md`.

## Build, Test, and Development Commands

Use the Gradle wrapper only. It needs JDK 17+; if the shell's default JDK is older, set `JAVA_HOME`.

- `./gradlew clean build`: compile, run tests, and assemble the application.
- `./gradlew test`: run the test suite.
- `./gradlew test --tests "*SocialLoginTest*"`: run a single test class (keep the trailing wildcard).
- `docker compose up -d`: start the local MySQL 8.0 and Redis 7 services.
- `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`: start the API on port `8080` (actuator on `7070`). There is no
  default profile; without `dev`, `ddl-auto` falls back to `validate` and startup fails on an empty database.

## Coding Style & Naming Conventions

- Java is tab-indented. Keep package names lowercase and don't rename packages (including `respository`) unless the
  feature is moving.
- Class suffixes: `*Controller`, `*Service`, `*Repository`, `*Config`. Many features split reads from writes, e.g.
  `GymController` / `GymCommandController`, `TrainerScheduleCommandService`.
- DTOs use `Command*` for writes and `Retrieve*` for reads; `*Response`, `*Result` and `*Cond` also appear.
- Dynamic queries go in `XxxRepositoryCustom` + `XxxRepositoryCustomImpl` (QueryDSL).
- Success responses are wrapped in `ApiResult<T>`. Business errors throw `CustomException(ErrorCode.X)`, which
  `GlobalExceptionHandler` renders as `ErrorResponse`.
- Build web links (notification click URLs, invitation links) from `Utils.WEB_BASE_URL`; never hardcode a domain.

## Hidden Side Effects

Read these before changing reservation, cancellation, workout or diet logic:

- Course (수강권) count changes on reserve/cancel live in `common/aop/CourseAspect` (`@AfterReturning`), not in the
  schedule services.
- Points (+1 for workout/diet logs, −3 for no-show) live in `common/aop/PointAspect`.
- Notifications, FCM pushes and waiting-list promotion run in `common/event/CustomEventListener`
  (`@Async` + `@TransactionalEventListener` + `REQUIRES_NEW`, i.e. after the original transaction commits).

## Testing Guidelines

Tests use JUnit 5 and Mockito (`spring-boot-starter-test`, `spring-security-test`). They are plain unit tests with no
Spring context and no database: H2 is on the classpath but unused, and there is no `src/test/resources`. Name tests
`*Test` and place them beside the feature you changed.

Because no test loads the Spring context, context-startup failures (invalid derived query names, bean wiring,
listener/transaction misconfiguration) are not caught by `./gradlew test`. When touching those, boot the app locally.

## Commit & Pull Request Guidelines

- Commits: `type: summary` with a lowercase type: `feat`, `fix`, `refactor`, `test`, `docs`, `style`, `chore`, `ci`.
  The template is `.github/.gitmessage.txt`.
- PRs follow `.github/PULL_REQUEST_TEMPLATE.md`: the Jira issue in the title, a summary, the work type, follow-up
  concerns and completed tests.

## Deployment

- Pushing to `develop` deploys to production automatically: GitHub Actions builds the image, pushes it to GHCR,
  bumps the tag in `seonwooj0810-homelab/homelab-gitops`, and Argo CD rolls it out. CI runs no tests and the Docker
  build uses `-x test`, so run `./gradlew clean build` before pushing.
- The root `deployment.yaml` is the live production Deployment: the GitOps repo pulls it directly from the `develop`
  branch. Changes to it reach production on the next sync.

## Security & Configuration Tips

`application.yml` is environment-driven: `DB_*`, `DATA_REDIS_*`, `JWT_*`, `MAIL_*`,
`OAUTH_{KAKAO,NAVER,GOOGLE,APPLE}_*`, `FILE_UPLOAD_DIR`, `FILE_BASE_URL`, `FIREBASE_ADMIN_SDK_FILE`. Keep secrets in
local env files or shell variables, not in Git (`.env` is ignored). Uploaded files go to local disk
(`FILE_UPLOAD_DIR`, a PVC at `/data/files` in production); there is no S3. Production secrets are Sealed Secrets in
the GitOps repo.
