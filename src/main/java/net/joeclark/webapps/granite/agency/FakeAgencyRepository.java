package net.joeclark.webapps.granite.agency;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

// TODO: create tests of this repository implementation, or actually move this to the testing class hierarchy

@Repository
@Profile("fake")
public class FakeAgencyRepository implements AgencyRepository {


    @Override
    public List<Agency> findAll() {
        Agency joes = new Agency("Joe's Sundries","Bangor","ME","555-207-1234");
        Agency clarks = new Agency("Clark & Sons","Farmington","ME","555-207-9876");
        return Arrays.asList(joes,clarks);
    }


}
