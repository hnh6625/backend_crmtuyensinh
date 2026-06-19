# CRM Admissions Management - Backend API

Hệ thống Backend API quản lý tuyển sinh (CRM) được xây dựng dựa trên nền tảng **Java Spring Boot**, tuân thủ nghiêm ngặt mô hình **Clean Architecture**. Hệ thống được thiết kế để chịu tải cao, xử lý dữ liệu lớn (Big Data) và đảm bảo tính bảo mật.

## Tính năng nổi bật
- **Clean Architecture:** Phân tách rõ ràng các layer (Domain, Application, Infrastructure, Controller), giúp code dễ bảo trì và mở rộng.
- **Spring Batch Processing:** Tích hợp bộ xử lý luồng tốc độ cao, hỗ trợ Import hàng loạt file Excel/CSV lên tới **100.000+ hồ sơ** cùng cơ chế validation đa luồng, chống trùng lặp SĐT trên RAM.
- **Bảo mật (Spring Security + JWT):** Phân quyền chặt chẽ theo Role (Admin, Manager, Collaborator). Bảo vệ API ở tầng Controller và Service bằng `@PreAuthorize`.
- **Automated Cron Jobs:** Tự động hóa các tác vụ ngầm (Background Tasks) như tự động hủy lịch hẹn (Follow-up) khi quá hạn.

## 🛠 Công nghệ sử dụng
- **Framework:** Java 17, Spring Boot 3.x
- **Database:** MySQL / Spring Data JPA / Hibernate
- **Security:** Spring Security, JSON Web Token (JWT)
- **Batch Processing:** Spring Batch
- **Định hướng mở rộng (Roadmap):** Tích hợp Redis (Caching), RabbitMQ (Message Queue), và Dockerize / CI/CD pipeline.

## Hướng dẫn cài đặt và khởi chạy (Local Development)

### 1. Yêu cầu hệ thống
- Java Development Kit (JDK) 17 trở lên.
- Maven 3.8+
- MySQL 8.0+

### 2. Build và chạy dự án
- **Tải dependencies và build dự án**

    mvn clean install -DskipTests

- **Chạy ứng dụng**
  
    mvn spring-boot:run

### 3. Cấu hình Database
Tạo một database trống trong MySQL:
```sql
CREATE DATABASE crm_tuyensinh CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
