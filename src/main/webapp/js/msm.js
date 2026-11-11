// Shared MSM interactions for every page.
const headerLinks = [...document.querySelectorAll('.nav-links a.nav-link')];
const updateHeaderNavigation = () => {
  const homeLink = headerLinks.find(link => !new URL(link.href).hash && new URL(link.href).pathname.endsWith('/home'));
  const homePath = homeLink ? new URL(homeLink.href).pathname : '';
  const rootPath = homePath.replace(/home$/, '');
  const currentPath = location.pathname === rootPath || location.pathname === rootPath.replace(/\/$/, '')
    ? homePath : location.pathname;
  const sectionLink = headerLinks.find(link => {
    const url = new URL(link.href);
    return url.pathname === currentPath && url.hash && url.hash === location.hash;
  });
  headerLinks.forEach(link => {
    const url = new URL(link.href);
    const active = sectionLink ? link === sectionLink : url.pathname === currentPath && !url.hash;
    if (active) link.setAttribute('aria-current', url.hash ? 'location' : 'page');
    else link.removeAttribute('aria-current');
  });
};
updateHeaderNavigation();
window.addEventListener('hashchange', updateHeaderNavigation);
window.addEventListener('pageshow', updateHeaderNavigation);

const toastRegion = document.createElement('div');
toastRegion.className = 'toast-region';
toastRegion.setAttribute('aria-label', 'Thông báo');
toastRegion.setAttribute('aria-live', 'polite');
toastRegion.setAttribute('aria-relevant', 'additions');
document.body.appendChild(toastRegion);

const dismissToast = (toast) => {
  toast.classList.add('is-leaving');
  window.setTimeout(() => toast.remove(), 220);
};

window.showToast = (message, type = 'info', duration = 6500) => {
  if (!message) return;
  const toast = document.createElement('div');
  toast.className = `toast-notification toast-${type}`;
  toast.setAttribute('role', type === 'error' ? 'alert' : 'status');
  const icon = document.createElement('i');
  icon.className = type === 'error' ? 'ri-error-warning-line' : 'ri-checkbox-circle-line';
  icon.setAttribute('aria-hidden', 'true');
  const text = document.createElement('span');
  text.textContent = message;
  const close = document.createElement('button');
  close.className = 'toast-close';
  close.type = 'button';
  close.setAttribute('aria-label', 'Đóng thông báo');
  close.textContent = '×';
  close.addEventListener('click', () => dismissToast(toast));
  toast.append(icon, text, close);
  toastRegion.appendChild(toast);
  if (duration > 0) window.setTimeout(() => dismissToast(toast), duration);
};

document.querySelectorAll('.msm-content .error, .msm-content .success, .msm-content .message').forEach((notice) => {
  const type = notice.classList.contains('error') ? 'error' : 'success';
  const content = document.createElement('div');
  while (notice.firstChild) content.appendChild(notice.firstChild);
  notice.classList.add('toast-notification', `toast-${type}`);
  notice.setAttribute('role', type === 'error' ? 'alert' : 'status');
  const icon = document.createElement('i');
  icon.className = type === 'error' ? 'ri-error-warning-line' : 'ri-checkbox-circle-line';
  icon.setAttribute('aria-hidden', 'true');
  const close = document.createElement('button');
  close.className = 'toast-close';
  close.type = 'button';
  close.setAttribute('aria-label', 'Đóng thông báo');
  close.textContent = '×';
  close.addEventListener('click', () => dismissToast(notice));
  notice.append(icon, content, close);
  toastRegion.appendChild(notice);
  window.setTimeout(() => dismissToast(notice), type === 'error' ? 8500 : 6500);
});

document.querySelectorAll(".product-active-form").forEach((form) => {
  form.querySelector('[name="active"]').addEventListener("change", () => form.requestSubmit());
});

