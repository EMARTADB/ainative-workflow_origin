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
package org.springframework.samples.petclinic.repository;

import org.springframework.samples.petclinic.model.PetPhoto;

import java.util.Optional;

/**
 * Storage abstraction for pet photos.
 * Implementations exist for JPA, JDBC, and Spring Data JPA profiles.
 * Controllers and services must only interact with this interface.
 */
public interface PetPhotoStore {

    /**
     * Save (insert or replace) a photo for the given pet.
     */
    void save(int petId, byte[] content, String contentType);

    /**
     * Find the photo for the given pet, or empty if none exists.
     */
    Optional<PetPhoto> findByPetId(int petId);

    /**
     * Delete the photo for the given pet. No-op if none exists.
     */
    void deleteByPetId(int petId);

}
