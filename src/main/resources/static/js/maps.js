// TO SHOW MAPS IN PAGE
// Initialize the map
const map = L.map('map').setView([41.9, 12.5], 6); // Default view on Rome

// Add OpenStreetMap tile layer
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '&copy; OpenStreetMap contributors'
}).addTo(map);
