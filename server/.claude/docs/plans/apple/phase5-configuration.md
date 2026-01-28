# Phase 5: Configuration 구현

## 목표
Apple 로그인에 필요한 환경 설정을 추가합니다.

---

## 1. 환경 변수 설정

### 필요 환경 변수

| 환경 변수 | 설명 | 예시 |
|----------|------|------|
| `APPLE_CLIENT_IDS` | 허용된 Apple Client ID 목록 (쉼표 구분) | `com.moment.app,com.moment.app.dev` |

---

## 2. application-dev.yml 수정

### 파일 위치
`src/main/resources/application-dev.yml`

### 추가 내용
```yaml
auth:
  google:
    # 기존 Google 설정 유지
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: ${GOOGLE_REDIRECT_URI}
    client-uri: ${GOOGLE_CLIENT_URI}
  apple:
    client-ids: ${APPLE_CLIENT_IDS:com.moment.app}  # 기본값 설정
```

---

## 3. application-prod.yml 수정

### 파일 위치
`src/main/resources/application-prod.yml`

### 추가 내용
```yaml
auth:
  google:
    # 기존 Google 설정 유지
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: ${GOOGLE_REDIRECT_URI}
    client-uri: ${GOOGLE_CLIENT_URI}
  apple:
    client-ids: ${APPLE_CLIENT_IDS}
```

---

## 4. application-test.yml 수정 (테스트용)

### 파일 위치
`src/test/resources/application-test.yml` (또는 `application.yml`)

### 추가 내용
```yaml
auth:
  apple:
    client-ids: com.moment.app,com.moment.app.test
```

---

## 5. 캐시 설정 추가

### 5.1 Caffeine 의존성 확인

#### build.gradle
```gradle
dependencies {
    // 기존 의존성...

    // 캐싱 (없으면 추가)
    implementation 'com.github.ben-manes.caffeine:caffeine'
}
```

### 5.2 CacheConfig 생성/수정

#### 파일 위치
`src/main/java/moment/global/config/CacheConfig.java`

#### 구현
```java
package moment.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // applePublicKeys 캐시 설정 (5분 만료)
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(100));

        // 필요한 캐시 이름 등록
        cacheManager.setCacheNames(java.util.List.of("applePublicKeys"));

        return cacheManager;
    }
}
```

### 5.3 기존 캐시 설정 확인

기존에 CacheConfig가 있다면 `applePublicKeys` 캐시만 추가:

```java
// 기존 CacheConfig에 추가
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager(
        "existingCache1",
        "existingCache2",
        "applePublicKeys"  // 추가
    );
    // ...
}
```

---

## 6. RestTemplate Bean 확인/추가

### 파일 위치
`src/main/java/moment/global/config/RestTemplateConfig.java`

### 확인 사항
기존에 `RestTemplate` Bean이 있는지 확인

### 없으면 생성
```java
package moment.global.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();
    }
}
```

---

## 7. 로컬 개발 환경 설정

### .env.example 업데이트 (있다면)
```env
# Apple Sign In
APPLE_CLIENT_IDS=com.moment.app,com.moment.app.dev
```

### IntelliJ 환경 변수 설정
Run Configuration → Environment variables에 추가:
```
APPLE_CLIENT_IDS=com.moment.app
```

---

## 8. 보안 설정 확인

### SecurityConfig 확인
Apple 로그인 엔드포인트가 인증 없이 접근 가능한지 확인

#### 파일 위치
`src/main/java/moment/global/config/SecurityConfig.java`

#### 확인 사항
```java
// /api/v2/auth/** 경로가 permitAll인지 확인
.requestMatchers("/api/v2/auth/**").permitAll()
```

---

## 구현 순서

### Step 1: 의존성 확인
1. `build.gradle`에 Caffeine 의존성 확인/추가
2. `./gradlew build` 실행

### Step 2: 캐시 설정
1. 기존 `CacheConfig` 확인
2. 없으면 생성, 있으면 `applePublicKeys` 추가

### Step 3: RestTemplate 설정
1. 기존 `RestTemplate` Bean 확인
2. 없으면 `RestTemplateConfig` 생성

### Step 4: application.yml 수정
1. `application-dev.yml` 수정
2. `application-prod.yml` 수정
3. `application-test.yml` 수정 (또는 생성)

### Step 5: 보안 설정 확인
1. `SecurityConfig` 확인
2. `/api/v2/auth/apple` 경로 접근 허용 확인

### Step 6: 로컬 테스트
1. 환경 변수 설정
2. `./gradlew bootRun` 실행
3. 애플리케이션 정상 구동 확인

---

## 체크리스트

- [ ] `build.gradle` Caffeine 의존성 확인/추가
- [ ] `CacheConfig` 생성 또는 수정
  - [ ] `@EnableCaching` 활성화
  - [ ] `applePublicKeys` 캐시 설정 (5분 만료)
- [ ] `RestTemplate` Bean 확인/생성
- [ ] `application-dev.yml` 수정
  - [ ] `auth.apple.client-ids` 추가
- [ ] `application-prod.yml` 수정
  - [ ] `auth.apple.client-ids` 추가
- [ ] 테스트용 설정 파일 수정
- [ ] `SecurityConfig` 확인
  - [ ] `/api/v2/auth/apple` 경로 접근 허용
- [ ] 로컬 환경 변수 설정
- [ ] `./gradlew build` 성공
- [ ] `./gradlew bootRun` 정상 구동
- [ ] `./gradlew fastTest` 전체 통과

---

## 배포 시 필요 작업

### AWS/서버 환경 변수 설정
```bash
# 환경 변수 추가
export APPLE_CLIENT_IDS=com.moment.app,com.moment.app.service
```

### Docker Compose (사용 시)
```yaml
services:
  app:
    environment:
      - APPLE_CLIENT_IDS=${APPLE_CLIENT_IDS}
```

### Kubernetes ConfigMap/Secret (사용 시)
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: moment-config
data:
  APPLE_CLIENT_IDS: "com.moment.app"
```