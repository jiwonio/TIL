# Spring & Node.js 앱 Docker/Kubernetes 배포 프로젝트 (Windows 환경 상세 가이드)

본 문서는 Windows 환경에서 Spring Boot + MySQL 애플리케이션과 Node.js 채팅 애플리케이션을 개발하고, Docker로 컨테이너화하여 Kubernetes 클러스터에 배포하는 전체 과정을 상세하게 안내합니다.

## 1. 프로젝트 구조

```
.
├── .gitignore
├── README.md
├── chat-app/
│   ├── Dockerfile
│   ├── index.html
│   ├── index.js
│   └── package.json
├── k8s/
│   ├── chat-app-deployment.yaml
│   ├── ingress.yaml
│   ├── mysql-deployment.yaml
│   ├── mysql-secret.yaml
│   └── spring-app-deployment.yaml
└── spring-app/
    ├── Dockerfile
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    └── src/
        └── ... (소스 코드)
```

## 2. Windows 환경 사전 준비 사항

아래 도구들이 설치되어 있어야 합니다. **PowerShell을 관리자 권한으로 실행**하여 설치를 진행하는 것을 권장합니다.

1.  **WSL2 (Windows Subsystem for Linux 2)**: Docker Desktop이 최적의 성능을 내기 위해 필수적입니다.
    ```powershell
    wsl --install
    ```
    설치 후 컴퓨터를 재시작해야 할 수 있습니다.

