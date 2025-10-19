/* eslint-env browser, serviceworker */
/* global importScripts, firebase, self, URL, clients, caches */

// --- 캐시 설정 ---
const CACHE_NAME = 'moment-cache-v1';
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

  const messaging = firebase.messaging();

  messaging.onBackgroundMessage(({ notification, data }) => {
    const title = (notification && notification.title) || '새 알림';
    const options = {
      body: (notification && notification.body) || '',
      icon: '/icon-512x512.png',
      badge: '/icon-192x192.png',
      data,
      requireInteraction: true,
    };

    self.registration.showNotification(title, options);
  });
} catch (error) {
  console.warn('[SW] Firebase FCM 초기화 실패:', error);
}

// MSW 로드 (개발 환경)
if (self.location.hostname === 'localhost') {
  try {
    importScripts('/mockServiceWorker.js');
  } catch {
    // MSW 로드 실패는 정상 (프로덕션 환경)
  }
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

  // API 요청은 서비스 워커가 개입하지 않고 브라우저가 직접 처리하도록 함
  const isApiRequest =
    url.hostname === 'localhost' ||
    url.hostname.startsWith('api-') ||
    url.hostname.startsWith('api.') ||
    request.headers.get('content-type')?.includes('application/json');

  if (isApiRequest) {
    // early return: 서비스 워커를 거치지 않고 바로 네트워크로 요청
    return;
  }

  // 정적 리소스만 캐싱 처리
  event.respondWith(
    caches.match(request).then(cachedResponse => {
      if (cachedResponse) {
        return cachedResponse;
      }

      return fetch(request)
        .then(response => {
          // 캐싱 가능한 응답인지 체크
          if (
            !response ||
            response.status !== 200 ||
            response.type === 'error' ||
            response.type === 'opaque'
          ) {
            return response;
          }

          // GET 요청만 캐싱
          if (request.method === 'GET') {
            const responseToCache = response.clone();
            caches
              .open(CACHE_NAME)
              .then(cache => {
                return cache.put(request, responseToCache);
              })
              .catch(() => {
                // 캐싱 실패는 조용히 무시 (앱 동작에 영향 없음)
              });
          }

          return response;
        })
        .catch(() => {
          // 오프라인 또는 네트워크 오류 시 대체 페이지
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
