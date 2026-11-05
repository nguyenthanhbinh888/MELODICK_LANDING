# MELODICK RECS. — Landing Page & Admin CMS

> **Thương hiệu:** MELODICK RECS.  
> **Tagline:** Architects of the underground sound.  
> **Kiến trúc:** React + TypeScript (Vite) Single Page Application kết hợp Java 21 Spring Boot REST API & MySQL 8.0.

---

## 1. Giới thiệu dự án

Website **MELODICK RECS.** được phát triển từ Landing Page dark/minimal nguyên bản thành hệ thống hoàn chỉnh gồm hai phân hệ độc lập:

1. **Public Landing Page (`/`, `/artists`, `/submit`):**
   - Giữ trọn vẹn nhận diện dark/underground, typography font Montserrat, hiệu ứng Liquid Glow bám sát con trỏ chuột, hiệu ứng hover flicker.
   - Roster nghệ sĩ tải động từ MySQL thông qua REST API công khai.
   - Hệ thống overlay liên kết nền tảng (Spotify, SoundCloud, YouTube, Instagram...) chỉ hiển thị các URL hợp lệ được cấu hình từ Admin (không hiển thị link rỗng hay placeholder `#`).
   - Cổng Submit Demo trỏ tới URL Google Forms được quản lý động từ CMS.
   - Chân trang tự động lọc mạng xã hội và hiển thị thông tin bản quyền.

2. **Admin CMS (`/admin/login`, `/admin`, `/admin/artists`, `/admin/settings`):**
   - Khu vực quản trị độc lập, bảo vệ hoàn toàn ở backend bằng Spring Security và JWT.
   - Không để lộ bất kỳ liên kết quản trị nào trên giao diện Public.
   - **Dashboard:** Thống kê trực tiếp tổng số nghệ sĩ, số nghệ sĩ công khai (Published) và số nghệ sĩ ẩn (Hidden).
   - **Quản lý Nghệ sĩ:** Tìm kiếm, thêm mới, sửa, xóa, bật/tắt hiển thị, xem trước ảnh avatar và quản lý danh sách đa nền tảng cho từng nghệ sĩ.
   - **Cấu hình Landing Page:** Tùy biến tiêu đề thương hiệu, thông điệp Hero, URL Google Forms và toàn bộ link mạng xã hội Footer.

---

## 2. Công nghệ sử dụng

### Frontend (`/frontend`)
- **Ngôn ngữ:** TypeScript (Strict mode)
- **Framework & Build tool:** React 19 + Vite 8
- **Điều hướng:** React Router v7 (`react-router-dom`)
- **Iconography:** Lucide React
- **Thiết kế & Styling:** CSS Variables, Responsive Grid & Flexbox, Dark Minimal Design Tokens.
- **API Client:** Fetch API client tập trung, token lưu trữ an toàn trong `sessionStorage` (tránh lưu token dài hạn trong `localStorage`).

### Backend (`/backend`)
- **Ngôn ngữ & Nền tảng:** Java 21 (LTS), Spring Boot 3.3.5
- **Modules:** Spring Web, Spring Data JPA, Spring Security, Bean Validation
- **Bảo mật:** Mã hóa mật khẩu BCrypt, xác thực phân quyền qua JWT (JJWT 0.12.6), CORS whitelist nghiêm ngặt.
- **Database & Migration:** MySQL 8.0, Flyway Migration (`ddl-auto: validate`).
- **Build tool:** Apache Maven 3.9+

---

## 3. Kiến trúc thư mục

