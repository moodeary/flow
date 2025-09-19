# Flow Project

Spring Boot + Vue.js 웹 애플리케이션

## 프로젝트 구조

```
flow/
├── flow_frontend/    # Vue.js 프론트엔드
├── flow_backend/     # Spring Boot 백엔드
├── flow_nginx/       # Nginx 설정
├── docker-compose.yml
└── README.md
```

## 개발 환경 설정

### 로컬 개발
1. Backend: `cd flow_backend && ./gradlew bootRun`
2. Frontend: `cd flow_frontend && pnpm dev`

### Docker 배포
```bash
docker-compose up -d
```

## 서비스 구성
- **Frontend**: Vue.js (Port: 5173)
- **Backend**: Spring Boot (Port: 8080)
- **Database**: MariaDB (Port: 3306)
- **Reverse Proxy**: Nginx (Port: 80, 443)# flow
