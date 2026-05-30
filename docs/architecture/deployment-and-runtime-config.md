# Docker Compose 기반 배포와 런타임 설정 주입

## 개요

기존 dev/prod 배포는 GitHub Actions self-hosted runner에서 이미지를 빌드한 뒤 `docker run` 명령으로 컨테이너를 직접 교체했다.
이 방식은 서버에 배포 상태를 설명하는 `docker-compose.yml`이 없어서 컨테이너 이름, 포트, 환경변수, 재시작 정책이 워크플로 스크립트 안에 흩어지는 문제가 있었다.

또한 `application-private.yml`이라는 파일이 실제 비밀값 파일처럼 보였지만, 내용은 환경변수 placeholder였다.
이 이름은 "private 값은 어디에 넣어야 하는가"라는 혼란을 만들었다.

현재 배포 구조는 dev/prod 모두 `docker-compose.yml`을 기준으로 구성한다.

## 배포 구조

dev/prod 배포는 `docker-compose.yml` 기반으로 수행한다.

GitHub Actions는 다음 책임만 가진다.

1. 애플리케이션 JAR 빌드
2. Docker 이미지 빌드 및 Docker Hub push
3. 배포 대상 디렉터리에 `docker-compose.yml` 복사
4. GitHub Secrets 값을 이용해 서버의 `.env` 생성
5. `docker compose pull app` 및 `docker compose up -d --remove-orphans` 실행

Spring 설정은 프로필별 설정 파일에 직접 둔다.

- `application.yml`: 활성 프로필 기본값만 정의
- `application-local.yml`: 로컬 실행용 H2, 더미 JWT/AWS 값 정의
- `application-dev.yml`: dev DB/JWT/AWS placeholder 정의
- `application-prod.yml`: prod DB/JWT/AWS placeholder 정의

`application-private.yml`은 삭제한다.

## 설정 값 주입 흐름

비밀값은 Git 저장소에 커밋하지 않는다.

배포 시 값은 아래 순서로 전달된다.

```text
GitHub Secrets
-> GitHub Actions가 서버에 생성하는 .env
-> Docker Compose env_file
-> 컨테이너 환경변수
-> Spring Boot placeholder 해석
```

예를 들어 `application-prod.yml`의 값은 다음처럼 둔다.

```yml
security:
  jwt:
    token:
      secret-key: ${JWT_SECRET_KEY}
```

그리고 배포 시 `.env`에는 실제 값을 넣는다.

```env
JWT_SECRET_KEY=actual-secret-value
```

Docker가 secret을 자동으로 만들어 주는 것은 아니다.
Docker Compose는 `.env`에 적힌 값을 컨테이너 환경변수로 전달하고, Spring Boot가 그 값을 읽는다.

## 이미지 태그 정책

dev와 prod가 같은 `latest` 태그를 공유하지 않는다.

- dev: `crew-wiki:dev-latest`, `crew-wiki:dev-${GITHUB_SHA}`
- prod: `crew-wiki:prod-latest`, `crew-wiki:prod-${GITHUB_SHA}`

이렇게 분리해 develop 브랜치 배포가 main 브랜치 배포 이미지를 덮어쓰지 않도록 한다.

## 서버 디렉터리와 포트

dev/prod는 서로 다른 인스턴스에서 실행된다.
따라서 각 인스턴스 내부의 배포 디렉터리, 컨테이너 이름, 기본 호스트 포트는 동일하게 사용한다.

- 배포 디렉터리: `/home/ubuntu/crew-wiki`
- 컨테이너 이름: `crew-wiki`
- 기본 호스트 포트: `8080`

같은 인스턴스 안에서 dev/prod를 동시에 실행하는 구조로 바뀌면 그때 디렉터리, 컨테이너 이름, 포트를 환경별로 분리한다.

## Runner 라우팅

dev/prod 인스턴스가 다르면 GitHub Actions self-hosted runner도 환경별로 명확히 라우팅되어야 한다.

워크플로가 `runs-on: self-hosted`만 사용하면 GitHub가 접근 가능한 self-hosted runner 중 하나를 선택할 수 있다.
dev runner와 prod runner가 같은 저장소에 모두 등록되어 있고 추가 label이 없다면 dev 배포가 prod 인스턴스에서 실행되거나, 반대로 prod 배포가 dev 인스턴스에서 실행될 위험이 있다.

