const net = require('net');

const socketPath = '/tmp/test_socket.sock';

const requestData = {
    id: 1,
    params: [42, 23],
    param_types: ["int", "int"], // JSONなので型名は文字列にする
    method: "subtract",
};

const client = net.createConnection(socketPath, () => {
    console.log('サーバーに接続しました');
    client.write(JSON.stringify(requestData) + "\n");
    // 書き込み終わったら、読み取り専用モードにする（またはそのまま待つ）
});

// サーバーからのレスポンスを受け取る
client.on('data', (data) => {
    const response = JSON.parse(data.toString());
    console.log('サーバーからのレスポンス:', response);
    // レスポンスを受け取ったら接続を閉じる
    client.end();
});

client.on('error', (err) => {
    console.error('接続エラー:', err.message);
});