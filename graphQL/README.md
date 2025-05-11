# 概要

このプロジェクトは、GraphQLをKotlin * Spring Bootを使用してCRUDを実装してみたプロジェクトです。

# やったこと

- application.ymlでURLを設定
- Basic認証を使用
- 複数のcontrollerの定義
- CRUDの実装
- resourcesフォルダにGraphQLの定義を記述

# 動作確認方法

1. Spring Bootの起動
2. 好きなツールを使用してリクエストを投げる
   ツールは、ブラウザでGraphQLをリクエストできる「graphiql」とゆうツールもある org.springframework.boot:
   spring-boot-starter-graphqlのライブラリに標準搭載されている

- リクエストURL
  POST or GET http://localhost:8080/api/graphql

- リクエスト定義
  graphQLMemo.txtに記載している

# メモ

GraphQLは一つのリクエストで、クライアントが必要な情報を全て取得することができる新しいリクエスト方式。
ただし、サーバー側目線だと一つのエンドポイントで、リクスト内容によって取得するデータが異なるため、ハンドリングが複雑化する可能性がある。

その性質を生かして、BackEnd For FrontEnd（BFF）やマイクロサービスアーキテクチャと相性がいい。
逆にシンプルなCRUDで、元々複数のAPIリクエストをしていないアプリでは機能過剰になる気がして向いていないと思う。
ある程度の規模かつ複数のAPIへリクエストしていたアプリでは有効だと思われる。

キャッシュやドキュメントで課題があるため、適切に設計を行うことが必要となる。

認証や認可はフィールド単位で行う必要がある

設計についてはこれから勉強していく必要がる。



