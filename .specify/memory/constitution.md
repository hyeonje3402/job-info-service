<!--
Version change: 0.0.0 → 1.0.0
List of modified principles:
  - [PRINCIPLE_1_NAME] → I. 테스트 중심 개발 (TDD)
  - [PRINCIPLE_2_NAME] → II. 보안 및 개인정보 보호 우선
  - [PRINCIPLE_3_NAME] → III. 한국어 중심 산출물 및 소통
  - [PRINCIPLE_4_NAME] → IV. AI 기반 매칭 및 사용자 경험
  - [PRINCIPLE_5_NAME] → V. 지속적인 통합 및 품질 관리
Added sections:
  - 기술 스택 및 제약 사항
  - 개발 워크플로우
Removed sections: None
Templates requiring updates:
  - .specify/templates/plan-template.md (✅ updated/verified)
  - .specify/templates/spec-template.md (✅ updated/verified)
  - .specify/templates/tasks-template.md (✅ updated/verified)
Follow-up TODOs: None
-->

# HireSync Constitution

## Core Principles

### I. 테스트 중심 개발 (TDD)
모든 기능 개발은 테스트 케이스 작성을 우선합니다. 테스트가 실패하는 것을 확인한 후 코드를 구현하며, Red-Green-Refactor 사이클을 엄격히 준수합니다. 이는 코드의 안정성과 유지보수성을 보장하는 핵심 원칙입니다.

### II. 보안 및 개인정보 보호 우선
사용자의 개인정보(전화번호, 주소, 이력서 등)를 다루는 서비스인 만큼 보안을 최우선으로 합니다. 모든 데이터는 암호화하여 저장하며, HTTPS 프로토콜을 필수적으로 사용합니다. 보안 취약점 점검을 개발 프로세스의 필수 단계로 포함합니다.

### III. 한국어 중심 산출물 및 소통
모든 코드 코멘트, 문서(PRD, Spec, Plan, Tasks 등), 그리고 AI와의 대화는 한국어로 진행합니다. 이는 협업의 효율성을 높이고 프로젝트의 일관성을 유지하기 위함입니다.

### IV. AI 기반 매칭 및 사용자 경험
단순한 공고 나열을 넘어, AI 기술을 활용하여 구직자와 기업의 적합도를 분석하고 사용자에게 최적의 경험을 제공합니다. 모든 기능은 사용자의 편의성을 최우선으로 설계합니다.

### V. 지속적인 통합 및 품질 관리
코드 변경 사항은 지속적으로 통합(CI)되며, 자동화된 테스트를 통해 품질을 검증합니다. 99.9% 이상의 가용성을 유지하기 위해 시스템의 안정성을 지속적으로 모니터링합니다.

## 기술 스택 및 제약 사항
- **Backend**: Node.js (Express/NestJS) 또는 Python (FastAPI)
- **Frontend**: React (TypeScript)
- **Database**: PostgreSQL (개인정보 암호화 저장)
- **Infrastructure**: AWS (Auto-scaling, HTTPS 적용)
- **Security**: OAuth 2.0, JWT, SSL/TLS

## 개발 워크플로우
1. **Constitution**: 프로젝트 원칙 확인 및 필요시 업데이트
2. **Specification**: `spec.md` 작성 (사용자 시나리오 및 수락 기준 정의)
3. **Planning**: `plan.md` 작성 (기술 설계 및 TDD 전략 수립)
4. **Task Generation**: `tasks.md` 작성 (테스트 및 구현 작업 분할)
5. **Implementation**: TDD 사이클에 따른 개발
6. **Validation**: 테스트 통과 및 원칙 준수 여부 검증

## Governance
- 본 헌장은 프로젝트의 모든 의사결정과 개발 활동의 최상위 가이드라인입니다.
- 헌장 수정은 신중하게 이루어져야 하며, 수정 시 버전 번호를 갱신하고 변경 사항을 기록합니다.
- 모든 PR과 코드 리뷰는 본 헌장의 원칙(특히 TDD와 보안)을 준수하는지 확인해야 합니다.
- 헌장과 실제 구현 사이에 충돌이 발생할 경우, 헌장의 원칙을 우선하되 필요시 헌장을 개정합니다.

**Version**: 1.0.0 | **Ratified**: 2026-03-19 | **Last Amended**: 2026-03-19
