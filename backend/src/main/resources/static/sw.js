// Epigraph Service Worker
// Handles incoming push events and notification clicks.

self.addEventListener('push', event => {
    let data = {};
    try {
        data = event.data ? event.data.json() : {};
    } catch (_) {
    }

    const title = data.title || 'Цитата дня · Epigraph';
    const options = {
        body: data.body || '',
        icon: data.icon || '/icon.png',
        badge: '/icon.png',
        tag: 'epigraph-qod',   // replaces previous, no stacking
        renotify: false,
        data: {url: '/'}
    };

    event.waitUntil(self.registration.showNotification(title, options));
});

self.addEventListener('notificationclick', event => {
    event.notification.close();
    event.waitUntil(
        clients.matchAll({type: 'window', includeUncontrolled: true}).then(list => {
            // Focus existing tab if open
            for (const client of list) {
                if (client.url.includes(self.location.origin) && 'focus' in client) {
                    return client.focus();
                }
            }
            // Otherwise open new tab
            return clients.openWindow(event.notification.data.url);
        })
    );
});
