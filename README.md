
# 🎓 Graduation Album Web Service

Spring Boot 기반으로 관리자 / 사용자 권한을 분리하고 사진·영상 미디어를 NAS 파일 시스템으로 스트리밍하는 온라인 졸업 앨범 웹 서비스입니다.

> 오프라인 졸업 앨범을 웹 서비스로 전환한 개인 백엔드 프로젝트

> 개발 기간 : 2026.01.19(월) ~ 2026.01.23(금)
## 📌 프로젝트 개요

본 프로젝트는 초등학교 졸업 앨범을 온라인 웹 서비스 형태로 제공하기 위한 프로젝트입니다.

관리자는 반, 학생, 교사 정보를 관리하고 사진 및 영상 콘텐츠를 업로드할 수 있으며,
일반 사용자는 반별 페이지를 통해 학생·교사 정보와 추억이 담긴 사진·영상 콘텐츠를 열람할 수 있습니다.

## 🎯 개발 목적

Spring Boot 기반 실무형 백엔드 프로젝트 경험

파일 업로드 및 미디어 스트리밍 구조 이해

NAS 환경 배포 및 운영 경험

DB 중심 설계와 파일 시스템 분리 아키텍처 학습


## 🛠 기술 스택
### Backend

* Java 17

* Spring Boot 3.x

* Spring MVC

* Spring Data JPA (Hibernate)

* Jakarta Validation

* Frontend

* Thymeleaf

* Bootstrap 5

### Database

* H2 (개발)

* MariaDB(개발 & 배포)

### Infra / DevOps

* Docker

* Synology NAS

## 🧱 시스템 아키텍처

` [Browser]`

` ↓`

` [Spring Boot Application]`

` ↓`

` [JPA Repository]`

` ↓`

` [MariaDB]`

` ↓`

` [File System (NAS /data)]`

### 설계 포인트

* DB에는 미디어 파일의 상대 경로만 저장

* 실제 이미지/영상 파일은 NAS 파일 시스템에 저장

* MediaController가 DB 경로를 기반으로 파일을 직접 스트리밍

## 📂 미디어 처리 설계
### 파일 저장 구조

` /data `

` ├─ photos `
 
` │   ├─ students/{classNum}/UUID.jpg `
 
` │   ├─ teachers/UUID.jpg `
 
` │   └─ classrooms/UUID.jpg `
 
` └─ videos `
 
`     ├─ teachers/{classNum}.mp4 `
  
`     └─ students/{classNum}.mp4 `

### DB 저장 예시

` photos/students/05/2f764168-aa96-4243-a24e-8e8c6d995085.jpg `

* DB에는 절대 경로가 아닌 상대 경로만 저장

* 환경(NAS, 로컬, 컨테이너)에 독립적인 구조


## 🎯 주요 기능 (Use Case)
### 👨‍💼 관리자(Admin)

* 반(Classroom) 관리

    * 반 생성 / 수정 / 삭제

    * 반 단체 사진 업로드

    * 교사·학생 영상 편지 업로드

* 학생(Student) 관리

    * 학생 등록 / 수정 / 삭제

    * 개인 사진 업로드

    * 손편지 사진 업로드

* 교사(Teacher) 관리

    * 교사 등록 / 수정 / 삭제

    * 교사 사진 업로드

### 👨‍👩‍👧 일반 사용자(User)

* 메인 페이지

    * 전체 반 목록 조회

* 반별 페이지

    * 반 정보 조회

    * 담임 교사 정보

    * 반 단체 사진

    * 학생 목록 및 개인 사진

    * 교사/학생 영상 편지 재생

## 🧠 핵심 설계 원칙
### ✅ DB = 미디어 메타데이터의 단일 진실 (Single Source of Truth)

* 파일명 규칙을 서버 코드에서 추측하지 않음

* 미디어 파일에 대한 정답 정보는 오직 DB

* MediaController는 DB에 저장된 경로를 그대로 사용

### ✅ MediaController는 경로 변환기 역할만 수행

* /media/** 요청을 받아 DB의 상대 경로 → 실제 파일 시스템 경로로 변환

* 이미지 / 영상 스트리밍만 담당

## 🚨 트러블 슈팅 경험
### 1️⃣ NAS 배포 환경에서 BLOB 사용으로 인한 메모리 초과
#### ❗ 문제

초기 설계에서는 사진 및 영상 파일을 DB(BLOB 타입)에 저장했습니다.
로컬 환경에서는 문제가 없었지만, NAS에 Docker로 배포한 후
다수의 미디어 요청 시 JVM 메모리 사용량이 급증하여 컨테이너가 OOM으로 종료되었습니다.

#### 🔍 원인

* Hibernate가 BLOB 데이터를 한 번에 메모리로 로딩

* NAS의 제한된 메모리 자원

#### ✅ 해결

* 미디어 파일을 파일 시스템(NAS)으로 분리

* DB에는 파일의 상대 경로만 저장

* MediaController를 통한 직접 스트리밍 방식으로 구조 변경

#### 🎓 배운 점

* 대용량 미디어는 DB가 아닌 파일 시스템에 저장해야 함

* 배포 환경을 고려한 설계의 중요성

* DB는 메타데이터 관리에 집중해야 함

### 2️⃣ Media URL과 실제 저장 경로 불일치 문제
#### ❗ 문제

DB에는 UUID 기반 파일명이 저장되어 있었으나,
MediaController에서는 classNum 기반으로 파일을 조회하여 404 오류 발생

#### 🔍 원인

* 저장: UUID 파일명

* 조회: classNum 기반 파일명 추측

#### ✅ 해결

* MediaController에서 DB에 저장된 경로를 그대로 사용하도록 수정

* 파일명 규칙을 코드에서 가정하지 않도록 설계 변경

## 📁 프로젝트 구조
` com.elementaryschool.graduation_album`

` ├─ domain        # JPA Entity`

` ├─ repository    # JPA Repository`

` ├─ service       # FileStorageService`

` ├─ web`

` │   ├─ admin     # 관리자 컨트롤러`

` │   ├─ MediaController`

` │   └─ ClassroomController`

` └─ GraduationAlbumApplication`

## ▶️ 실행 및 배포

* Docker 기반 실행

* NAS 볼륨(/data) 마운트

* Nginx Reverse Proxy 구성 가능

* HTTPS 적용 가능

## ✨ 한 줄 요약

> Spring Boot 기반으로 파일 업로드, 미디어 스트리밍,
> NAS 배포까지 고려한 온라인 졸업 앨범 웹 서비스입니다.

## 🚀 향후 개선 사항

* Spring Security 기반 인증/인가

* AWS S3 또는 Object Storage 연동

* REST API 분리 + React 프론트엔드

* CI/CD (GitHub Actions)

* 테스트 코드(JUnit5) 작성

* HTTPS 적용 및 포트 은닉을 위해 Nginx Reverse Proxy를 적용
-----------