let cartRequestQueue = Promise.resolve();
document.addEventListener('submit', async (event) => {
  const form = event.target.closest('form.shop-cart-form');
  if (!form) return;

  event.preventDefault();
  const submitButton = event.submitter || form.querySelector('[name="action"][value="add"]');
  if (submitButton?.disabled) return;

  const originalDisabled = submitButton?.disabled ?? false;
  if (submitButton) submitButton.disabled = true;

  const previousRequest = cartRequestQueue;
  let completeRequest;
  cartRequestQueue = new Promise(resolve => { completeRequest = resolve; });

  try {
    await previousRequest;
    const formData = new URLSearchParams(new FormData(form));
    formData.set('action', 'add');
    // A form control named "action" overrides form.action in the browser DOM.
    const response = await fetch(form.getAttribute('action'), {
      method: 'POST',
      body: formData,
      credentials: 'same-origin',
      headers: {
        'X-Requested-With': 'XMLHttpRequest',
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      }
    });

    let result;
    try {
      result = await response.json();
    } catch {
      result = null;
    }

    if (result && result.requiresLogin && result.loginUrl) {
      window.location.assign(result.loginUrl);
      return;
    }

    if (!response.ok || !result || !result.success) {
      throw new Error((result && result.message) || 'Không thể thêm sản phẩm vào giỏ hàng. Vui lòng thử lại.');
    }

    const cartLink = document.querySelector('.cart-link');
    const cartCount = cartLink?.querySelector('.cart-count');
    if (cartCount) {
      cartCount.textContent = result.quantity > 99 ? '99+' : String(result.quantity);
      cartCount.hidden = result.quantity < 1;
    }
    if (cartLink) {
      const label = `Giỏ hàng, ${result.quantity} sản phẩm`;
      cartLink.setAttribute('aria-label', label);
      cartLink.setAttribute('title', label);
      cartLink.classList.remove('cart-link-added');
      void cartLink.offsetWidth;
      cartLink.classList.add('cart-link-added');
    }
    window.showToast?.(result.message || 'Đã thêm sản phẩm vào giỏ hàng.', 'success');
  } catch (error) {
    window.showToast?.(error.message || 'Không thể thêm sản phẩm vào giỏ hàng. Vui lòng thử lại.', 'error');
  } finally {
    if (submitButton) submitButton.disabled = originalDisabled;
    completeRequest();
  }
});

const categoryDialog = document.getElementById('category-dialog');
if (categoryDialog) {
  const categoryForm = document.getElementById('category-form');
  const categoryName = document.getElementById('category-name');
  const categoryId = document.getElementById('category-edit-id');
  const categoryImage = document.getElementById('category-image');
  const categoryPreview = document.getElementById('category-image-preview');
  const categoryPlaceholder = document.getElementById('category-image-placeholder');
  const categoryHeading = document.getElementById('category-form-title');
  const categorySubmit = document.getElementById('category-submit');
  const categoryImageError = document.getElementById('category-image-error');
  const openDialog = () => categoryDialog.showModal();
  const resetCategoryForm = () => {
    categoryForm.reset();
    categoryId.value = '';
    categoryName.value = '';
    categoryImage.value = '';
    categoryPreview.dataset.currentSrc = '';
    categoryPreview.removeAttribute('src');
    categoryPreview.hidden = true;
    categoryPlaceholder.hidden = false;
    categoryImageError.textContent = '';
    categoryHeading.textContent = 'Thêm danh mục';
    categorySubmit.value = 'insert';
    categorySubmit.textContent = 'Thêm danh mục';
  };

  document.getElementById('add-category-button')?.addEventListener('click', () => {
    resetCategoryForm();
    openDialog();
    categoryName.focus();
  });
  document.querySelectorAll('.category-edit-button').forEach((button) => {
    button.addEventListener('click', () => {
      resetCategoryForm();
      categoryId.value = button.dataset.id || '';
      categoryName.value = button.dataset.name || '';
      categoryPreview.dataset.currentSrc = button.dataset.image || '';
      if (categoryPreview.dataset.currentSrc) {
        categoryPreview.src = categoryPreview.dataset.currentSrc;
        categoryPreview.hidden = false;
        categoryPlaceholder.hidden = true;
      }
      categoryHeading.textContent = 'Cập nhật danh mục';
      categorySubmit.value = 'update';
      categorySubmit.textContent = 'Cập nhật danh mục';
      openDialog();
      categoryName.focus();
    });
  });
  categoryDialog.querySelectorAll('.category-dialog-close, .category-dialog-cancel').forEach((button) => {
    button.addEventListener('click', () => categoryDialog.close());
  });
  categoryDialog.addEventListener('click', (event) => {
    if (event.target === categoryDialog) categoryDialog.close();
  });
  if (categoryDialog.dataset.openOnLoad === 'true') openDialog();
}

