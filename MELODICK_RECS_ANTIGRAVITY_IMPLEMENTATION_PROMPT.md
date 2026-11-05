# ANTIGRAVITY PROJECT PROMPT — MELODICK RECS.

> **Mục tiêu:** Phát triển website MELODICK RECS. hiện tại thành một website Landing Page có hệ thống quản trị nội dung (Admin CMS) riêng biệt. Giữ tinh thần thiết kế hiện tại, chuyển frontend sang React + TypeScript, sử dụng MySQL để lưu dữ liệu và Java Spring Boot làm backend/API.
>
> **Cách làm:** Đọc và phân tích repository hiện tại trước khi sửa code. Không tạo một dự án mới rồi xóa bỏ sản phẩm hiện tại. Hãy nâng cấp có kiểm soát, giữ lại những phần đang hoạt động và hoàn thiện từng giai đoạn.

---

## 1. Bối cảnh dự án hiện tại

Website hiện tại có tên **MELODICK RECS.**, là một Landing Page giới thiệu các nghệ sĩ/producer và định hướng âm nhạc underground.

Tệp HTML hiện tại là tài liệu tham chiếu cho giao diện và nội dung. Hãy đọc kỹ mã nguồn đó trước khi triển khai. Những đặc điểm cần giữ lại:

- Tên thương hiệu: `MELODICK RECS.`
- Tagline: `Architects of the underground sound.`
- Phong cách: dark/minimal, nền gần đen, chữ trắng, nội dung tối giản, điểm nhấn ánh sáng xanh dương và tím.
- Font hiện tại: Montserrat.
- Hiệu ứng Liquid Glow: vùng sáng xanh/tím di chuyển theo vị trí con trỏ.
- Thanh điều hướng hiện có: Home, Artists, Submit.
- Trang Artists hiển thị artist card dạng lưới. Khi chọn card, lớp overlay hiển thị các liên kết nền tảng âm nhạc/mạng xã hội.
- Trang Submit có nút `SUBMIT DEMO`, hiện đang trỏ tới Google Forms:
  `https://forms.gle/Zj4QpgXX8MuMcGhRA`
- Footer hiển thị bản quyền và các liên kết Spotify, YouTube, Instagram, TikTok.

### Dữ liệu đang có trong HTML tham chiếu

| Nghệ sĩ | Nền tảng đang được hiển thị |
|---|---|
| MIRAGEMFXNK | Spotify, SoundCloud, Instagram |
| 411N:)) | Spotify, YouTube |
| SPIDEX | Spotify, Instagram |

**Lưu ý quan trọng:** Các đường dẫn của artist và social ở HTML hiện tại đang để `#`. Đây chỉ là placeholder, không phải URL thật. Khi khởi tạo dữ liệu ban đầu, hãy giữ tên nghệ sĩ và tên nền tảng nhưng để URL trống/chưa cấu hình. Không biến `#` thành liên kết hoạt động. Ảnh artist hiện cũng là placeholder `[ AVATAR ]`; cần cho phép Admin bổ sung URL ảnh sau này.

---

## 2. Mục tiêu sau khi nâng cấp

Xây dựng hai khu vực tách biệt rõ ràng:

### A. Khu vực người dùng — Public Landing Page

Đây là website công khai, dành cho khách truy cập. Người dùng không cần đăng nhập.

Các trang chính:

- `/` — Home.
- `/artists` — Danh sách nghệ sĩ.
- `/submit` — Gửi demo.

Public site chỉ đọc dữ liệu đã được Admin công khai. Không hiển thị các chức năng quản trị hoặc thông tin nhạy cảm.

### B. Khu vực quản trị — Admin CMS

Đây là hệ thống quản trị riêng, có giao diện và bố cục khác với Landing Page.

Các route đề xuất:

- `/admin/login` — Đăng nhập Admin.
- `/admin` — Dashboard.
- `/admin/artists` — Quản lý nghệ sĩ.
- `/admin/settings` — Quản lý nội dung và cấu hình Landing Page.

Yêu cầu tách biệt:

