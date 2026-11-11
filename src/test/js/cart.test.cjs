const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const { test } = require('node:test');
const vm = require('node:vm');

function setup(fetch) {
  const listeners = new Map();
  const notices = [];
  const element = () => ({ setAttribute() {}, appendChild() {}, append() {}, addEventListener() {} });
  const document = {
    querySelectorAll: () => [], querySelector: () => null, getElementById: () => null,
    createElement: element, body: element(),
    addEventListener: (name, handler) => listeners.set(name, handler),
  };
  const location = { pathname: '/msm/shop', search: '', hash: '' };
  const window = { location, addEventListener() {}, setTimeout() {} };
  vm.runInNewContext(readFileSync(resolve(__dirname, '../../main/webapp/js/msm.js'), 'utf8'), {
    document, window, location, URL, URLSearchParams, fetch,
    FormData: class { constructor(form) { return new Map(Object.entries(form.fields)); } },
  });
  window.showToast = (message, type) => notices.push({ message, type });
  return {
    notices,
    submit(quantity = '1') {
      const button = { disabled: false };
      const form = {
        // Named form controls shadow native DOM properties, including form.action.
        get action() { throw new Error('form.action was shadowed by a named control'); },
        getAttribute(name) { assert.equal(name, 'action'); return '/msm/cart'; },
        fields: { productId: 'abcdef0123456789abcdef01', quantity, action: 'add' },
        querySelector: () => button,
      };
      let prevented = false;
      const completion = listeners.get('submit')({
        target: { closest: () => form }, submitter: button,
        preventDefault() { prevented = true; },
      });
      assert(prevented);
      return { button, completion };
    },
  };
}

test('cart uses the form URL even when a control shadows form.action', async () => {
  const calls = [];
  const ui = setup(async (url, options) => {
    calls.push({ url, options });
    return { ok: true, json: async () => ({ success: true, quantity: 1, message: 'Đã thêm sản phẩm vào giỏ hàng.' }) };
  });
  const submit = ui.submit();
  await submit.completion;
  assert.equal(calls[0].url, '/msm/cart');
  assert.equal(calls[0].options.body.get('action'), 'add');
  assert.equal(calls[0].options.credentials, 'same-origin');
  assert.equal(submit.button.disabled, false);
  assert.equal(ui.notices.length, 0);
});

test('rapid adds wait for the previous response so the count cannot move backwards', async () => {
  let release;
  const firstResponse = new Promise(resolve => { release = resolve; });
  const calls = [];
  const ui = setup(async (url, options) => {
    calls.push(options.body.get('quantity'));
    if (calls.length === 1) await firstResponse;
    return { ok: true, json: async () => ({ success: true, quantity: calls.length }) };
  });
  const first = ui.submit('1');
  const second = ui.submit('2');
  await new Promise(resolve => setImmediate(resolve));
  assert.deepEqual(calls, ['1']);
  release();
  await Promise.all([first.completion, second.completion]);
  assert.deepEqual(calls, ['1', '2']);
  assert.equal(ui.notices.length, 0);
});

test('a non JSON failure restores the button and leaves the request queue usable', async () => {
  let count = 0;
  const ui = setup(async () => {
    count++;
    if (count === 1) return { ok: false, json: async () => { throw new SyntaxError('HTML error page'); } };
    return { ok: true, json: async () => ({ success: true, quantity: 1 }) };
  });
  const first = ui.submit();
  await first.completion;
  assert.equal(first.button.disabled, false);
  assert.equal(ui.notices[0].type, 'error');
  await ui.submit().completion;
  assert.equal(ui.notices.length, 1);
  assert.equal(count, 2);
});
