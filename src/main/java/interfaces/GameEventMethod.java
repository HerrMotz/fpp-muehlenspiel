package interfaces;

public enum GameEventMethod {
    Ping,
    Pong,

    Login,
    Register,
    Logout,
    AuthResponse,

    GameStart,
    GameAborted,
    GameOver,

    IllegalMove,
    PlaceStone,
    RemoveStone,
    MoveStone,

    EnterQuickMatchQueue,
    LeaveQuickMatchQueue,

    BroadcastPlayerPool,
    MatchRequest,
    MatchRequestResponse
}
