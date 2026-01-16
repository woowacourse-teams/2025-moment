#!/usr/bin/env python3
"""
.env 파일 접근 차단 hook
Claude Code가 민감한 환경 변수 파일에 접근하는 것을 방지합니다.
"""
import json
import sys
import re

PROTECTED_PATTERNS = [
    r'\.env$',
    r'\.env\.',
    r'\.env\.local',
    r'\.env\.production',
    r'\.env\.development',
]

try:
    input_data = json.load(sys.stdin)
except json.JSONDecodeError:
    sys.exit(0)

tool_name = input_data.get('tool_name', '')
tool_input = input_data.get('tool_input', {})
file_path = tool_input.get('file_path', '')

if tool_name in ['Read', 'Edit', 'Write'] and file_path:
    for pattern in PROTECTED_PATTERNS:
        if re.search(pattern, file_path):
            print(f"Access denied: .env files are protected", file=sys.stderr)
            sys.exit(2)

sys.exit(0)
