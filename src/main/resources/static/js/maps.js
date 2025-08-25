// TO SHOW MAPS IN PAGE
const travels = document.querySelectorAll('.travels');

// Initialize the map
const map = L.map('map', {zoomControl: false}).setView([0, 0], 2); // Default view on Rome

// Add OpenStreetMap tile layer
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '&copy; OpenStreetMap contributors'
}).addTo(map);

// Add a marker for each travel
travels.forEach(travel => {
  const lat = travel.dataset.lat;
  const lon = travel.dataset.lon;
  const place = travel.textContent;

  L.marker([lat, lon]).addTo(map)
    .bindPopup(place);
});

L.control.zoom({
  position: 'bottomleft' // Values: topleft, topright, bottomleft, bottomright
}).addTo(map);