const productDialog = document.getElementById('product-dialog');
if (productDialog) {
  const productForm = document.getElementById('product-form');
  const productId = document.getElementById('product-edit-id');
  const productName = document.getElementById('product-name');
  const productPrice = document.getElementById('product-price');
  const productStock = document.getElementById('product-stock');
  const productCategory = document.getElementById('product-category');
  const productManufactureDate = document.getElementById('product-manufacture-date');
  const productExpiryDate = document.getElementById('product-expiry-date');
  const productImageInput = document.getElementById('product-image');
  const productPreview = document.getElementById('product-image-preview');
  const productPlaceholder = document.getElementById('product-image-placeholder');
  const productActive = document.getElementById('product-active');
  const productHeading = document.getElementById('product-form-title');
  const productSubmit = document.getElementById('product-submit');
  const productImageError = document.getElementById('product-image-error');
  const openDialog = () => productDialog.showModal();
  const resetProductForm = () => {
    productForm.reset();
    productId.value = '';
    productImageInput.value = '';
    productPreview.dataset.currentSrc = '';
    productPreview.removeAttribute('src');
    productPreview.hidden = true;
    productPlaceholder.hidden = false;
    productImageError.textContent = '';
    productActive.checked = true;
    productExpiryDate.min = productManufactureDate.value || '0001-01-01';
    productHeading.textContent = 'Thêm sản phẩm';
    productSubmit.value = 'insert';
    productSubmit.textContent = 'Thêm sản phẩm';
  };

  document.getElementById('add-product-button')?.addEventListener('click', () => {
    resetProductForm();
    openDialog();
    productName.focus();
  });
  document.querySelectorAll('.product-edit-button').forEach((button) => {
    button.addEventListener('click', () => {
      resetProductForm();
      productId.value = button.dataset.id || '';
      productName.value = button.dataset.name || '';
      productPrice.value = button.dataset.price || '';
      productStock.value = button.dataset.stock || '';
      productCategory.value = button.dataset.categoryId || '';
      productManufactureDate.value = button.dataset.manufactureDate || '';
      productExpiryDate.value = button.dataset.expiryDate || '';
      productActive.checked = button.dataset.active === 'true';
      productPreview.dataset.currentSrc = button.dataset.image || '';
      if (productPreview.dataset.currentSrc) {
        productPreview.src = productPreview.dataset.currentSrc;
        productPreview.hidden = false;
        productPlaceholder.hidden = true;
      }
      productExpiryDate.min = productManufactureDate.value || '0001-01-01';
      productHeading.textContent = 'Cập nhật sản phẩm';
      productSubmit.value = 'update';
      productSubmit.textContent = 'Cập nhật sản phẩm';
      openDialog();
      productName.focus();
    });
  });
  productDialog.querySelectorAll('.category-dialog-close, .category-dialog-cancel').forEach((button) => {
    button.addEventListener('click', () => productDialog.close());
  });
  productDialog.addEventListener('click', (event) => {
    if (event.target === productDialog) productDialog.close();
  });
  if (productDialog.dataset.openOnLoad === 'true') openDialog();
}

const manufactureDateInput = document.getElementById("product-manufacture-date");
const expiryDateInput = document.getElementById("product-expiry-date");
if (manufactureDateInput && expiryDateInput) {
  const updateExpiryLimit = () => {
    expiryDateInput.min = manufactureDateInput.value || "0001-01-01";
  };
  manufactureDateInput.addEventListener("input", updateExpiryLimit);
  updateExpiryLimit();
}

document.querySelectorAll(".cart-quantity-form").forEach((form) => {
  const quantityInput = form.querySelector('[name="quantity"]');
  quantityInput.addEventListener("change", () => {
    if (quantityInput.value === quantityInput.defaultValue) return;
    if (form.reportValidity()) form.requestSubmit();
  });
});

document.querySelectorAll(".filter-buttons").forEach((group) => {
  const section = group.closest("section");
  const cards = section.querySelectorAll(".product-box");
  const productLimit = Number(section.dataset.productLimit) || Infinity;
  const normalize = (value) => value.trim().toLowerCase().replace(/s$/, "");
  group.querySelectorAll(".filter-btn").forEach((button) => {
    button.addEventListener("click", () => {
      const category = button.dataset.category || normalize(button.textContent);
      group.querySelectorAll(".filter-btn").forEach((item) => {
        item.classList.toggle("active", item === button);
        item.setAttribute("aria-pressed", String(item === button));
      });
      let visible = 0;
      cards.forEach((card) => {
        const cardCategory = card.dataset.category || normalize(card.querySelector(".category").textContent);
        const matchesCategory = category === "all" || cardCategory === category;
        card.hidden = !matchesCategory || visible >= productLimit;
        if (!card.hidden) visible++;
      });
      const empty = section.querySelector(".shop-empty");
      if (empty) empty.hidden = visible > 0;
    });
  });
  const selectedCategory = new URLSearchParams(window.location.search).get("category");
  if (selectedCategory) {
    const selectedButton = Array.from(group.querySelectorAll(".filter-btn"))
      .find((button) => button.dataset.category === selectedCategory);
    if (selectedButton) selectedButton.click();
  }
});

