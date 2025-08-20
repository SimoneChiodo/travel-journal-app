// TO CALCULATE LATITUDE AND LONGITUDE FROM PLACE INPUT
// Initialize an invisible map for the geocoder
var map = L.map(document.createElement('div')).setView([41.9, 12.5], 6);

// Initialize the geocoder
var geocoder = L.Control.Geocoder.nominatim();

// Listen for changes on the "place" input field after the DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  // Initialize a modern geocoder
  var geocoder = L.Control.Geocoder.nominatim();

  // When the input loses focus, update latitude and longitude fields
  document.getElementById("place").addEventListener("blur", function() {
    var query = this.value;
    if (query && geocoder) {
      geocoder.geocode(query).then(results => {
        if (results && results.length > 0) {
          var latlng = results[0].center;
          document.getElementById("latitude").value = latlng.lat;
          document.getElementById("longitude").value = latlng.lng;
        }
      }).catch(err => {
        // Handle geocoding errors
        console.error("Geocoding error:", err);
      });
    }
  });
});

// TO SHOW SUGGESTIONS IN PLACE INPUT
const input = document.getElementById('place');
const suggestions = document.getElementById('suggestions');

let debounceTimeout;

// Debounce function to reduce geocoder requests
function debounce(func, delay) {
  return (...args) => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(() => func(...args), delay);
  };
}

// Function to search for cities
async function searchCities(query) {
  if (!query || !geocoder) return;

  const results = await geocoder.geocode(query);

  // Clear previous suggestions
  suggestions.innerHTML = "";

  // Populate suggestion list
  results.forEach(res => {
    const li = document.createElement('li');
    li.classList.add('list-group-item', 'list-group-item-action'); // Bootstrap styling
    li.textContent = res.name || res.html || res.display_name; 
    li.dataset.lat = res.center.lat;
    li.dataset.lon = res.center.lng;

    // When a suggestion is clicked, fill input and hidden fields
    li.addEventListener('click', () => {
      input.value = li.textContent;
      document.getElementById("latitude").value = res.center.lat;
      document.getElementById("longitude").value = res.center.lng;
      suggestions.innerHTML = '';
    });

    suggestions.appendChild(li);
  });
}

// Input event with debounce to search cities
input.addEventListener("input", debounce((e) => {
  const query = e.target.value;
  searchCities(query);
}, 300));

// Close the suggestion list if clicking outside
document.addEventListener("click", (e) => {
  if (!input.contains(e.target) && !suggestions.contains(e.target)) {
    suggestions.innerHTML = "";
  }
});
