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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.samples.petclinic.model.PetTransfer;
import org.springframework.samples.petclinic.repository.PetTransferRepository;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of {@link PetTransferRepository}.
 */
@Repository
public class JdbcPetTransferRepositoryImpl implements PetTransferRepository {

    private final JdbcClient jdbcClient;
    private final SimpleJdbcInsert insertTransfer;

    public JdbcPetTransferRepositoryImpl(JdbcClient jdbcClient, DataSource dataSource) {
        this.jdbcClient = jdbcClient;
        this.insertTransfer = new SimpleJdbcInsert(dataSource)
            .withTableName("pet_transfers")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public PetTransfer save(PetTransfer transfer) {
        Map<String, Object> params = new HashMap<>();
        params.put("pet_id", transfer.getPetId());
        params.put("from_owner_id", transfer.getFromOwnerId());
        params.put("to_owner_id", transfer.getToOwnerId());
        params.put("transferred_at", Timestamp.valueOf(transfer.getTransferredAt()));
        params.put("performed_by", transfer.getPerformedBy());
        Number key = this.insertTransfer.executeAndReturnKey(params);
        transfer.setId(key.intValue());
        return transfer;
    }

    @Override
    public List<PetTransfer> findByPetId(int petId) {
        return this.jdbcClient
            .sql("SELECT id, pet_id, from_owner_id, to_owner_id, transferred_at, performed_by " +
                 "FROM pet_transfers WHERE pet_id = :petId ORDER BY transferred_at DESC")
            .param("petId", petId)
            .query(new PetTransferRowMapper())
            .list();
    }

    private static class PetTransferRowMapper implements RowMapper<PetTransfer> {
        @Override
        public PetTransfer mapRow(ResultSet rs, int rowNum) throws SQLException {
            PetTransfer t = new PetTransfer();
            t.setId(rs.getInt("id"));
            t.setPetId(rs.getInt("pet_id"));
            t.setFromOwnerId(rs.getInt("from_owner_id"));
            t.setToOwnerId(rs.getInt("to_owner_id"));
            Timestamp ts = rs.getTimestamp("transferred_at");
            t.setTransferredAt(ts.toLocalDateTime());
            t.setPerformedBy(rs.getString("performed_by"));
            return t;
        }
    }

}
