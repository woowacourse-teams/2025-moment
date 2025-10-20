// --- 캐시 설정 ---
// PWA 필수 리소스만 캐싱 (manifest, icons, offline.html)
const CACHE_NAME = 'moment-pwa-cache-v1';
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

  // 같은 origin의 리소스가 아니면 개입하지 않음
  if (url.origin !== self.location.origin) {
    return;
  }

  // PWA 필수 리소스(manifest, icons, offline.html)만 캐싱: Cache First
  const isOfflineResource = urlsToCache.some(cachedUrl => url.pathname === cachedUrl);
  if (isOfflineResource) {
    event.respondWith(
      caches.match(request).then(cachedResponse => {
        return cachedResponse || fetch(request);
      }),
    );
    return;
  }

  // 나머지 모든 리소스(JS, CSS, 이미지, 폰트, API 등)는 브라우저 기본 처리에 맡김
  // Service Worker가 개입하지 않음
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