1. Không đặt liên kết Admin trong thanh điều hướng hoặc footer công khai.
2. Public Layout và Admin Layout phải là hai layout độc lập.
3. Tất cả API quản trị phải được bảo vệ ở backend. Chỉ ẩn nút hoặc chặn route ở frontend là không đủ.
4. Người chưa đăng nhập truy cập route Admin phải được chuyển đến trang đăng nhập.
5. Người đã đăng nhập nhưng không có quyền ADMIN phải nhận phản hồi 403 từ backend.
6. Admin đăng xuất phải xóa trạng thái xác thực hợp lệ và không thể tiếp tục gọi API quản trị.
7. Có thể dùng chung một repository/frontend application với route và layout tách biệt. Không bắt buộc phải triển khai thành hai domain riêng, nhưng ranh giới public/admin phải rõ ràng cả về UI lẫn phân quyền.

---

## 3. Công nghệ bắt buộc và kiến trúc đề xuất

### Frontend

- HTML5.
- CSS3.
- TypeScript.
- React.
- Vite để phát triển và build frontend (nếu repository chưa có công cụ tương đương).
- React Router để quản lý các route.
- Fetch API hoặc Axios để gọi backend, có một API client tập trung và kiểu dữ liệu TypeScript rõ ràng.

Có thể sử dụng Lucide React cho icon. Không thêm quá nhiều thư viện nếu chức năng có thể giải quyết bằng React/CSS thông thường.

### Backend

Sử dụng **Java + Spring Boot** vì dự án cần đăng nhập Admin, phân quyền và thao tác dữ liệu MySQL. Frontend tuyệt đối không được kết nối trực tiếp tới MySQL.

Đề xuất:

- Java 17 trở lên, ưu tiên phiên bản tương thích với môi trường hiện có.
- Spring Boot.
- Spring Web.
- Spring Data JPA.
- Spring Security.
- MySQL Connector/J.
- Bean Validation.
- Flyway để quản lý database migration.
- Maven (trừ khi repository hiện tại đã sử dụng Gradle).

### Database

- MySQL 8.0 trở lên.
- Dùng migration SQL có version để tạo/cập nhật cấu trúc database.
- Không tự ý xóa database hoặc dữ liệu hiện tại.
- Không dùng `ddl-auto: update` trong môi trường production. Ưu tiên Flyway quản lý schema và `ddl-auto: validate`.

### Kiến trúc tổng thể

```text
Browser
  |
  v
React + TypeScript
  |-- Public Layout
  |     |-- Home
  |     |-- Artists
  |     `-- Submit
  |
  `-- Admin Layout
        |-- Login
        |-- Dashboard
        |-- Artists Management
        `-- Site Settings
  |
  v
Spring Boot REST API
  |-- Public API (read-only)
  |-- Admin Authentication
  |-- Admin API (ADMIN only)
  |-- Service Layer
  |-- Repository Layer
  |
  v
