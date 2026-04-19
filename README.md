# Link Purify ✨

[![Android Release CI](https://github.com/vutranHS/link-purify/actions/workflows/release.yml/badge.svg)](https://github.com/vutranHS/link-purify/actions/workflows/release.yml)

A lightweight, high-performance Android application designed to intercept, resolve, and clean tracking parameters from affiliate links. 

Protect your privacy and ensure you're opening direct product pages without being followed by affiliate cookies or hidden redirects.

## 🚀 Supported Platforms

- **Shopee:** `s.shopee.vn` ➡️ `shopee.vn/product/...`
- **Lazada:** `s.lazada.vn` ➡️ `lazada.vn/products/...`
- **TikTok Shop:** `vt.tiktok.com` ➡️ `tiktok.com/view/product/...`

## ✨ Key Features

- **Advanced Redirect Engine:** Automatically handles HTTP 301/302 redirects and even complex HTML-based redirects (Meta Refresh / JavaScript).
- **Aggressive Cleaning:** Strips all tracking tokens (`utm_*`, `af_*`, `click_id`, `sec_user_id`, etc.) to ensure a parameter-free destination URL.
- **Privacy First (No Cookies):** The cleaning engine uses an anonymous HTTP client with **no cookies**, breaking the link between the affiliate click and your identity.
- **Flexible Flow:**
    - **Auto-Open Mode:** Instantly redirects you to the clean product page.
    - **Manual Review:** Shows a premium card UI to let you inspect the link before opening.
- **Link Tester:** A built-in dashboard to test and copy cleaned links manually.

## 📥 Installation

1. Go to the [Releases](https://github.com/vutranHS/link-purify/releases) page.
2. Download the latest `app-release.apk`.
3. Install the APK on your Android device.
4. (Optional) Set the app as the default handler for `shopee.vn`, `lazada.vn`, and `tiktok.com` in your system settings.

## 🛠 Technical Details

- **Language:** Kotlin
- **Networking:** OkHttp (Headless redirect resolution)
- **UI:** Material 3 (Day/Night support)
- **CI/CD:** Automated builds and releases via GitHub Actions

## 🤝 Contributing

Contributions are welcome! If you find a new tracking parameter or a store that isn't handled correctly, feel free to open an Issue or a Pull Request.

---

*Note: This app is intended for personal privacy use. It does not interfere with the functionality of the shopping apps themselves.*
