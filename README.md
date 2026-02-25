# QTPot 🌱

> 교회 그룹을 위한 큐티(QT) 인증 앱 백엔드 서버

실제 교회 그룹 **50명**이 매일 사용하는 큐티 인증 서비스입니다.
사진 업로드, 댓글, 뱃지/랭킹 시스템, 푸시 알림 기능을 제공합니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.2 |
| Database | MySQL, Spring Data JPA |
| Auth | Spring Security, JWT |
| Cloud | AWS EC2, AWS S3 |
| Push 알림 | Firebase FCM |

---

## 주요 기능

- **큐티 인증** - 사진 업로드로 큐티 완료 인증
- **댓글** - 인증 게시글에 댓글 작성
- **뱃지 & 랭킹** - 인증 횟수 기반 뱃지 획득 및 랭킹
- **푸시 알림** - Firebase FCM을 통한 알림
- **관리자** - 멤버 관리 및 통계
- **이미지 최적화** - 업로드 시 자동 압축 (최대 1280px, 80% 품질)

---

## 아키텍처

```
클라이언트 (모바일 앱)
       │
       ▼
  AWS EC2 (Spring Boot)
       │
  ┌────┴────┐
  │         │
MySQL     AWS S3
(DB)    (이미지 저장)
```

---

## 프로젝트 구조

```
src/main/java/com/qttracker/
├── config/          # Security, Firebase, Scheduling 설정
├── domain/
│   ├── attendance/  # 큐티 인증, 댓글
│   ├── badge/       # 뱃지, 랭킹
│   ├── member/      # 회원 관리
│   └── admin/       # 관리자
├── security/        # JWT 인증 필터
├── service/         # FCM 푸시 알림
└── util/            # S3 업로더
```

---

## 실행 방법

**1. `application-local.properties` 생성**

```properties
# DB
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# JWT
jwt.secret=your_jwt_secret

# AWS S3
aws.access-key=your_access_key
aws.secret-key=your_secret_key
```

**2. 실행**

```bash
./gradlew bootRun
```

---

## 개발 기간 & 운영 현황

- 1인 개발
- 실사용자 50명 / 매일 사용 중
