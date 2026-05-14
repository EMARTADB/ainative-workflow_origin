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
package org.springframework.samples.petclinic.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.samples.petclinic.model.PetPhoto;
import org.springframework.samples.petclinic.repository.PetPhotoStore;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * JPA implementation of {@link PetPhotoStore}.
 */
@Repository
public class JpaPetPhotoStoreImpl implements PetPhotoStore {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void save(int petId, byte[] content, String contentType) {
        Optional<PetPhoto> existing = findByPetId(petId);
        if (existing.isPresent()) {
            PetPhoto photo = existing.get();
            photo.setContent(content);
            photo.setContentType(contentType);
            em.merge(photo);
        } else {
            PetPhoto photo = new PetPhoto();
            photo.setPetId(petId);
            photo.setContent(content);
            photo.setContentType(contentType);
            em.persist(photo);
        }
    }

    @Override
    public Optional<PetPhoto> findByPetId(int petId) {
        try {
            PetPhoto photo = em.createQuery(
                    "SELECT p FROM PetPhoto p WHERE p.petId = :petId", PetPhoto.class)
                .setParameter("petId", petId)
                .getSingleResult();
            return Optional.of(photo);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void deleteByPetId(int petId) {
        em.createQuery("DELETE FROM PetPhoto p WHERE p.petId = :petId")
            .setParameter("petId", petId)
            .executeUpdate();
    }

}
