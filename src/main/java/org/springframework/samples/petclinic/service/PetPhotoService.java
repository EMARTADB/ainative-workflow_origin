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

import java.util.Optional;

/**
 * Service for managing pet photos.
 * All photo operations must go through this interface.
 */
public interface PetPhotoService {

    /**
     * Save (insert or replace) a photo for the given pet.
     *
     * @param petId       the pet ID
     * @param content     raw image bytes
     * @param contentType MIME type (image/jpeg or image/png)
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
