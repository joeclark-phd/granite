package net.joeclark.webapps.granite.agency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgencyServiceImpl implements AgencyService {

    private final AgencyRepository agencyRepository;

    public AgencyServiceImpl(AgencyRepository agencyRepository) {
        this.agencyRepository=agencyRepository;
    }

    @Override
    public List<Agency> findAll() {
        return agencyRepository.findAll();
    }

}
