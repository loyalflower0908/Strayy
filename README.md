# 🐶 프로젝트 제목 : Stray 🐱
&nbsp;

실종 동물을 찾거나 유기 동물을 접수하는데 도움을 주는 서비스를 제공한다.

실시간으로 카메라를 찍으면 종을 분석하고 위치와 시간을 업로드해서 떠돌이 동물 지도에 등록, 해당 동물 정보를 확인할 수 있다.

그리고 커뮤니티를 넣고 각종 비즈니스 모델을 위한 틀을 구현해놓았다.

&nbsp;

&nbsp;

## 💻 담당한 개발 파트 💻
&nbsp;

전체 UI는 디자이너와 Figma를 통한 협업을 통해 UI 디자인을 보고 그대로 개발하였고

네이버 지도 API를 사용하여 Mobile Dynamic Map를 사용해서 동물 지도를 만들고

Geocoding, Reverse Geocoding 을 등록과 검색 등에 적용하였다.

동물들의 데이터관리는 Firebase Storage와 Firebase Firestore DB를 통해 구현하였고 지도와 연동하였다.

Flask 서버를 만들고 이미지를 통한 종 분석 모델을 넣는것을 했고 APP과 이미지와 값을 주고 받는 REST API 통신을

Retrofit2를 통해서 구현하였고 받은 값을 관리하기 위해 DB와 연동하였다.

로그인 페이지와 커뮤니티 파트는 협업을 통해 다른 팀원이 제작하였다.

&nbsp;

&nbsp;
_____________________________________________________
### 스크린샷
   


&nbsp;
   
<div style="display: flex; flex-direction: row;">
    <img src="https://github.com/loyalflower0908/Strayy/blob/7b33d60d51a239d630c86ec4688d93f60d0ab12b/app%20screenshot/main.png" width="20%" height="20%" style="margin: 0 10px;">
    <img src="https://github.com/loyalflower0908/Strayy/blob/7b33d60d51a239d630c86ec4688d93f60d0ab12b/app%20screenshot/main%20animal.png" width="20%" height="20%" style="margin: 0 10px;">
    <img src="https://github.com/loyalflower0908/Strayy/blob/7b33d60d51a239d630c86ec4688d93f60d0ab12b/app%20screenshot/pic%20upload.png" width="20%" height="20%" style="margin: 0 10px;">
    <img src="https://github.com/loyalflower0908/Strayy/blob/7b33d60d51a239d630c86ec4688d93f60d0ab12b/app%20screenshot/pic%20analysis.png" width="20%" height="20%" style="margin: 0 10px;">
</div>

<div style="display: flex; flex-direction: row;">
    <img src="https://github.com/loyalflower0908/Strayy/blob/7b33d60d51a239d630c86ec4688d93f60d0ab12b/app%20screenshot/animal%20map.png" width="20%" height="20%" style="margin: 0 10px;">
    <img src="https://github.com/loyalflower0908/Strayy/blob/7b33d60d51a239d630c86ec4688d93f60d0ab12b/app%20screenshot/map%20animal%20info.png" width="20%" height="20%" style="margin: 0 10px;">
    <img src="https://github.com/loyalflower0908/Strayy/blob/7b33d60d51a239d630c86ec4688d93f60d0ab12b/app%20screenshot/location%20search.png" width="20%" height="20%" style="margin: 0 10px;">
    <img src="https://github.com/loyalflower0908/Strayy/blob/7b33d60d51a239d630c86ec4688d93f60d0ab12b/app%20screenshot/species%20search.png" width="20%" height="20%" style="margin: 0 10px;">
</div>

&nbsp;

_____________________________________________________
### 📚 기술스택 📚
Jetpack Compose, Firebase Authentication, Firebase Storage, Firebase FirestoreDB, Naver Map API(Mobile Dynamic Map, Geocoding, Reverse Geocoding), Retrofit2, Flask, Figma

&nbsp;