MySQL
```

Giữ frontend và backend thành hai phần có trách nhiệm rõ ràng. Controller không chứa toàn bộ business logic; hãy phân chia Controller, Service, Repository, Entity, DTO, Security và Config hợp lý.

---

## 4. Yêu cầu giao diện Public Landing Page

### 4.1. Nguyên tắc thiết kế

Không thiết kế lại theo một template SaaS/dashboard chung. Đây là website thương hiệu âm nhạc, vì vậy phải giữ chất riêng tối giản, tối, hiện đại và có cảm giác underground.

Design tokens tham khảo từ HTML hiện tại:

```css
:root {
  --bg-color: #020202;
  --text-main: #f0f0f0;
  --text-muted: #888888;
  --border-color: #222222;
  --accent-color: #ffffff;
  --glow-color-1: rgba(59, 130, 246, 0.35);
  --glow-color-2: rgba(147, 51, 234, 0.35);
}
```

Có thể tinh chỉnh kích thước, khoảng cách, typography và trạng thái tương tác để giao diện chuyên nghiệp hơn, nhưng không làm mất nhận diện gốc.

Yêu cầu chung:

- Responsive cho desktop, tablet và mobile.
- Có trạng thái hover, focus, active rõ ràng.
- Có loading, empty state và error state khi lấy dữ liệu từ API.
- Ảnh nghệ sĩ cần lazy-load và có fallback khi ảnh lỗi.
- Không để nội dung bị tràn ngang trên màn hình nhỏ.
- Tôn trọng `prefers-reduced-motion`; giảm/tắt hiệu ứng khi người dùng yêu cầu giảm chuyển động.
- Hiệu ứng cursor glow không được chặn thao tác chuột; nên tắt hoặc giảm hiệu ứng trên thiết bị cảm ứng.
- Dùng semantic HTML, đảm bảo các thành phần tương tác có thể dùng bằng bàn phím.

### 4.2. Home

- Hiển thị thương hiệu lớn `MELODICK RECS.`.
- Hiển thị tagline `Architects of the underground sound.`.
- Giữ hiệu ứng Liquid Glow theo con trỏ, nhưng tối ưu hiệu năng.
- Có điều hướng rõ ràng tới Artists và Submit.
- Nội dung hero phải lấy từ cấu hình backend để Admin có thể chỉnh sửa, không hard-code rải rác trong component.

### 4.3. Artists

- Lấy danh sách nghệ sĩ đã được công khai từ Public API.
- Hiển thị dạng grid responsive.
- Mỗi card có ảnh đại diện, tên nghệ sĩ và overlay chứa các liên kết nền tảng.
- Khi click card, overlay mở/đóng như hành vi hiện tại.
- Khi click một link trong overlay, không được kích hoạt nhầm hành vi click của card.
- Trên mobile, thao tác mở overlay phải thuận tiện bằng chạm; không phụ thuộc hoàn toàn vào hover.
- Link chỉ hiển thị khi có URL hợp lệ. Không render link `#` hoặc URL rỗng.
- Mở liên kết ngoài bằng tab mới với thuộc tính an toàn phù hợp (`rel="noopener noreferrer"`).
- Có trạng thái khi chưa có nghệ sĩ nào được công khai.

### 4.4. Submit

- Giữ thông điệp `Looking for new signatures.`.
- Giữ nút `SUBMIT DEMO`.
- URL Google Forms ban đầu phải được lưu trong cấu hình database và có thể thay đổi từ Admin.
- Nếu Admin chưa cấu hình URL hợp lệ, không hiển thị một nút dẫn tới `#`; hãy hiển thị trạng thái phù hợp hoặc ẩn nút.

### 4.5. Footer

- Hiển thị copyright, mặc định tương ứng `© 2026 Melodick Recs. All rights reserved.`.
- Các liên kết Spotify, YouTube, Instagram, TikTok được quản lý qua Admin.
- Không hiển thị link nếu URL chưa được cấu hình.
- Năm bản quyền nên tự cập nhật hoặc được quản lý theo cấu hình hợp lý; tránh hard-code nhiều nơi.

---

## 5. Chức năng Admin CMS

### 5.1. Đăng nhập và bảo mật

Tạo trang đăng nhập dành riêng cho Admin với các trường:

- Username.
- Password.
- Nút Login.
- Hiển thị lỗi đăng nhập rõ ràng nhưng không tiết lộ username có tồn tại hay không.
- Có trạng thái loading khi gửi request.

Yêu cầu bảo mật:

- Mật khẩu phải được hash bằng BCrypt (hoặc PasswordEncoder an toàn tương đương), tuyệt đối không lưu plaintext.
- Tạo cơ chế xác thực phía backend bằng Spring Security. Có thể dùng JWT access token ngắn hạn kết hợp refresh token an toàn hoặc cơ chế session phù hợp; hãy chọn một phương án nhất quán và giải thích trong README.
- Nếu dùng refresh token/cookie, cookie phải có cấu hình HttpOnly, Secure khi chạy HTTPS, SameSite phù hợp; xử lý CSRF/Origin và CORS đúng cách.
- Không lưu mật khẩu hoặc refresh token dạng rõ trong database.
- Không lưu access/refresh token dài hạn trong localStorage.
- Chỉ API công khai được truy cập không cần đăng nhập.
- Tất cả API `/api/admin/**` phải yêu cầu quyền ADMIN ở backend.
- Không tạo sẵn tài khoản/mật khẩu yếu trong source code.
- Khởi tạo tài khoản Admin đầu tiên bằng biến môi trường hoặc quy trình bootstrap an toàn; bắt buộc đổi mật khẩu mặc định nếu có cơ chế mật khẩu khởi tạo.
- Cấu hình CORS chỉ cho phép origin frontend đã khai báo, không mở wildcard tùy tiện khi dùng credentials.
- Kiểm tra dữ liệu đầu vào và giới hạn độ dài/định dạng URL.
- Có xử lý 401 (chưa xác thực/hết phiên) và 403 (không đủ quyền) thống nhất.

