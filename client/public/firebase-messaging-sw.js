// --- 캐시 설정 ---
// 빌드마다 새로운 캐시를 사용하도록 버전 관리
const CACHE_VERSION = '__BUILD_VERSION__';
const CACHE_NAME = `moment-cache-${CACHE_VERSION}`;
// 오프라인 지원을 위한 필수 리소스만 캐싱
const urlsToCache = ['/manifest.json', '/icon-192x192.png', '/icon-512x512.png', '/offline.html'];

// --- Firebase 초기화 ---
try {
  importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-app-compat.js');
  importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-messaging-compat.js');

  firebase.initializeApp({
    apiKey: 'AIzaSyD4qy5-cB5BclCX36uoyWx0RjOs2vZ-i1c',
    authDomain: 'moment-8787a.firebaseapp.com',
    projectId: 'moment-8787a',
    storageBucket: 'moment-8787a.firebasestorage.app',
    messagingSenderId: '138468882061',
    appId: '1:138468882061:web:d2b1ee112d4e98a322d4c4',
    measurementId: 'G-NM044KCWN9',
  });

  // Firebase가 자동으로 백그라운드 알림을 처리하므로 별도 핸들러 불필요
  // -> 필요 시 messaging.onBackgroundMessage()로 커스텀 처리 가능
} catch (error) {
  console.warn('[SW] Firebase FCM 초기화 실패:', error);
}

self.addEventListener('install', event => {
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then(cache => {
        return cache.addAll(urlsToCache);
      })
      .then(() => self.skipWaiting()),
  );
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches
      .keys()
      .then(cacheNames => {
        return Promise.all(
          cacheNames.map(cacheName => {
            if (cacheName !== CACHE_NAME) {
              return caches.delete(cacheName);
            }
          }),
        );
      })
      .then(() => self.clients.claim()),
  );
});

self.addEventListener('fetch', event => {
  const request = event.request;
  const url = new URL(request.url);

  const isApiRequest =
    url.hostname === 'localhost' ||
    url.hostname.startsWith('api-') ||
    url.hostname.startsWith('api.') ||
    url.pathname.startsWith('/api/');

  if (isApiRequest) {
    //  API 요청 -> 서비스 워커를 거치지 않고 바로 네트워크로 요청
    return;
  }

  // 오프라인 지원용 정적 리소스 (manifest, icons, offline.html)만 캐시 우선
  const isOfflineResource = urlsToCache.some(cachedUrl => url.pathname === cachedUrl);

  if (isOfflineResource) {
    event.respondWith(
      caches.match(request).then(cachedResponse => {
        return cachedResponse || fetch(request);
      }),
    );
    return;
  }

  event.respondWith(
    fetch(request)
      .then(response => {
        if (!response || response.status !== 200 || response.type === 'error') {
          return response;
        }
        return response;
      })
      .catch(() => {
        // 오프라인 또는 네트워크 오류 시에만 캐시에서 찾거나 오프라인 페이지 표시
        return caches.match(request).then(cachedResponse => {
          if (cachedResponse) {
            return cachedResponse;
          }
          // HTML 요청이면 오프라인 페이지 표시
          if (request.headers.get('accept')?.includes('text/html')) {
            return caches.match('/offline.html');
          }
        });
      }),
  );
});

self.addEventListener('notificationclick', event => {
  event.notification.close();

  const urlToOpen = event.notification.data?.redirectUrl || '/';

  event.waitUntil(
    clients.matchAll({ type: 'window' }).then(clientList => {
      for (const client of clientList) {
        if (client.url.startsWith(self.location.origin)) {
          client.navigate(urlToOpen);
          return client.focus();
        }
      }

      if (clients.openWindow) {
        return clients.openWindow(urlToOpen);
      }
    }),
  );
});
