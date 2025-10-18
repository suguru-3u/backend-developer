// ファイル名が app.12345.js であることを想定

document.addEventListener('DOMContentLoaded', () => {
    const articleContentDiv = document.getElementById('article-content');
    const articleId = 1; // 取得する記事ID (Kotlin Controllerの例と一致)
    const apiEndpoint = `/api/articles/${articleId}`;

    function fetchArticle() {
        console.log(`APIリクエスト開始: ${apiEndpoint}`);

        // 通常のGETリクエスト。ブラウザが自動的にキャッシュヘッダー (If-None-Matchなど) を付与します。
        fetch(apiEndpoint)
            .then(response => {
                // 開発者ツールで確認すべきステータスコード
                console.log(`HTTPステータス: ${response.status}`);

                if (response.status === 304) {
                    // 304 Not Modified の場合、データ本体は含まれないため、既存のキャッシュを使用
                    console.log('304 Not Modified: キャッシュされた記事データを使用します。');
                    return { content: articleContentDiv.innerHTML, isCached: true };
                }

                // 200 OK の場合、新しいデータが含まれる
                return response.text().then(text => ({ content: text, isCached: false }));
            })
            .then(data => {
                // 取得したデータ（またはキャッシュされたデータ）を表示
                const status = data.isCached ? 'キャッシュから再利用' : 'サーバーから新規取得';
                articleContentDiv.innerHTML = `
                    <h2>${data.content}</h2>
                    <p><strong>データソース:</strong> ${status}</p>
                    <p>現在の時刻: ${new Date().toLocaleTimeString()}</p>
                `;
            })
            .catch(error => {
                console.error('API呼び出しエラー:', error);
                articleContentDiv.innerHTML = '<p style="color: red;">記事のロード中にエラーが発生しました。</p>';
            });
    }

    // ページロード時と、キャッシュ制御を確認しやすいように数秒ごとにAPIを呼び出し
    fetchArticle();

    setInterval(fetchArticle, 30000); // 30秒ごとにAPIを再呼び出し
});