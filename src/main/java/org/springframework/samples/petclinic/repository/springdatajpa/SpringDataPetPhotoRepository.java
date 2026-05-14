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
package org.springframework.samples.petclinic.repository.springdatajpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.PetPhoto;
import org.springframework.samples.petclinic.repository.PetPhotoStore;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Spring Data JPA implementation of {@link PetPhotoStore}.
 * Provides store/retrieve/delete via derived and JPQL queries.
 */
public interface SpringDataPetPhotoRepository extends JpaRepository<PetPhoto, Integer>, PetPhotoStore {

    Optional<PetPhoto> findByPetId(int petId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PetPhoto p WHERE p.petId = :petId")
    void deleteByPetId(@Param("petId") int petId);

    @Override
    default void save(int petId, byte[] content, String contentType) {
        Optional<PetPhoto> existing = findByPetId(petId);
        if (existing.isPresent()) {
            PetPhoto photo = existing.get();
            photo.setContent(content);
            photo.setContentType(contentType);
            save(photo);
        } else {
            PetPhoto photo = new PetPhoto();
            photo.setPetId(petId);
            photo.setContent(content);
            photo.setContentType(contentType);
            save(photo);
        }
    }

}