### 5.2. Dashboard

Trang Dashboard nên có:

- Tổng số nghệ sĩ.
- Số nghệ sĩ đang công khai.
- Số nghệ sĩ đang ẩn.
- Lối tắt tới quản lý nghệ sĩ và cài đặt website.
- Thông tin tài khoản Admin đang đăng nhập và nút Logout.

Không cần tạo biểu đồ giả hoặc số liệu không có nguồn dữ liệu. Chỉ hiển thị các thống kê thực tế lấy từ API.

### 5.3. Quản lý nghệ sĩ

Tạo trang quản lý với bảng/danh sách nghệ sĩ.

Thông tin một nghệ sĩ:

- ID.
- Tên nghệ sĩ.
- Slug (nếu sử dụng URL chi tiết).
- URL ảnh đại diện.
- Mô tả ngắn (optional, có thể dùng trong tương lai).
- Trạng thái: Published / Hidden.
- Thứ tự hiển thị.
- Danh sách liên kết nền tảng.
- Ngày tạo và ngày cập nhật.

Các chức năng:

- Xem danh sách nghệ sĩ.
- Tìm kiếm theo tên.
- Thêm nghệ sĩ.
- Chỉnh sửa nghệ sĩ.
- Ẩn/hiện nghệ sĩ.
- Xóa nghệ sĩ với hộp thoại xác nhận.
- Chỉnh sửa thứ tự hiển thị.
- Quản lý nhiều link cho một nghệ sĩ: Spotify, SoundCloud, YouTube, Instagram và các nền tảng khác.
- Thêm, sửa, xóa từng link nền tảng.
- Kiểm tra URL hợp lệ trước khi lưu.
- Hiển thị ảnh preview khi Admin nhập URL ảnh.
- Có validation, thông báo thành công/thất bại và chống gửi form nhiều lần.

Dữ liệu khởi tạo cần có ba artist từ HTML tham chiếu: MIRAGEMFXNK, 411N:)), SPIDEX. Giữ tên nền tảng đang có của từng nghệ sĩ, nhưng URL để trống vì các URL cũ chỉ là `#` placeholder. Cho phép Admin cập nhật sau.

### 5.4. Quản lý nội dung Landing Page

Tạo trang Site Settings để Admin chỉnh sửa:

**Thông tin thương hiệu**
- Brand name: mặc định `MELODICK RECS.`.
- Hero title.
- Tagline: mặc định `Architects of the underground sound.`.

**Submit**
- Nội dung giới thiệu: mặc định `Looking for new signatures.`.
- Nút label: mặc định `SUBMIT DEMO`.
- Demo submission URL: mặc định Google Forms hiện tại.

**Footer và social links**
- Copyright text.
- Spotify URL.
- YouTube URL.
- Instagram URL.
- TikTok URL.

Yêu cầu:

- Có nút Save Changes.
- Có trạng thái chưa lưu/lưu thành công/lưu thất bại.
- Có validation cho URL.
- Public site phải cập nhật nội dung theo database sau khi Admin lưu thành công.
- Không bắt buộc làm trình soạn thảo kéo-thả hoặc page builder phức tạp.

---

## 6. Thiết kế database MySQL

Hãy thiết kế schema chuẩn hóa vừa đủ, có khóa chính, khóa ngoại, unique constraint, index phù hợp và timestamps.

Các bảng đề xuất:

### `admin_users`

