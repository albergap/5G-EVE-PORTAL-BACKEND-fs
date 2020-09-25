package com.uc3m.fs.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends CrudRepository<File, String> {

	public static String SITE_SEPARATOR = ",", SITE_OPEN = "(", SITE_CLOSE = ")";

	@Query("SELECT f FROM files f where f.owner = :owner") 
	Optional<List<File>> findByOwner(@Param("owner") String owner);

	@Query("SELECT f FROM files f where f.sites LIKE '%" + SITE_OPEN + ":site" + SITE_CLOSE + "%'") 
	Optional<List<File>> findBySite(@Param("site") String site);

}