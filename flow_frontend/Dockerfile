# Vue.js 애플리케이션 빌드
FROM node:20-alpine AS build

# Enable pnpm
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
RUN corepack enable

WORKDIR /app

# Copy package files
COPY package.json pnpm-lock.yaml ./

# Install dependencies
RUN pnpm install --frozen-lockfile --prod=false

# Copy source code
COPY . .

# Build the application
RUN pnpm run build

# Production stage - 정적 파일만
FROM alpine:latest

# Copy built files from build stage
COPY --from=build /app/dist /app

# Expose port (사실상 의미없음 - infra nginx가 처리)
EXPOSE 80

# 컨테이너 유지만을 위한 명령어
CMD ["tail", "-f", "/dev/null"]