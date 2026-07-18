# 스피드-스태깅-타이머 (codex)

[![Android CI](https://github.com/xiriuz/speed-stacking-timer-codex/actions/workflows/ci.yml/badge.svg)](https://github.com/xiriuz/speed-stacking-timer-codex/actions/workflows/ci.yml)
[![Latest release](https://img.shields.io/github/v/release/xiriuz/speed-stacking-timer-codex)](https://github.com/xiriuz/speed-stacking-timer-codex/releases/latest)

두 손으로 준비하고 손을 떼면 시작되며, 다시 두 손으로 터치하면 멈추는 Android 스피드 스태깅 타이머입니다.

## APK 다운로드

1. [최신 Release](https://github.com/xiriuz/speed-stacking-timer-codex/releases/latest)를 엽니다.
2. `speed-stacking-timer-codex-vX.Y.Z.apk`를 다운로드합니다.
3. Android에서 필요할 경우 해당 브라우저의 **알 수 없는 앱 설치** 권한을 허용합니다.
4. APK를 열어 설치합니다.

APK 옆의 `.sha256` 파일로 다운로드 무결성을 확인할 수 있습니다.

## 사용 방법

1. 왼손과 오른손을 화면 아래의 각 패드에 동시에 올립니다.
2. 초록불이 켜진 상태로 잠시 유지합니다.
3. 빨간불로 바뀌면 두 손을 모두 뗍니다.
4. 타이머가 시작됩니다.
5. 두 패드를 다시 동시에 터치하면 타이머가 멈춥니다.
6. 두 손을 떼면 다음 측정을 준비할 수 있습니다.

한쪽 손만으로는 준비, 시작 또는 정지가 실행되지 않습니다.

## 구조

핵심 로직은 Compose UI와 분리된 순수 Kotlin 모듈로 구성되어 있습니다.

```text
core/
├── NanoClock.kt                 # 교체 가능한 단조 증가 시계
├── Stopwatch.kt                 # 시작·정지·경과 시간 계산
├── DualTouchTracker.kt          # 포인터별 왼손·오른손 판정
└── StackingTimerController.kt   # 초록→빨강→실행→정지 상태 머신

presentation/
└── TimerFormatter.kt            # 밀리초 표시 형식

MainActivity.kt                  # Compose UI와 터치 어댑터
```

## TDD

각 동작은 실패하는 테스트를 먼저 추가한 뒤 구현했습니다.

```bash
./gradlew testDebugUnitTest
```

테스트 범위:

- 단조 시계 기반 경과 시간, 정지 및 초기화
- 두 패드 동시 터치와 포인터 해제·이동·취소
- 준비 시간, 조기 해제, 손 떼기 시작, 양손 정지
- 밀리초 및 1분 이상 기록 표시

## 빌드

JDK 17과 Android SDK가 필요합니다.

```bash
./gradlew testDebugUnitTest assembleDebug
```

생성 파일:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Git 및 릴리즈

- `main`: 항상 빌드 가능한 안정 브랜치
- 기능 브랜치 → Pull Request → CI 통과 → `main` 병합
- `v1.0.0` 형식의 태그를 푸시하면 서명된 APK Release 자동 생성
- 모든 `main` 푸시와 Pull Request에서 테스트 및 디버그 APK 빌드

```bash
git tag v1.0.0
git push origin v1.0.0
```

릴리즈 서명 정보는 GitHub Actions Secrets에만 보관하며 저장소에는 키 파일이나 비밀번호를 커밋하지 않습니다.

