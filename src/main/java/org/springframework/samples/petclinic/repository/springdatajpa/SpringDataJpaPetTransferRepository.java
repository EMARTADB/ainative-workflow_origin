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

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.samples.petclinic.model.PetTransfer;
import org.springframework.samples.petclinic.repository.PetTransferRepository;

/**
 * Spring Data JPA implementation of {@link PetTransferRepository}.
 */
public interface SpringDataJpaPetTransferRepository extends PetTransferRepository, CrudRepository<PetTransfer, Integer> {

    @Override
    @Query("SELECT t FROM PetTransfer t WHERE t.petId = :petId ORDER BY t.transferredAt DESC")
    List<PetTransfer> findByPetId(int petId);

}
