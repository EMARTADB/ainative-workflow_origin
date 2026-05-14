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

import java.util.List;

import org.springframework.samples.petclinic.model.PetTransfer;

/**
 * Repository interface for {@link PetTransfer} entities. No delete operations are exposed.
 */
public interface PetTransferRepository {

    /**
     * Save (insert) a new {@link PetTransfer} audit record.
     *
     * @param transfer the transfer record to persist
     * @return the persisted transfer (with id populated)
     */
    PetTransfer save(PetTransfer transfer);

    /**
     * Return all {@link PetTransfer} records for a given pet.
     *
     * @param petId the id of the pet
     * @return transfer history, may be empty
     */
    List<PetTransfer> findByPetId(int petId);

}
