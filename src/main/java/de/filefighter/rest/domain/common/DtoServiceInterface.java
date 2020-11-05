package de.filefighter.rest.domain.common;

public interface DtoServiceInterface<D,E> {
    D createDto(E entity);
    E findEntity(D dto);
}
