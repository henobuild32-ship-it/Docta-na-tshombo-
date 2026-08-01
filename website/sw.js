const CACHE = "docta-tshombo-v1.4.0";
const PRECACHE = [
  "./",
  "./index.html",
  "./app.html",
  "./manifest.webmanifest",
  "./css/style.css",
  "./js/main.js",
  "./js/app.js",
  "./assets/app-icon.jpg",
  "./assets/teleconsult-banner.jpg"
];

// Installation & immediate activation for auto-updates
self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE).then((cache) => cache.addAll(PRECACHE)).catch(() => {})
  );
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k)))
    ).catch(() => {})
  );
  self.clients.claim();
});

// Skip waiting message from UI update banner
self.addEventListener("message", (event) => {
  if (event.data && event.data.type === "SKIP_WAITING") {
    self.skipWaiting();
  }
});

// BACKGROUND PUSH NOTIFICATIONS WITH SOUND & VIBRATION (WORKS EVEN WHEN APP CLOSED)
self.addEventListener("push", (event) => {
  let data = { title: "Docta na Tshombo", body: "Nouveau rappel ou message médical reçu !" };
  if (event.data) {
    try {
      data = event.data.json();
    } catch (e) {
      data.body = event.data.text();
    }
  }

  const options = {
    body: data.body || "Rappel de rendez-vous ou nouveau message.",
    icon: "./assets/app-icon.jpg",
    badge: "./assets/app-icon.jpg",
    vibrate: [200, 100, 200, 100, 200], // Vibration pattern
    sound: "./assets/notification.mp3", // Sound file
    tag: "docta-notification",
    renotify: true,
    requireInteraction: true,
    data: { url: "./app.html" },
    actions: [
      { action: "open", title: "🔔 Ouvrir L'App" },
      { action: "close", title: "Fermer" }
    ]
  };

  event.waitUntil(
    self.registration.showNotification(data.title || "Docta na Tshombo 🩺", options)
  );
});

// Handle notification click when app is closed
self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  if (event.action === "close") return;

  event.waitUntil(
    clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if (client.url.includes("app.html") && "focus" in client) {
          return client.focus();
        }
      }
      if (clients.openWindow) {
        return clients.openWindow("./app.html");
      }
    })
  );
});

// Network First for Navigation to detect updates immediately
self.addEventListener("fetch", (event) => {
  const request = event.request;

  if (request.url.indexOf(".apk") !== -1 || request.method !== "GET") {
    return;
  }

  if (request.mode === "navigate") {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const copy = response.clone();
          caches.open(CACHE).then((cache) => cache.put(request, copy)).catch(() => {});
          return response;
        })
        .catch(() =>
          caches.match(request).then((cached) => cached || caches.match("./app.html") || caches.match("./index.html"))
        )
    );
    return;
  }

  event.respondWith(
    caches.match(request).then((cached) => {
      const network = fetch(request)
        .then((response) => {
          if (response && response.ok && new URL(request.url).origin === self.location.origin) {
            const copy = response.clone();
            caches.open(CACHE).then((cache) => cache.put(request, copy)).catch(() => {});
          }
          return response;
        })
        .catch(() => cached);
      return cached || network;
    })
  );
});