```text
MELODICK_LANDING/
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── router/          # Định tuyến public & admin
│   │   │   └── providers/       # AuthContext, SiteSettingsContext
│   │   ├── components/
│   │   │   ├── common/          # CursorGlow, ProtectedRoute
│   │   │   └── public/          # Navbar, Footer
│   │   ├── layouts/
│   │   │   ├── PublicLayout.tsx # Layout người dùng
│   │   │   └── AdminLayout.tsx  # Layout quản trị viên
│   │   ├── pages/
│   │   │   ├── public/          # Home, Artists, Submit
│   │   │   └── admin/           # Login, Dashboard, Artists, SiteSettings
│   │   ├── services/            # api.ts (Tập trung toàn bộ endpoint)
│   │   ├── types/               # TypeScript interfaces
│   │   └── styles/              # global.css & theme tokens
│   ├── .env.example
│   └── package.json
│
├── backend/
│   ├── src/main/java/com/melodick/recs/
│   │   ├── config/              # SecurityConfig, CorsConfig
│   │   ├── controller/          # Public & Admin REST Controllers
│   │   ├── dto/                 # Request & Response DTOs
│   │   ├── entity/              # AdminUser, Artist, ArtistLink, SiteSetting
│   │   ├── exception/           # GlobalExceptionHandler
│   │   ├── initializer/         # AdminBootstrapInitializer
│   │   ├── repository/          # Spring Data JPA Repositories
│   │   ├── security/            # JWT Provider, Filter, EntryPoint
│   │   └── service/             # Business Logic Layer
│   ├── src/main/resources/
│   │   ├── db/migration/        # V1__init_schema.sql, V2__seed_initial_data.sql
│   │   └── application.yml
│   ├── src/test/                # Unit & Integration Tests
│   ├── .env.example
│   └── pom.xml
│
├── skills/                      # Quy chuẩn dự án (git-workflow, architectures...)
├── MELODICK LANDING.html        # File HTML tham chiếu gốc
└── README.md
```

---

## 4. Hướng dẫn cài đặt và chạy Local

### 4.1. Yêu cầu hệ thống
- **Node.js:** v18 trở lên (Khuyến nghị Node 20 hoặc 24)
- **Java:** JDK 21 trở lên
- **Maven:** 3.9 trở lên
- **MySQL Server:** 8.0 trở lên

---

### 4.2. Khởi tạo cơ sở dữ liệu MySQL

Đăng nhập vào MySQL và tạo database `melodick_db`:

```sql
CREATE DATABASE IF NOT EXISTS melodick_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### 4.3. Cấu hình Backend

1. Di chuyển vào thư mục `backend`:
   ```bash
   cd backend
   ```
2. Có thể tạo file `.env` hoặc truyền biến môi trường (mặc định đã cấu hình sẵn trong `application.yml` cho môi trường phát triển):
   - `SPRING_DATASOURCE_URL`: `jdbc:mysql://localhost:3306/melodick_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8`
   - `SPRING_DATASOURCE_USERNAME`: `root`
   - `SPRING_DATASOURCE_PASSWORD`: Mật khẩu MySQL của bạn (VD: `123456`)
   - `JWT_SECRET`: Khóa bí mật tối thiểu 256 bits cho JWT
   - `ADMIN_USERNAME`: `admin`
   - `ADMIN_PASSWORD`: Mật khẩu khởi tạo admin (mặc định `Admin@123456`)

3. Biên dịch và chạy kiểm thử tự động:
   ```bash
   mvn clean test
   ```

4. Khởi chạy Backend Server:
   ```bash
   mvn spring-boot:run
   ```
   *Khi khởi động lần đầu, Flyway sẽ tự động chạy migration `V1` và `V2`, tạo schema và seed 3 nghệ sĩ mẫu cùng tài khoản admin.*

---

### 4.4. Cấu hình Frontend

1. Mở một terminal mới và di chuyển vào thư mục `frontend`:
   ```bash
   cd frontend
   ```
2. Cài đặt dependencies (nếu chưa cài):
   ```bash
   npm install
   ```
3. Chạy môi trường phát triển (Dev Server):
   ```bash
   npm run dev
   ```
   *Vite sẽ khởi chạy tại `http://localhost:5173` và tự động proxy các request `/api` sang backend port `8080`.*

4. Build production frontend (kiểm tra type-check và bundle):
   ```bash
   npm run build
   ```

---

## 5. Danh sách Route & Tài khoản truy cập

### 5.1. Public Pages
| Đường dẫn | Mô tả |
|---|---|
| `http://localhost:5173/` | Trang chủ (Hero title, tagline, flicker effect, liquid glow) |
| `http://localhost:5173/artists` | Danh sách nghệ sĩ (Card tương tác overlay liên kết âm nhạc) |
| `http://localhost:5173/submit` | Gửi Demo (Nút Submit Demo trỏ tới Google Forms) |

