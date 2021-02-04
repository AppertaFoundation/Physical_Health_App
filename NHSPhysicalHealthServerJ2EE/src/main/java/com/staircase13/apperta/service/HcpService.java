package com.staircase13.apperta.service;

import com.staircase13.apperta.entities.User;
import com.staircase13.apperta.entities.Hcp;
import com.staircase13.apperta.repository.HcpRepository;
import com.staircase13.apperta.repository.AppertaUserRepository;
import com.staircase13.apperta.service.dto.HcpDto;
import com.staircase13.apperta.service.dto.HcpSummaryDto;
import com.staircase13.apperta.service.exception.InvalidHcpUsernameException;
import com.staircase13.apperta.service.exception.NhsIdAlreadyRegisteredException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.staircase13.apperta.entities.Role.HCP;

@Service
public class HcpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HcpService.class);

    private final HcpRepository hcpRepository;

    private final AppertaUserRepository appertaUserRepository;

    @Autowired
    public HcpService(HcpRepository hcpRepository, AppertaUserRepository appertaUserRepository) {
        this.hcpRepository = hcpRepository;
        this.appertaUserRepository = appertaUserRepository;
    }

    public Optional<HcpSummaryDto> findByNhsId(String nhsId) {
        LOGGER.debug("Find HCP by NHS ID [{}]",nhsId);
        Optional<Hcp> hcp = hcpRepository.findByNhsId(nhsId);

        if(!hcp.isPresent()) {
            LOGGER.debug("No HCP Found");
            return Optional.empty();
        }

        HcpSummaryDto result = new HcpSummaryDto();
        BeanUtils.copyProperties(hcp.get(), result);

        LOGGER.debug("Returning HCP [{}]",hcp.get().getId());
        return Optional.of(result);
    }

    /**
     * Return HCP information for the specified user
     *
     * @param username The HCP for which the profile is required
     * @return The users profile. If the HCP user hasn't provided any HCP specific information, this will just contain their username and email address
     * @throws InvalidHcpUsernameException If the user doesn't exist, or doesn't have the HCP role
     */
    public HcpDto getProfile(String username) throws InvalidHcpUsernameException {
        LOGGER.debug("Get Profile by username [{}]",username);

        User user = getHcpUser(username);

        HcpDto.HcpDtoBuilder builder = HcpDto.builder()
                .username(user.getUsername())
                .email((user.getEmailAddress()));

        Optional<Hcp> hcpOptional = hcpRepository.findByUserUsername(username);

        if(hcpOptional.isPresent()) {
            Hcp hcp = hcpOptional.get();

            builder
                    .firstNames(hcp.getFirstNames())
                    .jobTitle(hcp.getJobTitle())
                    .lastName(hcp.getLastName())
                    .location(hcp.getLocation())
                    .nhsId(hcp.getNhsId())
                    .title(hcp.getTitle())
                    .build();
        } else {
            LOGGER.info("No HCP record for user [{}]",username);
        }

        return builder.build();
    }

    /**
     * Updates the HCP information. Will also update the email address associated with the user account
     *
     * @param hcpDto The HCP information to update
     * @throws InvalidHcpUsernameException If the user doesn't exist, or doesn't have the HCP role
     * @throws NhsIdAlreadyRegisteredException If the NHS ID has already been registered by a different HCP
     */
    public void updateProfile(HcpDto hcpDto) throws InvalidHcpUsernameException, NhsIdAlreadyRegisteredException  {
        String username = hcpDto.getUsername();
        String nhsId = hcpDto.getNhsId();

        LOGGER.debug("Update Profile for username [{}]",username);

        Optional<String> alreadyRegisteredTo = findUsernameForNhsId(nhsId);
        if(alreadyRegisteredTo.isPresent() && !alreadyRegisteredTo.get().equals(username)) {
            throw new NhsIdAlreadyRegisteredException(nhsId);
        }

        User user = getHcpUser(hcpDto.getUsername());
        user.setEmailAddress(hcpDto.getEmail());
        appertaUserRepository.save(user);

        Optional<Hcp> hcpOptional = hcpRepository.findByUserUsername(hcpDto.getUsername());

        LOGGER.debug("Is existing HCP Profile Present? '{}'", hcpOptional.isPresent());

        Hcp hcp = updateHcp(hcpOptional.orElse(new Hcp()), hcpDto, user);

        hcpRepository.save(hcp);
    }

    /**
     * Populate a list of hcp records from a list of nhsIds. Entries are only returned if they
     * contain at least a valid surname.
     * @param hcpList The list of NHS ids
     * @return The populated list of HCP summaries.
     */
    public List<HcpSummaryDto> populateDtoList(List<String> hcpList) {
        HcpSummaryDto dummyHcp = new HcpSummaryDto();
        return hcpList.stream().map(h -> findByNhsId(h).orElse(dummyHcp)).filter(h -> StringUtils.isNotEmpty(h.getLastName())).collect(Collectors.toList());
    }

    private Hcp updateHcp(Hcp target, HcpDto newContent, User user) {
        BeanUtils.copyProperties(newContent,target);
        target.setUser(user);
        return target;
    }

    private User getHcpUser(String username) throws InvalidHcpUsernameException{
        Optional<User> userOptional = appertaUserRepository.findByUsernameAndRole(username, HCP);
        if(!userOptional.isPresent()) {
            throw new InvalidHcpUsernameException(username);
        }
        return userOptional.get();
    }

    private Optional<String> findUsernameForNhsId(String nhsId) {
        Optional<Hcp> hcp = hcpRepository.findByNhsId(nhsId);

        if(!hcp.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(hcp.get().getUser().getUsername());
    }


}
