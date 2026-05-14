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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.model.PetPhoto;
import org.springframework.samples.petclinic.repository.PetPhotoStore;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PetPhotoServiceImpl}.
 * Verifies delegation to {@link PetPhotoStore}.
 */
@ExtendWith(MockitoExtension.class)
class PetPhotoServiceTests {

    @Mock
    private PetPhotoStore petPhotoStore;

    @InjectMocks
    private PetPhotoServiceImpl petPhotoService;

    @Test
    void saveDelegatesToStore() {
        byte[] content = new byte[]{1, 2, 3};
        petPhotoService.save(42, content, "image/jpeg");
        verify(petPhotoStore).save(42, content, "image/jpeg");
    }

    @Test
    void findByPetIdDelegatesToStore() {
        PetPhoto photo = new PetPhoto();
        photo.setPetId(42);
        when(petPhotoStore.findByPetId(42)).thenReturn(Optional.of(photo));

        Optional<PetPhoto> result = petPhotoService.findByPetId(42);

        assertThat(result).isPresent();
        assertThat(result.get().getPetId()).isEqualTo(42);
    }

    @Test
    void findByPetIdReturnsEmptyWhenNotFound() {
        when(petPhotoStore.findByPetId(99)).thenReturn(Optional.empty());

        Optional<PetPhoto> result = petPhotoService.findByPetId(99);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByPetIdDelegatesToStore() {
        petPhotoService.deleteByPetId(42);
        verify(petPhotoStore).deleteByPetId(42);
    }

}
