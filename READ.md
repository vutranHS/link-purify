**App: Affiliate Link Cleaner (Android APK)**

**Goal:**
Remove affiliate tracking from Shopee/Lazada links before opening product.

---

**Supported links:**

* https://s.shopee.vn/*
* https://s.lazada.vn/*

---

**Core features:**

1. **Intent Handler (auto mode)**

* Register for above domains
* When user clicks link → app opens
* Resolve redirect (follow 3xx)
* Remove tracking params:

  * af_*, utm_*, pid, spm, etc.
* Convert to clean URL:

  * Shopee: https://shopee.vn/product/{shopid}/{itemid}
  * Lazada: keep product URL, remove params
* Open cleaned URL (browser or app)
* App closes immediately (no UI)

---

2. **Share Target (fallback mode)**

* Accept shared text (URL)
* Process same as above
* Open clean link

---

**Behavior:**

* No UI / invisible processing (<300ms target)
* If cleaning fails → open original link

---

**Notes:**

* Must handle multiple redirects
* Must support both WebView & external sources
* Do NOT rely on cookies
* Prefer extracting product ID if possible (avoid redirect)

---

**Optional (nice to have):**

* Toggle: open in browser vs Shopee/Lazada app
* Lightweight logging for debug

---

**User setup:**

* Install APK
* Set as default for supported links (1 time)

---

**Expected result:**
User clicks or shares link → opens product normally → NO affiliate tracking
