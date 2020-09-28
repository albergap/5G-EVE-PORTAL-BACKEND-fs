package com.uc3m.fs.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface FileRepository extends CrudRepository<File, String> {

	Optional<List<File>> findByOwner(String owner);

	Optional<List<File>> findBySitesContaining(String site);

}