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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetPhoto;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PetPhotoService}.
 * Uses the JPA profile with H2 in-memory database.
 * Tests upload, replace, delete, and cascade-delete flows.
 */
@SpringJUnitConfig(locations = {"classpath:spring/business-config.xml"})
@ActiveProfiles("jpa")
@Transactional
class ClinicServicePetPhotoTests {

    @Autowired
    private ClinicService clinicService;

    @Autowired
    private PetPhotoService petPhotoService;

    private Pet createAndSavePet() {
        Collection<Owner> owners = clinicService.findOwnerByLastName("Franklin");
        Owner owner = owners.iterator().next();

        Collection<PetType> petTypes = clinicService.findPetTypes();
        PetType petType = petTypes.iterator().next();

        Pet pet = new Pet();
        pet.setName("PhotoTestPet");
        pet.setBirthDate(LocalDate.now());
        pet.setType(petType);
        owner.addPet(pet);
        clinicService.savePet(pet);
        return pet;
    }

    @Test
    void uploadPhotoAndRetrieveIt() {
        Pet pet = createAndSavePet();
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG magic bytes

        petPhotoService.save(pet.getId(), content, "image/jpeg");

        Optional<PetPhoto> found = petPhotoService.findByPetId(pet.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo(content);
        assertThat(found.get().getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void uploadPhotoReplacesExisting() {
        Pet pet = createAndSavePet();
        byte[] first = new byte[]{1, 2, 3};
        byte[] second = new byte[]{4, 5, 6};

        petPhotoService.save(pet.getId(), first, "image/jpeg");
        petPhotoService.save(pet.getId(), second, "image/png");

        Optional<PetPhoto> found = petPhotoService.findByPetId(pet.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo(second);
        assertThat(found.get().getContentType()).isEqualTo("image/png");
    }

    @Test
    void deletePhotoRemovesIt() {
        Pet pet = createAndSavePet();
        petPhotoService.save(pet.getId(), new byte[]{1}, "image/jpeg");

        petPhotoService.deleteByPetId(pet.getId());

        assertThat(petPhotoService.findByPetId(pet.getId())).isEmpty();
    }

    @Test
    void findByPetIdReturnsEmptyForPetWithNoPhoto() {
        Pet pet = createAndSavePet();

        assertThat(petPhotoService.findByPetId(pet.getId())).isEmpty();
    }

}