실제 점검 중 dev 인스턴스의 runner에서 `prod-cd.yml`이 실행된 흔적이 확인되었다.
따라서 워크플로는 generic `self-hosted`가 아니라 환경별 label을 반드시 지정한다.

runner는 인스턴스마다 하나씩 둔다.

- dev 인스턴스 runner name: `crew-wiki-dev`
- dev 인스턴스 runner label: `crew-wiki-dev`
- prod 인스턴스 runner name: `crew-wiki-prod`
- prod 인스턴스 runner label: `crew-wiki-prod`

워크플로의 build/deploy job은 다음처럼 환경별 label을 지정한다.

```yml
# dev-cd.yml
runs-on: [self-hosted, crew-wiki-dev]

# prod-cd.yml
runs-on: [self-hosted, crew-wiki-prod]
```

runner 재설정 절차는 다음 순서로 진행한다.

1. GitHub repository settings에서 기존 runner를 확인한다.
2. dev 인스턴스의 기존 runner는 `crew-wiki-dev` 이름과 label로 재등록한다.
3. prod 인스턴스에는 새 runner를 설치하고 `crew-wiki-prod` 이름과 label로 등록한다.
4. 각 인스턴스에서 runner service가 `active (running)`인지 확인한다.
5. GitHub repository settings의 runner 목록에서 dev/prod runner가 각각 Online인지 확인한다.

runner 등록 명령은 GitHub UI에서 발급되는 registration token이 필요하다.
토큰은 짧은 시간만 유효하므로 runner 재등록 시점에 GitHub UI에서 새로 발급받는다.

등록 명령 예시는 다음과 같다.

```sh
./config.sh \
  --url https://github.com/Crew-Wiki/backend \
  --token <registration-token> \
  --name crew-wiki-dev \
  --labels crew-wiki-dev \
  --unattended \
  --replace

sudo ./svc.sh install
sudo ./svc.sh start
```

prod 인스턴스에서는 `--name crew-wiki-prod`, `--labels crew-wiki-prod`를 사용한다.

## 필요한 GitHub Secrets

공통:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `JWT_SECRET_KEY`
- `JWT_SECRET_EXPIRE_LENGTH`
- `AWS_CREDENTIALS_ACCESS_KEY`
- `AWS_CREDENTIALS_SECRET_KEY`
- `S3_BUCKET`

dev:

- `DEV_DB_URL`
- `DEV_DB_USERNAME`
- `DEV_DB_PASSWORD`
- `DEV_HOST_PORT` optional
- `DEV_CORS_ALLOWED_ORIGINS` optional
- `DEV_SWAGGER_SERVER_URL` optional

prod:

- `PROD_DB_URL`
- `PROD_DB_USERNAME`
- `PROD_DB_PASSWORD`
- `PROD_HOST_PORT` optional
- `PROD_CORS_ALLOWED_ORIGINS` optional
- `PROD_SWAGGER_SERVER_URL` optional

GitHub Secrets 값에는 YAML key를 포함하지 않고 실제 값만 저장한다.
예를 들어 `PROD_DB_URL` secret value는 `jdbc:mysql://...` 형식이어야 하며, `url: jdbc:mysql://...`처럼 설정 파일 조각을 넣지 않는다.

## 운영상 이점

- 배포 상태가 `docker-compose.yml`에 명시된다.
- dev/prod 이미지 태그 충돌을 피한다.
- 실제 비밀값은 Git 저장소와 Docker 이미지에 들어가지 않는다.
- `application-private.yml` 이름에서 오는 혼란이 사라진다.

## 주의점

- self-hosted runner가 실제 배포 서버와 같은 인스턴스라는 가정에서는 SSH가 필요 없다.
- runner와 배포 서버가 다르면 GitHub Actions에서 SSH로 서버에 접속해 `docker-compose.yml`과 `.env`를 배치해야 한다.
- 서버에는 Docker Compose v2(`docker compose`) 또는 v1(`docker-compose`)가 설치되어 있어야 한다.
- 기존 `docker run` 방식으로 생성된 `crew-wiki` 컨테이너는 compose label이 없으므로, 최초 compose 전환 배포 때 제거한 뒤 compose가 다시 생성한다.