2.  **Docker Desktop for Windows**: [공식 홈페이지](https://www.docker.com/products/docker-desktop/)에서 다운로드하여 설치합니다. 설치 과정에서 "Use WSL 2 instead of Hyper-V" 옵션을 반드시 체크하세요.
    * **설치 후 설정**: Docker Desktop 실행 > `Settings` (톱니바퀴 아이콘) > `Kubernetes` 메뉴 > `Enable Kubernetes` 체크 박스 활성화.

3.  **kubectl (Kubernetes CLI)**: Kubernetes 클러스터와 통신하기 위한 명령어 도구입니다.
    ```powershell
    # winget 사용 (권장):
    winget install -e --id Kubernetes.kubectl
    ```

4.  **Java 17 (JDK)**: Spring Boot 앱 개발 및 빌드를 위해 필요합니다.
    ```powershell
    # winget 사용 (권장):
    winget install -e --id Microsoft.OpenJDK.17
    ```

5.  **Node.js & npm**: Node.js 채팅 앱 개발을 위해 필요합니다. [공식 홈페이지](https://nodejs.org/)에서 LTS 버전을 다운로드하여 설치합니다.

6.  **Docker Hub 계정**: 빌드한 이미지를 저장하고 공유하기 위해 필요합니다. [Docker Hub](https://hub.docker.com/)에서 계정을 생성하세요.

## 3. 배포 절차 (Step-by-Step)

모든 명령어는 **PowerShell** 또는 **CMD**에서 실행합니다.

### 3.1. NGINX Ingress Controller 설치 (최초 1회)

```powershell
kubectl apply -f [https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.1/deploy/static/provider/cloud/deploy.yaml](https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.1/deploy/static/provider/cloud/deploy.yaml)
```
`kubectl get pods -n ingress-nginx` 명령어로 파드들이 `Running` 또는 `Completed` 상태가 될 때까지 1~2분 정도 기다립니다.

### 3.2. MySQL 및 Secret 배포

1.  PowerShell에서 Base64 인코딩 값을 생성합니다.
    ```powershell
    # 'your-password'를 실제 비밀번호로 변경하여 실행
    $text = "your-password"
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($text)
    $encoded = [System.Convert]::ToBase64String($bytes)
    $encoded
    ```
    출력된 문자열을 복사하여 `k8s/mysql-secret.yaml` 파일의 `<BASE64_ENCODED_PASSWORD>` 자리에 붙여넣습니다.

2.  `k8s` 디렉토리로 이동하여 Secret과 MySQL을 배포합니다.
    ```powershell
    cd k8s
    kubectl apply -f mysql-secret.yaml
    kubectl apply -f mysql-deployment.yaml
    ```

### 3.3. 애플리케이션 빌드 및 배포

`your-dockerhub-username` 부분은 실제 Docker Hub 사용자 이름으로 변경해야 합니다.

#### **Spring Boot 앱**

```powershell
# spring-app 디렉토리로 이동
cd ..\spring-app

# 애플리케이션 빌드 (Windows에서는 mvnw.cmd 사용)
.\mvnw.cmd clean package

# Docker Hub 로그인 (최초 1회)
docker login

# Docker 이미지 빌드 및 푸시
docker build -t your-dockerhub-username/spring-app:latest .
docker push your-dockerhub-username/spring-app:latest

# Kubernetes에 배포
cd ..\k8s
kubectl apply -f spring-app-deployment.yaml
```

#### **Node.js 채팅 앱**

```powershell
# chat-app 디렉토리로 이동
cd ..\chat-app

# Docker 이미지 빌드 및 푸시
docker build -t your-dockerhub-username/chat-app:latest .
docker push your-dockerhub-username/chat-app:latest

# Kubernetes에 배포
cd ..\k8s
kubectl apply -f chat-app-deployment.yaml
```

### 3.4. Ingress 배포

```powershell
# k8s 디렉토리에 있는지 확인
cd ..\k8s

# Ingress 적용
kubectl apply -f ingress.yaml
```

## 4. 동작 확인

### **채팅 앱**
웹 브라우저를 열고 주소창에 `http://localhost` 를 입력하여 접속합니다.

### **Spring Boot API**
PowerShell 또는 CMD에서 `curl` 명령어를 사용하여 테스트합니다.
```powershell
# 사용자 생성 (Windows CMD에서는 큰따옴표 앞에 \ 사용)
curl -X POST -H "Content-Type: application/json" -d "{\"name\":\"testuser\"}" http://localhost/api/users

# 사용자 조회
curl http://localhost/api/users
```

## 5. Docker Desktop for Windows 환경에서 실행 중인 애플리케이션을 관리하는 핵심 kubectl 명령어들

### 1. 애플리케이션 임시 중지 (파드 멈추기)
설정은 그대로 둔 채, 실행 중인 파드(Pod)만 멈추고 싶을 때는 **복제본(replicas) 개수를 0으로 조절(scale down)**합니다.

### 1.1. 특정 애플리케이션 멈추기
```powershell
# spring-app의 복제본을 0으로 만들어 파드를 모두 멈춤
kubectl scale deployment spring-app --replicas=0

# chat-app 멈추기
kubectl scale deployment chat-app --replicas=0

# php-app 멈추기
kubectl scale deployment php-app --replicas=0

# Oracle DB 멈추기 (StatefulSet은 명령어가 다름)
kubectl scale statefulset oracle-db --replicas=0

# Ingress Controller 멈추기
kubectl scale deployment ingress-nginx-controller --replicas=0 -n ingress-nginx
```

### 1.2. 모든 애플리케이션 한번에 멈추기
```powershell
# 모든 Deployment의 복제본을 0으로 설정
kubectl scale deployment --all --replicas=0

# 모든 StatefulSet의 복제본을 0으로 설정
kubectl scale statefulset --all --replicas=0

# 'ingress-nginx' 네임스페이스의 Ingress Controller(정문) 멈추기
kubectl scale deployment --all --replicas=0 -n ingress-nginx
```

### 2. 임시 중지한 애플리케이션 다시 시작하기
복제본 개수를 원래대로 되돌려 **다시 파드를 실행(scale up)**합니다.

### 2.1. 특정 애플리케이션 다시 시작하기
```powershell
# spring-app의 복제본을 2개로 다시 늘림
kubectl scale deployment spring-app --replicas=2

# chat-app의 복제본을 2개로 다시 늘림
kubectl scale deployment chat-app --replicas=2

# php-app의 복제본을 1개로 다시 늘림
kubectl scale deployment php-app --replicas=1

# MySQL 다시 시작
kubectl scale statefulset mysql --replicas=1

# Oracle DB 다시 시작
kubectl scale statefulset oracle-db --replicas=1

# Ingress Controller 다시 시작하기
kubectl scale deployment ingress-nginx-controller --replicas=1 -n ingress-nginx
```

### 3. 애플리케이션 완전 삭제 (리소스 제거)
파드뿐만 아니라 Deployment, Service 등 관련된 모든 설정을 클러스터에서 완전히 제거합니다.

### 3.1. YAML 파일 기준으로 삭제 (가장 권장되는 방법)
각 YAML 파일에 정의된 모든 리소스를 한번에 삭제합니다.

```powershell
# k8s 디렉토리로 이동
cd k8s

# 각 파일별로 삭제
kubectl delete -f spring-app-deployment.yaml
kubectl delete -f chat-app-deployment.yaml
kubectl delete -f php-app-deployment.yaml
kubectl delete -f oracle-deployment.yaml
kubectl delete -f mysql-deployment.yaml
kubectl delete -f ingress.yaml
kubectl delete -f oracle-secret.yaml
kubectl delete -f mysql-secret.yaml
```

### 3.2. 폴더 전체 한번에 삭제
```powershell
# k8s 디렉토리로 이동
cd k8s

# 폴더 내 모든 yaml 파일 적용하여 삭제
kubectl delete -f .

```

### 4. 삭제 후 다시 배포하기
완전히 삭제한 후 처음부터 다시 배포하는 방법입니다.

```powershell
# k8s 디렉토리로 이동
cd k8s

# Secret 먼저 배포
kubectl apply -f mysql-secret.yaml
kubectl apply -f oracle-secret.yaml

# 나머지 리소스 배포 (폴더 전체 적용)
kubectl apply -f .

```

### 5. 쿠버네티스 클러스터 자체를 끄고 켜기 (XAMPP 사용 시 최적)
가장 간단한 방법입니다. Docker Desktop의 쿠버네티스 클러스터 자체를 잠시 멈추면 포트 80을 포함한 모든 리소스 사용이 중단됩니다.

**끄는 법**
1. Windows 작업 표시줄 트레이에서 Docker Desktop 아이콘을 우클릭합니다.
2. 메뉴에서 Kubernetes 항목 위에 마우스를 올립니다.
3. Running 상태 옆의 일시정지(Pause) 아이콘을 클릭합니다.

**켜는 법**
1. 마찬가지로 Docker Desktop 아이콘 우클릭 > Kubernetes 메뉴로 이동합니다.
2. Paused 상태 옆의 재생(Resume) 아이콘을 클릭합니다. 이전에 실행 중이던 모든 파드와 설정이 그대로 다시 살아납니다.