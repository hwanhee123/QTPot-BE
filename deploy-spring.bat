@echo off
chcp 65001 > nul
echo 🚀 스프링 부트 빌드 시작 (테스트 스킵)...
call gradlew clean build -x test

echo 📦 빌드된 JAR 파일 EC2로 전송 중...
:: 백엔드 빌드 파일은 보통 build/libs 폴더 안에 생깁니다.
scp -i "new-key.pem" build/libs/*-SNAPSHOT.jar ec2-user@3.36.24.242:/home/ec2-user/qt-tracker-server.jar

echo 🛠️ EC2 서버에서 기존 서버 종료 및 새 서버 실행 중...
ssh -i "new-key.pem" ec2-user@3.36.24.242 "sudo systemctl restart qttracker"

echo ✅ 스프링 백엔드 배포 완료! (서버가 켜지는데 10~20초 정도 걸립니다)