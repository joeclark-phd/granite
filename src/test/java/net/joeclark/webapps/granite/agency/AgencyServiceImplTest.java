package net.joeclark.webapps.granite.agency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AgencyServiceImplTest {

    private AgencyRepository emptyRepository = new AgencyRepository() {
        @Override
        public List<Agency> findAll() {
            return List.of();
        }
    };
    private AgencyRepository fullRepository = new AgencyRepository() {
        @Override
        public List<Agency> findAll() {
            return List.of(new Agency(),new Agency());
        }
    };

    private AgencyService agencyService;

    @Test
    @DisplayName("findAll should return all records found in/by the AgencyRepository")
    public void findAllShouldReturnAllRecords() {
        agencyService = new AgencyServiceImpl(fullRepository);
        assertFalse(agencyService.findAll().isEmpty());
        assertEquals(2,agencyService.findAll().size());
    }

    @Test
    @DisplayName("if AgencyRepository is empty, findAll should return an empty List<Agency>")
    public void findAllShouldReturnEmptyListWhenRepositoryIsEmpty() {
        agencyService = new AgencyServiceImpl(emptyRepository);
        assertTrue(agencyService.findAll().isEmpty());
    }


}