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

import java.util.List;

import jakarta.persistence.EntityManager;

import org.springframework.samples.petclinic.model.PetTransfer;
import org.springframework.samples.petclinic.repository.PetTransferRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of {@link PetTransferRepository}.
 */
@Repository
public class JpaPetTransferRepositoryImpl implements PetTransferRepository {

    private final EntityManager em;

    public JpaPetTransferRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public PetTransfer save(PetTransfer transfer) {
        this.em.persist(transfer);
        return transfer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PetTransfer> findByPetId(int petId) {
        return this.em
            .createQuery("SELECT t FROM PetTransfer t WHERE t.petId = :petId ORDER BY t.transferredAt DESC")
            .setParameter("petId", petId)
            .getResultList();
    }

}
