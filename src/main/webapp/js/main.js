const menuIcon = document.querySelector(".menu-icon");
const navLinks = document.querySelector(".nav-links");

const setMenuOpen = (open) => {
  menuIcon?.classList.toggle("move", open);
  menuIcon?.setAttribute("aria-expanded", String(open));
  navLinks?.classList.toggle("open-menu", open);
};

menuIcon?.addEventListener("click", () => setMenuOpen(menuIcon.getAttribute("aria-expanded") !== "true"));

const header = document.querySelector("header");
window.addEventListener("scroll", () => {
  setMenuOpen(false);
  header?.classList.toggle("shadow", window.scrollY > 0);
}, { passive: true });
