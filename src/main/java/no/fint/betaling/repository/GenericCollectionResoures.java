package no.fint.betaling.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import no.fint.model.resource.AbstractCollectionResources;

import java.util.List;

public class GenericCollectionResoures<T>  extends AbstractCollectionResources<T> {
    @Override
    public TypeReference<List<T>> getTypeReference() {
        return null;
    }
}
