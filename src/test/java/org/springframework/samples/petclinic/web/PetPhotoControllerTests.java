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
package org.springframework.samples.petclinic.web;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetPhoto;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.PetPhotoService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for photo-related endpoints in {@link PetController}.
 */
@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-core-config.xml", "classpath:spring/mvc-test-config.xml"})
class PetPhotoControllerTests {

    private static final int TEST_OWNER_ID = 1;
    private static final int TEST_PET_ID = 1;

    @Autowired
    private PetController petController;

    @Autowired
    private FormattingConversionServiceFactoryBean formattingConversionServiceFactoryBean;

    @Autowired
    private ClinicService clinicService;

    @Autowired
    private PetPhotoService petPhotoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(petController)
            .setConversionService(formattingConversionServiceFactoryBean.getObject())
            .build();

        PetType cat = new PetType();
        cat.setId(3);
        cat.setName("hamster");
        Owner owner = new Owner();
        owner.setId(TEST_OWNER_ID);
        Pet pet = new Pet();
        pet.setId(TEST_PET_ID);
        owner.addPet(pet);

        given(clinicService.findPetTypes()).willReturn(Lists.newArrayList(cat));
        given(clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(owner);
        given(clinicService.findPetById(TEST_PET_ID)).willReturn(pet);
    }

    @Test
    void testUploadPhotoSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "photo", "cat.jpg", "image/jpeg", new byte[1024]);

        mockMvc.perform(multipart("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID)
                .file(file))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/owners/*"));

        verify(petPhotoService).save(eq(TEST_PET_ID), any(byte[].class), eq("image/jpeg"));
    }

    @Test
    void testUploadPhotoWrongMimeType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "photo", "cat.gif", "image/gif", new byte[512]);

        mockMvc.perform(multipart("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID)
                .file(file))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("photoError"));
    }

    @Test
    void testUploadPhotoTooLarge() throws Exception {
        byte[] bigContent = new byte[3 * 1024 * 1024]; // 3 MB
        MockMultipartFile file = new MockMultipartFile(
            "photo", "big.jpg", "image/jpeg", bigContent);

        mockMvc.perform(multipart("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID)
                .file(file))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("photoError"));
    }

    @Test
    void testDeletePhoto() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/photo/delete", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().is3xxRedirection());

        verify(petPhotoService).deleteByPetId(TEST_PET_ID);
    }

    @Test
    void testServePhotoFound() throws Exception {
        PetPhoto photo = new PetPhoto();
        photo.setPetId(TEST_PET_ID);
        photo.setContent(new byte[]{(byte) 0xFF, (byte) 0xD8}); // minimal JPEG header
        photo.setContentType("image/jpeg");
        given(petPhotoService.findByPetId(TEST_PET_ID)).willReturn(Optional.of(photo));

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void testServePhotoNotFound() throws Exception {
        given(petPhotoService.findByPetId(TEST_PET_ID)).willReturn(Optional.empty());

        mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/photo", TEST_OWNER_ID, TEST_PET_ID))
            .andExpect(status().isNotFound());
    }

}
