# 🧠 BDSP ASD 관리 플랫폼 - 개인 기여 모듈 (by ecomylee)

본 프로젝트는 협업 프로젝트 중 본인이 개발 및 유지보수한 **백엔드(Java Spring 기반)** 및 **프론트엔드(Vue.js 기반)**의 소스만을 선별하여 정리한 이력용 레포지토리입니다. 민감 정보는 익명 처리되었습니다.

---

## 🔧 기술 스택

**Backend**
- Java 11, Spring Boot
- Spring Data JPA, MyBatis
- MySQL,MongoDB, Maven
- REST API, JWT, Email SMTP

**Frontend**
- Vue.js 2.x
- Vue Router, Axios
- Chart.js, SCSS

---

## 🧩 담당 기능

### ✅ Backend (src/)

| 기능 | 설명 |
|------|------|
| 사용자 인증 | JWT 기반 로그인, 인증 처리 |
| 데이터 업로드 | EyeTracking, fnirs, video, microbiome 등 뇌신경계 데이터 수집 |
| 설문 관리 | M-CHAT 설문 수집 및 결과 저장 처리 |
| 대시보드 통계 | 다양한 실험군별 통계 데이터 생성 |
| 메일 발송 | SMTP 기반 이벤트 알림 메일 전송 |
| 배치 작업 | 정기적 데이터 동기화 및 파일 업로드 |
Naver Cloud Platform Object Storage (S3 호환)

Java AWS SDK (NCP 호환 라이브러리 사용)

AES-256 암복호화

Apache POI (Excel 파싱)

FFmpeg or OpenCV 기반 mp4 비디오 마스킹 처리 (Defacing)

Naver Cloud ObjectStorage S3 연동
---

### ✅ Frontend (vue-app/)

| 기능 | 설명 |
|------|------|
| 로그인 화면 | 사용자 로그인 및 권한 검증 처리 |
| 프로젝트 등록 | 모달 기반 프로젝트 추가 및 편집 |
| 뷰어 연동 | fMRI 등 BrainBrowser 기반 시각화 연동 |
| 실험 데이터 업로드 | Drag & Drop 기반 시선추적 등 업로드 |
| 대시보드 | 통계 시각화 및 분석 데이터 제공 |
| 모바일 대응 | 모바일 뷰 최적화된 컴포넌트 구성 |

---

## 📁 디렉토리 구조

```
ecomylee-contributions/
├── src/                  # 💡 Java 기반 Spring Boot 백엔드 프로젝트
│   ├── main/
│   │   ├── java/         # 핵심 비즈니스 로직 (Controller, Service, Repository 등)
│   │   └── resources/    # 설정 파일 (application.yml 등) 및 Mapper XML
│   └── test/             # 단위 테스트 코드
│
├── vue-app/              # 💻 Vue.js 기반 프론트엔드 프로젝트
│   ├── public/           # 정적 파일 (index.html, favicon 등)
│   └── src/              # Vue 컴포넌트, 라우터, 상태 관리(Vuex), API 모듈
│       ├── views/        # 화면 단위 구성 (e.g., 로그인, 대시보드, 등록 등)
│       ├── components/   # 재사용 가능한 UI 구성 요소
│       ├── router/       # Vue Router 설정
│       ├── store/        # Vuex 상태 관리
│       └── api/          # Axios 기반 API 호출 모듈
│
├── *.sql                 # 🗄️ DB 초기 데이터 삽입 및 변경 이력 SQL
├── pom.xml               # ⚙️ Maven 기반 의존성 관리 및 빌드 설정
└── README.md             # 📘 프로젝트 설명서
```

---

## 🚀 실행 방법

```bash
# 백엔드
cd ecomylee-contributions
mvn spring-boot:run

# 프론트엔드
cd vue-app
npm install
npm run serve
```

---

## 🙋🏻‍♂️ About

이 저장소는 Lee Minyoung (이민영)의 실무 경험을 기술 기반으로 정리한 개인 포트폴리오입니다.  
GitHub: [@eeeminokr](https://github.com/eeeminokr)
