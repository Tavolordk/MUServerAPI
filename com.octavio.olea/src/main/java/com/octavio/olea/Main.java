package com.octavio.olea;

import io.muserver.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Main {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        objectMapper.registerModule(module);

        MuServer server = MuServerBuilder.httpServer()
                .addHandler(Method.POST, "/restaurant/reserve", (request, response, pathParams) -> reserveTable(request, response))
                .addHandler(Method.GET, "/restaurant/reservations", (request, response, pathParams) -> getReservations(request, response))
                .start();
        System.out.println("Server started at " + server.uri());
    }

    private static void reserveTable(MuRequest request, MuResponse response) throws Exception {
        String payload = request.readBodyAsString();
        Reservation reservation = objectMapper.readValue(payload, Reservation.class);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO reservations (client_name, table_size, date_time) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, reservation.getClientName());
                statement.setInt(2, reservation.getTableSize());
                statement.setTimestamp(3, Timestamp.valueOf(reservation.getDateTime()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        response.status(Response.Status.CREATED.getStatusCode());
    }

    private static void getReservations(MuRequest request, MuResponse response) throws Exception {
        String dateTimeString = request.query().get("dateTime");
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            response.status(Response.Status.BAD_REQUEST.getStatusCode());
            response.write("The Date is null or empty");
            return;
        }
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);

        List<Reservation> reservationsForDateTime = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM reservations WHERE date_time = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setTimestamp(1, Timestamp.valueOf(dateTime));

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Reservation reservation = new Reservation();
                        reservation.setClientName(resultSet.getString("client_name"));
                        reservation.setTableSize(resultSet.getInt("table_size"));
                        reservation.setDateTime(resultSet.getTimestamp("date_time").toLocalDateTime());
                        reservationsForDateTime.add(reservation);
                    }
                }
            }
        } catch (SQLException e) {
            response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        response.contentType(MediaType.APPLICATION_JSON);
        response.write(objectMapper.writeValueAsString(reservationsForDateTime));
    }
}
