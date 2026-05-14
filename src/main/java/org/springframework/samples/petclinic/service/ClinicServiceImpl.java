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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetTransfer;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTransferRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mostly used as a facade for all Petclinic controllers
 * Also a placeholder for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 */
@Service
public class ClinicServiceImpl implements ClinicService {

    private static final Logger log = LoggerFactory.getLogger(ClinicServiceImpl.class);

    private final PetRepository petRepository;
    private final VetRepository vetRepository;
    private final OwnerRepository ownerRepository;
    private final VisitRepository visitRepository;
    private final PetTransferRepository petTransferRepository;

    public ClinicServiceImpl(PetRepository petRepository, VetRepository vetRepository, OwnerRepository ownerRepository, VisitRepository visitRepository, PetTransferRepository petTransferRepository) {
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
        this.visitRepository = visitRepository;
        this.petTransferRepository = petTransferRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<PetType> findPetTypes() {
        return petRepository.findPetTypes();
    }

    @Override
    @Transactional(readOnly = true)
    public Owner findOwnerById(int id) {
        return ownerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) {
        return ownerRepository.findByLastName(lastName);
    }

    @Override
    @Transactional
    public void saveOwner(Owner owner) {
        ownerRepository.save(owner);
    }


    @Override
    @Transactional
    public void saveVisit(Visit visit) {
        visitRepository.save(visit);
    }


    @Override
    @Transactional(readOnly = true)
    public Pet findPetById(int id) {
        return petRepository.findById(id);
    }

    @Override
    @Transactional
    public void savePet(Pet pet) {
        petRepository.save(pet);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "vets")
    public Collection<Vet> findVets() {
        return vetRepository.findAll();
    }

	@Override
	public Collection<Visit> findVisitsByPetId(int petId) {
		return visitRepository.findByPetId(petId);
	}

    @Override
    @Transactional(readOnly = true)
    public Owner findOwnerByPetMicrochipId(String microchipId) {
        return ownerRepository.findOwnerByPetMicrochipId(microchipId);
    }

    @Override
    @Transactional
    public void transferPet(int petId, int newOwnerId, String performedBy) {
        try {
            Pet pet = petRepository.findById(petId);
            int fromOwnerId = pet.getOwner().getId();

            // Self-transfer check
            if (fromOwnerId == newOwnerId) {
                log.warn("Transfer blocked for pet_id={}: reason=SELF_TRANSFER", petId);
                throw new PetTransferException(PetTransferException.Reason.SELF_TRANSFER);
            }

            // Pending-visits check
            Collection<Visit> visits = visitRepository.findByPetId(petId);
            boolean hasPendingVisits = visits.stream()
                .anyMatch(v -> v.getDate() != null && !v.getDate().isBefore(java.time.LocalDate.now()));
            if (hasPendingVisits) {
                log.warn("Transfer blocked for pet_id={}: reason=PENDING_VISITS", petId);
                throw new PetTransferException(PetTransferException.Reason.PENDING_VISITS);
            }

            // Reassign pet to new owner
            Owner newOwner = ownerRepository.findById(newOwnerId);
            newOwner.addPet(pet);
            petRepository.save(pet);

            // Record audit
            PetTransfer transfer = new PetTransfer();
            transfer.setPetId(petId);
            transfer.setFromOwnerId(fromOwnerId);
            transfer.setToOwnerId(newOwnerId);
            transfer.setTransferredAt(java.time.LocalDateTime.now());
            transfer.setPerformedBy(performedBy);
            petTransferRepository.save(transfer);

            log.info("Transfer complete: pet_id={}, from_owner_id={}, to_owner_id={}, performed_by={}",
                petId, fromOwnerId, newOwnerId, performedBy);

        } catch (PetTransferException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during transfer of pet_id={}", petId, e);
            throw e;
        }
    }


}
