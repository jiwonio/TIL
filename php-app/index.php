<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>PHP DB Connection Test</title>
    <style>
        body { font-family: sans-serif; margin: 2em; }
        .container { max-width: 800px; margin: auto; padding: 1em; border: 1px solid #ccc; border-radius: 5px; }
        h1 { color: #333; }
        .status { padding: 0.5em; margin-bottom: 1em; border-radius: 3px; }
        .success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Oracle DB 연결 테스트</h1>
        <?php
        // Kubernetes 환경 변수에서 비밀번호를 가져옵니다.
        $db_password = getenv('ORACLE_PASSWORD');

        // Oracle DB 접속 정보
        $db_host = 'oracle-db-service'; // Kubernetes 서비스 이름
        $db_port = 1521;
        $db_service_name = 'FREEPDB1'; // Oracle Free 이미지의 기본 서비스 이름
        $db_user = 'system';

        // DSN (Data Source Name) 문자열 생성
        $dsn = "oci:dbname=//{$db_host}:{$db_port}/{$db_service_name};charset=AL32UTF8";

        try {
            // PDO 객체 생성 및 DB 연결
            $pdo = new PDO($dsn, $db_user, $db_password);
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

            echo '<div class="status success">성공: Oracle 데이터베이스에 성공적으로 연결되었습니다.</div>';

            // 간단한 쿼리 실행
            $stmt = $pdo->query("SELECT 'Oracle DB 응답: ' || dummy AS response FROM DUAL");
            $result = $stmt->fetch(PDO::FETCH_ASSOC);

            echo '<p><strong>쿼리 결과:</strong> ' . htmlspecialchars($result['RESPONSE']) . '</p>';

        } catch (PDOException $e) {
            // 에러 메시지 출력
            echo '<div class="status error">실패: 데이터베이스 연결에 실패했습니다.</div>';
            echo '<p><strong>에러 내용:</strong> ' . htmlspecialchars($e->getMessage()) . '</p>';
        }
        ?>
    </div>
</body>
</html>