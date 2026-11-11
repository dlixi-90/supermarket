package com.market.servlet;

import com.market.config.MongoDBConnection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.io.PrintWriter;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/test-mongodb")
public class TestMongoServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {

            MongoDatabase database
                    = MongoDBConnection.getDatabase();

            Document result
                    = database.runCommand(
                            new Document("ping", 1)
                    );

            out.println("<h1>MongoDB Connected Successfully!</h1>");

            out.println("<p>Database: "
                    + database.getName()
                    + "</p>");

            out.println("<h2>Collections:</h2>");

            for (String name : database.listCollectionNames()) {
                out.println("<p>" + name + "</p>");
            }

        } catch (Exception e) {

            out.println("<h1>MongoDB Connection Failed!</h1>");

            out.println("<pre>");
            out.println(e.getMessage());
            out.println("</pre>");

            e.printStackTrace();
        }
    }
}