document.querySelectorAll(".home-category-slider").forEach((slider) => {
  const viewport = slider.querySelector(".home-category-viewport");
  const track = slider.querySelector(".home-category-track");
  const previous = slider.querySelector('[data-category-slide="prev"]');
  const next = slider.querySelector('[data-category-slide="next"]');
  if (!viewport || !track) return;
  const scrollByCard = (direction) => {
    const card = track.querySelector(".box");
    const gap = Number.parseFloat(getComputedStyle(track).columnGap) || 16;
    viewport.scrollBy({ left: direction * ((card?.getBoundingClientRect().width || viewport.clientWidth / 5) + gap), behavior: "smooth" });
  };
  previous?.addEventListener("click", () => scrollByCard(-1));
  next?.addEventListener("click", () => scrollByCard(1));
  const updateButtons = () => {
    const canScroll = viewport.scrollWidth > viewport.clientWidth + 1;
    if (previous) previous.disabled = !canScroll || viewport.scrollLeft <= 1;
    if (next) next.disabled = !canScroll || viewport.scrollLeft + viewport.clientWidth >= viewport.scrollWidth - 1;
  };
  viewport.addEventListener("scroll", updateButtons, { passive: true });
  window.addEventListener("resize", updateButtons);
  updateButtons();
});

const shopSidebar = document.querySelector(".shop-sidebar");
if (shopSidebar) {
  const search = shopSidebar.querySelector('#shop-search');
  const sort = shopSidebar.querySelector('#shop-sort');
  const categories = [...shopSidebar.querySelectorAll('input[name="category"]')];
  const container = document.querySelector('.shop-results .product-container');
  const cards = [...container.querySelectorAll('.product-box')];
  const empty = document.querySelector('.shop-results .shop-empty');
  const pagination = document.querySelector('.shop-pagination');
  const pageSize = 9;
  let currentPage = 1;
  const normalize = value => value.trim().toLocaleLowerCase();
  const products = cards.map((card, index) => ({
    card, index,
    name: normalize(card.querySelector('h3').textContent),
    price: Number(card.dataset.price),
    category: card.dataset.category,
  }));
  const renderPagination = pageCount => {
    if (!pagination) return;
    pagination.replaceChildren();
    if (pageCount <= 1) return;
    const makeButton = (label, page, { disabled = false, current = false, ariaLabel } = {}) => {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'shop-page-button';
      button.textContent = label;
      button.disabled = disabled;
      if (current) {
        button.classList.add('active');
        button.setAttribute('aria-current', 'page');
      }
      if (ariaLabel) button.setAttribute('aria-label', ariaLabel);
      button.addEventListener('click', () => {
        currentPage = page;
        updateShop(false);
        document.querySelector('.shop-results')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
      pagination.appendChild(button);
    };
    makeButton('‹', currentPage - 1, { disabled: currentPage === 1, ariaLabel: 'Trang trước' });
    let pageItems;
    if (pageCount <= 5) {
      pageItems = Array.from({ length: pageCount }, (_, index) => index + 1);
    } else if (currentPage <= 3) {
      pageItems = [1, 2, 3, 'ellipsis', pageCount];
    } else if (currentPage >= pageCount - 2) {
      pageItems = [1, 'ellipsis', pageCount - 2, pageCount - 1, pageCount];
    } else {
      pageItems = [1, 'ellipsis', currentPage - 1, currentPage, currentPage + 1, 'ellipsis', pageCount];
    }
    pageItems.forEach(page => {
      if (page === 'ellipsis') {
        const ellipsis = document.createElement('span');
        ellipsis.className = 'shop-page-ellipsis';
        ellipsis.textContent = '…';
        ellipsis.setAttribute('aria-hidden', 'true');
        pagination.appendChild(ellipsis);
      } else {
        makeButton(String(page), page, { current: page === currentPage, ariaLabel: `Trang ${page}` });
      }
    });
    makeButton('›', currentPage + 1, { disabled: currentPage === pageCount, ariaLabel: 'Trang sau' });
  };
  const updateShop = (resetPage = true) => {
    if (resetPage) currentPage = 1;
    const query = normalize(search.value);
    const selected = new Set(categories.filter(input => input.checked).map(input => input.value));
    const sorted = [...products];
    if (sort.value === 'price-asc') sorted.sort((a, b) => a.price - b.price || a.index - b.index);
    if (sort.value === 'price-desc') sorted.sort((a, b) => b.price - a.price || a.index - b.index);
    const matching = sorted.filter(product => product.name.includes(query) && (selected.size === 0 || selected.has(product.category)));
    const pageCount = Math.ceil(matching.length / pageSize);
    currentPage = Math.min(currentPage, Math.max(pageCount, 1));
    const first = (currentPage - 1) * pageSize;
    const pageProducts = new Set(matching.slice(first, first + pageSize).map(product => product.card));
    sorted.forEach(product => {
      product.card.hidden = !pageProducts.has(product.card);
      container.appendChild(product.card);
    });
    empty.hidden = matching.length > 0;
    empty.textContent = products.length === 0 ? 'Hiện chưa có sản phẩm.' : 'Không tìm thấy sản phẩm phù hợp với tìm kiếm và bộ lọc.';
    renderPagination(pageCount);
  };
  const initialCategory = new URLSearchParams(window.location.search).get('category');
  categories.forEach(input => {
    input.checked = input.value === initialCategory;
    input.addEventListener('change', updateShop);
  });
  search.addEventListener('input', updateShop);
  sort.addEventListener('change', updateShop);
  shopSidebar.querySelector('.shop-search').addEventListener('submit', event => {
    event.preventDefault();
    updateShop();
  });
  updateShop();
}

const menu = document.querySelector(".menu-icon");
if (menu) {
  menu.addEventListener("keydown", (event) => {
    if (event.key === "Enter" || event.key === " ") { event.preventDefault(); menu.click(); }
  });
  new MutationObserver(() => menu.setAttribute("aria-expanded", String(menu.classList.contains("move"))))
    .observe(menu, { attributes: true, attributeFilter: ["class"] });
  document.querySelectorAll(".nav-links a").forEach((link) => link.addEventListener("click", () => {
    menu.classList.remove("move");
    document.querySelector(".nav-links").classList.remove("open-menu");
  }));
}
document.addEventListener("click", (event) => {
  document.querySelectorAll(".account-menu[open]").forEach((account) => {
    if (!account.contains(event.target)) account.open = false;
  });
});
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    document.querySelectorAll(".account-menu[open]").forEach((account) => { account.open = false; });
    menu?.classList.remove("move");
    document.querySelector(".nav-links")?.classList.remove("open-menu");
  }
});
document.querySelectorAll(".admin-nav a").forEach((link) => {
  if (new URL(link.href).pathname === location.pathname) link.setAttribute("aria-current", "page");
});

