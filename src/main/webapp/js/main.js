// Menu Open Close
let menuIcon = document.querySelector(".menu-icon");
let navLinks = document.querySelector(".nav-links");

menuIcon.onclick = () => {
  menuIcon.classList.toggle("move");
  navLinks.classList.toggle("open-menu");
};
// Close Menu On Scroll
window.onscroll = () => {
  menuIcon.classList.remove("move");
  navLinks.classList.remove("open-menu");
};
// Header
let header = document.querySelector("header");

window.addEventListener("scroll", () => {
  header.classList.toggle("shadow", window.scrollY > 0);
});

// Reveal each Home element once per page load; other pages stay static.
if (document.body.dataset.page === "home" && typeof window.ScrollReveal === "function") {
  const animate = window.ScrollReveal({
    origin: "top",
    distance: "60px",
    duration: 1500,
    delay: 400,
    reset: false,
    cleanup: true,
  });

  animate.reveal(".nav, .heading");
  animate.reveal(".home-text, .shipping-content", { origin: "left" });
  animate.reveal(".home-img", { origin: "bottom" });
  animate.reveal(".shipping-image", { origin: "right" });
  // Product cards are filtered after load; ScrollReveal can leave cards that
  // started with the hidden attribute invisible after a category is selected.
  animate.reveal(".box, .banner-box, .footer-col", {
    interval: 100,
  });
}
