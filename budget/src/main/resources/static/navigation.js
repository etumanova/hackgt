// Add this script to your HTML or existing JavaScript file
document.addEventListener('DOMContentLoaded', function() {
  // Get current page from URL
  const currentPage = window.location.pathname;
  
  // Map URLs to button data-page values
  const pageMap = {
    '/': 'dashboard',
    '/budgeter': 'budgeter',
    '/invest': 'invest'
  };
  
  // Find the current active page
  let activePage = 'dashboard'; // default
  for (const [path, page] of Object.entries(pageMap)) {
    if (currentPage === path || currentPage.startsWith(path + '/')) {
      activePage = page;
      break;
    }
  }
  
  // Set active class to the correct button
  const activeButton = document.querySelector(`.sidebar-btn[data-page="${activePage}"]`);
  if (activeButton) {
    activeButton.classList.add('active');
  }
  
  // Add click handlers to update active state
  document.querySelectorAll('.sidebar-btn').forEach(button => {
    button.addEventListener('click', function(e) {
      // Remove active class from all buttons
      document.querySelectorAll('.sidebar-btn').forEach(btn => {
        btn.classList.remove('active');
      });
      
      // Add active class to clicked button
      this.classList.add('active');
    });
  });
});