document.querySelectorAll("#product-image, #category-image").forEach((productImageInput) => {
  const preview = document.getElementById(productImageInput.id + "-preview");
  const placeholder = document.getElementById(productImageInput.id + "-placeholder");
  const error = document.getElementById(productImageInput.id + "-error");
  let previewUrl;
  const restorePreview = () => {
    const current = preview.dataset.currentSrc;
    if (current) preview.src = current;
    else preview.removeAttribute("src");
    preview.hidden = !current;
    placeholder.hidden = Boolean(current);
  };
  productImageInput.addEventListener("change", () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    previewUrl = undefined;
    error.textContent = "";
    const file = productImageInput.files[0];
    if (!file) { restorePreview(); return; }
    if (!["image/jpeg", "image/png"].includes(file.type)) {
      error.textContent = "Vui lòng chọn ảnh JPG hoặc PNG.";
    } else if (file.size > 5 * 1024 * 1024) {
      error.textContent = "Vui lòng chọn ảnh có dung lượng tối đa 5 MB.";
    }
    if (error.textContent) {
      const message = error.textContent;
      error.textContent = "";
      window.showToast(message, "error", 8500);
      productImageInput.value = "";
      restorePreview();
      return;
    }
    previewUrl = URL.createObjectURL(file);
    preview.src = previewUrl;
    preview.hidden = false;
    placeholder.hidden = true;
  });
  window.addEventListener("pagehide", () => { if (previewUrl) URL.revokeObjectURL(previewUrl); });
});
