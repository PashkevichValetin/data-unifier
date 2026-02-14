package com.pashcevich.data_unifier.adapter;

import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.exception.AdapterException;

import java.util.List;
import java.util.Optional;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

public abstract class BaseDateAdapter<T> {

    protected abstract List<T> fetchAllData() throws Exception;
    protected abstract Optional<T> fetchById(Long id) throws Exception;
    protected abstract String getAdapterName();

    public List<UnifiedCustomerDto> getAll() {
        try {
            return convertToUnified(fetchAllData());
        } catch (Exception e) {
            log.error("Failed to fetch all data from {}", getAdapterName(), e);
            throw new AdapterException("Failed to connect to " + getAdapterName(), e);
        }
    }

    public Optional<UnifiedCustomerDto> getById(Long id) {
        try {
            Optional<T> data = fetchById(id);
            return data.map(this::convertSingleToUnified);
        } catch (Exception e) {
            log.error("Failed to fetch data with id {} from {}", id, getAdapterName(), e);
            throw new AdapterException("Failed to fetch data with id " + id + " from " +
                    getAdapterName(), e);
        }
    }

    protected abstract List<UnifiedCustomerDto> convertToUnified(List<T> data);
    protected abstract UnifiedCustomerDto convertSingleToUnified(T data);
}