### 5.2. Admin CMS
| Đường dẫn | Mô tả | Phân quyền |
|---|---|---|
| `http://localhost:5173/admin/login` | Đăng nhập quản trị viên | Công khai |
| `http://localhost:5173/admin` | Dashboard thống kê tổng quan | ROLE_ADMIN |
| `http://localhost:5173/admin/artists` | Quản lý danh sách nghệ sĩ, ảnh, links | ROLE_ADMIN |
| `http://localhost:5173/admin/settings` | Cấu hình nội dung Landing Page & Socials | ROLE_ADMIN |

### 5.3. Tài khoản Quản trị mặc định (Bootstrap)
- **Tên đăng nhập:** `admin`
- **Mật khẩu:** `Admin@123456`
*(Mật khẩu được băm tự động bằng BCrypt và lưu trữ trong bảng `admin_users`)*

---

## 6. Danh mục REST API chính

### 6.1. Public API (Không yêu cầu đăng nhập)
- `GET /api/public/site`: Lấy toàn bộ cấu hình thương hiệu, hero, submit và footer.
- `GET /api/public/artists`: Lấy danh sách nghệ sĩ đang ở trạng thái Published (sắp xếp theo `display_order`).

### 6.2. Authentication API
- `POST /api/admin/auth/login`: Xác thực username/password, trả về JWT token và thông tin admin.
- `POST /api/admin/auth/logout`: Xóa phiên làm việc.
- `GET /api/admin/auth/me`: Lấy thông tin admin hiện tại dựa trên Bearer token.

### 6.3. Admin API (Yêu cầu Authorization: Bearer `<token>`)
- `GET /api/admin/dashboard`: Thống kê tổng số artist, số artist published và số artist hidden.
- `GET /api/admin/artists`: Danh sách toàn bộ nghệ sĩ (hỗ trợ `?search=`).
- `POST /api/admin/artists`: Tạo nghệ sĩ mới cùng danh sách platform links.
- `GET /api/admin/artists/{id}`: Xem chi tiết nghệ sĩ.
- `PUT /api/admin/artists/{id}`: Cập nhật thông tin nghệ sĩ và platform links.
- `PATCH /api/admin/artists/{id}/visibility`: Đổi trạng thái ẩn/hiện.
- `DELETE /api/admin/artists/{id}`: Xóa nghệ sĩ và cascade xóa các liên kết con.
- `PUT /api/admin/artists/reorder`: Cập nhật thứ tự hiển thị của nhiều nghệ sĩ.
- `GET /api/admin/site-settings`: Lấy cấu hình website cho form chỉnh sửa.
- `PUT /api/admin/site-settings`: Cập nhật cấu hình website.

---

## 7. Xử lý sự cố thường gặp (Troubleshooting)

1. **Lỗi kết nối MySQL (`Access denied for user 'root'@'localhost'`):**
   - Đảm bảo MySQL server 8.0 đang chạy (`Get-Service MySQL80`).
   - Cung cấp đúng mật khẩu trong biến môi trường `SPRING_DATASOURCE_PASSWORD` hoặc chỉnh sửa file `backend/src/main/resources/application.yml`.
2. **Lỗi Flyway Validate Schema (`Schema-validation: missing table...`):**
   - Chạy `mvn clean test` hoặc khởi động ứng dụng để Flyway tự động migrate schema.
3. **Lỗi CORS khi gọi API:**
   - Đảm bảo URL frontend chạy trên origin đã đăng ký (`http://localhost:5173` hoặc `http://localhost:3000`). Nếu chạy trên port khác, hãy bổ sung vào biến `CORS_ALLOWED_ORIGINS`.
4. **Token bị hết hạn (401):**
   - Hệ thống tự động xóa token khỏi session và chuyển hướng người dùng về trang `/admin/login`.

---

## 8. Quy chuẩn Git & Đóng góp mã nguồn

Dự án tuân thủ nghiêm ngặt kỹ năng `git-workflow`:
- Nhánh chính: `main` (production), `develop` (integration).
- Nhánh tính năng: `feature/<feature-name>`.
- Mỗi commit là một thay đổi logic độc lập (Atomic Commits) có đầy đủ phần thân giải thích `Changes`, `Files changed`, `Reason`, `Verification`.
