#!/bin/bash

# 테스트할 파일명 (GitHub Classroom 과제에서 요구하는 파일명으로 수정하세요)
TARGET_FILE="README.md"

echo "Checking $TARGET_FILE..."

# 1. 파일 존재 여부 확인
if [ ! -f "$TARGET_FILE" ]; then
    echo "❌ 에러: $TARGET_FILE 파일이 제출되지 않았습니다."
    exit 1
fi

# 2. 파일 형식 확인 (MIME Type이 text인지)
MIME_TYPE=$(file --mime-type -b "$TARGET_FILE")
if [[ "$MIME_TYPE" != text/* ]]; then
    echo "❌ 에러: $TARGET_FILE은 텍스트 파일이 아닙니다. ($MIME_TYPE)"
    exit 1
fi

# 3. 최소 마크다운 문법 포함 여부 (예: 하나 이상의 헤더 '#' 포함)
# 학생들에게 특정 문법을 강제하고 싶을 때 유용합니다.
if ! grep -q "^# " "$TARGET_FILE"; then
    echo "❌ 에러: 마크다운 헤더(# )가 포함되어 있지 않습니다."
    exit 1
fi

echo "✅ 모든 테스트를 통과했습니다!"
exit 0
