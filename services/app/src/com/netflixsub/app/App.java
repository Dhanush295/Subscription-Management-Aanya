package com.netflixsub.app;

import com.netflixsub.admin.AdminHandler;
import com.netflixsub.auth.AuthHandler;
import com.netflixsub.auth.AuthStore;
import com.netflixsub.catalog.CatalogHandler;
import com.netflixsub.catalog.CatalogStore;
import com.netflixsub.common.Http;
import com.netflixsub.common.Json;
import com.netflixsub.model.Db;
import com.netflixsub.model.Schema;
import com.netflixsub.notification.NotificationHandler;
import com.netflixsub.payment.PaymentHandler;
import com.netflixsub.payment.PaymentProcessor;
import com.netflixsub.plan.PlanHandler;
import com.netflixsub.plan.PlanStore;
import com.netflixsub.subscription.SubHandler;
import com.netflixsub.subscription.SubStore;
import com.netflixsub.user.ProfileStore;
import com.netflixsub.user.UserHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class App {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        System.out.println("[db] connecting to Supabase...");
        Db.init();
        System.out.println("[db] applying schema + seed (drops and reseeds every boot)");
        Schema.applyAndSeed(AuthStore.hash("admin@123"));
        Runtime.getRuntime().addShutdownHook(new Thread(Db::close));

        ProfileStore profiles     = new ProfileStore();
        AuthStore auth            = new AuthStore();
        PlanStore plans           = new PlanStore();
        CatalogStore catalog      = new CatalogStore();
        PaymentProcessor payments = new PaymentProcessor();
        SubStore subs             = new SubStore();

        // admin@gmail.com account+profile are inserted by seed.sql and loaded into
        // AuthStore/ProfileStore above. Nothing more to do here.

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/auth",           new AuthHandler(auth, profiles));
        server.createContext("/users",          new UserHandler(profiles));
        server.createContext("/plans",          new PlanHandler(plans));
        server.createContext("/movies",         new CatalogHandler(catalog));
        server.createContext("/subscriptions",  new SubHandler(subs, plans, payments));
        server.createContext("/payments",       new PaymentHandler(payments));
        server.createContext("/invoices",       new PaymentHandler(payments));
        server.createContext("/notifications",  new NotificationHandler());
        server.createContext("/admin",          new AdminHandler(auth, profiles, subs, plans));
        server.createContext("/",               new Root());

        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("======================================================");
        System.out.println(" NETFLIX.sub  —  single-port server on http://localhost:" + port);
        System.out.println(" Endpoints:");
        System.out.println("   POST/GET   /auth/{signup,login,logout,validate}");
        System.out.println("   GET/POST   /users[/{id}]");
        System.out.println("   GET        /plans[/{code}]");
        System.out.println("   GET        /movies?language=&genre=");
        System.out.println("   POST/GET   /subscriptions[/user/{id}|/{id}/{cancel,renew,change}]");
        System.out.println("   POST/GET   /payments[/charge|/user/{id}]");
        System.out.println("   GET        /invoices[/user/{id}]");
        System.out.println("   POST/GET   /notifications[/user/{id}]");
        System.out.println("   GET/PUT/DEL /admin/{subscriptions,users,plans}   (admin@gmail.com / admin@123)");
        System.out.println("======================================================");
    }

    static class Root implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (Http.cors(ex)) return;
            Http.send(ex, 200, "{\"service\":\"netflix.sub\",\"status\":\"ok\"}");
        }
    }
}
