// When page start
document.addEventListener('DOMContentLoaded', () => {

  // Function to make images and videos input links interactive
  function initLinkInputs(containerId, inputName) {
    const container = document.getElementById(containerId);

    // Add logic to all the buttons in a row
    function initRow(row) {
      const addBtn = row.querySelector('.add-btn'); // Take "Add another input" button
      const removeBtn = row.querySelector('.remove-btn'); // Take "Remove this input" button
      const input = row.querySelector(`input[name="${inputName}"]`); // Take the input

      // Add logic to "Add another input" button
      addBtn.addEventListener('click', () => {
        // Don't create another input if value is empty
        if (!input.value.trim()) return; 

        addBtn.classList.add('d-none'); // Hide Add button
        removeBtn.classList.remove('d-none'); // Show Remove button

        const newRow = row.cloneNode(true); // Create another row
        const newInput = newRow.querySelector(`input[name="${inputName}"]`);  // Get the input in new row
        newInput.value = ''; // Reset input value
        newRow.querySelector('.add-btn').classList.remove('d-none'); // Show Add button
        newRow.querySelector('.remove-btn').classList.add('d-none'); // Hide Remove button
        container.appendChild(newRow); // Add input to HTML
        initRow(newRow); // Add logic to the new input
      });

      // Add logic to "Remove this input" button
      removeBtn.addEventListener('click', () => {
        row.remove();
      });
    }

    // Initialize all rows present in HTML
    const rows = container.querySelectorAll('.input-group');
    rows.forEach(row => initRow(row));
  }

  // Add logic to Previews
  function initPreviewRemoveButtons(containerId) {
    const container = document.getElementById(containerId);

    // Add logic to "Remove this preview" button
    container.querySelectorAll('.remove-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const parent = btn.closest('.col'); 
        if (parent) 
          parent.remove();
      });
    });
  }

  // Initialize input link for images and videos
  initLinkInputs('imageUrlContainer', 'photoLinks');
  initLinkInputs('videoUrlContainer', 'videoLinks');

  // Initialize preview for images and videos
  initPreviewRemoveButtons('photoPreview');
  initPreviewRemoveButtons('videoPreview');

});
