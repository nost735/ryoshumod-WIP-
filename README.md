# [Arayashiki Mod] - Issue: Model & Skill Keys Not Working

## 🇰🇷 문제 설명 (Korean)

### 1. 요약
현재 프로젝트에서 **모델이 렌더링/작동하지 않는 문제**와 **스킬 키(우클릭, F키)가 입력되지 않는 문제**가 발생하고 있습니다.

### 2. 상세 문제
* **모델 관련**: 프로그램을 실행해도 모델이 화면에 나타나지 않거나 애니메이션이 작동하지 않습니다.
* **스킬 키 관련**: 
    * **우클릭(Right-Click)**: 스킬이 발동되지 않음.
    * **F키**: 상호작용 및 스킬 사용이 불가능함.

### 3. 코드 위치
* 입력 로직 관련: `src/` 내의 입력 처리 파일
* 모델 로딩 관련: `src/` 내의 렌더링 또는 캐릭터 설정 파일

---

## 🇺🇸 Issue Description (English)

### 1. Summary
I am experiencing issues where the **model does not function/render**, and **skill keys (Right-click, F key) are unresponsive**.

### 2. Specific Issues
* **Model Issue**: The model does not appear on the screen or the animations are not playing upon execution.
* **Skill Key Issue**:
    * **Right-Click**: Skill trigger is not working.
    * **F Key**: Interaction or skill use is not responding.

### 3. Relevant Code
* Input Logic: Files related to input handling in the `/src` directory.
* Model Loading: Files related to rendering or character setup in the `/src` directory.

---

## 🛠 실행 및 재현 방법 (How to Reproduce)
1. Clone this repo.
2. Install dependencies.
3. Run the project.
4. Try pressing 'F' or 'Right-Click' to see the issue.
