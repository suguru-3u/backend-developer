# ドメイン
oauth.sample

# 画面
- ホーム画面⚪  
GET /home　

- ログインページ表示⚪(APIへリクエストを送信する)
GET /login

- Oauthログイン画面⚪(APIへリクエストを送信する)  
Googleのoauthログインを行える
GET /oauth/login 

- サインイン画面⚪(APIへリクエストを送信する)  
GET /register

- 一覧画面  
一覧の表示・登録・削除が行える  
GET /tasks/index

- ユーザー管理画面
ユーザー情報の編集・更新・ログアウトが行える  
GET /user/me

# API
- ログイン  
POST api/login

- サインイン  
POST api/singing

- タスク一覧情報取得  
GET api/tasks

- タスク登録  
POST api/register/task  

- タスク削除  
DELETE api/task/{id}

- ユーザー情報取得  
GET api/user/me

- ユーザー情報更新  
PUT api/user/me

- ログアウト  
DELETE api/logout