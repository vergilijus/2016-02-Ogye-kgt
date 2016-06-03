package mechanics;

import models.GameUser;

import java.util.HashMap;
import java.util.Map;

public class WebSocketService {
    private Map<String, GameWebSocket> userSockets = new HashMap<>();

    public void addUser(GameWebSocket socket) {
        userSockets.put(socket.getMyName(), socket);
    }

    public void notifyStartGame(GameUser user) {
        final GameWebSocket gameWebSocket = userSockets.get(user.getMyName());
        gameWebSocket.startGame(user);
    }

    public void notifyStartWaiting(String user) {
        final GameWebSocket gameWebSocket = userSockets.get(user);
        gameWebSocket.waitForSession();
    }

    public void notifyGameOver(GameUser user, boolean win) {
        userSockets.get(user.getMyName()).gameOver(win);
    }
}