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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.PetTransferException;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p> Base class for {@link ClinicService} integration tests. </p> <p> Subclasses should specify Spring context
 * configuration using {@link ContextConfiguration @ContextConfiguration} annotation </p> <p>
 * AbstractclinicServiceTests and its subclasses benefit from the following services provided by the Spring
 * TestContext Framework: </p> <ul> <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li> <li><strong>Dependency Injection</strong> of test fixture instances, meaning that
 * we don't need to perform application context lookups. See the use of {@link Autowired @Autowired} on the <code>{@link
 * AbstractClinicServiceTests#clinicService clinicService}</code> instance variable, which uses autowiring <em>by
 * type</em>. <li><strong>Transaction management</strong>, meaning each test method is executed in its own transaction,
 * which is automatically rolled back by default. Thus, even if tests insert or otherwise change database state, there
 * is no need for a teardown or cleanup script. <li> An {@link org.springframework.context.ApplicationContext
 * ApplicationContext} is also inherited and can be used for explicit bean lookup if necessary. </li> </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
abstract class AbstractClinicServiceTests {

    @Autowired
    protected ClinicService clinicService;

    @Test
    void shouldFindOwnersByLastName() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Davis");
        assertThat(owners).hasSize(2);

        owners = this.clinicService.findOwnerByLastName("Daviss");
        assertThat(owners).isEmpty();
    }

    @Test
    void shouldFindSingleOwnerWithPet() {
        Owner owner = this.clinicService.findOwnerById(1);
        assertThat(owner.getLastName()).startsWith("Franklin");
        assertThat(owner.getPets()).hasSize(1);
        assertThat(owner.getPets().get(0).getType()).isNotNull();
        assertThat(owner.getPets().get(0).getType().getName()).isEqualTo("cat");
    }

    @Test
    @Transactional
    public void shouldInsertOwner() {
        Collection<Owner> owners = this.clinicService.findOwnerByLastName("Schultz");
        int found = owners.size();

        Owner owner = new Owner();
        owner.setFirstName("Sam");
        owner.setLastName("Schultz");
        owner.setAddress("4, Evans Street");
        owner.setCity("Wollongong");
        owner.setTelephone("4444444444");
        this.clinicService.saveOwner(owner);
        assertThat(owner.getId().longValue()).isNotZero();

        owners = this.clinicService.findOwnerByLastName("Schultz");
        assertThat(owners).hasSize(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdateOwner() {
        Owner owner = this.clinicService.findOwnerById(1);
        String oldLastName = owner.getLastName();
        String newLastName = oldLastName + "X";

        owner.setLastName(newLastName);
        this.clinicService.saveOwner(owner);

        // retrieving new name from database
        owner = this.clinicService.findOwnerById(1);
        assertThat(owner.getLastName()).isEqualTo(newLastName);
    }

    @Test
    void shouldFindPetWithCorrectId() {
        Pet pet7 = this.clinicService.findPetById(7);
        assertThat(pet7.getName()).startsWith("Samantha");
        assertThat(pet7.getOwner().getFirstName()).isEqualTo("Jean");

    }

    @Test
    void shouldFindAllPetTypes() {
        Collection<PetType> petTypes = this.clinicService.findPetTypes();

        PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
        assertThat(petType1.getName()).isEqualTo("cat");
        PetType petType4 = EntityUtils.getById(petTypes, PetType.class, 4);
        assertThat(petType4.getName()).isEqualTo("snake");
    }

    @Test
    @Transactional
    public void shouldInsertPetIntoDatabaseAndGenerateId() {
        Owner owner6 = this.clinicService.findOwnerById(6);
        int found = owner6.getPets().size();

        Pet pet = new Pet();
        pet.setName("bowser");
        Collection<PetType> types = this.clinicService.findPetTypes();
        pet.setType(EntityUtils.getById(types, PetType.class, 2));
        pet.setBirthDate(LocalDate.now());
        owner6.addPet(pet);
        assertThat(owner6.getPets()).hasSize(found + 1);

        this.clinicService.savePet(pet);
        this.clinicService.saveOwner(owner6);

        owner6 = this.clinicService.findOwnerById(6);
        assertThat(owner6.getPets()).hasSize(found + 1);
        // checks that id has been generated
        assertThat(pet.getId()).isNotNull();
    }

    @Test
    @Transactional
    public void shouldUpdatePetName() throws Exception {
        Pet pet7 = this.clinicService.findPetById(7);
        String oldName = pet7.getName();

        String newName = oldName + "X";
        pet7.setName(newName);
        this.clinicService.savePet(pet7);

        pet7 = this.clinicService.findPetById(7);
        assertThat(pet7.getName()).isEqualTo(newName);
    }

    @Test
    void shouldFindVets() {
        Collection<Vet> vets = this.clinicService.findVets();

        Vet vet = EntityUtils.getById(vets, Vet.class, 3);
        assertThat(vet.getLastName()).isEqualTo("Douglas");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
        assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
    }

    @Test
    @Transactional
    public void shouldAddNewVisitForPet() {
        Pet pet7 = this.clinicService.findPetById(7);
        int found = pet7.getVisits().size();
        Visit visit = new Visit();
        pet7.addVisit(visit);
        visit.setDescription("test");
        this.clinicService.saveVisit(visit);
        this.clinicService.savePet(pet7);

        pet7 = this.clinicService.findPetById(7);
        assertThat(pet7.getVisits()).hasSize(found + 1);
        assertThat(visit.getId()).isNotNull();
    }

    @Test
    void shouldFindVisitsByPetId() throws Exception {
        Collection<Visit> visits = this.clinicService.findVisitsByPetId(7);
        assertThat(visits).hasSize(2);
        Visit[] visitArr = visits.toArray(new Visit[visits.size()]);
        assertThat(visitArr[0].getPet()).isNotNull();
        assertThat(visitArr[0].getDate()).isNotNull();
        assertThat(visitArr[0].getPet().getId()).isEqualTo(7);
    }

    @Test
    @Transactional
    public void saveVisit_withVetAssignment() {
        Collection<Vet> vets = this.clinicService.findVets();
        Vet vet = vets.iterator().next();

        Pet pet7 = this.clinicService.findPetById(7);
        Visit visit = new Visit();
        pet7.addVisit(visit);
        visit.setDescription("visit with vet");
        visit.setVet(vet);
        this.clinicService.saveVisit(visit);

        Collection<Visit> reloaded = this.clinicService.findVisitsByPetId(7);
        Visit saved = reloaded.stream()
            .filter(v -> visit.getId().equals(v.getId()))
            .findFirst()
            .orElseThrow();
        assertThat(saved.getVet()).isNotNull();
        assertThat(saved.getVet().getId()).isEqualTo(vet.getId());
    }

    @Test
    @Transactional
    public void saveVisit_withoutVetAssignment() {
        Pet pet7 = this.clinicService.findPetById(7);
        Visit visit = new Visit();
        pet7.addVisit(visit);
        visit.setDescription("visit without vet");
        visit.setVet(null);
        this.clinicService.saveVisit(visit);

        Collection<Visit> reloaded = this.clinicService.findVisitsByPetId(7);
        Visit saved = reloaded.stream()
            .filter(v -> visit.getId().equals(v.getId()))
            .findFirst()
            .orElseThrow();
        assertThat(saved.getVet()).isNull();
    }

    @Test
    @Transactional
    public void testFindOwnerByMicrochipId() {
        Owner owner6 = this.clinicService.findOwnerById(6);
        Pet pet = new Pet();
        pet.setName("chippy");
        Collection<PetType> types = this.clinicService.findPetTypes();
        pet.setType(EntityUtils.getById(types, PetType.class, 2));
        pet.setBirthDate(LocalDate.now());
        pet.setMicrochipId("123456789012345");
        owner6.addPet(pet);
        this.clinicService.savePet(pet);

        Owner found = this.clinicService.findOwnerByPetMicrochipId("123456789012345");
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(owner6.getId());
    }

    @Test
    @Transactional
    public void testMicrochipIdUniquenessViolation() {
        Owner owner6 = this.clinicService.findOwnerById(6);
        Collection<PetType> types = this.clinicService.findPetTypes();

        Pet pet1 = new Pet();
        pet1.setName("chip1");
        pet1.setType(EntityUtils.getById(types, PetType.class, 2));
        pet1.setBirthDate(LocalDate.now());
        pet1.setMicrochipId("999888777666555");
        owner6.addPet(pet1);
        this.clinicService.savePet(pet1);

        Owner owner3 = this.clinicService.findOwnerById(3);
        Pet pet2 = new Pet();
        pet2.setName("chip2");
        pet2.setType(EntityUtils.getById(types, PetType.class, 2));
        pet2.setBirthDate(LocalDate.now());
        pet2.setMicrochipId("999888777666555");
        owner3.addPet(pet2);

        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> this.clinicService.savePet(pet2));
    }

    @Test
    @Transactional
    public void testSavePetWithNullMicrochipId() {
        Owner owner6 = this.clinicService.findOwnerById(6);
        Pet pet = new Pet();
        pet.setName("nullchip");
        Collection<PetType> types = this.clinicService.findPetTypes();
        pet.setType(EntityUtils.getById(types, PetType.class, 2));
        pet.setBirthDate(LocalDate.now());
        pet.setMicrochipId(null);
        owner6.addPet(pet);
        this.clinicService.savePet(pet);

        assertThat(pet.getId()).isNotNull();
        Pet saved = this.clinicService.findPetById(pet.getId());
        assertThat(saved.getMicrochipId()).isNull();
    }

    // ---- Pet Transfer integration tests (8.1) ----

    @Test
    @Transactional
    public void shouldTransferPet_happyPath_integration() {
        // Pet 1 (Leo) is owned by owner 1 (George Franklin); transfer to owner 2 (Betty Davis)
        Pet pet = this.clinicService.findPetById(1);
        int originalOwnerId = pet.getOwner().getId();
        assertThat(originalOwnerId).isEqualTo(1);

        this.clinicService.transferPet(1, 2, "integration-test");

        Pet transferred = this.clinicService.findPetById(1);
        assertThat(transferred.getOwner().getId()).isEqualTo(2);
    }

    @Test
    @Transactional
    public void shouldThrow_selfTransfer_integration() {
        // Pet 1 is owned by owner 1 — self-transfer should be blocked
        assertThatExceptionOfType(PetTransferException.class)
            .isThrownBy(() -> this.clinicService.transferPet(1, 1, "integration-test"))
            .satisfies(e -> assertThat(e.getReason()).isEqualTo(PetTransferException.Reason.SELF_TRANSFER));
    }

    @Test
    @Transactional
    public void shouldThrow_pendingVisits_integration() {
        // Add a future visit to pet 1, then attempt transfer
        Visit futureVisit = new Visit();
        futureVisit.setDate(LocalDate.now().plusDays(14));
        futureVisit.setDescription("future check-up");
        Pet pet = this.clinicService.findPetById(1);
        pet.addVisit(futureVisit);
        this.clinicService.saveVisit(futureVisit);

        assertThatExceptionOfType(PetTransferException.class)
            .isThrownBy(() -> this.clinicService.transferPet(1, 2, "integration-test"))
            .satisfies(e -> assertThat(e.getReason()).isEqualTo(PetTransferException.Reason.PENDING_VISITS));
    }


}
