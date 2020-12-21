package de.filefighter.rest.domain.common;

public interface DTOServiceInterface<D,E> {
    D createDto(E entity);
    E findEntity(D dto);
}
