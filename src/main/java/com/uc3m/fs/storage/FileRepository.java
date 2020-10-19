package com.uc3m.fs.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.uc3m.fs.storage.model.File;
import com.uc3m.fs.storage.model.FilePK;

public interface FileRepository extends CrudRepository<File, FilePK> {

	Optional<File> findById(FilePK fileId);

	void deleteById(FilePK fileId);

	@Query(value = "SELECT f FROM File f WHERE f.id.owner = :owner")
	List<File> findByOwner(String owner);

}