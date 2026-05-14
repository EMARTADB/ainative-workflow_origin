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

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;

import org.jspecify.annotations.NullMarked;
import org.springframework.format.Formatter;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.service.ClinicService;

/**
 * Instructs Spring MVC on how to parse and print elements of type {@link Vet}.
 * Converts between a {@link Vet} and its string-encoded ID so that a JSP {@code <select>}
 * can bind a selected vet to the {@code visit.vet} property.
 */
@NullMarked
public class VetFormatter implements Formatter<Vet> {

    private final ClinicService clinicService;

    public VetFormatter(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public String print(Vet vet, Locale locale) {
        return String.valueOf(vet.getId());
    }

    @Override
    public Vet parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isBlank()) {
            return null;
        }
        int id;
        try {
            id = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new ParseException("vet id not a number: " + text, 0);
        }
        Collection<Vet> vets = this.clinicService.findVets();
        for (Vet vet : vets) {
            if (vet.getId() != null && vet.getId() == id) {
                return vet;
            }
        }
        throw new ParseException("vet not found with id: " + id, 0);
    }

}
