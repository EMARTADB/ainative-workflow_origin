/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.PetPhoto;
import org.springframework.samples.petclinic.repository.PetPhotoStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Default implementation of {@link PetPhotoService}.
 * Delegates persistence to the active {@link PetPhotoStore} implementation.
 */
@Service
public class PetPhotoServiceImpl implements PetPhotoService {

    private final PetPhotoStore petPhotoStore;

    public PetPhotoServiceImpl(PetPhotoStore petPhotoStore) {
        this.petPhotoStore = petPhotoStore;
    }

    @Override
    @Transactional
    public void save(int petId, byte[] content, String contentType) {
        petPhotoStore.save(petId, content, contentType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PetPhoto> findByPetId(int petId) {
        return petPhotoStore.findByPetId(petId);
    }

    @Override
    @Transactional
    public void deleteByPetId(int petId) {
        petPhotoStore.deleteByPetId(petId);
    }

}
