package net.joeclark.webapps.granite.agency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AgencyRepositoryImpl implements AgencyRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    class AgencyRowMapper implements RowMapper<Agency> {
        @Override
        public Agency mapRow(ResultSet rs, int rowNum) throws SQLException {
            Agency agency = new Agency();
            agency.setAgencyName(rs.getString("name"));
            agency.setCity(rs.getString("city"));
            agency.setState(rs.getString("state"));
            agency.setPhoneNumber(rs.getString("phone_number"));
            return agency;
        }
    }

    @Override
    public List<Agency> findAll() {
        return jdbcTemplate.query("select * from agencies", new AgencyRowMapper());
    }
}