| Cột | Kiểu gợi ý | Ý nghĩa |
|---|---|---|
| id | BIGINT, PK, AUTO_INCREMENT | ID Admin |
| username | VARCHAR(100), UNIQUE, NOT NULL | Tên đăng nhập |
| password_hash | VARCHAR(255), NOT NULL | Mật khẩu đã hash |
| display_name | VARCHAR(150) | Tên hiển thị |
| active | BOOLEAN, NOT NULL | Trạng thái tài khoản |
| created_at | DATETIME | Ngày tạo |
| updated_at | DATETIME | Ngày cập nhật |
| last_login_at | DATETIME, nullable | Lần đăng nhập gần nhất |

### `artists`

| Cột | Kiểu gợi ý | Ý nghĩa |
|---|---|---|
| id | BIGINT, PK, AUTO_INCREMENT | ID nghệ sĩ |
| name | VARCHAR(150), NOT NULL | Tên nghệ sĩ |
| slug | VARCHAR(180), UNIQUE | Slug nếu cần URL chi tiết |
| image_url | VARCHAR(1000), nullable | URL ảnh đại diện |
| bio | TEXT, nullable | Mô tả ngắn |
| published | BOOLEAN, NOT NULL | Có hiển thị trên public site hay không |
| display_order | INT, NOT NULL | Thứ tự hiển thị |
| created_at | DATETIME | Ngày tạo |
| updated_at | DATETIME | Ngày cập nhật |

### `artist_links`

| Cột | Kiểu gợi ý | Ý nghĩa |
|---|---|---|
| id | BIGINT, PK, AUTO_INCREMENT | ID liên kết |
| artist_id | BIGINT, FK → artists.id | Nghệ sĩ sở hữu link |
| platform | VARCHAR(50), NOT NULL | Spotify/SoundCloud/YouTube/Instagram/... |
| url | VARCHAR(1000), NOT NULL | URL nền tảng |
| display_order | INT, NOT NULL | Thứ tự hiển thị |
| created_at | DATETIME | Ngày tạo |
| updated_at | DATETIME | Ngày cập nhật |

Quan hệ: một artist có nhiều artist_links. Khi xóa artist, xử lý link con bằng cascade phù hợp ở database/JPA.

### `site_settings`

Có thể dùng mô hình key-value để dễ bổ sung cấu hình mà không phải tạo cột mới thường xuyên:

| Cột | Kiểu gợi ý | Ý nghĩa |
|---|---|---|
| id | BIGINT, PK, AUTO_INCREMENT | ID |
| setting_key | VARCHAR(100), UNIQUE, NOT NULL | Khóa cấu hình |
| setting_value | TEXT, nullable | Giá trị |
| updated_at | DATETIME | Lần cập nhật |

Các key khởi tạo tối thiểu:

- `brand_name`
- `hero_title`
- `tagline`
- `submit_description`
- `submit_button_label`
- `submit_url`
- `copyright_text`
- `footer_spotify_url`
- `footer_youtube_url`
- `footer_instagram_url`
- `footer_tiktok_url`

Có thể thay đổi thiết kế database nếu có lý do kỹ thuật tốt hơn, nhưng phải đảm bảo các chức năng CMS hoạt động đầy đủ và dễ mở rộng.

---

## 7. REST API đề xuất

Dùng prefix `/api`. Response JSON phải nhất quán, DTO rõ ràng; không trả password hash hoặc thông tin nội bộ không cần thiết.

### Public API — không yêu cầu đăng nhập

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/public/site` | Lấy brand, hero, submit, footer settings |
| GET | `/api/public/artists` | Lấy danh sách artist đang published, sắp theo display_order |

### Authentication API

| Method | Endpoint | Mục đích |
|---|---|---|
| POST | `/api/admin/auth/login` | Đăng nhập Admin |
| POST | `/api/admin/auth/refresh` | Làm mới phiên/token nếu dùng refresh flow |
| POST | `/api/admin/auth/logout` | Đăng xuất |
| GET | `/api/admin/auth/me` | Lấy thông tin Admin hiện tại |

Điều chỉnh endpoint refresh theo phương thức xác thực cuối cùng đã chọn.

### Admin API — yêu cầu quyền ADMIN

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/admin/dashboard` | Thống kê dashboard |
| GET | `/api/admin/artists` | Danh sách artist, hỗ trợ search/filter/page nếu cần |
| POST | `/api/admin/artists` | Tạo artist |
| GET | `/api/admin/artists/{id}` | Chi tiết artist |
| PUT | `/api/admin/artists/{id}` | Cập nhật artist và links |
| PATCH | `/api/admin/artists/{id}/visibility` | Ẩn/hiện artist |
| DELETE | `/api/admin/artists/{id}` | Xóa artist |
| PUT | `/api/admin/artists/reorder` | Cập nhật thứ tự hiển thị |
| GET | `/api/admin/site-settings` | Lấy cấu hình để chỉnh sửa |
| PUT | `/api/admin/site-settings` | Lưu cấu hình Landing Page |

