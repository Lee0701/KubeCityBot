package kr.kubecity.bot;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VotesDatabase {
    private final Connection connection;

    public VotesDatabase(String path) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        try(Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS votes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
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

    public Vote getVote(int id) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM votes WHERE id = ?"
        )) {
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return parseVote(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

    public List<Vote> getVotes(String voterUuid) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM votes WHERE voter_uuid = ?"
        )) {
            statement.setString(1, voterUuid);

            ResultSet results = statement.executeQuery();
            return parseVotes(results);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int deleteVote(int id) {
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM votes WHERE id = ?"
        )) {
            statement.setInt(1, id);
            return statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int clearVotes(String voterUuid) {
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM votes WHERE voter_uuid = ?"
        )) {
            statement.setString(1, voterUuid);
            return statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private List<Vote> parseVotes(ResultSet results) throws SQLException {
        List<Vote> votes = new ArrayList<>();
        while (results.next()) {
            votes.add(parseVote(results));
        }
        return votes;
    }

    private Vote parseVote(ResultSet results) throws SQLException {
        int id = results.getInt("id");
        int buildingId = results.getInt("building_id");
        String voterUuid = results.getString("voter_uuid");
        Date date = new Date(results.getLong("date"));
        return new Vote(id, buildingId, voterUuid, date);
    }

    public void close() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
