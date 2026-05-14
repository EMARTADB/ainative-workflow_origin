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
package org.springframework.samples.petclinic.repository.jdbc;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.samples.petclinic.model.PetPhoto;
import org.springframework.samples.petclinic.repository.PetPhotoStore;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link PetPhotoStore}.
 */
@Repository
public class JdbcPetPhotoStoreImpl implements PetPhotoStore {

    private final NamedParameterJdbcTemplate jdbc;
    private final SimpleJdbcInsert insertPhoto;

    public JdbcPetPhotoStoreImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate, DataSource dataSource) {
        this.jdbc = namedParameterJdbcTemplate;
        this.insertPhoto = new SimpleJdbcInsert(dataSource)
            .withTableName("pet_photos")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public void save(int petId, byte[] content, String contentType) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM pet_photos WHERE pet_id = :petId",
            new MapSqlParameterSource("petId", petId),
            Integer.class);

        if (count != null && count > 0) {
            jdbc.update(
                "UPDATE pet_photos SET content = :content, content_type = :contentType WHERE pet_id = :petId",
                new MapSqlParameterSource()
                    .addValue("content", content)
                    .addValue("contentType", contentType)
                    .addValue("petId", petId));
        } else {
            insertPhoto.execute(new MapSqlParameterSource()
                .addValue("pet_id", petId)
                .addValue("content", content)
                .addValue("content_type", contentType));
        }
    }

    @Override
    public Optional<PetPhoto> findByPetId(int petId) {
        List<PetPhoto> results = jdbc.query(
            "SELECT id, pet_id, content, content_type FROM pet_photos WHERE pet_id = :petId",
            new MapSqlParameterSource("petId", petId),
            (rs, rowNum) -> {
                PetPhoto photo = new PetPhoto();
                photo.setId(rs.getInt("id"));
                photo.setPetId(rs.getInt("pet_id"));
                photo.setContent(rs.getBytes("content"));
                photo.setContentType(rs.getString("content_type"));
                return photo;
            });
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void deleteByPetId(int petId) {
        jdbc.update(
            "DELETE FROM pet_photos WHERE pet_id = :petId",
            new MapSqlParameterSource("petId", petId));
    }

}
