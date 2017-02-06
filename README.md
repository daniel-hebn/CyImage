# CyImage
싸이월드에 공개로 설정된 나의 이미지 로컬로 전체 다운받기

cyworld 에 업로드된 사진들이 아까우나 너무 많아서 일일이 다운 받기는 어렵고...

간단한 툴로 로컬에 이미지를 내려받도록 하였다. 

## To Use
a. 우선 www.cyworld.co.kr 에서 로그인을 하고 자신의 홈피의 고유 번호(tid)를 알아낸다.

b. 그 후 해당 프로젝트를 실행 후 다음의 api 를 실행한다. 
http://localhost:4242/api/cyImageDownload (POST)
- tid : 자신의 싸이월드 홈피 고유 번호
- directoryPath : 사진을 다운받을 자신의 로컬 

c. 만약 자신만의 비공개 폴더 내의 이미지도 내려 받고 싶다면 api 호출 시 다음의 2개 파라미터를 추가로 던진다.
- email: 자신의 이메일
- passwdRsa : 싸이월드 로그인 페이지 내에 구현된 자바스크립트로 암호화되어 넘어가는 패스워드 rsa 값 (로그인 후 개발자 도구에서 확인 가능)

## ETC 
- 사용성이 좋은 UI 를 구현하려 api 로 만들어 진행했으나.. 귀찮
- www.cyworld.co.kr 의 미니홈피 html 구조가 바뀌면 동작 안될 것임..

## Example movie
http://greg-lee.fun25.co.kr:13808/static/GetCyImage.mov
