package io.easeci.server;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ratpack.server.RatpackServer;

import static java.util.Objects.isNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerBootstrapper {
    private static ServerBootstrapper bootstrapper;

    public static ServerBootstrapper getInstance() {
        if (isNull(ServerBootstrapper.bootstrapper)) {
            ServerBootstrapper.bootstrapper = new ServerBootstrapper();
        }
        return bootstrapper;
    }

    public void run() {
        try {
            RatpackServer.start(server -> {
                server.handlers(chain -> chain
                        .get(ctx -> ctx.render("Hello"))
                        .get(":name", ctx -> ctx.render("Hello " + ctx.getPathTokens().get("name") + "!")));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
