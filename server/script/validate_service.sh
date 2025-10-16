#!/bin/bash
for i in {1..10}; do
    # localhost의 8080 포트로 헬스 체크
    RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/health) # Spring Actuator health endpoint
    if [ "$RESPONSE_CODE" -eq 200 ]; then
        echo "Health check successful."
        exit 0 # 성공 시 0 반환
    fi
    echo "Health check attempt $i failed. Retrying in 5 seconds..."
    sleep 5
done

echo "Health check failed after multiple attempts."
exit 1 # 실패 시 1 반환
