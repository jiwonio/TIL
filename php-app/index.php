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
        // 환경 변수에서 DB 접속 정보 가져오기
        $db_password = getenv('ORACLE_PASSWORD');
        // docker-compose에서는 'oracle-dev', k8s에서는 'oracle-db-service'
        $db_host = getenv('ORACLE_HOST') ?: 'oracle-db-service';

        $db_port = 1521;
        $db_service_name = 'FREEPDB1';
        $db_user = 'system';

        $dsn = "oci:dbname=//{$db_host}:{$db_port}/{$db_service_name};charset=AL32UTF8";

        try {
            $pdo = new PDO($dsn, $db_user, $db_password);
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

            echo '<div class="status success">성공: Oracle 데이터베이스에 성공적으로 연결되었습니다. (Host: ' . htmlspecialchars($db_host) . ')</div>';

            $stmt = $pdo->query("SELECT 'Oracle DB 응답: ' || dummy AS response FROM DUAL");
            $result = $stmt->fetch(PDO::FETCH_ASSOC);

            echo '<p><strong>쿼리 결과:</strong> ' . htmlspecialchars($result['RESPONSE']) . '</p>';

        } catch (PDOException $e) {
            echo '<div class="status error">실패: 데이터베이스 연결에 실패했습니다. (Host: ' . htmlspecialchars($db_host) . ')</div>';
            echo '<p><strong>에러 내용:</strong> ' . htmlspecialchars($e->getMessage()) . '</p>';
        }
        ?>
    </div>
</body>
</html>