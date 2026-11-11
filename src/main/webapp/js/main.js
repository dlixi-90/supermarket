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
