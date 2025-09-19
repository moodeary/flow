# Flow Project Structure

## 프로젝트 개요
Spring Boot + Vue.js + Nginx를 사용한 풀스택 웹 애플리케이션의 모노레포 구조

## 디렉토리 구조

```
flow/                           # 루트 디렉토리 (모노레포)
├── .git/                       # Git 저장소
├── .gitignore                  # Git 무시 파일 설정
├── README.md                   # 프로젝트 전체 설명
├── STRUCTURE.md               # 이 파일 (프로젝트 구조 문서)
├── docker-compose.yml          # 전체 서비스 오케스트레이션
│
├── flow_backend/              # Spring Boot 백엔드
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/flow/
│   │   │   │   ├── Application.java
│   │   │   │   ├── common/          # 공통 설정/유틸리티
│   │   │   │   ├── config/          # 설정 클래스
│   │   │   │   └── domain/          # 도메인별 패키지
│   │   │   │       ├── extension/   # 확장자 관리 도메인
│   │   │   │       └── file/        # 파일 관리 도메인
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       └── application-prod.yml
│   │   └── test/                    # 테스트 코드
│   ├── build.gradle                 # 빌드 설정
│   ├── Dockerfile                   # 백엔드 컨테이너 설정
│   ├── gradlew                      # Gradle 래퍼
│   └── logs/                        # 애플리케이션 로그
│
├── flow_frontend/             # Vue.js 프론트엔드
│   ├── src/
│   │   ├── components/              # Vue 컴포넌트
│   │   │   ├── common/              # 공통 컴포넌트
│   │   │   └── extensions/          # 확장자 관련 컴포넌트
│   │   ├── views/                   # 페이지 컴포넌트
│   │   ├── stores/                  # Pinia 상태 관리
│   │   ├── router/                  # Vue Router 설정
│   │   ├── api/                     # API 통신 모듈
│   │   ├── assets/                  # 정적 리소스
│   │   └── main.js                  # 앱 진입점
│   ├── public/                      # 공개 정적 파일
│   ├── package.json                 # 의존성 관리
│   ├── pnpm-lock.yaml              # 패키지 잠금 파일
│   ├── Dockerfile                   # 프론트엔드 컨테이너 설정
│   ├── vite.config.js              # Vite 빌드 설정
│   └── playwright.config.js         # E2E 테스트 설정
│
└── flow_nginx/                # Nginx 리버스 프록시
    ├── nginx.conf                   # Nginx 설정 파일
    └── Dockerfile.nginx             # Nginx 컨테이너 설정
```

## 서비스 구성

### 1. Backend (flow_backend)
- **기술 스택**: Spring Boot 3.x, Java 17, Gradle
- **포트**: 8080
- **역할**: REST API 서버, 비즈니스 로직 처리
- **주요 기능**:
  - 파일 업로드/다운로드
  - 확장자 관리 (고정/커스텀)
  - 데이터베이스 연동 (MariaDB)

### 2. Frontend (flow_frontend)
- **기술 스택**: Vue.js 3, Vite, Pinia, Vue Router
- **개발 포트**: 5173
- **역할**: 사용자 인터페이스, SPA
- **주요 기능**:
  - 파일 업로드 인터페이스
  - 확장자 설정 관리
  - 반응형 웹 디자인

### 3. Nginx (flow_nginx)
- **포트**: 80 (HTTP), 443 (HTTPS)
- **역할**: 리버스 프록시, 정적 파일 서빙
- **기능**:
  - Frontend 정적 파일 제공
  - API 요청을 Backend로 프록시
  - 로드 밸런싱 (필요시)

### 4. Database
- **기술**: MariaDB 11.2
- **포트**: 3306
- **역할**: 데이터 영속성
- **데이터**: 파일 메타데이터, 확장자 설정

## 배포 환경

### 개발 환경
```bash
# Backend 실행
cd flow_backend
./gradlew bootRun

# Frontend 실행
cd flow_frontend
pnpm dev
```

### Docker 배포
```bash
# 전체 스택 실행
docker-compose up -d

# 개별 서비스 실행
docker-compose up -d mariadb
docker-compose up -d flow_backend
docker-compose up -d flow_frontend
docker-compose up -d nginx
```

## 네트워크 구성

```
[Client]
    ↓ (Port 80/443)
[Nginx Reverse Proxy]
    ├─→ [Frontend Static Files] (Vue.js SPA)
    └─→ [Backend API] (Port 8080)
            ↓
        [MariaDB] (Port 3306)
```

## Git 관리 전략

### 모노레포 장점
- **통합 버전 관리**: 전체 프로젝트의 일관된 버전 태깅
- **의존성 동기화**: Frontend-Backend 간 API 스펙 일치
- **배포 단순화**: 단일 저장소에서 전체 스택 배포
- **코드 공유**: 공통 설정 및 유틸리티 공유

### 브랜치 전략
- `main`: 프로덕션 배포 브랜치
- `develop`: 개발 통합 브랜치
- `feature/*`: 기능 개발 브랜치
- `hotfix/*`: 긴급 수정 브랜치

## 개발 워크플로우

1. **로컬 개발**
   - Backend와 Frontend를 각각 독립적으로 실행
   - API 통신은 CORS 설정으로 해결

2. **통합 테스트**
   - Docker Compose로 전체 환경 구성
   - E2E 테스트 실행

3. **배포**
   - Docker 이미지 빌드 및 레지스트리 푸시
   - 운영 환경에서 Docker Compose 실행

## 확장성 고려사항

- **마이크로서비스 전환**: 필요시 각 도메인을 독립 서비스로 분리 가능
- **로드 밸런싱**: Nginx에서 여러 Backend 인스턴스로 분산 가능
- **데이터베이스 확장**: 읽기 전용 복제본, 샤딩 등 적용 가능
- **CDN 연동**: 정적 리소스의 CDN 배포 고려