package com.uc3m.fs.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface FileRepository extends CrudRepository<File, FileId> {

	Optional<File> findById(FileId fileId);

	void deleteById(FileId fileId);

	@Query(value = "SELECT f FROM files f WHERE f.fileId.owner = :owner")
	Optional<List<File>> findByOwner(String owner);

	Optional<List<File>> findBySitesContaining(String site);

}