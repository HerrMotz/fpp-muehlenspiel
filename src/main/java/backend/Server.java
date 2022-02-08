package backend;

import backend.helpers.Match;
import backend.handlers.ServerWorker;
import interfaces.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.bson.Document;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

public class Server extends Thread {
    public static final String authenticationDatabaseUserCollectionName = "User";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final int port;
    private final String authenticationDatabaseName;

    private final Set<ServerWorker> serverWorkers = ConcurrentHashMap.newKeySet();
    private final AtomicInteger serverWorkersSize = new AtomicInteger(0);

    private final ConcurrentLinkedQueue<ServerWorker> quickMatchQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger quickMatchQueueSize = new AtomicInteger(0);

    // Player Pool and logged-in users contain the same "players", only that playerPool contains ServerWorkers and the other Users
    private final Set<ServerWorker> playerPool = ConcurrentHashMap.newKeySet();
    private final AtomicInteger playerPoolSize = new AtomicInteger(0);
    private final Map<User, ServerWorker> loggedInUsers = new ConcurrentHashMap<>();
    private final AtomicInteger loggedInUsersSize = new AtomicInteger(0);

    private final Map<User, User> openRequests = new ConcurrentHashMap<>();

    private final MongoClient mongoClient;

    public Server(int port, String mongoConnectionString, String authenticationDatabaseName) {
        this.port = port;
        this.authenticationDatabaseName = authenticationDatabaseName;
        this.mongoClient = MongoClients.create(mongoConnectionString);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            // DEBUG
            System.out.println("Server started on " + port);

            //noinspection InfiniteLoopStatement
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // add ServerWorker
                ServerWorker serverWorker = new ServerWorker(this, clientSocket);
                serverWorker.start();

                addServerWorker(serverWorker);

                // DEBUG
                System.out.println("Connected to: " + clientSocket);
                System.out.println("Client count: " + serverWorkersSize.get());
            }
        } catch (IOException ioException) {
            System.err.println("The connection was interrupted while spinning up a server worker: " + ioException);
        }
    }

    private synchronized MongoCollection<Document> getUserCollection() {
        return mongoClient
                .getDatabase(authenticationDatabaseName)
                .getCollection(authenticationDatabaseUserCollectionName);
    }

    public synchronized void broadcastPlayerPool() {
        //DEBUG
        System.out.println("Broadcast Playerpool");

        Set<User> vacantPlayers = loggedInUsers.entrySet().stream()
                .filter(e -> !e.getValue().isInMatch())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        GameEvent event = new GameEvent(
                GameEventMethod.BroadcastPlayerPool,
                0,
                null,
                vacantPlayers
        );

        for (ServerWorker serverWorker: playerPool) {
            // DEBUG
            System.out.println("[PlayerPool] Broadcast to " + serverWorker + " " + Arrays.toString(loggedInUsers.keySet().toArray()));
            serverWorker.emit(event);
        }
    }

    public synchronized AuthenticationResponse login(ServerWorker client, String username, String password) {
        Document user = getUserCollection().find(
            eq("username", username)
        ).first();

        if (user == null) {
            return new AuthenticationResponse(
                    "This username is unknown.",
                    false,
                    AuthenticationResponseMethod.Login,
                    null
            );
        }

        if (!passwordEncoder.matches(password, (String) user.get("password"))) {
            return new AuthenticationResponse(
                    "This password is unfortunately not what we have in our records. Try again.",
                    false,
                    AuthenticationResponseMethod.Login,
                    null
            );
        }

        User userInterface = new User(
                user.get("_id").toString(),
                username
        );

        if (loggedInUsers.containsKey(userInterface)) {
            return new AuthenticationResponse(
                    "You are already logged in from another client.",
                    false,
                    AuthenticationResponseMethod.Login,
                    null
            );
        }

        client.setUser(userInterface);

        addToPlayerPool(client);
        return new AuthenticationResponse(
                "You have successfully logged in",
                true,
                AuthenticationResponseMethod.Login,
                userInterface
        );
    }

    public synchronized AuthenticationResponse register(String username, String password) {
        Document user = getUserCollection().find(
                eq("username", username)
        ).first();

        if (user != null) {
            return new AuthenticationResponse(
                    "This username is unfortunately taken.",
                    false,
                    AuthenticationResponseMethod.Register,
                    null
            );
        }

        getUserCollection().insertOne(new Document(new HashMap<>() {{
            put("username", username);
            put("password", passwordEncoder.encode(password));
        }}));

        return new AuthenticationResponse(
                "You have successfully registered. You may now login.",
                true,
                AuthenticationResponseMethod.Register,
                null
        );
    }

    public synchronized AuthenticationResponse logout(ServerWorker client) {
        removeFromPlayerPool(client);
        client.setUser(null);
        return new AuthenticationResponse(
                "You have successfully logged out.",
                true,
                AuthenticationResponseMethod.Logout,
                null
        );
    }

    public synchronized void createMatch(ServerWorker client1, ServerWorker client2) throws IllegalMoveException {
        new Match(client1, client2);

        System.out.println("[Matchmaking] New match created " + client1 + " & "+ client2);
    }

    public synchronized void relayMatchRequest(User toUser, User byUser, ServerWorker serverWorker) {
        if (toUser.equals(byUser)) {
            serverWorker.emit(new GameEvent(
                    GameEventMethod.IllegalMove,
                    -2,
                    null,
                    "Unfortunately, you may only invite other players to a match."
            ));
            return;
        }

        // There may only be one request at a time
        if (!openRequests.containsKey(byUser)) {
            openRequests.put(byUser, toUser);
            loggedInUsers.get(toUser).emit(new GameEvent(
                    GameEventMethod.MatchRequest,
                    0,
                    null,
                    byUser
            ));
        }

        // DEBUG
        System.out.println("Added new request: to:" + toUser + " , by:" + byUser);
    }

    public synchronized void relayMatchRequestResponse(User toUser, User byUser, ServerWorker respondee, boolean accepted) {
        // DEBUG
        System.out.println("Open requests: " + openRequests);

        if (openRequests.get(toUser).equals(byUser)) {
            ServerWorker requestee = loggedInUsers.get(toUser);

            requestee.emit(new GameEvent(
                    GameEventMethod.MatchRequestResponse,
                    0,
                    null,
                    byUser,
                    accepted
            ));

            openRequests.remove(toUser);

            if (accepted) {
                try {
                    createMatch(requestee, respondee);
                } catch (IllegalMoveException e) {
                    e.printStackTrace();
                }
            }
        } else {
            respondee.emit(new GameEvent(
                    GameEventMethod.IllegalMove,
                    -2,
                    null,
                    "The user you would like to respond to, has not sent you a match request."
            ));
        }

        // DEBUG
        System.out.println("Relayed response: to:" + toUser + " , by:" + byUser);
        System.out.println("Accepted? " + accepted);
    }

    public synchronized void addToQuickMatchQueue(ServerWorker serverWorker) {
        if (!quickMatchQueue.contains(serverWorker)) {
            quickMatchQueue.add(serverWorker);

            // Check if a new match can be made (so are there two or more players)
            if (quickMatchQueueSize.incrementAndGet() > 1) {
                try {
                    createMatch(popQuickMatchQueue(), popQuickMatchQueue());
                } catch (IllegalMoveException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized ServerWorker popQuickMatchQueue() {
        if (!quickMatchQueue.isEmpty()) {
            quickMatchQueueSize.decrementAndGet();
            return quickMatchQueue.poll();
        } else {
            return null;
        }
    }

    public synchronized void removeFromQuickMatchQueue(ServerWorker serverWorker) {
        if (quickMatchQueue.contains(serverWorker)) {
            quickMatchQueue.remove(serverWorker);
            quickMatchQueueSize.decrementAndGet();
        }
    }

    private synchronized void addServerWorker(ServerWorker serverWorker) {
        serverWorkersSize.incrementAndGet();
        serverWorkers.add(serverWorker);
    }

    public synchronized void removeServerWorker(ServerWorker serverWorker) {
        if (serverWorkers.contains(serverWorker)) {
            serverWorkers.remove(serverWorker);
            serverWorkersSize.decrementAndGet();

            removeFromQuickMatchQueue(serverWorker);
            removeFromPlayerPool(serverWorker);

            // DEBUG
            System.out.println("Client count: " + serverWorkersSize.get());
        }
    }

    public synchronized void addToPlayerPool(ServerWorker serverWorker) {
        if (!playerPool.contains(serverWorker)) {
            playerPoolSize.incrementAndGet();
            playerPool.add(serverWorker);
        }

        if (!loggedInUsers.containsKey(serverWorker.getUser())) {
            loggedInUsers.put(serverWorker.getUser(), serverWorker);
            loggedInUsersSize.incrementAndGet();
        }

        // DEBUG
        System.out.println("[PlayerPool] Player logged in: " + serverWorker.getUser());
        System.out.println("[PlayerPool] Current players: " + playerPoolSize.get() + ", " + playerPool);

        broadcastPlayerPool();

    }

    public synchronized void removeFromPlayerPool(ServerWorker serverWorker) {
        if (playerPool.contains(serverWorker)) {
            playerPool.remove(serverWorker);
            playerPoolSize.decrementAndGet();
        }

        // DEBUG
        System.out.println("loggedInUsers: " + loggedInUsers);

        if (serverWorker.getUser() != null) {
            if (loggedInUsers.containsKey(serverWorker.getUser())) {
                loggedInUsers.remove(serverWorker.getUser());
                loggedInUsersSize.decrementAndGet();
            }
        }

        broadcastPlayerPool();

        // DEBUG
        System.out.println("[PlayerPool] Player logged out " + serverWorker.getUser());
        System.out.println(playerPool);
    }

    public static void main(String[] args) {
        Server server = new Server(2302, "mongodb://mandant3-dev.via.local:27017", "mill_game");
        server.start();
    }
}
