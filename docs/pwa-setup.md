# PWA 설치 프롬프트를 위한 체크리스트

PWA 설치 배너가 뜨려면 브라우저가 아래 조건들을 모두 충족한다고 판단해야 합니다.

1. **웹 앱 매니페스트**
   - `client/public/manifest.webmanifest` 파일이 존재하고 `name`, `short_name`, `start_url`, `display`, `background_color`, `theme_color` 등의 기본 속성이 채워져 있어야 합니다.
   - 192px 이상과 512px 이상 두 종류의 아이콘이 포함되어야 합니다. 현재는 `/images/logo.webp` 파일을 192×192와 512×512로 선언했습니다.
   - `index.html`에서 `<link rel="manifest" href="/manifest.webmanifest" />`로 참조해야 합니다.

2. **서비스 워커**
   - 오프라인 응답을 제공할 수 있는 서비스 워커가 있어야 합니다. `client/public/service-worker.js`가 이 역할을 담당합니다.
   - 서비스 워커는 프로덕션 번들에서만 등록되도록 `client/src/app/registerServiceWorker.ts`에서 가드하고 있습니다. 개발 서버에서는 MSW(`mockServiceWorker.js`)와 충돌하지 않도록 등록하지 않습니다.

3. **보안 컨텍스트**
   - HTTPS로 서비스되거나 `localhost`에서 접근해야 합니다. 그렇지 않으면 브라우저가 설치 가능 여부를 판단하지 않습니다.

4. **사용자 참여**
   - 사용자가 사이트를 충분히 방문하거나 브라우저가 "설치할 가치가 있다"고 판단해야 합니다. 이 조건은 브라우저마다 다르며, 개발자 도구의 Application 탭에서 `manifest`, `service workers` 섹션을 확인하면 현재 상태를 점검할 수 있습니다.

## 현재 저장소에 포함된 관련 파일

- `client/public/index.html` – 매니페스트 링크와 `theme-color` 메타 태그 추가
- `client/public/manifest.webmanifest` – 앱 이름, 표시 모드, 아이콘 정의
- `client/public/service-worker.js` – 간단한 오프라인 캐시 및 fetch 핸들러
- `client/src/app/registerServiceWorker.ts` – 프로덕션에서 서비스 워커 등록 로직
- `client/src/index.tsx` – 앱 초기화 시 서비스 워커 등록 함수 호출

위 파일들이 정상적으로 빌드 아티팩트에 포함되고 HTTPS 환경에서 배포된다면, 설치 프롬프트가 나타날 조건을 충족하게 됩니다.
