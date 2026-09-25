function connectWebSocket() {
    return new Promise((resolve, reject) => {
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);

        stompClient.connect(
            {},
            () => {
                console.log("WebSocket connected");
                resolve(stompClient);
            },
            error => {
                console.error("WebSocket error:", error);
                reject(error);
            }
        );
    });
}