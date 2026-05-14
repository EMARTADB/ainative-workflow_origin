/*
 * Copyright 2002-2022 the original author or authors.
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

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;


/**
 * Mostly used as a facade so all controllers have a single point of entry
 *
 * @author Michael Isvy
 */
public interface ClinicService {

    Collection<PetType> findPetTypes();

    Owner findOwnerById(int id);

    Pet findPetById(int id);

    void savePet(Pet pet);

    void saveVisit(Visit visit);

    Collection<Vet> findVets();

    void saveOwner(Owner owner);

    Collection<Owner> findOwnerByLastName(String lastName);

	Collection<Visit> findVisitsByPetId(int petId);

    /**
     * Find the owner of the pet with the given microchip ID.
     *
     * @param microchipId exact 15-digit microchip ID
     * @return the owner, or {@code null} if not found
     */
    Owner findOwnerByPetMicrochipId(String microchipId);

    /**
     * Transfer a pet to a new owner atomically: updates {@code pet.owner_id} and inserts an
     * immutable {@code PetTransfer} audit record in a single transaction.
     *
     * @param petId        the id of the pet to transfer
     * @param newOwnerId   the id of the target owner
     * @param performedBy  username of the staff member performing the transfer
     * @throws PetTransferException if the transfer is blocked (SELF_TRANSFER or PENDING_VISITS)
     */
    void transferPet(int petId, int newOwnerId, String performedBy);

}