Có thể điều chỉnh endpoint nếu cần, nhưng phải giữ nguyên nguyên tắc: public API chỉ đọc dữ liệu đã công khai; API ghi/sửa/xóa phải được bảo vệ.

### Chuẩn xử lý lỗi

Tạo một format lỗi thống nhất, ví dụ:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "name": "Name is required"
  }
}
```

Không trả stack trace, SQL, secret hoặc thông tin nhạy cảm về frontend trong production. Phân biệt hợp lý các mã HTTP 400, 401, 403, 404, 409 và 500.

---

## 8. Cấu trúc source code tham khảo

Hãy điều chỉnh theo cấu trúc repository thật. Không ép buộc đổi toàn bộ nếu đã có cấu trúc tốt.

```text
MELODICK-RECS/
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── router/
│   │   │   └── providers/
│   │   ├── components/
│   │   │   ├── common/
│   │   │   └── public/
│   │   ├── layouts/
│   │   │   ├── PublicLayout.tsx
│   │   │   └── AdminLayout.tsx
│   │   ├── pages/
│   │   │   ├── public/
│   │   │   └── admin/
│   │   ├── features/
│   │   │   ├── artists/
│   │   │   ├── authentication/
│   │   │   └── site-settings/
│   │   ├── services/
│   │   ├── hooks/
│   │   ├── types/
│   │   ├── styles/
│   │   └── main.tsx
│   ├── .env.example
│   └── package.json
│
├── backend/
│   ├── src/main/java/.../
│   │   ├── config/
│   │   ├── security/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── mapper/
│   │   └── exception/
│   ├── src/main/resources/
│   │   ├── db/migration/
│   │   └── application.yml
│   └── pom.xml
│
├── docs/
├── .gitignore
└── README.md
```

Không tạo một `utils` hoặc `common` khổng lồ chứa mọi thứ. Tổ chức source theo feature và trách nhiệm rõ ràng; component React nên nhỏ, có thể tái sử dụng và có type cụ thể.

---

## 9. Quy tắc code và chất lượng

### Frontend

- Dùng TypeScript strict mode nếu khả thi.
- Tránh `any`; định nghĩa interface/type cho API request/response.
- Tách API calls khỏi UI components.
- Không viết toàn bộ website trong một file component.
- Quản lý loading, error, empty và success state.
- Form phải có validation ở frontend và backend.
- Dùng environment variable cho API base URL.
- Không đưa secret database/JWT vào frontend.
- Tránh hard-code artist, social link và site settings trong giao diện sau khi API sẵn sàng.

### Backend

- Dùng DTO cho request/response; không trả trực tiếp Entity cho API public/admin.
- Phân chia Controller → Service → Repository.
- Dùng transaction khi thao tác nhiều bảng liên quan, đặc biệt khi lưu artist cùng danh sách links.
- Validation tại request DTO.
- Global exception handler.
- Phân quyền ở Spring Security và method/service khi phù hợp.
- Không log password, token hoặc secret.
- Dùng migration để seed dữ liệu mặc định và quản lý schema.

### UI/UX

- Giao diện phải giống một sản phẩm thực tế, không phải bản demo sơ sài.
- Public site giữ dark visual identity hiện có.
- Admin có thể dùng phong cách dashboard chuyên nghiệp, rõ ràng, dễ thao tác; không cần mô phỏng hiệu ứng Liquid Glow ở Admin.
- Có thông báo toast hoặc inline feedback sau mỗi thao tác.
- Có xác nhận trước thao tác xóa.
- Có trạng thái disabled/loading để tránh gửi request lặp.
- Có empty state hướng dẫn người quản trị thêm dữ liệu.

---

## 10. Git workflow trong quá trình triển khai

Trước khi sửa code, chạy và kiểm tra:

```bash
git status
git branch --show-current
git remote -v
```

Quy tắc:

1. Không làm việc trực tiếp trên `main`/`master`.
2. Tạo branch theo convention thông thường, ví dụ `feat/react-typescript-migration`, `feat/admin-auth`, `feat/artist-management`, `feat/site-settings`; không đặt tên branch kiểu `phase-1`, `phase-2`.
3. Chia công việc thành thay đổi nhỏ, có thể review và chạy thử độc lập.
4. Dùng Conventional Commits: `feat`, `fix`, `refactor`, `style`, `test`, `docs`, `chore`, `build`, `ci`...
5. Commit phải atomic và mô tả cụ thể phần thay đổi, không dùng message chung chung như `update code` hoặc `finish phase 1`.
6. Không commit file `.env`, password, token, private key hoặc dữ liệu cá nhân.
7. Chỉ push lên remote branch sau khi kiểm tra remote và xác nhận repository/credentials đang trỏ đúng nơi. Không force-push và không ghi đè lịch sử của người khác.
8. Không tự ý merge vào `main`.

Sau mỗi phần việc, chạy build/test phù hợp trước khi commit. Nếu phát hiện lỗi có sẵn từ trước, ghi rõ thay vì che giấu hoặc sửa lan man.

---

## 11. Các giai đoạn triển khai

Không cố viết toàn bộ ứng dụng trong một lần bằng cách tạo nhiều file sơ sài. Hãy triển khai tuần tự, hoàn thiện và kiểm tra từng phần.

### Giai đoạn 0 — Khảo sát repository

- Kiểm tra cấu trúc thư mục, framework, dependency và trạng thái Git.
- Đọc toàn bộ HTML hiện tại và xác định các thành phần cần giữ lại.
- Xác định phần nào đã có, phần nào cần bổ sung.
- Đề xuất kế hoạch migration ngắn gọn và nêu rõ file sẽ được sửa/tạo.
- Không xóa giao diện cũ trước khi đã có bản React tương đương hoạt động.

### Giai đoạn 1 — Frontend React + TypeScript

- Chuyển HTML hiện tại thành React components.
- Thiết lập React Router cho Public routes.
- Tách CSS/design tokens hợp lý.
- Hoàn thiện responsive và các hiệu ứng hiện có.
- Ban đầu có thể dùng mock data được tách riêng, nhưng phải chuẩn bị cấu trúc để thay bằng API.
- Build thành công và kiểm tra các trang Home, Artists, Submit.

### Giai đoạn 2 — Backend và MySQL

- Khởi tạo Spring Boot nếu repository chưa có backend.
- Cấu hình kết nối MySQL bằng environment variables.
- Tạo Flyway migrations.
- Tạo Entity, Repository, DTO, Service, Controller.
- Seed ba nghệ sĩ ban đầu và site settings mặc định.
- Tạo public API và xác minh dữ liệu trả về đúng.

### Giai đoạn 3 — Admin Authentication

- Tạo tài khoản Admin đầu tiên bằng bootstrap an toàn.
- Cấu hình Spring Security và phân quyền.
- Xây dựng Admin Login và bảo vệ admin routes.
- Kiểm tra các tình huống chưa đăng nhập, token/phiên hết hạn và không đủ quyền.

### Giai đoạn 4 — Admin Dashboard và CMS

- Xây dựng Admin Layout.
- Dashboard lấy số liệu thật.
- CRUD nghệ sĩ và quản lý artist links.
- Quản lý trạng thái published/hidden và thứ tự hiển thị.
- Quản lý nội dung thương hiệu, Submit URL và Footer social links.

### Giai đoạn 5 — Tích hợp và kiểm thử

- Bỏ mock data khỏi các luồng đã có API.
- Kiểm tra toàn bộ Public site đọc dữ liệu thật từ MySQL thông qua backend.
- Kiểm tra thao tác CRUD trong Admin cập nhật nội dung Public site.
- Kiểm tra quyền truy cập API, validation, lỗi mạng và dữ liệu rỗng.
- Chạy frontend build, backend tests và kiểm tra thủ công các luồng chính.
- Cập nhật README hướng dẫn chạy local.

---

## 12. Tiêu chí nghiệm thu bắt buộc

Chỉ xem là hoàn thành khi đạt các tiêu chí sau:

### Public

- [ ] Giao diện MELODICK RECS. hoạt động bằng React + TypeScript.
- [ ] Giữ đúng tinh thần dark/minimal, brand name, tagline và Liquid Glow.
- [ ] Home, Artists, Submit hoạt động trên các route riêng.
- [ ] Danh sách nghệ sĩ lấy từ backend/MySQL, không còn phụ thuộc dữ liệu hard-code.
- [ ] Artist links chỉ hiển thị khi URL hợp lệ.
- [ ] Submit URL và footer links lấy từ site settings.
- [ ] Responsive trên desktop và mobile.

### Admin

- [ ] Admin có trang đăng nhập riêng.
- [ ] Admin Layout độc lập với Public Layout.
- [ ] Không có link quản trị lộ trên public site.
- [ ] API admin được bảo vệ ở backend.
- [ ] Dashboard hiển thị thống kê thật.
- [ ] CRUD artist hoạt động.
- [ ] Quản lý artist links hoạt động.
- [ ] Ẩn/hiện và sắp xếp artist hoạt động.
- [ ] Cập nhật nội dung Home, Submit và Footer hoạt động.
- [ ] Logout hoạt động.

### Backend và database

- [ ] Spring Boot kết nối MySQL thành công qua environment variables.
- [ ] Migration chạy được trên database mới.
- [ ] Không lưu mật khẩu plaintext.
- [ ] Public API không làm lộ thông tin Admin.
- [ ] API admin trả 401/403 đúng tình huống.
- [ ] Không có secret trong source code hoặc Git.

### Bàn giao

- [ ] Frontend build thành công.
- [ ] Backend khởi động thành công.
- [ ] Có README hướng dẫn cấu hình MySQL, tạo Admin ban đầu và chạy cả frontend/backend.
- [ ] Có `.env.example` chỉ chứa tên biến và giá trị mẫu không nhạy cảm.
- [ ] Có ghi chú các chức năng chưa hoàn thiện (nếu có), không được đánh dấu hoàn thành giả.

---

## 13. Hướng dẫn chạy local cần ghi trong README

README cuối cùng cần có:

1. Yêu cầu cài đặt Node.js, Java, Maven và MySQL.
2. Cách tạo database MySQL.
3. Cách cấu hình biến môi trường cho backend và frontend.
4. Cách chạy Flyway migration và bootstrap tài khoản Admin.
5. Lệnh chạy backend.
6. Lệnh chạy frontend.
7. URL public site và URL Admin khi chạy local.
8. Cách build và chạy test.
9. Mô tả ngắn API chính.
10. Hướng dẫn xử lý một số lỗi cấu hình thường gặp.

Không ghi thông tin đăng nhập hoặc secret thật vào README.

---

## 14. Chỉ dẫn cuối cùng dành cho Antigravity

Hãy đóng vai trò một Senior Full-stack Developer, chịu trách nhiệm nâng cấp sản phẩm hiện có chứ không chỉ tạo giao diện minh họa.

Ưu tiên theo thứ tự:

1. **Bảo toàn và nâng cấp giao diện MELODICK RECS. hiện tại.** Không thay thế bằng một template không liên quan.
2. **Tạo Public Landing Page bằng React + TypeScript.**
3. **Tạo backend Spring Boot và database MySQL** để lưu dữ liệu thực tế.
4. **Tạo Admin CMS độc lập về giao diện và được bảo vệ bằng backend security.**
5. **Tích hợp end-to-end, kiểm thử và viết README.**

Hãy bắt đầu bằng việc khảo sát repository và báo cáo ngắn gọn cấu trúc hiện tại. Sau đó triển khai từng phần theo các giai đoạn ở trên. Không bỏ qua backend security, không giả lập CRUD bằng cách chỉ thay đổi state trên frontend, không hard-code dữ liệu CMS vào component và không đánh dấu tính năng hoàn thành khi chưa chạy thử thành công.
