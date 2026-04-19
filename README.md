# Link Purify ✨

**Tiếng Việt** | [English](README_EN.md)

[![Android Release CI](https://github.com/vutranHS/link-purify/actions/workflows/release.yml/badge.svg)](https://github.com/vutranHS/link-purify/actions/workflows/release.yml)

Một ứng dụng Android nhẹ, hiệu năng cao được thiết kế để chặn, giải mã và làm sạch các tham số theo dõi (tracking) từ các link affiliate.

Bảo vệ quyền riêng tư của bạn và đảm bảo bạn đang mở trực tiếp trang sản phẩm mà không bị theo dõi bởi các cookie affiliate hoặc các trang chuyển hướng ẩn.

## 🚀 Các nền tảng hỗ trợ

- **Shopee:** `s.shopee.vn` ➡️ `shopee.vn/product/...`
- **Lazada:** `s.lazada.vn` ➡️ `lazada.vn/products/...`
- **TikTok Shop:** `vt.tiktok.com` ➡️ `tiktok.com/view/product/...`

## ✨ Các tính năng chính

- **Công cụ giải mã chuyển hướng nâng cao:** Tự động xử lý các chuyển hướng HTTP 301/302 và thậm chí cả các chuyển hướng phức tạp dựa trên HTML (Meta Refresh / JavaScript).
- **Làm sạch triệt để:** Loại bỏ tất cả các mã theo dõi (`utm_*`, `af_*`, `click_id`, `sec_user_id`, v.v.) để đảm bảo URL đích hoàn toàn sạch sẽ.
- **Quyền riêng tư là trên hết (Không Cookie):** Công cụ làm sạch sử dụng một trình duyệt ẩn danh nội bộ **không lưu cookie**, cắt đứt mối liên hệ giữa việc click link và danh tính của bạn.
- **Luồng xử lý linh hoạt:**
    - **Chế độ Tự động mở:** Chuyển hướng bạn ngay lập tức đến trang sản phẩm sạch.
    - **Kiểm tra thủ công:** Hiển thị giao diện thẻ cao cấp để bạn kiểm tra link trước khi mở.
- **Trình kiểm tra link:** Một bảng điều khiển tích hợp để kiểm tra và copy các link đã làm sạch một cách thủ công.

## 📖 Hướng dẫn sử dụng

Bạn có thể sử dụng **Link Purify** theo 2 cách cực kỳ đơn giản:

### Cách 1: Chia sẻ trực tiếp (Khuyên dùng)
- Khi bạn thấy một link Shopee, Lazada hoặc TikTok trong bất kỳ ứng dụng nào (ví dụ: Facebook, Messenger, trình duyệt).
- Nhấn nút **Chia sẻ** (Share) ➡️ Chọn ứng dụng **Link Purify**.
- Ứng dụng sẽ tự động làm sạch và mở sản phẩm cho bạn (hoặc hiện bảng kiểm tra tùy cấu hình).

### Cách 2: Xử lý thủ công trong App
- Sao chép (Copy) link bạn muốn làm sạch.
- Mở ứng dụng **Link Purify**.
- Dán link vào ô **"Dán link cần kiểm tra"** ➡️ Nhấn nút **Làm Sạch Link**.
- Xem kết quả và nhấn **Mở Sản Phẩm**.

## 📥 Cài đặt

1. Truy cập trang [Releases](https://github.com/vutranHS/link-purify/releases).
2. Tải về file `app-release.apk` mới nhất.
3. Cài đặt file APK trên thiết bị Android của bạn.
4. (Tùy chọn) Thiết đặt ứng dụng làm trình xử lý mặc định cho `shopee.vn`, `lazada.vn`, và `tiktok.com` trong cài đặt hệ thống.

## 🛠 Chi tiết kỹ thuật

- **Ngôn ngữ:** Kotlin
- **Networking:** OkHttp (Giải mã chuyển hướng headless)
- **Giao diện:** Material 3 (Hỗ trợ chế độ Sáng/Tối)
- **CI/CD:** Tự động build và release thông qua GitHub Actions

## 🤝 Đóng góp

Mọi đóng góp đều được chào đón! Nếu bạn tìm thấy một tham số theo dõi mới hoặc một cửa hàng chưa được xử lý đúng, đừng ngần ngại tạo Issue hoặc Pull Request.

---

*Lưu ý: Ứng dụng này được xây dựng cho mục đích quyền riêng tư cá nhân. Nó không can thiệp vào chức năng của chính các ứng dụng mua sắm.*
