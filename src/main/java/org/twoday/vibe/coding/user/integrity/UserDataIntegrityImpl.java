package org.twoday.vibe.coding.user.integrity;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserDataIntegrityImpl implements UserDataIntegrity {

    @Override
    public void validateId(UUID id) {
        if(id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
    }
}
