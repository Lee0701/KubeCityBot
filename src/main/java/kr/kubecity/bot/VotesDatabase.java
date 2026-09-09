package kr.kubecity.bot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VotesDatabase {
    private final Connection connection;

    public VotesDatabase(String path) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        try(Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS votes (" +
                    "building_id INTEGER, " +
                    "voter_uuid TEXT, " +
                    "date INTEGER)"
            );
        }
    }

    public void putVote(Vote vote) {
        try(PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO votes (building_id, voter_uuid, date) VALUES (?, ?, ?)"
        )) {
            statement.setInt(1, vote.buildingId());
            statement.setString(2, vote.voterUuid());
            statement.setLong(3, vote.date().getTime());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Vote> getVotes(Building building) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM votes WHERE building_id = ?"
        )) {
            statement.setInt(1, building.getWikiPageId());

            ResultSet resultSet = statement.executeQuery();
            return parseVotes(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Vote> getVotes(Building building, Date startDate, Date endDate) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM votes WHERE building_id = ? AND date >= ? AND date < ?"
        )) {
            statement.setInt(1, building.getWikiPageId());
            statement.setLong(2, startDate.getTime());
            statement.setLong(3, endDate.getTime());

            ResultSet results = statement.executeQuery();
            return parseVotes(results);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Vote> parseVotes(ResultSet results) throws SQLException {
        List<Vote> votes = new ArrayList<>();
        while (results.next()) {
            int buildingId = results.getInt("building_id");
            String voterUuid = results.getString("voter_uuid");
            Date date = new Date(results.getLong("date"));
            Vote vote = new Vote(buildingId, voterUuid, date);
            votes.add(vote);
        }
        return votes;
    }

    public void close() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
