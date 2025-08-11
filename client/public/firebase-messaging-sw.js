// --- 필수 import (Service Worker 전용) ---
importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-messaging-compat.js');

// --- 캐시 설정 ---
const CACHE_NAME = 'moment-cache-v1';
const urlsToCache = ['/manifest.json', '/icon-192x192.png', '/icon-512x512.png', '/offline.html'];

// --- Firebase 초기화 ---
const firebaseConfig = {
  apiKey: 'AIzaSyD4qy5-cB5BclCX36uoyWx0RjOs2vZ-i1c',
  authDomain: 'moment-8787a.firebaseapp.com',
  projectId: 'moment-8787a',
  storageBucket: 'moment-8787a.firebasestorage.app',
  messagingSenderId: '138468882061',
  appId: '1:138468882061:web:d2b1ee112d4e98a322d4c4',
  measurementId: 'G-NM044KCWN9',
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// --- 백그라운드 메시지 처리 ---
messaging.onBackgroundMessage(payload => {
  const notificationTitle = payload.notification?.title || '새 알림';
  const notificationOptions = {
    body: payload.notification?.body || '',
    icon: '/icon-192x192.png',
    data: payload.data,
  };
  self.registration.showNotification(notificationTitle, notificationOptions);
});

// --- Service Worker 이벤트 처리 ---

// 설치(필수 파일 캐싱)
self.addEventListener('install', event => {
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
      .then(() => self.skipWaiting()),
  );
});

// 활성화(즉시 제어권)
self.addEventListener('activate', event => {
  event.waitUntil(self.clients.claim());
});

// 네트워크 요청 가로채기
self.addEventListener('fetch', event => {
  if (event.request.mode === 'navigate') {
    event.respondWith(fetch(event.request).catch(() => caches.match('/offline.html')));
  } else {
    event.respondWith(caches.match(event.request).then(res => res || fetch(event.request)));
  }
});

// 알림 클릭
self.addEventListener('notificationclick', event => {
  event.notification.close();
  const urlToOpen = event.notification.data?.url || '/';

  event.waitUntil(
    clients.matchAll({ type: 'window' }).then(clientList => {
      for (const client of clientList) {
        if (client.url === urlToOpen && 'focus' in client) return client.focus();
      }
      return clients.openWindow(urlToOpen);
    }),
  );
});
